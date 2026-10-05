package org.rescuelink.model;

import jakarta.persistence.*;
import lombok.*;
import org.rescuelink.model.enums.TipoDonacion;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "donaciones")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "albergue_id", nullable = false)
    private Albergue albergue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donante_id")
    private Usuario donante;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoDonacion tipo;

    @Column(name = "monto_estimado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoEstimado;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private Instant fechaRegistro = Instant.now();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
