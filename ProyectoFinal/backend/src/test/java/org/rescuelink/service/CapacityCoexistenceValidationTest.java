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
import org.rescuelink.dto.RescueInboxDtos.AsignarVoluntarioRequest;
import org.rescuelink.dto.TicketDtos.TicketTrackerResponse;
import org.rescuelink.exception.CapacityExceededException;
import org.rescuelink.model.Albergue;
import org.rescuelink.model.TicketRescate;
import org.rescuelink.model.Usuario;
import org.rescuelink.model.enums.EstadoTicket;
import org.rescuelink.model.enums.GravedadTicket;
import org.rescuelink.model.enums.RolUsuario;
import org.rescuelink.repository.AlbergueRepository;
import org.rescuelink.repository.TicketRescateRepository;
import org.rescuelink.repository.UsuarioRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Evidencia de verificación APF3 (HU12) — ISO/IEC 25010: compatibilidad.
 *
 * <p>Valida dos reglas auditables del entregable:</p>
 * <ol>
 *   <li><strong>Alerta preventiva al 90%</strong> (Ley N.º 30407): alcanzar el
 *       90% de ocupación es una señal de advertencia, no un bloqueo. La
 *       asignación solo se rechaza cuando el albergue está al 100%.</li>
 *   <li><strong>Coexistencia multi-instancia</strong>: el compose de coexistencia
 *       debe publicar puertos de host únicos para que dos instancias convivan en
 *       el mismo servidor sin colisiones.</li>
 * </ol>
 *
 * <p>La alerta visual del 90% se calcula en el directorio de albergues
 * ({@code porcentajeOcupacion >= 90}); este test fija el límite del backend para
 * que la advertencia y el bloqueo no se contradigan.</p>
 */
@ExtendWith(MockitoExtension.class)
class CapacityCoexistenceValidationTest {

    private static final int CAPACIDAD_MAXIMA = 40;
    private static final long OCUPACION_ALERTA_PREVENTIVA = 36L; // 90% exacto
    private static final long OCUPACION_SATURADA = 40L;
    private static final long OCUPACION_INCONSISTENTE = 41L;

    /** Puertos de host que la guía de coexistencia exige mantener aislados. */
    private static final List<Integer> PUERTOS_ESPERADOS = List.of(5434, 5435, 8080, 8081);

    /** Mapeo de puerto en docker-compose: {@code "8080:8080"} o {@code "${VAR:-5434}:5432"}. */
    private static final Pattern MAPEO_PUERTO = Pattern.compile(
            "^\\s*-\\s*\"?(?:\\$\\{[A-Z_]+:-)?(\\d+)\\}?:(\\d+)\"?\\s*$");

    private static final String COMPOSE_COEXISTENCIA = "docker-compose.coexistence.yml";

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

    // ------------------------- HU12: control de capacidad -------------------------

    @Test
    @DisplayName("HU12: al 90% de ocupación la asignación se permite (la alerta es preventiva, no un bloqueo)")
    void alNoventaPorCientoLaAsignacionSiguePermitida() {
        Escenario escenario = prepararEscenario(CAPACIDAD_MAXIMA, OCUPACION_ALERTA_PREVENTIVA);
        permitirPersistencia();

        TicketTrackerResponse respuesta = asignar(escenario);

        assertThat(respuesta.estado()).isEqualTo(EstadoTicket.ASIGNADO);
        assertThat(respuesta.albergueNombre()).isEqualTo(escenario.nombreAlbergue());
        verify(ticketRepository).save(any(TicketRescate.class));
    }

    @Test
    @DisplayName("HU12: al llegar al 100% de capacidad se bloquea la asignación con CapacityExceededException")
    void alCienPorCientoLaAsignacionSeBloquea() {
        Escenario escenario = prepararEscenario(CAPACIDAD_MAXIMA, OCUPACION_SATURADA);

        assertThatThrownBy(() -> asignar(escenario))
                .isInstanceOf(CapacityExceededException.class)
                .hasMessageContaining(escenario.nombreAlbergue())
                .hasMessageContaining(String.valueOf(OCUPACION_SATURADA));

        verify(ticketRepository, never()).save(any(TicketRescate.class));
    }

