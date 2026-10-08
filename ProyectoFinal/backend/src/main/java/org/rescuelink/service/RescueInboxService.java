package org.rescuelink.service;

import lombok.RequiredArgsConstructor;
import org.rescuelink.dto.RescueInboxDtos.ActualizarEstadoTicketRequest;
import org.rescuelink.dto.RescueInboxDtos.AsignarVoluntarioRequest;
import org.rescuelink.dto.RescueInboxDtos.InboxTicketSummaryDto;
import org.rescuelink.dto.TicketDtos.TicketTrackerResponse;
import org.rescuelink.exception.BusinessException;
import org.rescuelink.exception.CapacityExceededException;
import org.rescuelink.exception.ResourceNotFoundException;
import org.rescuelink.model.Albergue;
import org.rescuelink.model.TicketRescate;
import org.rescuelink.model.Usuario;
import org.rescuelink.model.enums.EstadoTicket;
import org.rescuelink.model.enums.GravedadTicket;
import org.rescuelink.model.enums.RolUsuario;
import org.rescuelink.repository.AlbergueRepository;
import org.rescuelink.repository.TicketRescateRepository;
import org.rescuelink.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Caso de uso HU10: bandeja operativa de rescates por proximidad al albergue y
 * asignación de voluntarios/refugios. Servicio administrativo independiente de
 * {@link TicketService}: no altera la auto-asignación de reportarEmergencia.
 */
@Service
@RequiredArgsConstructor
public class RescueInboxService {

    /** Estados operativos que el administrador puede fijar manualmente (HU10). */
    private static final Set<EstadoTicket> ESTADOS_OPERATIVOS =
            EnumSet.of(EstadoTicket.EN_CAMINO, EstadoTicket.RESCATADO,
                    EstadoTicket.NO_LOCALIZADO, EstadoTicket.RECHAZADO);

    private final TicketRescateRepository ticketRepository;
    private final AlbergueRepository albergueRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Bandeja de rescates dentro del radio geodésico (metros) alrededor del
     * albergue indicado, ordenada de menor a mayor distancia. Incluye tickets
     * PENDIENTE y ASIGNADO.
     */
    @Transactional(readOnly = true)
    public List<InboxTicketSummaryDto> obtenerBandeja(UUID albergueId, double radioMetros) {
        if (!Double.isFinite(radioMetros) || radioMetros <= 0) {
            throw new BusinessException("El radio en metros debe ser un número positivo.");
        }
        Albergue albergue = albergueRepository.findById(albergueId)
                .orElseThrow(() -> new ResourceNotFoundException("Albergue", albergueId));
        if (albergue.getUbicacion() == null) {
            throw new BusinessException(String.format(
                    "El albergue '%s' no tiene coordenadas registradas.", albergue.getNombre()));
        }
        double longitud = albergue.getUbicacion().getX();
        double latitud = albergue.getUbicacion().getY();

        return ticketRepository.findBandejaOperativa(longitud, latitud, radioMetros)
                .stream()
                .map(this::mapToSummary)
                .toList();
    }

    /**
     * Asigna un voluntario y un albergue receptor a un ticket PENDIENTE,
     * validando la capacidad disponible del albergue (bloqueo si está saturado).
     */
    @Transactional
    public TicketTrackerResponse asignarRescate(UUID ticketId, AsignarVoluntarioRequest request) {
        TicketRescate ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket de rescate", ticketId));

        if (ticket.getEstado() != EstadoTicket.PENDIENTE) {
            throw new BusinessException(String.format(
                    "Solo se pueden asignar tickets en estado PENDIENTE; el ticket '%s' se encuentra en estado %s.",
                    ticket.getCodigoTracking(), ticket.getEstado()));
        }

        Usuario voluntario = usuarioRepository.findById(request.voluntarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Voluntario", request.voluntarioId()));
        if (voluntario.getRol() != RolUsuario.ROLE_VOLUNTARIO) {
            throw new BusinessException(String.format(
                    "El usuario '%s' no posee el rol de VOLUNTARIO requerido para ser asignado.",
                    voluntario.getEmail()));
        }

        Albergue albergue = albergueRepository.findById(request.albergueId())
                .orElseThrow(() -> new ResourceNotFoundException("Albergue", request.albergueId()));

        Object[] capacidad = albergueRepository.findCapacidadDisponible(albergue.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Albergue", albergue.getId()));
        int capacidadMax = ((Number) capacidad[0]).intValue();
        long ocupacionActual = ((Number) capacidad[1]).longValue();
        if (ocupacionActual >= capacidadMax) {
            throw new CapacityExceededException(String.format(
                    "El albergue '%s' se encuentra al máximo de su capacidad (%d de %d cupos ocupados); no es posible asignarle el rescate.",
                    albergue.getNombre(), ocupacionActual, capacidadMax));
        }

        ticket.setVoluntario(voluntario);
        ticket.setAlbergue(albergue);
        ticket.setEstado(EstadoTicket.ASIGNADO);
        return mapToTrackerResponse(ticketRepository.save(ticket));
    }

    /**
     * Actualiza el estado operativo del ticket (EN_CAMINO, RESCATADO,
     * NO_LOCALIZADO, RECHAZADO) para que el tracking público refleje el avance
     * en vivo. PENDIENTE/ASIGNADO se gestionan únicamente vía asignación.
     */
    @Transactional
    public TicketTrackerResponse actualizarEstado(UUID ticketId, ActualizarEstadoTicketRequest request) {
        TicketRescate ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket de rescate", ticketId));

        EstadoTicket nuevoEstado = request.nuevoEstado();
        if (!ESTADOS_OPERATIVOS.contains(nuevoEstado)) {
            throw new BusinessException(String.format(
                    "El estado '%s' no es transicionable por este endpoint; use PENDIENTE/ASIGNADO vía asignación.",
                    nuevoEstado));
        }
        ticket.setEstado(nuevoEstado);
        return mapToTrackerResponse(ticketRepository.save(ticket));
    }

    private InboxTicketSummaryDto mapToSummary(Object[] fila) {
        return new InboxTicketSummaryDto(
                (UUID) fila[0],
                (String) fila[1],
                (String) fila[2],
                GravedadTicket.valueOf((String) fila[3]),
                EstadoTicket.valueOf((String) fila[4]),
                ((Number) fila[5]).doubleValue(),
                ((Number) fila[6]).doubleValue(),
                fila[7] == null ? null : ((Number) fila[7]).doubleValue(),
                (String) fila[8],
                (String) fila[9],
                (String) fila[10],
                toInstant(fila[11])
        );
    }

    private TicketTrackerResponse mapToTrackerResponse(TicketRescate t) {
        return new TicketTrackerResponse(
                t.getId(),
                t.getCodigoTracking(),
                t.getNombreReportante(),
                t.getDescripcion(),
                t.getGravedad(),
                t.getEstado(),
                t.getUbicacion().getX(),
                t.getUbicacion().getY(),
                t.getFotoUrl(),
                t.getAlbergue() != null ? t.getAlbergue().getNombre() : null,
                t.getVoluntario() != null ? t.getVoluntario().getNombre() : null,
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }

    /**
     * Normaliza el tipo temporal devuelto por las consultas nativas
     * (PgJDBC entrega OffsetDateTime para TIMESTAMPTZ).
     */
    private Instant toInstant(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Instant instant) {
            return instant;
        }
        if (valor instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        if (valor instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant();
        }
        throw new IllegalStateException("Tipo temporal no soportado en la bandeja: " + valor.getClass().getName());
    }
}