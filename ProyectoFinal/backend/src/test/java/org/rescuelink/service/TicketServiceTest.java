package org.rescuelink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rescuelink.dto.TicketDtos.ReportEmergencyRequest;
import org.rescuelink.dto.TicketDtos.TicketTrackerResponse;
import org.rescuelink.exception.DuplicateReportException;
import org.rescuelink.exception.ResourceNotFoundException;
import org.rescuelink.model.TicketRescate;
import org.rescuelink.model.enums.EstadoTicket;
import org.rescuelink.model.enums.GravedadTicket;
import org.rescuelink.repository.AlbergueRepository;
import org.rescuelink.repository.TicketRescateRepository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRescateRepository ticketRepository;

    @Mock
    private AlbergueRepository albergueRepository;

    @InjectMocks
    private TicketService ticketService;

    private GeometryFactory geometryFactory;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }

    @Test
    @DisplayName("Debe registrar reporte y generar código de tracking cuando no hay colisiones espaciales")
    void reportarEmergencia_Exitoso() {
        // Arrange
        ReportEmergencyRequest request = new ReportEmergencyRequest(
                "Carlos Benavides",
                "+51987654321",
                "Perrito atropellado con herida grave en pata",
                GravedadTicket.CRITICA,
                -77.0315,
                -12.1221,
                "https://images.unsplash.com/photo-1543466835-00a7907e9de1"
        );

        Point punto = geometryFactory.createPoint(new Coordinate(request.longitud(), request.latitud()));

        when(ticketRepository.findDuplicadosCercanos(anyDouble(), anyDouble(), eq(50.0), any(Instant.class)))
                .thenReturn(Collections.emptyList());
        when(albergueRepository.findCercanos(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Collections.emptyList());

        TicketRescate ticketMock = TicketRescate.builder()
                .id(UUID.randomUUID())
                .codigoTracking("TICK-7711")
                .nombreReportante(request.nombreReportante())
                .whatsappReportante(request.whatsappReportante())
                .descripcion(request.descripcion())
                .gravedad(request.gravedad())
                .estado(EstadoTicket.PENDIENTE)
                .ubicacion(punto)
                .fotoUrl(request.fotoUrl())
                .build();

        when(ticketRepository.save(any(TicketRescate.class))).thenReturn(ticketMock);

        // Act
        TicketTrackerResponse response = ticketService.reportarEmergencia(request);

        // Assert
        assertNotNull(response);
        assertEquals("TICK-7711", response.codigoTracking());
        assertEquals(EstadoTicket.PENDIENTE, response.estado());
        assertEquals("Carlos Benavides", response.nombreReportante());
        verify(ticketRepository, times(1)).save(any(TicketRescate.class));
    }

    @Test
    @DisplayName("Debe lanzar DuplicateReportException si existe reporte en radio de 50m en menos de 2h")
    void reportarEmergencia_ColisionEspacial_LanzaExcepcion() {
        // Arrange
        ReportEmergencyRequest request = new ReportEmergencyRequest(
                "Carlos Benavides",
                "+51987654321",
                "Perrito atropellado con herida grave en pata",
                GravedadTicket.CRITICA,
                -77.0315,
                -12.1221,
                "https://images.unsplash.com/foto.jpg"
        );

        TicketRescate reporteExistente = TicketRescate.builder()
                .codigoTracking("TICK-1025")
                .build();

        when(ticketRepository.findDuplicadosCercanos(anyDouble(), anyDouble(), eq(50.0), any(Instant.class)))
                .thenReturn(List.of(reporteExistente));

        // Act & Assert
        DuplicateReportException exception = assertThrows(
                DuplicateReportException.class,
                () -> ticketService.reportarEmergencia(request)
        );

        assertTrue(exception.getMessage().contains("TICK-1025"));
        verify(ticketRepository, never()).save(any(TicketRescate.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el código de tracking no existe")
    void obtenerPorCodigo_NoExiste_LanzaExcepcion() {
        when(ticketRepository.findByCodigoTracking("TICK-INEXISTENTE")).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.obtenerPorCodigo("TICK-INEXISTENTE")
        );
    }
}
