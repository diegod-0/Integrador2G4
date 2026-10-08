package org.rescuelink.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.rescuelink.dto.ApiResponse;
import org.rescuelink.dto.TicketDtos.ReportEmergencyRequest;
import org.rescuelink.dto.TicketDtos.TicketTrackerResponse;
import org.rescuelink.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.rescuelink.dto.DuplicateCheckDtos.DuplicateCheckRequest;
import org.rescuelink.dto.DuplicateCheckDtos.DuplicateCheckResponse;
import org.rescuelink.service.DuplicateDetectionService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Emergencias y Rescates", description = "Endpoints ciudadanos y operativos para reporte ágil y Rescue Tracker")
public class TicketController {

    private final TicketService ticketService;
    private final DuplicateDetectionService duplicateDetectionService;

    @PostMapping("/reportar")
    @Operation(summary = "Reportar emergencia animal con geolocalización")
    public ResponseEntity<ApiResponse<TicketTrackerResponse>> reportarEmergencia(
            @Valid @RequestBody ReportEmergencyRequest request) {
        TicketTrackerResponse ticket = ticketService.reportarEmergencia(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Emergencia registrada exitosamente", ticket));
    }

    @GetMapping("/tracking/{codigo}")
    @Operation(summary = "Consultar estado de un ticket por código de tracking")
    public ResponseEntity<ApiResponse<TicketTrackerResponse>> consultarTracking(
            @PathVariable("codigo") String codigo) {
        TicketTrackerResponse ticket = ticketService.obtenerPorCodigo(codigo);
        return ResponseEntity.ok(ApiResponse.ok(ticket));
    }

    @GetMapping("/verificar-duplicado")
    @Operation(summary = "Verificar si ya existe un reporte activo cercano antes de registrar la emergencia")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> verificarDuplicado(
            @Valid @ModelAttribute @ParameterObject DuplicateCheckRequest request) {
        double radio = request.radioMetros() != null
                ? request.radioMetros().doubleValue()
                : DuplicateDetectionService.RADIO_POR_DEFECTO_METROS;

        DuplicateCheckResponse resultado = duplicateDetectionService.verificar(
                request.lng().doubleValue(),
                request.lat().doubleValue(),
                radio);
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }
}
