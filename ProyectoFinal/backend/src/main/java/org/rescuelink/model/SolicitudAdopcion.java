package org.rescuelink.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.rescuelink.model.base.AuditableEntity;
import org.rescuelink.model.enums.EstadoSolicitud;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "solicitudes_adopcion")
@SQLDelete(sql = "UPDATE solicitudes_adopcion SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudAdopcion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adoptante_id", nullable = false)
    private Usuario adoptante;

    @Column(name = "codigo_certificado_qr", unique = true, length = 100)
    private String codigoCertificadoQr;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoSolicitud estado = EstadoSolicitud.RECIBIDA;

    @Column(name = "tipo_vivienda", nullable = false, length = 50)
    private String tipoVivienda;

    @Column(name = "tiene_otras_mascotas", nullable = false)
    @Builder.Default
    private Boolean tieneOtrasMascotas = false;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "fecha_solicitud", nullable = false)
    @Builder.Default
    private Instant fechaSolicitud = Instant.now();
}
