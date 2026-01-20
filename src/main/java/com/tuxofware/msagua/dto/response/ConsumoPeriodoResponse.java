package com.tuxofware.msagua.dto.response;

import com.tuxofware.msagua.enums.TipoToma;

import java.math.BigDecimal;
import java.util.UUID;

public record ConsumoPeriodoResponse(
        UUID contratoId,
        UUID predioId,
        BigDecimal consumoM3,
        TipoToma tipoToma,
        String periodo,   // Formato "YYYY-MM"
        boolean estimado  // Flag útil para auditoría
) {}