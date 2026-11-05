package org.rescuelink.service;

import lombok.RequiredArgsConstructor;
import org.rescuelink.dto.AdopcionDtos.PostularAdopcionRequest;
import org.rescuelink.dto.AdopcionDtos.SolicitudAdopcionResponse;
import org.rescuelink.dto.AdopcionDtos.VerificacionCertificadoQrResponse;
import org.rescuelink.exception.BusinessException;
import org.rescuelink.exception.ResourceNotFoundException;
import org.rescuelink.model.Animal;
import org.rescuelink.model.SolicitudAdopcion;
import org.rescuelink.model.Usuario;
import org.rescuelink.model.enums.EstadoAnimal;
import org.rescuelink.model.enums.EstadoSolicitud;
import org.rescuelink.model.enums.RolUsuario;
import org.rescuelink.port.NotificationPort;
import org.rescuelink.port.NotificationPort.AdoptionNotificationCommand;
import org.rescuelink.port.NotificationPort.NotificationDeliveryResult;
import org.rescuelink.repository.AnimalRepository;
import org.rescuelink.repository.SolicitudAdopcionRepository;
import org.rescuelink.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Servicio de dominio para la gestión de postulaciones, dictamen concurrente,
 * emisión de Certificado Digital con QR y orquestación de interoperabilidad externa.
 */
@Service
@RequiredArgsConstructor
public class AdopcionService {

    private static final Set<EstadoAnimal> ESTADOS_POSTULABLES = Set.of(
            EstadoAnimal.APTO_ADOPCION,
            EstadoAnimal.EN_CATALOGO,
            EstadoAnimal.EN_EVALUACION
    );

    private final SolicitudAdopcionRepository solicitudRepository;
    private final AnimalRepository animalRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificationPort notificationPort;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SolicitudAdopcionResponse postularAdopcion(PostularAdopcionRequest request, String idempotencyKey) {
        Animal animal = animalRepository.findById(request.animalId())
                .orElseThrow(() -> new ResourceNotFoundException("Animal", request.animalId()));

        if (!ESTADOS_POSTULABLES.contains(animal.getEstado())) {
            throw new BusinessException(String.format(
                    "El animal '%s' no se encuentra habilitado para adopción (estado actual: %s).",
                    animal.getNombre(), animal.getEstado()
            ));
        }

        Usuario adoptante = obtenerOCrearCiudadano(
                request.nombreAdoptante(),
                request.emailAdoptante(),
                request.telefonoAdoptante()
        );

        Optional<SolicitudAdopcion> existenteOpt = solicitudRepository
                .findByAnimalIdAndAdoptanteId(animal.getId(), adoptante.getId());

        if (existenteOpt.isPresent()) {
            SolicitudAdopcion existente = existenteOpt.get();
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                return mapToResponse(
                        existente,
                        new NotificationDeliveryResult(true, "IDEMPOTENT_REPLAY", "idem-" + idempotencyKey, 200, "Respuesta idempotente sin duplicar registro.")
                );
            }
            throw new BusinessException(String.format(
                    "Ya existe una solicitud activa de %s para adoptar a %s.",
                    adoptante.getEmail(), animal.getNombre()
            ));
        }

        SolicitudAdopcion nueva = SolicitudAdopcion.builder()
                .animal(animal)
                .adoptante(adoptante)
                .estado(EstadoSolicitud.RECIBIDA)
                .tipoVivienda(request.tipoVivienda())
                .tieneOtrasMascotas(request.tieneOtrasMascotas())
                .motivo(request.motivo())
                .build();

        SolicitudAdopcion guardada = solicitudRepository.save(nueva);

        NotificationDeliveryResult resultadoNotificacion = notificationPort.enviarNotificacionAdopcion(
                new AdoptionNotificationCommand(
                        guardada.getId(),
                        adoptante.getEmail(),
                        adoptante.getNombre(),
                        animal.getNombre(),
                        guardada.getEstado().name(),
                        null,
                        idempotencyKey
                )
        );

