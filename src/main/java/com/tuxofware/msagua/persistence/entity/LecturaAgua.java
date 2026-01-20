package com.tuxofware.msagua.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lectura_agua")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LecturaAgua {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private ContratoAgua contrato;

    @Column(nullable = false)
    private LocalDate fechaLectura;

    @Column(nullable = false,
            precision = 10, scale = 2,
            name = "lectura_m3"
    )
    private BigDecimal lecturaM3;

    private String observaciones;
}
