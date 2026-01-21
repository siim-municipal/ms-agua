package com.tuxofware.msagua.persistence.repository;

import com.tuxofware.msagua.dto.projection.UltimaLecturaProjection;
import com.tuxofware.msagua.persistence.entity.LecturaAgua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface LecturaAguaRepository extends JpaRepository<LecturaAgua, UUID> {

    List<LecturaAgua> findTop2ByContratoIdOrderByFechaLecturaDesc(UUID contratoId);

    @Query("SELECT l.lecturaM3 FROM LecturaAgua l WHERE l.contrato.id = :contratoId ORDER BY l.fechaLectura DESC LIMIT 1")
    Optional<BigDecimal> findUltimaLecturaValorByContratoId(UUID contratoId);

    // Obtener la última lectura registrada antes de una fecha dada (o la más reciente en general)
    Optional<LecturaAgua> findFirstByContratoIdOrderByFechaLecturaDesc(UUID contratoId);

    @Query("SELECT UltimaLecturaProjection(l.contrato.id, l.lecturaM3) " +
            "FROM LecturaAgua l " +
            "WHERE l.contrato.id IN :contratoIds " +
            "AND l.fechaLectura = (SELECT MAX(l2.fechaLectura) FROM LecturaAgua l2 WHERE l2.contrato.id = l.contrato.id)")
    List<UltimaLecturaProjection> findUltimasLecturasBatch(Set<UUID> contratoIds);

    // Buscar lectura exacta en un mes y año específico
    @Query("SELECT l FROM LecturaAgua l WHERE l.contrato.id = :contratoId " +
            "AND MONTH(l.fechaLectura) = :mes AND YEAR(l.fechaLectura) = :anio")
    Optional<LecturaAgua> findByPeriodo(@Param("contratoId") UUID contratoId,
                                        @Param("mes") int mes,
                                        @Param("anio") int anio);

    // Obtener la última lectura ANTES de una fecha específica (para la resta)
    Optional<LecturaAgua> findFirstByContratoIdAndFechaLecturaBeforeOrderByFechaLecturaDesc(
            UUID contratoId, LocalDate fecha);

    // Obtener las últimas X lecturas para estimación (Promedio)
    List<LecturaAgua> findTop3ByContratoIdAndFechaLecturaBeforeOrderByFechaLecturaDesc(
            UUID contratoId, LocalDate fecha);
}