        return mapToResponse(guardada, resultadoNotificacion);
    }

    @Transactional
    public SolicitudAdopcionResponse aprobarSolicitud(UUID solicitudId, String idempotencyKey) {
        SolicitudAdopcion solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de adopción", solicitudId));

        if (solicitud.getEstado() == EstadoSolicitud.APROBADA && idempotencyKey != null && !idempotencyKey.isBlank()) {
            return mapToResponse(
                    solicitud,
                    new NotificationDeliveryResult(true, "IDEMPOTENT_REPLAY", "idem-" + idempotencyKey, 200, "Aprobación previamente procesada.")
            );
        }

        if (solicitud.getEstado() != EstadoSolicitud.RECIBIDA && solicitud.getEstado() != EstadoSolicitud.EN_REVISION) {
            throw new BusinessException(String.format(
                    "Solo se pueden aprobar solicitudes en estado RECIBIDA o EN_REVISION (estado actual: %s).",
                    solicitud.getEstado()
            ));
        }

        Animal animal = solicitud.getAnimal();
        if (animal.getEstado() == EstadoAnimal.ADOPTADO) {
            throw new BusinessException(String.format("El animal '%s' ya figura como ADOPTADO.", animal.getNombre()));
        }

        String codigoQr = generarCodigoCertificadoQr(solicitud.getId(), animal.getId(), solicitud.getAdoptante().getEmail());
        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setCodigoCertificadoQr(codigoQr);
        animal.setEstado(EstadoAnimal.ADOPTADO);

        animalRepository.save(animal);
        SolicitudAdopcion aprobada = solicitudRepository.save(solicitud);

        // Cierre automático de otras postulaciones pendientes para el mismo animal (HU13)
        List<SolicitudAdopcion> competidoras = solicitudRepository.findByAnimalId(animal.getId());
        for (SolicitudAdopcion otra : competidoras) {
            if (!otra.getId().equals(aprobada.getId())
                    && (otra.getEstado() == EstadoSolicitud.RECIBIDA || otra.getEstado() == EstadoSolicitud.EN_REVISION)) {
                otra.setEstado(EstadoSolicitud.CERRADA_POR_OTRA_ADOPCION);
                solicitudRepository.save(otra);
            }
        }

        NotificationDeliveryResult resultadoNotificacion = notificationPort.enviarNotificacionAdopcion(
                new AdoptionNotificationCommand(
                        aprobada.getId(),
                        aprobada.getAdoptante().getEmail(),
                        aprobada.getAdoptante().getNombre(),
                        animal.getNombre(),
                        aprobada.getEstado().name(),
                        codigoQr,
                        idempotencyKey
                )
        );

        return mapToResponse(aprobada, resultadoNotificacion);
    }

    @Transactional(readOnly = true)
    public VerificacionCertificadoQrResponse verificarCertificadoQr(String codigoQr) {
        SolicitudAdopcion solicitud = solicitudRepository.findByCodigoCertificadoQr(codigoQr)
                .orElseThrow(() -> new ResourceNotFoundException("Certificado QR", codigoQr));

        boolean esValido = solicitud.getEstado() == EstadoSolicitud.APROBADA;
        Animal animal = solicitud.getAnimal();

        return new VerificacionCertificadoQrResponse(
                esValido,
                solicitud.getCodigoCertificadoQr(),
                solicitud.getEstado().name(),
                animal.getNombre(),
                animal.getEspecie(),
                animal.getAlbergue() != null ? animal.getAlbergue().getNombre() : "Red Oficial RescueLink",
                solicitud.getAdoptante().getNombre(),
                solicitud.getFechaSolicitud(),
                esValido
                        ? "Certificado oficial auténtico verificado bajo Ley N° 30407."
                        : "El certificado consultado no se encuentra en estado aprobado vigente."
        );
    }

    @Transactional(readOnly = true)
    public List<SolicitudAdopcionResponse> listarPorAnimal(UUID animalId) {
        return solicitudRepository.findByAnimalId(animalId).stream()
                .map(s -> mapToResponse(s, null))
                .toList();
    }

    private Usuario obtenerOCrearCiudadano(String nombre, String email, String telefono) {
        String emailNormalizado = email.trim().toLowerCase();
        return usuarioRepository.findByEmail(emailNormalizado)
                .orElseGet(() -> {
                    Usuario nuevo = Usuario.builder()
                            .nombre(nombre.trim())
                            .email(emailNormalizado)
                            .telefono(telefono.trim())
                            .rol(RolUsuario.ROLE_CIUDADANO)
                            .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .build();
                    return usuarioRepository.save(nuevo);
                });
    }

    String generarCodigoCertificadoQr(UUID solicitudId, UUID animalId, String emailAdoptante) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String payload = solicitudId + "|" + animalId + "|" + emailAdoptante.toLowerCase();
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            String hex = HexFormat.of().withUpperCase().formatHex(hash).substring(0, 12);
            return "CERT-RL-" + hex;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 no disponible en el entorno JVM", e);
        }
    }

    private SolicitudAdopcionResponse mapToResponse(SolicitudAdopcion s, NotificationDeliveryResult notif) {
        return new SolicitudAdopcionResponse(
                s.getId(),
                s.getAnimal().getId(),
                s.getAnimal().getNombre(),
                s.getAdoptante().getNombre(),
                s.getAdoptante().getEmail(),
                s.getEstado(),
                s.getTipoVivienda(),
                s.getTieneOtrasMascotas(),
                s.getCodigoCertificadoQr(),
                notif != null ? notif.estadoEntrega() : "STORED",
                notif != null ? notif.externalMessageId() : null,
                s.getFechaSolicitud()
        );
    }
}

