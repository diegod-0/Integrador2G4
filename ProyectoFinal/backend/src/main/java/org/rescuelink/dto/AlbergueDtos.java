package org.rescuelink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.rescuelink.model.enums.TipoDonacion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AlbergueDtos {

    public record AlbergueSummaryDto(
            UUID id,
            String nombre,
            String direccion,
            String telefono,
            Integer capacidadMax,
            Long ocupacionActual,
            Double porcentajeOcupacion,
            Double longitud,
            Double latitud,
            Double distanciaMetros
    ) {}

    public record AlbergueDetailDto(
            UUID id,
            String nombre,
            String direccion,
            String telefono,
            Integer capacidadMax,
            Long ocupacionActual,
            Double porcentajeOcupacion,
            Double longitud,
            Double latitud
    ) {}

    public record RegistrarDonacionRequest(
            @NotNull(message = "El tipo de donación es obligatorio")
            TipoDonacion tipo,

            @NotNull(message = "El monto o valor estimado es obligatorio")
            @Positive(message = "El monto debe ser un valor positivo")
            BigDecimal montoEstimado,

            @NotBlank(message = "La descripción de la donación es obligatoria")
            String descripcion
    ) {}

    public record DonacionResponseDto(
            UUID id,
            UUID albergueId,
            TipoDonacion tipo,
            BigDecimal montoEstimado,
            String descripcion,
            Instant fechaRegistro
    ) {}
}
