package org.rescuelink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rescuelink.dto.AdopcionDtos.PostularAdopcionRequest;
import org.rescuelink.dto.AdopcionDtos.SolicitudAdopcionResponse;
import org.rescuelink.dto.AdopcionDtos.VerificacionCertificadoQrResponse;
import org.rescuelink.exception.BusinessException;
import org.rescuelink.model.Albergue;
import org.rescuelink.model.Animal;
import org.rescuelink.model.SolicitudAdopcion;
import org.rescuelink.model.Usuario;
import org.rescuelink.model.enums.EspecieAnimal;
import org.rescuelink.model.enums.EstadoAnimal;
import org.rescuelink.model.enums.EstadoSolicitud;
import org.rescuelink.model.enums.RolUsuario;
import org.rescuelink.port.NotificationPort;
import org.rescuelink.port.NotificationPort.NotificationDeliveryResult;
import org.rescuelink.repository.AnimalRepository;
import org.rescuelink.repository.SolicitudAdopcionRepository;
import org.rescuelink.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ISO 25010 Adecuación Funcional & V&V: AdopcionService")
class AdopcionServiceTest {

    @Mock
    private SolicitudAdopcionRepository solicitudRepository;
    @Mock
    private AnimalRepository animalRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private NotificationPort notificationPort;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdopcionService adopcionService;

    private Animal animalEnCatalogo;
    private Usuario adoptante;
    private UUID animalId;
    private UUID adoptanteId;

    @BeforeEach
    void setUp() {
        animalId = UUID.fromString("10000000-0000-0000-0000-000000000001");
        adoptanteId = UUID.fromString("20000000-0000-0000-0000-000000000002");

        Albergue albergue = Albergue.builder()
                .id(UUID.randomUUID())
                .nombre("Albergue Huellitas de Miraflores")
                .build();

        animalEnCatalogo = Animal.builder()
                .id(animalId)
                .nombre("Rocky")
                .especie(EspecieAnimal.CANINO)
                .estado(EstadoAnimal.EN_CATALOGO)
                .albergue(albergue)
                .build();

        adoptante = Usuario.builder()
                .id(adoptanteId)
                .nombre("Valeria Gómez")
                .email("valeria@ejemplo.pe")
                .rol(RolUsuario.ROLE_CIUDADANO)
                .build();
    }

