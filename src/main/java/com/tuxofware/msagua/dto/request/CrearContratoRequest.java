package com.tuxofware.msagua.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import com.tuxofware.msagua.enums.TipoToma;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Información requerida para dar de alta un nuevo contrato de agua.")
public record CrearContratoRequest(
        @NotNull
        @Schema(description = "Identificador único del predio (existente en Padrón) al cual se vinculará el servicio.",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID predioId,

        @NotBlank
        @Schema(
                description = "Número de serie o identificador físico del medidor. Debe ser único en el sistema.",
                example = "SN-2024-X99"
        )
        String numeroMedidor,

        @NotNull
        @Schema(
                description = "Clasificación del uso de la toma de agua.",
                example = "DOMESTICA",
                implementation = TipoToma.class
        )
        TipoToma tipoToma
) {}