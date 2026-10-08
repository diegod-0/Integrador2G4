package org.rescuelink.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rescuelink.dto.DuplicateCheckDtos.DuplicateCheckResponse;
import org.rescuelink.dto.DuplicateCheckDtos.DuplicateIncidentDetail;
import org.rescuelink.model.TicketRescate;
import org.rescuelink.model.enums.EstadoTicket;
import org.rescuelink.repository.TicketRescateRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DuplicateDetectionServiceTest {

    private static final double LNG = -77.0305;
    private static final double LAT = -12.12134;

    @Mock
    private TicketRescateRepository ticketRepository;

    @InjectMocks
    private DuplicateDetectionService service;

    @Test
    @DisplayName("Debe responder sin colisión cuando no hay reportes activos cercanos")
    void verificar_SinReportesCercanos_NoHayDuplicado() {
        when(ticketRepository.findDuplicadosCercanos(eq(LNG), eq(LAT), eq(50.0), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        DuplicateCheckResponse response = service.verificar(LNG, LAT, 50.0);

        assertFalse(response.hayDuplicado());
        assertNull(response.incidenteCercano());
        verify(ticketRepository, never()).calcularDistanciaMetros(any(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Debe detectar colisión y devolver código, estado y distancia redondeada del ticket más cercano")
    void verificar_ConReporteCercano_DevuelveDetalle() {
        UUID id = UUID.randomUUID();
        TicketRescate cercano = TicketRescate.builder()
                .id(id)
                .codigoTracking("TICK-1025")
                .estado(EstadoTicket.EN_CAMINO)
                .fotoUrl("https://images.unsplash.com/foto.jpg")
                .build();

        when(ticketRepository.findDuplicadosCercanos(eq(LNG), eq(LAT), eq(50.0), any(Instant.class)))
                .thenReturn(List.of(cercano));
        when(ticketRepository.calcularDistanciaMetros(id, LNG, LAT)).thenReturn(17.7049);

        DuplicateCheckResponse response = service.verificar(LNG, LAT, 50.0);

        assertTrue(response.hayDuplicado());
        DuplicateIncidentDetail detalle = response.incidenteCercano();
        assertEquals("TICK-1025", detalle.codigoTracking());
        assertEquals(EstadoTicket.EN_CAMINO, detalle.estado());
        assertEquals(17.7, detalle.distanciaMetros(), 0.001);
        assertEquals("https://images.unsplash.com/foto.jpg", detalle.fotoUrl());
    }

    @Test
    @DisplayName("Debe consultar solo reportes de las últimas 2 horas")
    void verificar_UsaVentanaDeDosHoras() {
        service.verificar(LNG, LAT, 50.0);

        ArgumentCaptor<Instant> limite = ArgumentCaptor.forClass(Instant.class);
        verify(ticketRepository).findDuplicadosCercanos(eq(LNG), eq(LAT), eq(50.0), limite.capture());

        long segundosAtras = Duration.between(limite.getValue(), Instant.now()).getSeconds();
        assertTrue(Math.abs(segundosAtras - 7200) < 5,
                "La ventana temporal debe ser de 2 horas, pero fue de " + segundosAtras + " s");
    }

    @Test
    @DisplayName("Debe respetar el radio recibido en metros")
    void verificar_RespetaRadioSolicitado() {
        service.verificar(LNG, LAT, 30.0);

        verify(ticketRepository).findDuplicadosCercanos(eq(LNG), eq(LAT), eq(30.0), any(Instant.class));
    }
}