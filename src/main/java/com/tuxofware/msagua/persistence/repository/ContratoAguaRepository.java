package com.tuxofware.msagua.persistence.repository;

import com.tuxofware.msagua.persistence.entity.ContratoAgua;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ContratoAguaRepository extends JpaRepository<ContratoAgua, UUID> {
    List<ContratoAgua> findByPredioId(UUID predioId);
    Optional<ContratoAgua> findByNumeroMedidor(String numeroMedidor);
    Optional<ContratoAgua> findByNumeroMedidorIn(Set<String> medidores);
    boolean existsByNumeroMedidor(String numeroMedidor);

}
