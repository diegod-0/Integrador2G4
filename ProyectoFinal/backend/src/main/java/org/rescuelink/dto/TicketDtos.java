package org.rescuelink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.rescuelink.model.enums.EstadoTicket;
import org.rescuelink.model.enums.GravedadTicket;

import java.time.Instant;
import java.util.UUID;

public class TicketDtos {

    public record ReportEmergencyRequest(
            @NotBlank(message = "El nombre del reportante es obligatorio")
            @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
            String nombreReportante,

            @NotBlank(message = "El WhatsApp de contacto es obligatorio")
            @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Número de WhatsApp inválido")
            String whatsappReportante,

            @NotBlank(message = "La descripción de la emergencia es obligatoria")
            @Size(min = 15, message = "La descripción debe tener al menos 15 caracteres para orientar el rescate")
            String descripcion,

            @NotNull(message = "El nivel de gravedad es obligatorio")
            GravedadTicket gravedad,

            @NotNull(message = "La longitud geodésica es obligatoria")
            Double longitud,

            @NotNull(message = "La latitud geodésica es obligatoria")
            Double latitud,

            @NotBlank(message = "La URL de la foto de evidencia es obligatoria")
            String fotoUrl
    ) {}

    public record TicketTrackerResponse(
            UUID id,
            String codigoTracking,
            String nombreReportante,
            String descripcion,
            GravedadTicket gravedad,
            EstadoTicket estado,
            Double longitud,
            Double latitud,
            String fotoUrl,
            String albergueNombre,
            String voluntarioNombre,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
