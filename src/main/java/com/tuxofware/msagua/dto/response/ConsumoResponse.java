package com.tuxofware.msagua.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Respuesta con el cálculo de consumo para cobro.")
public record ConsumoResponse(
        @Schema(description = "ID del contrato consultado")
        UUID contratoId,

        @Schema(description = "Lectura del mes/bimestre anterior", example = "100.00")
        BigDecimal lecturaAnterior,

        @Schema(description = "Lectura actual registrada", example = "150.00")
        BigDecimal lecturaActual,

        @Schema(description = "Diferencia consumida (Base gravable)", example = "50.00")
        BigDecimal consumoM3,

        @Schema(description = "Descripción del periodo calculado", example = "Periodo Regular")
        String periodo
) {}