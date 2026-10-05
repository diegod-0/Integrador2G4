package org.rescuelink.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.locationtech.jts.geom.Point;
import org.rescuelink.model.base.AuditableEntity;

import java.util.UUID;

@Entity
@Table(name = "albergues")
@SQLDelete(sql = "UPDATE albergues SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Albergue extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", nullable = false, unique = true, length = 120)
    private String nombre;

    @Column(name = "direccion", nullable = false, length = 200)
    private String direccion;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "capacidad_max", nullable = false)
    private Integer capacidadMax;

    /**
     * Coordenada geoespacial SRID 4326 (WGS 84).
     * Mapeado nativamente por Hibernate Spatial en PostgreSQL/PostGIS.
     */
    @Column(name = "ubicacion", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point ubicacion;
}
