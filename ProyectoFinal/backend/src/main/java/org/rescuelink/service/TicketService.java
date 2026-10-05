package org.rescuelink.service;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.rescuelink.dto.TicketDtos.ReportEmergencyRequest;
import org.rescuelink.dto.TicketDtos.TicketTrackerResponse;
import org.rescuelink.exception.DuplicateReportException;
import org.rescuelink.exception.ResourceNotFoundException;
import org.rescuelink.model.Albergue;
import org.rescuelink.model.TicketRescate;
import org.rescuelink.model.enums.EstadoTicket;
import org.rescuelink.repository.AlbergueRepository;
import org.rescuelink.repository.TicketRescateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRescateRepository ticketRepository;
    private final AlbergueRepository albergueRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public TicketTrackerResponse reportarEmergencia(ReportEmergencyRequest request) {
        // 1. Detección espacial de emergencias duplicadas (50 metros y 2 horas)
        Instant tiempoLimite = Instant.now().minus(2, ChronoUnit.HOURS);
        List<TicketRescate> duplicados = ticketRepository.findDuplicadosCercanos(
                request.longitud(),
                request.latitud(),
                50.0,
                tiempoLimite
        );

        if (!duplicados.isEmpty()) {
            TicketRescate colision = duplicados.get(0);
            throw new DuplicateReportException(String.format(
                    "Existe un reporte activo para un animal en un radio de 50 metros registrado hace menos de 2 horas (Código: %s).",
                    colision.getCodigoTracking()
            ));
        }

        // 2. Creación del punto geodésico PostGIS (SRID 4326: Longitud X, Latitud Y)
        Point ubicacion = geometryFactory.createPoint(new Coordinate(request.longitud(), request.latitud()));

        // 3. Generación de código público de tracking único (ej. TICK-8421)
        String codigoTracking = "TICK-" + (1000 + new Random().nextInt(9000));

        // 4. Búsqueda opcional del albergue más cercano en un radio de 10 km
        List<Albergue> cercanos = albergueRepository.findCercanos(request.longitud(), request.latitud(), 10000.0);
        Albergue albergueAsignado = cercanos.isEmpty() ? null : cercanos.get(0);

        TicketRescate ticket = TicketRescate.builder()
                .codigoTracking(codigoTracking)
                .nombreReportante(request.nombreReportante())
                .whatsappReportante(request.whatsappReportante())
                .descripcion(request.descripcion())
                .gravedad(request.gravedad())
                .estado(albergueAsignado != null ? EstadoTicket.ASIGNADO : EstadoTicket.PENDIENTE)
                .ubicacion(ubicacion)
                .fotoUrl(request.fotoUrl())
                .albergue(albergueAsignado)
                .build();

        TicketRescate guardado = ticketRepository.save(ticket);
        return mapToTrackerResponse(guardado);
    }

    @Transactional(readOnly = true)
    public TicketTrackerResponse obtenerPorCodigo(String codigoTracking) {
        TicketRescate ticket = ticketRepository.findByCodigoTracking(codigoTracking)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket de rescate", codigoTracking));

        return mapToTrackerResponse(ticket);
    }

    private TicketTrackerResponse mapToTrackerResponse(TicketRescate t) {
        return new TicketTrackerResponse(
                t.getId(),
                t.getCodigoTracking(),
                t.getNombreReportante(),
                t.getDescripcion(),
                t.getGravedad(),
                t.getEstado(),
                t.getUbicacion().getX(),
                t.getUbicacion().getY(),
                t.getFotoUrl(),
                t.getAlbergue() != null ? t.getAlbergue().getNombre() : null,
                t.getVoluntario() != null ? t.getVoluntario().getNombre() : null,
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
