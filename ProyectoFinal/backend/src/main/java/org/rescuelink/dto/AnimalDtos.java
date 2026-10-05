package org.rescuelink.dto;

import org.rescuelink.model.enums.EspecieAnimal;
import org.rescuelink.model.enums.EstadoAnimal;
import org.rescuelink.model.enums.NivelEnergia;

import java.math.BigDecimal;
import java.util.UUID;

public class AnimalDtos {

    public record AnimalCatalogDto(
            UUID id,
            String nombre,
            EspecieAnimal especie,
            String edadEstimada,
            BigDecimal peso,
            NivelEnergia nivelEnergia,
            Boolean aptoDepartamento,
            EstadoAnimal estado,
            String fotoPerfilUrl,
            String fotoAntesUrl,
            String fotoDespuesUrl,
            UUID albergueId,
            String albergueNombre,
            Double distanciaMetros
    ) {}
}
