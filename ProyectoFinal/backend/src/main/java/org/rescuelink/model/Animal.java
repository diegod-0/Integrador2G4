package org.rescuelink.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.rescuelink.model.base.AuditableEntity;
import org.rescuelink.model.enums.EspecieAnimal;
import org.rescuelink.model.enums.EstadoAnimal;
import org.rescuelink.model.enums.NivelEnergia;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "animales")
@SQLDelete(sql = "UPDATE animales SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Animal extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", unique = true)
    private TicketRescate ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "albergue_id", nullable = false)
    private Albergue albergue;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "especie", nullable = false, length = 20)
    private EspecieAnimal especie;

    @Column(name = "edad_estimada", nullable = false, length = 30)
    private String edadEstimada;

    @Column(name = "peso", nullable = false, precision = 5, scale = 2)
    private BigDecimal peso;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_energia", nullable = false, length = 20)
    private NivelEnergia nivelEnergia;

    @Column(name = "apto_departamento", nullable = false)
    @Builder.Default
    private Boolean aptoDepartamento = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 25)
    @Builder.Default
    private EstadoAnimal estado = EstadoAnimal.CUARENTENA;

    @Column(name = "foto_perfil_url", nullable = false, length = 255)
    private String fotoPerfilUrl;

    @Column(name = "foto_antes_url", length = 255)
    private String fotoAntesUrl;

    @Column(name = "foto_despues_url", length = 255)
    private String fotoDespuesUrl;
}
