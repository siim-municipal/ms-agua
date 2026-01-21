package com.tuxofware.msagua.service;

import com.tuxofware.msagua.dto.request.CambioEstatusRequest;
import com.tuxofware.msagua.dto.request.CrearContratoRequest;
import com.tuxofware.msagua.dto.response.ConsumoPeriodoResponse;
import com.tuxofware.msagua.dto.response.ContratoResumenResponse;

import java.util.List;
import java.util.UUID;

public interface ContratoService {

    List<ContratoResumenResponse> listarContratos();

    UUID crearContrato(CrearContratoRequest request);

    void cambiarEstatus(UUID id, CambioEstatusRequest request);

    ConsumoPeriodoResponse obtenerConsumoPeriodo(UUID contratoId, int mes, int anio);

    boolean existsByPredioId(UUID predioId);
}
