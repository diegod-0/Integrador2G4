package org.rescuelink.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.rescuelink.dto.AlbergueDtos.AlbergueDetailDto;
import org.rescuelink.dto.AlbergueDtos.AlbergueSummaryDto;
import org.rescuelink.dto.AlbergueDtos.DonacionResponseDto;
import org.rescuelink.dto.AlbergueDtos.RegistrarDonacionRequest;
import org.rescuelink.dto.ApiResponse;
import org.rescuelink.dto.ProblemDetailsResponse;
import org.rescuelink.service.AlbergueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/albergues")
@RequiredArgsConstructor
@Tag(name = "Directorio de Albergues", description = "Directorio geográfico, perfiles y registro de donaciones")
public class AlbergueController {

    private final AlbergueService albergueService;

    @GetMapping
    @Operation(summary = "Consultar el directorio de albergues y su ocupación")
    public ResponseEntity<?> obtenerDirectorio(
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double lat
    ) {
        if ((lng == null) != (lat == null)
                || (lng != null && (!Double.isFinite(lng) || !Double.isFinite(lat)
                || lng < -180 || lng > 180 || lat < -90 || lat > 90))) {
            ProblemDetailsResponse problem = ProblemDetailsResponse.of(
                    "https://rescuelink.org/errors/invalid-coordinates",
                    "Coordenadas Inválidas",
                    HttpStatus.BAD_REQUEST.value(),
                    "Proporcione longitud y latitud juntas, dentro de los rangos geográficos válidos.",
                    "/api/albergues"
            );
            return ResponseEntity.badRequest().body(problem);
        }
        return ResponseEntity.ok(ApiResponse.ok(albergueService.obtenerDirectorio(lng, lat)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar el perfil y ocupación de un albergue")
    public ResponseEntity<ApiResponse<AlbergueDetailDto>> obtenerDetalle(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(albergueService.obtenerDetalle(id)));
    }

    @PostMapping("/{id}/donaciones")
    @Operation(summary = "Registrar una intención de donación para un albergue")
    public ResponseEntity<ApiResponse<DonacionResponseDto>> registrarDonacion(
            @PathVariable UUID id,
            @Valid @RequestBody RegistrarDonacionRequest request,
            Authentication authentication
    ) {
        String emailDonante = authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserDetails userDetails
                ? userDetails.getUsername()
                : null;
        DonacionResponseDto donacion = albergueService.registrarDonacion(id, request, emailDonante);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Intención de donación registrada exitosamente", donacion));
    }
}
