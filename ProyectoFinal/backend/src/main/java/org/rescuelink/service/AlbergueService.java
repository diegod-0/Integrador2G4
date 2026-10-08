package org.rescuelink.service;

import lombok.RequiredArgsConstructor;
import org.rescuelink.dto.AlbergueDtos.AlbergueDetailDto;
import org.rescuelink.dto.AlbergueDtos.AlbergueSummaryDto;
import org.rescuelink.dto.AlbergueDtos.DonacionResponseDto;
import org.rescuelink.dto.AlbergueDtos.RegistrarDonacionRequest;
import org.rescuelink.exception.ResourceNotFoundException;
import org.rescuelink.model.Albergue;
import org.rescuelink.model.Donacion;
import org.rescuelink.model.Usuario;
import org.rescuelink.repository.AlbergueRepository;
import org.rescuelink.repository.DonacionRepository;
import org.rescuelink.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbergueService {

    private final AlbergueRepository albergueRepository;
    private final DonacionRepository donacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<AlbergueSummaryDto> obtenerDirectorio(Double longitud, Double latitud) {
        if ((longitud == null) != (latitud == null)) {
            throw new IllegalArgumentException("Debe proporcionar longitud y latitud juntas.");
        }
        if (longitud != null && (!Double.isFinite(longitud) || !Double.isFinite(latitud)
                || longitud < -180 || longitud > 180 || latitud < -90 || latitud > 90)) {
            throw new IllegalArgumentException("Las coordenadas están fuera del rango geográfico válido.");
        }

        List<Object[]> filas = longitud == null
                ? albergueRepository.findDirectorioConOcupacion()
                : albergueRepository.findDirectorioCercano(longitud, latitud);
        return filas.stream().map(this::mapToSummary).toList();
    }

    @Transactional(readOnly = true)
    public AlbergueDetailDto obtenerDetalle(UUID id) {
        Object[] fila = albergueRepository.findDirectorioPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Albergue", id));

        return new AlbergueDetailDto(
                (UUID) fila[0],
                (String) fila[1],
                (String) fila[2],
                (String) fila[3],
                ((Number) fila[4]).intValue(),
                ((Number) fila[5]).longValue(),
                ((Number) fila[6]).doubleValue(),
                ((Number) fila[7]).doubleValue(),
                ((Number) fila[8]).doubleValue()
        );
    }

    @Transactional
    public DonacionResponseDto registrarDonacion(
            UUID albergueId,
            RegistrarDonacionRequest request,
            String emailDonante
    ) {
        Albergue albergue = albergueRepository.findById(albergueId)
                .orElseThrow(() -> new ResourceNotFoundException("Albergue", albergueId));

        Usuario donante = emailDonante == null
                ? null
                : usuarioRepository.findByEmail(emailDonante)
                        .orElseThrow(() -> new ResourceNotFoundException("Donante", emailDonante));

        Donacion donacion = Donacion.builder()
                .albergue(albergue)
                .donante(donante)
                .tipo(request.tipo())
                .montoEstimado(request.montoEstimado())
                .descripcion(request.descripcion())
                .build();

        Donacion guardada = donacionRepository.save(donacion);
        return new DonacionResponseDto(
                guardada.getId(),
                albergueId,
                guardada.getTipo(),
                guardada.getMontoEstimado(),
                guardada.getDescripcion(),
                guardada.getFechaRegistro()
        );
    }

    private AlbergueSummaryDto mapToSummary(Object[] fila) {
        return new AlbergueSummaryDto(
                (UUID) fila[0],
                (String) fila[1],
                (String) fila[2],
                (String) fila[3],
                ((Number) fila[4]).intValue(),
                ((Number) fila[5]).longValue(),
                ((Number) fila[6]).doubleValue(),
                ((Number) fila[7]).doubleValue(),
                ((Number) fila[8]).doubleValue(),
                fila[9] == null ? null : ((Number) fila[9]).doubleValue()
        );
    }
}
