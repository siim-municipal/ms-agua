package com.tuxofware.msagua.service;

import com.tuxofware.msagua.dto.response.ConsumoPeriodoResponse;

import java.util.UUID;

public interface CalculoConsumoService {
    ConsumoPeriodoResponse obtenerConsumoPeriodo(UUID contratoId, int mes, int anio);
}
