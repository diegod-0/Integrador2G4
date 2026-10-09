
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
     * HU05: detecta emergencias duplicadas por proximidad geodésica en metros.
     * Busca tickets activos reportados dentro del intervalo temporal indicado,
     * ordenados por distancia ascendente.
     */
    @Query(value = """
            SELECT t.* FROM tickets_rescate t
            WHERE t.deleted_at IS NULL
              AND t.estado IN ('PENDIENTE', 'ASIGNADO', 'EN_CAMINO')
              AND t.created_at >= :tiempoLimite
              AND ST_DWithin(
                    t.ubicacion::geography,
                    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                    :radioMetros
              )
            ORDER BY ST_Distance(
                    t.ubicacion::geography,
                    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
            ) ASC
            """, nativeQuery = true)
    List<TicketRescate> findDuplicadosCercanos(
            @Param("lng") double longitud,
            @Param("lat") double latitud,
            @Param("radioMetros") double radioMetros,
            @Param("tiempoLimite") Instant tiempoLimite
    );

    /**
     * HU05: calcula la distancia geodésica en metros entre un ticket
     * y las coordenadas indicadas.
     */
    @Query(value = """
            SELECT ST_Distance(
                     t.ubicacion::geography,
                     ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography)
            FROM tickets_rescate t
            WHERE t.id = :id
            """, nativeQuery = true)
    Double calcularDistanciaMetros(
            @Param("id") UUID id,
            @Param("lng") double longitud,
            @Param("lat") double latitud
    );

    /**
     * HU10: obtiene la bandeja operativa de rescates cercanos a un albergue.
     * Devuelve tickets pendientes o asignados, ordenados por distancia
     * geodésica en metros y luego por fecha de creación.
     */
    @Query(value = """
        SELECT t.id, t.codigo_tracking, t.descripcion, t.gravedad, t.estado,
               ST_X(t.ubicacion::geometry) AS longitud,
               ST_Y(t.ubicacion::geometry) AS latitud,
               ST_Distance(
                   t.ubicacion::geography,
                   ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
               ) AS distancia_metros,
               t.foto_url,
               a.nombre AS albergue_asignado_nombre,
               u.nombre AS voluntario_nombre,
               t.created_at
        FROM tickets_rescate t
        LEFT JOIN albergues a ON t.albergue_id = a.id AND a.deleted_at IS NULL
        LEFT JOIN usuarios u ON t.voluntario_id = u.id AND u.deleted_at IS NULL
        WHERE t.deleted_at IS NULL
          AND t.estado IN ('PENDIENTE', 'ASIGNADO', 'EN_CAMINO')
          AND ST_DWithin(
                 t.ubicacion::geography,
                 ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                 :radioMetros)
        ORDER BY distancia_metros ASC, t.created_at ASC
        """, nativeQuery = true)
    List<Object[]> findBandejaOperativa(
            @Param("lng") double longitud,
            @Param("lat") double latitud,
            @Param("radioMetros") double radioMetros
    );
}