    @Test
    @DisplayName("1. Postulación válida: persiste solicitud en estado RECIBIDA y dispara NotificationPort")
    void debeRegistrarPostulacionYNotificarAlSistemaExterno() {
        PostularAdopcionRequest request = new PostularAdopcionRequest(
                animalId,
                "Valeria Gómez",
                "valeria@ejemplo.pe",
                "+51987654321",
                "CASA_CON_JARDIN",
                false,
                "Cuento con espacio amplio y presupuesto veterinario garantizado.",
                true
        );

        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animalEnCatalogo));
        when(usuarioRepository.findByEmail("valeria@ejemplo.pe")).thenReturn(Optional.of(adoptante));
        when(solicitudRepository.findByAnimalIdAndAdoptanteId(animalId, adoptanteId)).thenReturn(Optional.empty());
        when(solicitudRepository.save(any(SolicitudAdopcion.class))).thenAnswer(inv -> {
            SolicitudAdopcion s = inv.getArgument(0);
            s.setId(UUID.fromString("30000000-0000-0000-0000-000000000003"));
            s.setFechaSolicitud(Instant.now());
            return s;
        });
        when(notificationPort.enviarNotificacionAdopcion(any()))
                .thenReturn(NotificationDeliveryResult.delivered("msg_ext_101", 200));

        SolicitudAdopcionResponse response = adopcionService.postularAdopcion(request, "idem-101");

        assertThat(response.estado()).isEqualTo(EstadoSolicitud.RECIBIDA);
        assertThat(response.nombreAnimal()).isEqualTo("Rocky");
        assertThat(response.estadoNotificacionExterna()).isEqualTo("DELIVERED");
        assertThat(response.externalMessageId()).isEqualTo("msg_ext_101");
        verify(notificationPort, times(1)).enviarNotificacionAdopcion(any());
    }

    @Test
    @DisplayName("2. Protección contra errores: rechaza postulación si el animal está en CUARENTENA")
    void debeRechazarPostulacionCuandoAnimalEstaEnCuarentena() {
        animalEnCatalogo.setEstado(EstadoAnimal.CUARENTENA);
        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animalEnCatalogo));

        PostularAdopcionRequest request = new PostularAdopcionRequest(
                animalId,
                "Valeria Gómez",
                "valeria@ejemplo.pe",
                "+51987654321",
                "DEPARTAMENTO",
                false,
                "Motivo válido de más de quince caracteres.",
                true
        );

        assertThatThrownBy(() -> adopcionService.postularAdopcion(request, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no se encuentra habilitado para adopción");

        verify(solicitudRepository, never()).save(any());
    }

    @Test
    @DisplayName("3. Idempotencia HTTP: retorna IDEMPOTENT_REPLAY sin duplicar si se reenvía con Idempotency-Key")
    void debeRetornarReplayIdempotenteAnteReintentoConClaveDeIdempotencia() {
        SolicitudAdopcion existente = SolicitudAdopcion.builder()
                .id(UUID.randomUUID())
                .animal(animalEnCatalogo)
                .adoptante(adoptante)
                .estado(EstadoSolicitud.RECIBIDA)
                .tipoVivienda("CASA_CON_JARDIN")
                .tieneOtrasMascotas(false)
                .motivo("Motivo registrado previamente.")
                .fechaSolicitud(Instant.now())
                .build();

        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animalEnCatalogo));
        when(usuarioRepository.findByEmail("valeria@ejemplo.pe")).thenReturn(Optional.of(adoptante));
        when(solicitudRepository.findByAnimalIdAndAdoptanteId(animalId, adoptanteId))
                .thenReturn(Optional.of(existente));

        PostularAdopcionRequest request = new PostularAdopcionRequest(
                animalId,
                "Valeria Gómez",
                "valeria@ejemplo.pe",
                "+51987654321",
                "CASA_CON_JARDIN",
                false,
                "Motivo registrado previamente.",
                true
        );

        SolicitudAdopcionResponse response = adopcionService.postularAdopcion(request, "retry-key-777");

        assertThat(response.estadoNotificacionExterna()).isEqualTo("IDEMPOTENT_REPLAY");
        verify(solicitudRepository, never()).save(any());
        verify(notificationPort, never()).enviarNotificacionAdopcion(any());
    }

    @Test
    @DisplayName("4. Aprobación atómica: genera QR SHA-256, cambia animal a ADOPTADO y cierra competidoras")
    void debeAprobarSolicitudEmitirQrYCerrarSolicitudesCompetidoras() {
        UUID solGanadoraId = UUID.fromString("40000000-0000-0000-0000-000000000004");
        UUID solCompetidoraId = UUID.fromString("50000000-0000-0000-0000-000000000005");

        SolicitudAdopcion ganadora = SolicitudAdopcion.builder()
                .id(solGanadoraId)
                .animal(animalEnCatalogo)
                .adoptante(adoptante)
                .estado(EstadoSolicitud.RECIBIDA)
                .tipoVivienda("CASA_CON_JARDIN")
                .tieneOtrasMascotas(false)
                .motivo("Postulación ganadora")
                .build();

        SolicitudAdopcion competidora = SolicitudAdopcion.builder()
                .id(solCompetidoraId)
                .animal(animalEnCatalogo)
                .adoptante(Usuario.builder().id(UUID.randomUUID()).nombre("Otro").email("otro@pe.com").build())
                .estado(EstadoSolicitud.RECIBIDA)
                .build();

        when(solicitudRepository.findById(solGanadoraId)).thenReturn(Optional.of(ganadora));
        when(solicitudRepository.save(any(SolicitudAdopcion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(solicitudRepository.findByAnimalId(animalId)).thenReturn(List.of(ganadora, competidora));
        when(notificationPort.enviarNotificacionAdopcion(any()))
                .thenReturn(NotificationDeliveryResult.simulated("idem-approve"));

        SolicitudAdopcionResponse aprobada = adopcionService.aprobarSolicitud(solGanadoraId, "idem-approve");

        assertThat(aprobada.estado()).isEqualTo(EstadoSolicitud.APROBADA);
        assertThat(aprobada.codigoCertificadoQr()).startsWith("CERT-RL-");
        assertThat(animalEnCatalogo.getEstado()).isEqualTo(EstadoAnimal.ADOPTADO);
        assertThat(competidora.getEstado()).isEqualTo(EstadoSolicitud.CERRADA_POR_OTRA_ADOPCION);
    }

    @Test
    @DisplayName("5. Verificación pública de Certificado QR: confirma autenticidad de adopción aprobada")
    void debeVerificarCertificadoQrValido() {
        SolicitudAdopcion aprobada = SolicitudAdopcion.builder()
                .id(UUID.randomUUID())
                .animal(animalEnCatalogo)
                .adoptante(adoptante)
                .estado(EstadoSolicitud.APROBADA)
                .codigoCertificadoQr("CERT-RL-ABCDEF123456")
                .fechaSolicitud(Instant.now())
                .build();

        when(solicitudRepository.findByCodigoCertificadoQr("CERT-RL-ABCDEF123456"))
                .thenReturn(Optional.of(aprobada));

        VerificacionCertificadoQrResponse verificacion = adopcionService.verificarCertificadoQr("CERT-RL-ABCDEF123456");

        assertThat(verificacion.valido()).isTrue();
        assertThat(verificacion.codigoCertificadoQr()).isEqualTo("CERT-RL-ABCDEF123456");
        assertThat(verificacion.nombreAnimal()).isEqualTo("Rocky");
        assertThat(verificacion.nombreAdoptante()).isEqualTo("Valeria Gómez");
    }
}

