package org.rescuelink.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.rescuelink.dto.AdopcionDtos.PostularAdopcionRequest;
import org.rescuelink.dto.AdopcionDtos.SolicitudAdopcionResponse;
import org.rescuelink.dto.AdopcionDtos.VerificacionCertificadoQrResponse;
import org.rescuelink.dto.ApiResponse;
import org.rescuelink.service.AdopcionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/adopciones")
@RequiredArgsConstructor
@Tag(
        name = "Adopciones, Interoperabilidad y Certificados QR",
        description = "Postulación idempotente, dictamen administrativo, notificación externa y verificación criptográfica QR (HU06, HU13, HU14, HU16)"
)
public class AdopcionController {

    private final AdopcionService adopcionService;

    @PostMapping("/postular")
    @Operation(
            summary = "Registrar postulación ciudadana a adopción con soporte de idempotencia e interoperabilidad externa",
            description = "Valida requisitos de tenencia responsable (Ley 30407), evita duplicados por Idempotency-Key y notifica vía NotificationPort."
    )
    public ResponseEntity<ApiResponse<SolicitudAdopcionResponse>> postular(
            @Valid @RequestBody PostularAdopcionRequest request,
            @Parameter(description = "Clave de idempotencia opcional para prevenir envíos dobles ante reintentos de red")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        SolicitudAdopcionResponse response = adopcionService.postularAdopcion(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Solicitud de adopción procesada exitosamente.", response));
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Aprobar solicitud de adopción, emitir Certificado QR y notificar al sistema externo",
            description = "Transacción atómica: aprueba al postulante idóneo, genera código QR SHA-256, pasa el animal a ADOPTADO y cierra postulaciones competidoras."
    )
    public ResponseEntity<ApiResponse<SolicitudAdopcionResponse>> aprobar(
            @PathVariable UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        SolicitudAdopcionResponse response = adopcionService.aprobarSolicitud(id, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.ok("Adopción aprobada y certificado QR emitido.", response));
    }

    @GetMapping("/verificar/{codigoQr}")
    @Operation(
            summary = "Portal público de verificación criptográfica de Certificado Oficial de Adopción por código QR",
            description = "Permite a cualquier ciudadano o autoridad fiscalizadora comprobar la validez inmutable de un certificado emitido."
    )
    public ResponseEntity<ApiResponse<VerificacionCertificadoQrResponse>> verificarQr(
            @PathVariable String codigoQr
    ) {
        VerificacionCertificadoQrResponse response = adopcionService.verificarCertificadoQr(codigoQr);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/animal/{animalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar postulaciones recibidas para un animal específico")
    public ResponseEntity<ApiResponse<List<SolicitudAdopcionResponse>>> listarPorAnimal(
            @PathVariable UUID animalId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adopcionService.listarPorAnimal(animalId)));
    }
}

