package com.tuxofware.msagua.dto.projection;

import java.math.BigDecimal;

public record UltimaLecturaProjection(
        java.util.UUID contratoId,
        BigDecimal ultimaLectura
) {}