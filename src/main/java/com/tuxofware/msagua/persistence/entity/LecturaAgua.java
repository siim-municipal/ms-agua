package com.tuxofware.msagua.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lecturas_agua")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LecturaAgua {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Referencia lógica a ms-padron-unico (No es FK de base de datos)
    @Column(name = "predio_id", nullable = false)
    private UUID predioId;

    @Column(name = "fecha_lectura", nullable = false)
    private LocalDate fechaLectura;

    @Column(name = "lectura_anterior", precision = 12, scale = 2)
    private BigDecimal lecturaAnterior;

    @Column(name = "lectura_actual", nullable = false, precision = 12, scale = 2)
    private BigDecimal lecturaActual;

    // Este campo se calcula: actual - anterior
    @Column(name = "consumo_m3", nullable = false, precision = 12, scale = 2)
    private BigDecimal consumoM3;

    @Column(name = "bimestre")
    private Integer bimestre;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "observaciones")
    private String observaciones;
}
