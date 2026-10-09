package org.rescuelink.service;

import lombok.RequiredArgsConstructor;
import org.rescuelink.dto.AnimalDtos.AnimalCatalogDto;
import org.rescuelink.model.Animal;
import org.rescuelink.model.enums.EstadoAnimal;
import org.rescuelink.repository.AnimalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimalService {

    private final AnimalRepository animalRepository;

    @Transactional(readOnly = true)
    public List<AnimalCatalogDto> obtenerCatalogoAdopcion() {
        List<Animal> animales = animalRepository.findByEstado(EstadoAnimal.EN_CATALOGO);

        return animales.stream()
                .map(this::mapToCatalogDto)
                .toList();
    }

    private AnimalCatalogDto mapToCatalogDto(Animal animal) {
        return new AnimalCatalogDto(
                animal.getId(),
                animal.getNombre(),
                animal.getEspecie(),
                animal.getEdadEstimada(),
                animal.getPeso(),
                animal.getNivelEnergia(),
                animal.getAptoDepartamento(),
                animal.getEstado(),
                animal.getFotoPerfilUrl(),
                animal.getFotoAntesUrl(),
                animal.getFotoDespuesUrl(),
                animal.getAlbergue() != null ? animal.getAlbergue().getId() : null,
                animal.getAlbergue() != null ? animal.getAlbergue().getNombre() : "Albergue Aliado",
                null
        );
    }
}
