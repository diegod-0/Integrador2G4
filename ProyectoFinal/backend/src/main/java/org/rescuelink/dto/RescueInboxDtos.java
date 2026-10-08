package org.rescuelink.dto;

import jakarta.validation.constraints.NotNull;
import org.rescuelink.model.enums.EstadoTicket;
import org.rescuelink.model.enums.GravedadTicket;

import java.time.Instant;
import java.util.UUID;

/**
 * Contratos de la bandeja operativa de rescates (HU10).
 */
public class RescueInboxDtos {

    public record InboxTicketSummaryDto(
            UUID ticketId,
            String codigoTracking,
            String descripcion,
            GravedadTicket gravedad,
            EstadoTicket estado,
            Double longitud,
            Double latitud,
            Double distanciaMetros,
            String fotoUrl,
            String albergueAsignadoNombre,
            String voluntarioNombre,
            Instant fechaReporte
    ) {}

    public record AsignarVoluntarioRequest(
            @NotNull(message = "El identificador del voluntario es obligatorio")
            UUID voluntarioId,

            @NotNull(message = "El identificador del albergue es obligatorio")
            UUID albergueId
    ) {}

    public record ActualizarEstadoTicketRequest(
            @NotNull(message = "El nuevo estado del ticket es obligatorio")
            EstadoTicket nuevoEstado
    ) {}
}