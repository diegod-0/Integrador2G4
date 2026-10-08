package org.rescuelink.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.rescuelink.model.enums.EstadoTicket;

import java.math.BigDecimal;
import java.time.Instant;

public class DuplicateCheckDtos {

    /** Parámetros de consulta de la verificación preventiva de duplicados. */
    public record DuplicateCheckRequest(
            @NotNull(message = "La longitud es obligatoria") @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180") @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180") BigDecimal lng,

            @NotNull(message = "La latitud es obligatoria") @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90") @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90") BigDecimal lat,

            @DecimalMin(value = "1.0", message = "El radio mínimo es de 1 metro") @DecimalMax(value = "100.0", message = "El radio máximo permitido es de 100 metros") BigDecimal radioMetros) {
    }

    public record DuplicateCheckResponse(
            boolean hayDuplicado,
            String mensaje,
            DuplicateIncidentDetail incidenteCercano) {
        public static DuplicateCheckResponse sinColision() {
            return new DuplicateCheckResponse(false,
                    "No se detectaron reportes activos en la zona.", null);
        }

        public static DuplicateCheckResponse conColision(DuplicateIncidentDetail incidente) {
            return new DuplicateCheckResponse(true,
                    "Se encontró un reporte activo reciente en esta ubicación.", incidente);
        }
    }

    public record DuplicateIncidentDetail(
            String codigoTracking,
            EstadoTicket estado,
            Double distanciaMetros,
            String fotoUrl,
            Instant fechaReporte) {
    }
}