package com.tuxofware.msagua.dto.request;

import com.tuxofware.msagua.enums.EstatusContrato;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Cuerpo para la actualización de estado del contrato.")
public record CambioEstatusRequest(
        @NotNull
        @Schema(description = "Nuevo estatus a aplicar", example = "SUSPENDIDO")
        EstatusContrato estatus,

        @Schema(description = "Motivo del cambio (Opcional)", example = "Falta de pago reiterada")
        String motivo
) {}