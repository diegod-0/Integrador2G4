package org.rescuelink.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rescuelink.dto.AnimalDtos.AnimalCatalogDto;
import org.rescuelink.dto.ApiResponse;
import org.rescuelink.service.AnimalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/animales/adopcion")
@RequiredArgsConstructor
@Tag(name = "Catálogo de Animales", description = "Exploración de animales rescatados y en adopción (HU03)")
public class AnimalController {

    private final AnimalService animalService;

    @GetMapping
    @Operation(summary = "Obtener el catálogo público de animales en adopción")
    public ResponseEntity<ApiResponse<List<AnimalCatalogDto>>> obtenerCatalogo() {
        List<AnimalCatalogDto> catalogo = animalService.obtenerCatalogoAdopcion();
        return ResponseEntity.ok(ApiResponse.ok(catalogo));
    }
}
