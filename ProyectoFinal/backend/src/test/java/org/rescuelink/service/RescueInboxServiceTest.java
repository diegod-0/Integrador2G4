package org.rescuelink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rescuelink.dto.RescueInboxDtos.ActualizarEstadoTicketRequest;
import org.rescuelink.dto.RescueInboxDtos.AsignarVoluntarioRequest;
import org.rescuelink.dto.RescueInboxDtos.InboxTicketSummaryDto;
import org.rescuelink.dto.TicketDtos.TicketTrackerResponse;
import org.rescuelink.exception.BusinessException;
import org.rescuelink.exception.CapacityExceededException;
import org.rescuelink.exception.ResourceNotFoundException;
import org.rescuelink.model.Albergue;
import org.rescuelink.model.TicketRescate;
import org.rescuelink.model.Usuario;
import org.rescuelink.model.enums.EstadoTicket;
import org.rescuelink.model.enums.GravedadTicket;
import org.rescuelink.model.enums.RolUsuario;
import org.rescuelink.repository.AlbergueRepository;
import org.rescuelink.repository.TicketRescateRepository;
import org.rescuelink.repository.UsuarioRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RescueInboxServiceTest {

    @Mock
    private TicketRescateRepository ticketRepository;

    @Mock
    private AlbergueRepository albergueRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RescueInboxService rescueInboxService;

    private GeometryFactory geometryFactory;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }

    private Point punto(double lng, double lat) {
        return geometryFactory.createPoint(new Coordinate(lng, lat));
    }

    private TicketRescate ticketPendiente(UUID id, String codigo) {
        return TicketRescate.builder()
                .id(id)
                .codigoTracking(codigo)
                .nombreReportante("Reportante Test")
                .descripcion("Animal en riesgo durante la verificación automatizada")
                .gravedad(GravedadTicket.CRITICA)
                .estado(EstadoTicket.PENDIENTE)
                .ubicacion(punto(-77.03, -12.12))
                .fotoUrl("https://images.example.com/foto.jpg")
                .build();
    }

    // ----------------------------- BANDEJA -----------------------------

    @Test
    @DisplayName("obtenerBandeja: delega con las coordenadas del albergue y mapea las filas")
    void obtenerBandeja_happyPath_mapeaYDelega() {
        UUID albergueId = UUID.randomUUID();
        Point ubicacion = punto(-75.5, 4.5);
        Albergue albergue = Albergue.builder()
                .id(albergueId)
                .nombre("Albergue Centro")
                .capacidadMax(20)
                .ubicacion(ubicacion)
                .build();
        when(albergueRepository.findById(albergueId)).thenReturn(Optional.of(albergue));

        UUID ticketId = UUID.randomUUID();
        Instant created = Instant.parse("2026-10-08T10:00:00Z");
        Object[] row = new Object[]{
                ticketId, "TR-123", "Emergencia", "CRITICA", "PENDIENTE",
                -75.5, 4.5, 100.0, "http://foto.jpg", "Albergue Centro", "Juan Perez", created
        };
        when(ticketRepository.findBandejaOperativa(-75.5, 4.5, 15000.0))
                .thenReturn(List.<Object[]>of(row));

        List<InboxTicketSummaryDto> result = rescueInboxService.obtenerBandeja(albergueId, 15000.0);

        assertEquals(1, result.size());
        InboxTicketSummaryDto dto = result.get(0);
        assertEquals(ticketId, dto.ticketId());
        assertEquals("TR-123", dto.codigoTracking());
        assertEquals("Emergencia", dto.descripcion());
        assertEquals(GravedadTicket.CRITICA, dto.gravedad());
        assertEquals(EstadoTicket.PENDIENTE, dto.estado());
        assertEquals(-75.5, dto.longitud());
        assertEquals(4.5, dto.latitud());
        assertEquals(100.0, dto.distanciaMetros());
        assertEquals("http://foto.jpg", dto.fotoUrl());
        assertEquals("Albergue Centro", dto.albergueAsignadoNombre());
        assertEquals("Juan Perez", dto.voluntarioNombre());
        assertEquals(created, dto.fechaReporte());
        verify(ticketRepository).findBandejaOperativa(eq(-75.5), eq(4.5), eq(15000.0));
    }

    @Test
    @DisplayName("obtenerBandeja: distancia nula cuando el ticket no tiene referencia")
    void obtenerBandeja_distanciaNula_seMapeaComoNull() {
        UUID albergueId = UUID.randomUUID();
        Albergue albergue = Albergue.builder()
                .id(albergueId)
                .nombre("Albergue Centro")
                .capacidadMax(20)
                .ubicacion(punto(-75.5, 4.5))
                .build();
        when(albergueRepository.findById(albergueId)).thenReturn(Optional.of(albergue));

        Object[] row = new Object[]{
                UUID.randomUUID(), "TR-200", "Sin distancia", "LEVE", "ASIGNADO",
                -75.6, 4.6, null, "http://foto2.jpg", "Albergue Norte", null,
                Instant.parse("2026-10-08T09:00:00Z")
        };
        when(ticketRepository.findBandejaOperativa(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.<Object[]>of(row));

        List<InboxTicketSummaryDto> result = rescueInboxService.obtenerBandeja(albergueId, 5000.0);

        assertEquals(1, result.size());
        assertNull(result.get(0).distanciaMetros());
        assertNull(result.get(0).voluntarioNombre());
        assertEquals(EstadoTicket.ASIGNADO, result.get(0).estado());
    }

    @Test
    @DisplayName("obtenerBandeja: albergue inexistente lanza ResourceNotFoundException")
    void obtenerBandeja_albergueInexistente_lanza404() {
        UUID albergueId = UUID.randomUUID();
        when(albergueRepository.findById(albergueId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> rescueInboxService.obtenerBandeja(albergueId, 15000.0));
        verify(ticketRepository, never()).findBandejaOperativa(anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("obtenerBandeja: radio no positivo lanza BusinessException sin consultar el albergue")
    void obtenerBandeja_radioInvalido_lanzaBusinessException() {
        UUID albergueId = UUID.randomUUID();

        assertThrows(BusinessException.class,
                () -> rescueInboxService.obtenerBandeja(albergueId, 0.0));
        verify(albergueRepository, never()).findById(any());
        verify(ticketRepository, never()).findBandejaOperativa(anyDouble(), anyDouble(), anyDouble());
    }

    // ----------------------------- ASIGNAR -----------------------------

    @Test
    @DisplayName("asignarRescate: asigna voluntario y albergue con cupo y pasa a ASIGNADO")
    void asignarRescate_exitoso_asignaVoluntarioAlbergueYEstado() {
        UUID ticketId = UUID.randomUUID();
        TicketRescate ticket = ticketPendiente(ticketId, "TR-700");

        Usuario voluntario = Usuario.builder()
                .id(UUID.randomUUID())
                .nombre("Ana Voluntaria")
                .email("ana@rescuelink.org")
                .rol(RolUsuario.ROLE_VOLUNTARIO)
                .build();

        Albergue albergue = Albergue.builder()
                .id(UUID.randomUUID())
                .nombre("Refugio Esperanza")
                .capacidadMax(10)
                .ubicacion(punto(-77.03, -12.12))
                .build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(usuarioRepository.findById(voluntario.getId())).thenReturn(Optional.of(voluntario));
        when(albergueRepository.findById(albergue.getId())).thenReturn(Optional.of(albergue));
        when(albergueRepository.findCapacidadDisponible(albergue.getId()))
                .thenReturn(List.<Object[]>of(new Object[]{10, 3}));
        when(ticketRepository.save(any(TicketRescate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TicketTrackerResponse response = rescueInboxService.asignarRescate(
                ticketId,
                new AsignarVoluntarioRequest(voluntario.getId(), albergue.getId())
        );

        assertNotNull(response);
        assertEquals("TR-700", response.codigoTracking());
        assertEquals(EstadoTicket.ASIGNADO, response.estado());
        assertEquals("Refugio Esperanza", response.albergueNombre());
        assertEquals("Ana Voluntaria", response.voluntarioNombre());

        ArgumentCaptor<TicketRescate> captor = ArgumentCaptor.forClass(TicketRescate.class);
        verify(ticketRepository, times(1)).save(captor.capture());
        assertEquals(EstadoTicket.ASIGNADO, captor.getValue().getEstado());
        assertEquals(voluntario, captor.getValue().getVoluntario());
        assertEquals(albergue, captor.getValue().getAlbergue());
    }

    @Test
    @DisplayName("asignarRescate: ticket inexistente lanza ResourceNotFoundException")
    void asignarRescate_ticketInexistente_lanza404() {
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rescueInboxService.asignarRescate(
                ticketId, new AsignarVoluntarioRequest(UUID.randomUUID(), UUID.randomUUID())));
        verify(usuarioRepository, never()).findById(any());
        verify(ticketRepository, never()).save(any(TicketRescate.class));
    }

    @Test
    @DisplayName("asignarRescate: ticket no PENDIENTE lanza BusinessException")
    void asignarRescate_ticketNoPendiente_lanzaBusinessException() {
        UUID ticketId = UUID.randomUUID();
        TicketRescate ticket = ticketPendiente(ticketId, "TR-555");
        ticket.setEstado(EstadoTicket.ASIGNADO);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        BusinessException ex = assertThrows(BusinessException.class, () -> rescueInboxService.asignarRescate(
                ticketId, new AsignarVoluntarioRequest(UUID.randomUUID(), UUID.randomUUID())));

        assertTrue(ex.getMessage().contains("PENDIENTE"));
        assertTrue(ex.getMessage().contains("TR-555"));
        verify(usuarioRepository, never()).findById(any());
        verify(ticketRepository, never()).save(any(TicketRescate.class));
    }

    @Test
    @DisplayName("asignarRescate: voluntario inexistente lanza ResourceNotFoundException")
    void asignarRescate_voluntarioInexistente_lanza404() {
        UUID ticketId = UUID.randomUUID();
        UUID voluntarioId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticketPendiente(ticketId, "TR-601")));
        when(usuarioRepository.findById(voluntarioId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rescueInboxService.asignarRescate(
                ticketId, new AsignarVoluntarioRequest(voluntarioId, UUID.randomUUID())));
        verify(albergueRepository, never()).findById(any());
        verify(ticketRepository, never()).save(any(TicketRescate.class));
    }

    @Test
    @DisplayName("asignarRescate: usuario sin rol VOLUNTARIO lanza BusinessException")
    void asignarRescate_usuarioSinRolVoluntario_lanzaBusinessException() {
        UUID ticketId = UUID.randomUUID();
        Usuario admin = Usuario.builder()
                .id(UUID.randomUUID())
                .nombre("Admin")
                .email("admin@rescuelink.org")
                .rol(RolUsuario.ROLE_ADMIN)
                .build();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticketPendiente(ticketId, "TR-602")));
        when(usuarioRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        BusinessException ex = assertThrows(BusinessException.class, () -> rescueInboxService.asignarRescate(
                ticketId, new AsignarVoluntarioRequest(admin.getId(), UUID.randomUUID())));

        assertTrue(ex.getMessage().contains("VOLUNTARIO"));
        verify(albergueRepository, never()).findById(any());
        verify(ticketRepository, never()).save(any(TicketRescate.class));
    }

    @Test
    @DisplayName("asignarRescate: albergue inexistente lanza ResourceNotFoundException")
    void asignarRescate_albergueInexistente_lanza404() {
        UUID ticketId = UUID.randomUUID();
        UUID albergueId = UUID.randomUUID();
        Usuario voluntario = Usuario.builder()
                .id(UUID.randomUUID()).nombre("Ana").email("ana@rescuelink.org")
                .rol(RolUsuario.ROLE_VOLUNTARIO).build();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticketPendiente(ticketId, "TR-603")));
        when(usuarioRepository.findById(voluntario.getId())).thenReturn(Optional.of(voluntario));
        when(albergueRepository.findById(albergueId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rescueInboxService.asignarRescate(
                ticketId, new AsignarVoluntarioRequest(voluntario.getId(), albergueId)));
        verify(albergueRepository, never()).findCapacidadDisponible(any());
        verify(ticketRepository, never()).save(any(TicketRescate.class));
    }

    @Test
    @DisplayName("asignarRescate: albergue saturado lanza CapacityExceededException")
    void asignarRescate_albergueSaturado_lanzaCapacityExceeded() {
        UUID ticketId = UUID.randomUUID();
        Usuario voluntario = Usuario.builder()
                .id(UUID.randomUUID()).nombre("Ana").email("ana@rescuelink.org")
                .rol(RolUsuario.ROLE_VOLUNTARIO).build();
        Albergue albergue = Albergue.builder()
                .id(UUID.randomUUID()).nombre("Refugio Lleno").capacidadMax(10)
                .ubicacion(punto(-77.03, -12.12)).build();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticketPendiente(ticketId, "TR-604")));
        when(usuarioRepository.findById(voluntario.getId())).thenReturn(Optional.of(voluntario));
        when(albergueRepository.findById(albergue.getId())).thenReturn(Optional.of(albergue));
        when(albergueRepository.findCapacidadDisponible(albergue.getId()))
                .thenReturn(List.<Object[]>of(new Object[]{10, 10}));

        CapacityExceededException ex = assertThrows(CapacityExceededException.class,
                () -> rescueInboxService.asignarRescate(
                        ticketId, new AsignarVoluntarioRequest(voluntario.getId(), albergue.getId())));

        assertTrue(ex.getMessage().contains("Refugio Lleno"));
        verify(ticketRepository, never()).save(any(TicketRescate.class));
    }

    // ----------------------------- ESTADO -----------------------------

    @Test
    @DisplayName("actualizarEstado: transición válida a RESCATADO persiste el cambio")
    void actualizarEstado_rescatado_actualizaYGuarda() {
        UUID ticketId = UUID.randomUUID();
        TicketRescate ticket = ticketPendiente(ticketId, "TR-900");
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(TicketRescate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TicketTrackerResponse response = rescueInboxService.actualizarEstado(
                ticketId,
                new ActualizarEstadoTicketRequest(EstadoTicket.RESCATADO)
        );

        assertEquals(EstadoTicket.RESCATADO, response.estado());
        ArgumentCaptor<TicketRescate> captor = ArgumentCaptor.forClass(TicketRescate.class);
        verify(ticketRepository).save(captor.capture());
        assertEquals(EstadoTicket.RESCATADO, captor.getValue().getEstado());
    }

    @Test
    @DisplayName("actualizarEstado: estado no permitido (ASIGNADO) lanza BusinessException")
    void actualizarEstado_estadoNoPermitido_lanzaBusinessException() {
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticketPendiente(ticketId, "TR-901")));

        BusinessException ex = assertThrows(BusinessException.class, () -> rescueInboxService.actualizarEstado(
                ticketId, new ActualizarEstadoTicketRequest(EstadoTicket.ASIGNADO)));

        assertTrue(ex.getMessage().contains("ASIGNADO"));
        verify(ticketRepository, never()).save(any(TicketRescate.class));
    }

    @Test
    @DisplayName("actualizarEstado: ticket inexistente lanza ResourceNotFoundException")
    void actualizarEstado_ticketInexistente_lanza404() {
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rescueInboxService.actualizarEstado(
                ticketId, new ActualizarEstadoTicketRequest(EstadoTicket.EN_CAMINO)));
        verify(ticketRepository, never()).save(any(TicketRescate.class));
    }
}
