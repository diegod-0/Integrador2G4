package org.rescuelink.repository;

import org.rescuelink.model.Albergue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
}
