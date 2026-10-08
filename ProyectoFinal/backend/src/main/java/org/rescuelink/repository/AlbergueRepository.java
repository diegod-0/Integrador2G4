package org.rescuelink.repository;

import org.rescuelink.model.Albergue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlbergueRepository extends JpaRepository<Albergue, UUID> {

    /**
     * Busca albergues cercanos con geodistancia PostGIS utilizando el índice GiST.
     */
    @Query(value = """
        SELECT a.* FROM albergues a 
        WHERE a.deleted_at IS NULL 
          AND ST_DWithin(a.ubicacion, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326), :radioMetros) 
        ORDER BY ST_Distance(a.ubicacion, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)) ASC
        """, nativeQuery = true)
    List<Albergue> findCercanos(
            @Param("lng") double longitud,
            @Param("lat") double latitud,
            @Param("radioMetros") double radioMetros
    );

    @Query(value = """
        SELECT a.id, a.nombre, a.direccion, a.telefono, a.capacidad_max,
               COALESCE(v.animales_alojados, 0) AS ocupacion_actual,
               COALESCE(v.porcentaje_ocupacion, 0.0) AS porcentaje_ocupacion,
               ST_X(a.ubicacion::geometry) AS longitud,
               ST_Y(a.ubicacion::geometry) AS latitud,
               NULL::double precision AS distancia_metros
        FROM albergues a
        LEFT JOIN v_albergues_capacidad_disponible v ON a.id = v.albergue_id
        WHERE a.deleted_at IS NULL
        ORDER BY a.nombre ASC
        """, nativeQuery = true)
    List<Object[]> findDirectorioConOcupacion();

    @Query(value = """
        SELECT a.id, a.nombre, a.direccion, a.telefono, a.capacidad_max,
               COALESCE(v.animales_alojados, 0) AS ocupacion_actual,
               COALESCE(v.porcentaje_ocupacion, 0.0) AS porcentaje_ocupacion,
               ST_X(a.ubicacion::geometry) AS longitud,
               ST_Y(a.ubicacion::geometry) AS latitud,
               ST_Distance(
                   a.ubicacion::geography,
                   ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
               ) AS distancia_metros
        FROM albergues a
        LEFT JOIN v_albergues_capacidad_disponible v ON a.id = v.albergue_id
        WHERE a.deleted_at IS NULL
        ORDER BY distancia_metros ASC, a.nombre ASC
        """, nativeQuery = true)
    List<Object[]> findDirectorioCercano(
            @Param("lng") double longitud,
            @Param("lat") double latitud
    );

    @Query(value = """
        SELECT a.id, a.nombre, a.direccion, a.telefono, a.capacidad_max,
               COALESCE(v.animales_alojados, 0) AS ocupacion_actual,
               COALESCE(v.porcentaje_ocupacion, 0.0) AS porcentaje_ocupacion,
               ST_X(a.ubicacion::geometry) AS longitud,
               ST_Y(a.ubicacion::geometry) AS latitud
        FROM albergues a
        LEFT JOIN v_albergues_capacidad_disponible v ON a.id = v.albergue_id
        WHERE a.id = :id AND a.deleted_at IS NULL
        """, nativeQuery = true)
    Optional<Object[]> findDirectorioPorId(@Param("id") UUID id);
}
