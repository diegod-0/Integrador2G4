package org.rescuelink.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.rescuelink.dto.ApiResponse;
import org.rescuelink.dto.RescueInboxDtos.ActualizarEstadoTicketRequest;
import org.rescuelink.dto.RescueInboxDtos.AsignarVoluntarioRequest;
import org.rescuelink.dto.RescueInboxDtos.InboxTicketSummaryDto;
import org.rescuelink.dto.TicketDtos.TicketTrackerResponse;
import org.rescuelink.service.RescueInboxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/rescates")
@RequiredArgsConstructor
@Tag(name = "Bandeja Operativa", description = "Inbox operativo por proximidad al albergue y asignación de refugios (HU10)")
public class RescueInboxController {

    private final RescueInboxService rescueInboxService;

    @GetMapping("/bandeja")
    @Operation(summary = "Consultar los rescates cercanos al albergue ordenados por distancia")
    public ResponseEntity<ApiResponse<List<InboxTicketSummaryDto>>> obtenerBandeja(
            @RequestParam UUID albergueId,
            @RequestParam(defaultValue = "15000") double radioMetros
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                rescueInboxService.obtenerBandeja(albergueId, radioMetros)));
    }

    @PatchMapping("/{id}/asignar")
    @Operation(summary = "Asignar un voluntario y un albergue receptor a un ticket pendiente")
    public ResponseEntity<ApiResponse<TicketTrackerResponse>> asignarRescate(
            @PathVariable UUID id,
            @Valid @RequestBody AsignarVoluntarioRequest request
    ) {
        TicketTrackerResponse ticket = rescueInboxService.asignarRescate(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Rescate asignado exitosamente", ticket));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar el estado operativo del ticket para reflejarlo en el tracking en vivo")
    public ResponseEntity<ApiResponse<TicketTrackerResponse>> actualizarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarEstadoTicketRequest request
    ) {
        TicketTrackerResponse ticket = rescueInboxService.actualizarEstado(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Estado del ticket actualizado exitosamente", ticket));
    }
}