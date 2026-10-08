package org.rescuelink.service;

import lombok.RequiredArgsConstructor;
import org.rescuelink.dto.DuplicateCheckDtos.DuplicateCheckResponse;
import org.rescuelink.dto.DuplicateCheckDtos.DuplicateIncidentDetail;
import org.rescuelink.model.TicketRescate;
import org.rescuelink.repository.TicketRescateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Verificación preventiva de reportes duplicados (HU05).
 * Reutiliza la misma consulta espacial que usa el registro de emergencias,
 * de modo que ambos flujos apliquen siempre la misma regla (radio en metros y
 * ventana de 2 horas).
 */
@Service
@RequiredArgsConstructor
public class DuplicateDetectionService {

    public static final double RADIO_POR_DEFECTO_METROS = 50.0;
    public static final long VENTANA_HORAS = 2;

    private final TicketRescateRepository ticketRepository;

    @Transactional(readOnly = true)
    public DuplicateCheckResponse verificar(double longitud, double latitud, double radioMetros) {
        Instant tiempoLimite = Instant.now().minus(VENTANA_HORAS, ChronoUnit.HOURS);

        List<TicketRescate> cercanos = ticketRepository.findDuplicadosCercanos(longitud, latitud, radioMetros,
                tiempoLimite);

        if (cercanos.isEmpty()) {
            return DuplicateCheckResponse.sinColision();
        }

        // La consulta ordena por distancia ascendente: el primero es el más cercano.
        TicketRescate masCercano = cercanos.get(0);
        Double distancia = ticketRepository.calcularDistanciaMetros(masCercano.getId(), longitud, latitud);

        return DuplicateCheckResponse.conColision(new DuplicateIncidentDetail(
                masCercano.getCodigoTracking(),
                masCercano.getEstado(),
                distancia == null ? null : Math.round(distancia * 10.0) / 10.0,
                masCercano.getFotoUrl(),
                masCercano.getCreatedAt()));
    }
}