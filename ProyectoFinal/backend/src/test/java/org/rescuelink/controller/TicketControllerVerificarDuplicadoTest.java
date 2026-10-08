package org.rescuelink.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rescuelink.dto.DuplicateCheckDtos.DuplicateCheckResponse;
import org.rescuelink.dto.DuplicateCheckDtos.DuplicateIncidentDetail;
import org.rescuelink.exception.GlobalExceptionHandler;
import org.rescuelink.model.enums.EstadoTicket;
import org.rescuelink.service.DuplicateDetectionService;
import org.rescuelink.service.TicketService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TicketControllerVerificarDuplicadoTest {

    private static final String URL = "/api/tickets/verificar-duplicado";

    @Mock
    private TicketService ticketService;

    @Mock
    private DuplicateDetectionService duplicateDetectionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TicketController controller = new TicketController(ticketService, duplicateDetectionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Debe responder 200 con hayDuplicado=false y radio por defecto de 50 m")
    void verificarDuplicado_SinColision() throws Exception {
        when(duplicateDetectionService.verificar(-77.0305, -12.12134, 50.0))
                .thenReturn(DuplicateCheckResponse.sinColision());

        mockMvc.perform(get(URL).param("lng", "-77.0305").param("lat", "-12.12134"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hayDuplicado").value(false));
    }

    @Test
    @DisplayName("Debe responder 200 con el ticket cercano y su distancia cuando hay colisión")
    void verificarDuplicado_ConColision() throws Exception {
        DuplicateIncidentDetail detalle = new DuplicateIncidentDetail(
                "TICK-1025", EstadoTicket.EN_CAMINO, 17.7,
                "https://images.unsplash.com/foto.jpg", Instant.parse("2026-10-08T15:00:00Z"));
        when(duplicateDetectionService.verificar(-77.0305, -12.12134, 50.0))
                .thenReturn(DuplicateCheckResponse.conColision(detalle));

        mockMvc.perform(get(URL).param("lng", "-77.0305").param("lat", "-12.12134"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hayDuplicado").value(true))
                .andExpect(jsonPath("$.data.incidenteCercano.codigoTracking").value("TICK-1025"))
                .andExpect(jsonPath("$.data.incidenteCercano.distanciaMetros").value(17.7));
    }

    @ParameterizedTest(name = "[{index}] consulta inválida: {0}")
    @ValueSource(strings = {
            "lng=-77.03&lat=95",
            "lng=-200&lat=-12.12",
            "lng=-77.03",
            "lat=-12.12",
            "lng=-77.03&lat=-12.12&radioMetros=500",
            "lng=-77.03&lat=-12.12&radioMetros=0",
            "lng=abc&lat=-12.12"
    })
    @DisplayName("Debe responder 400 y no consultar al servicio cuando los parámetros son inválidos")
    void verificarDuplicado_ParametrosInvalidos_Responde400(String query) throws Exception {
        mockMvc.perform(get(URL + "?" + query))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(duplicateDetectionService);
    }
}