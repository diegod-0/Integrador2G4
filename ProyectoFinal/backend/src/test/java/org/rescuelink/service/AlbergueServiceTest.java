package org.rescuelink.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rescuelink.dto.AlbergueDtos.RegistrarDonacionRequest;
import org.rescuelink.exception.ResourceNotFoundException;
import org.rescuelink.model.Albergue;
import org.rescuelink.model.Donacion;
import org.rescuelink.model.Usuario;
import org.rescuelink.model.enums.TipoDonacion;
import org.rescuelink.repository.AlbergueRepository;
import org.rescuelink.repository.DonacionRepository;
import org.rescuelink.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbergueServiceTest {

    @Mock
    private AlbergueRepository albergueRepository;

    @Mock
    private DonacionRepository donacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AlbergueService albergueService;

    @Test
    void obtenerDirectorioSinCoordenadasDevuelveOcupacionSinDistancia() {
        UUID id = UUID.randomUUID();
        when(albergueRepository.findDirectorioConOcupacion()).thenReturn(Collections.singletonList(
                new Object[]{id, "Refugio", "Av. Lima 123", "999111222", 20, 5L,
                        new BigDecimal("25.00"), -77.03, -12.10, null}
        ));

        var resultados = albergueService.obtenerDirectorio(null, null);

        assertEquals(1, resultados.size());
        assertEquals(id, resultados.get(0).id());
        assertEquals(5L, resultados.get(0).ocupacionActual());
        assertEquals(25.0, resultados.get(0).porcentajeOcupacion());
        assertNull(resultados.get(0).distanciaMetros());
        verify(albergueRepository).findDirectorioConOcupacion();
        verify(albergueRepository, never()).findDirectorioCercano(anyDouble(), anyDouble());
    }

    @Test
    void obtenerDirectorioConCoordenadasOrdenaYDevuelveDistancia() {
        UUID id = UUID.randomUUID();
        when(albergueRepository.findDirectorioCercano(-77.03, -12.10)).thenReturn(Collections.singletonList(
                new Object[]{id, "Refugio", "Av. Lima 123", "999111222", 20, 5L,
                        new BigDecimal("25.00"), -77.03, -12.10, 125.5}
        ));

        var resultados = albergueService.obtenerDirectorio(-77.03, -12.10);

        assertEquals(125.5, resultados.get(0).distanciaMetros());
        verify(albergueRepository).findDirectorioCercano(-77.03, -12.10);
    }

    @Test
    void obtenerDirectorioRechazaCoordenadasIncompletas() {
        assertThrows(IllegalArgumentException.class, () -> albergueService.obtenerDirectorio(-77.03, null));
        verifyNoInteractions(albergueRepository);
    }

    @Test
    void obtenerDetalleInexistenteLanzaNotFound() {
        UUID id = UUID.randomUUID();
        when(albergueRepository.findDirectorioPorId(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> albergueService.obtenerDetalle(id));
    }

    @Test
    void registrarDonacionAnonimaPersisteYDevuelveDatosRegistrados() {
        UUID albergueId = UUID.randomUUID();
        UUID donacionId = UUID.randomUUID();
        Albergue albergue = Albergue.builder().id(albergueId).build();
        RegistrarDonacionRequest request = new RegistrarDonacionRequest(
                TipoDonacion.ALIMENTO, new BigDecimal("75.00"), "Alimento para perros"
        );
        Instant fechaRegistro = Instant.now();

        when(albergueRepository.findById(albergueId)).thenReturn(Optional.of(albergue));
        when(donacionRepository.save(any(Donacion.class))).thenAnswer(invocation -> {
            Donacion donacion = invocation.getArgument(0);
            donacion.setId(donacionId);
            donacion.setFechaRegistro(fechaRegistro);
            return donacion;
        });

        var respuesta = albergueService.registrarDonacion(albergueId, request, null);

        assertEquals(donacionId, respuesta.id());
        assertEquals(albergueId, respuesta.albergueId());
        assertEquals(TipoDonacion.ALIMENTO, respuesta.tipo());
        assertEquals(fechaRegistro, respuesta.fechaRegistro());
        verify(donacionRepository).save(argThat(donacion -> donacion.getDonante() == null));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void registrarDonacionAsociaDonanteRegistrado() {
        UUID albergueId = UUID.randomUUID();
        Albergue albergue = Albergue.builder().id(albergueId).build();
        Usuario donante = Usuario.builder().email("donante@example.com").build();
        RegistrarDonacionRequest request = new RegistrarDonacionRequest(
                TipoDonacion.MONETARIA, new BigDecimal("20.00"), "Aporte"
        );

        when(albergueRepository.findById(albergueId)).thenReturn(Optional.of(albergue));
        when(usuarioRepository.findByEmail("donante@example.com")).thenReturn(Optional.of(donante));
        when(donacionRepository.save(any(Donacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        albergueService.registrarDonacion(albergueId, request, "donante@example.com");

        verify(donacionRepository).save(argThat(donacion -> donacion.getDonante() == donante));
    }
}
