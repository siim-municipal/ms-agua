package com.tuxofware.msagua.service;

import com.tuxofware.msagua.dto.request.CrearContratoRequest;
import com.tuxofware.msagua.dto.response.ConsumoPeriodoResponse;

import java.util.UUID;

public interface ContratoService {

    UUID crearContrato(CrearContratoRequest request);

    ConsumoPeriodoResponse obtenerConsumoPeriodo(UUID contratoId, int mes, int anio);

    boolean existsByPredioId(UUID predioId);
}
