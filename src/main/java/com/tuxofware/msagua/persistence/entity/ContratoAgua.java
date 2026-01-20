package com.tuxofware.msagua.persistence.entity;

import com.tuxofware.msagua.enums.EstatusContrato;
import com.tuxofware.msagua.enums.TipoToma;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "contrato_agua", indexes = {
        @Index(name = "idx_contrato_predio", columnList = "predio_id")
})
public class ContratoAgua {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "predio_id", nullable = false)
    private UUID predioId;

    @Column(name = "numero_medidor", nullable = false, unique = true, length = 50)
    private String numeroMedidor;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lecturaInicial;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_toma", nullable = false)
    private TipoToma tipoToma;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstatusContrato estatus = EstatusContrato.ACTIVO;
}