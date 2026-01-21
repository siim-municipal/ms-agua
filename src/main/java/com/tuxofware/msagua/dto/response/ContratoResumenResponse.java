package com.tuxofware.msagua.dto.response;

import com.tuxofware.msagua.enums.EstatusContrato;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Resumen ligero del contrato para listados y tableros.")
public record ContratoResumenResponse(
        UUID id,
        UUID predioId,

        @Schema(description = "Dato proyectado de ms-padron", example = "URB-1020-X")
        String claveCatastral,

        @Schema(description = "Dato proyectado de ms-padron", example = "JUAN PEREZ")
        String propietario,

        String numeroMedidor,
        String tipoToma,
        EstatusContrato estatus,

        @Schema(description = "Última lectura registrada (m3)")
        BigDecimal ultimaLectura
) {}