    @Test
    @DisplayName("HU12: una ocupación por encima de la capacidad máxima también se rechaza")
    void porEncimaDeLaCapacidadMaximaLaAsignacionSeBloquea() {
        Escenario escenario = prepararEscenario(CAPACIDAD_MAXIMA, OCUPACION_INCONSISTENTE);

        assertThatThrownBy(() -> asignar(escenario))
                .isInstanceOf(CapacityExceededException.class);

        verify(ticketRepository, never()).save(any(TicketRescate.class));
    }

    // --------------------- ISO 25010: coexistencia multi-instancia ---------------------

    @Test
    @DisplayName("ISO 25010 coexistencia: los puertos de host del compose son únicos y no colisionan")
    void losPuertosDeHostDelComposeNoColisionan() throws IOException {
        Path compose = resolverComposeCoexistencia();
        assumeTrue(compose != null,
                "No se encontró " + COMPOSE_COEXISTENCIA + "; se omite la verificación de coexistencia.");

        List<Integer> puertosHost = extraerPuertosHost(compose);

        assertThat(puertosHost)
                .as("el compose de coexistencia debe publicar los puertos de las dos instancias")
                .isNotEmpty()
                .doesNotHaveDuplicates()
                .containsAll(PUERTOS_ESPERADOS);
    }

    // -------------------------------- Utilidades --------------------------------

    private Escenario prepararEscenario(int capacidadMaxima, long animalesAlojados) {
        UUID ticketId = UUID.randomUUID();
        UUID voluntarioId = UUID.randomUUID();
        UUID albergueId = UUID.randomUUID();
        String nombreAlbergue = "Refugio Esperanza";

        TicketRescate ticket = TicketRescate.builder()
                .id(ticketId)
                .codigoTracking("TR-HU12")
                .nombreReportante("Reportante Test")
                .descripcion("Verificación de capacidad HU12")
                .gravedad(GravedadTicket.CRITICA)
                .estado(EstadoTicket.PENDIENTE)
                .ubicacion(punto(-77.03, -12.12))
                .fotoUrl("https://images.example.com/foto.jpg")
                .build();

        Usuario voluntario = Usuario.builder()
                .id(voluntarioId)
                .nombre("Ana Voluntaria")
                .email("ana@rescuelink.org")
                .rol(RolUsuario.ROLE_VOLUNTARIO)
                .build();

        Albergue albergue = Albergue.builder()
                .id(albergueId)
                .nombre(nombreAlbergue)
                .capacidadMax(capacidadMaxima)
                .ubicacion(punto(-77.03, -12.12))
                .build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(usuarioRepository.findById(voluntarioId)).thenReturn(Optional.of(voluntario));
        when(albergueRepository.findById(albergueId)).thenReturn(Optional.of(albergue));
        when(albergueRepository.findCapacidadDisponible(albergueId))
                .thenReturn(List.<Object[]>of(new Object[]{capacidadMaxima, animalesAlojados}));

        return new Escenario(ticketId, voluntarioId, albergueId, nombreAlbergue);
    }

    private void permitirPersistencia() {
        when(ticketRepository.save(any(TicketRescate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private TicketTrackerResponse asignar(Escenario escenario) {
        return rescueInboxService.asignarRescate(
                escenario.ticketId(),
                new AsignarVoluntarioRequest(escenario.voluntarioId(), escenario.albergueId()));
    }

    private Point punto(double longitud, double latitud) {
        return geometryFactory.createPoint(new Coordinate(longitud, latitud));
    }

    /**
     * Busca el compose hacia arriba del directorio de trabajo para que el test
     * funcione tanto ejecutando el módulo como desde la raíz del monorepo.
     */
    private Path resolverComposeCoexistencia() {
        Path actual = Path.of("").toAbsolutePath();
        for (int nivel = 0; nivel < 5 && actual != null; nivel++) {
            Path candidato = actual.resolve(COMPOSE_COEXISTENCIA);
            if (Files.isRegularFile(candidato)) {
                return candidato;
            }
            actual = actual.getParent();
        }
        return null;
    }

    private List<Integer> extraerPuertosHost(Path compose) throws IOException {
        List<Integer> puertosHost = new ArrayList<>();
        for (String linea : Files.readAllLines(compose, StandardCharsets.UTF_8)) {
            Matcher mapeo = MAPEO_PUERTO.matcher(linea);
            if (mapeo.matches()) {
                puertosHost.add(Integer.parseInt(mapeo.group(1)));
            }
        }
        return puertosHost;
    }

    private record Escenario(UUID ticketId, UUID voluntarioId, UUID albergueId, String nombreAlbergue) {
    }
}
