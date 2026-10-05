package org.rescuelink.repository;

import org.rescuelink.model.TicketRescate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRescateRepository extends JpaRepository<TicketRescate, UUID> {

    Optional<TicketRescate> findByCodigoTracking(String codigoTracking);

    /**
     * Detección espacio-temporal de emergencias duplicadas.
     * Busca tickets activos en un radio geodésico (ej. 50 metros) reportados hace menos de N horas.
     */
    @Query(value = """
        SELECT t.* FROM tickets_rescate t 
        WHERE t.deleted_at IS NULL 
          AND t.estado IN ('PENDIENTE', 'ASIGNADO', 'EN_CAMINO') 
          AND t.created_at >= :tiempoLimite 
          AND ST_DWithin(t.ubicacion, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326), :radioMetros)
        """, nativeQuery = true)
    List<TicketRescate> findDuplicadosCercanos(
            @Param("lng") double longitud,
            @Param("lat") double latitud,
            @Param("radioMetros") double radioMetros,
            @Param("tiempoLimite") Instant tiempoLimite
    );
}
