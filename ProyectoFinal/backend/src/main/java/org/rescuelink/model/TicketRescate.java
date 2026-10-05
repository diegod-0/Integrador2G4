package org.rescuelink.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.locationtech.jts.geom.Point;
import org.rescuelink.model.base.AuditableEntity;
import org.rescuelink.model.enums.EstadoTicket;
import org.rescuelink.model.enums.GravedadTicket;

import java.util.UUID;

@Entity
@Table(name = "tickets_rescate")
@SQLDelete(sql = "UPDATE tickets_rescate SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRescate extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo_tracking", nullable = false, unique = true, length = 30)
    private String codigoTracking;

    @Column(name = "nombre_reportante", nullable = false, length = 100)
    private String nombreReportante;

    @Column(name = "whatsapp_reportante", nullable = false, length = 25)
    private String whatsappReportante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "albergue_id")
    private Albergue albergue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voluntario_id")
    private Usuario voluntario;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "gravedad", nullable = false, length = 20)
    private GravedadTicket gravedad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 25)
    @Builder.Default
    private EstadoTicket estado = EstadoTicket.PENDIENTE;

    @Column(name = "ubicacion", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point ubicacion;

    @Column(name = "foto_url", nullable = false, length = 255)
    private String fotoUrl;
}
