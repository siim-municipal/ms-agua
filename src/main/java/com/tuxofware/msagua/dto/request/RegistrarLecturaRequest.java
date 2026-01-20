package com.tuxofware.msagua.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Datos requeridos para capturar una lectura de medidor.")
public record RegistrarLecturaRequest(
        @NotNull
        @Schema(description = "ID único del contrato de agua", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID contratoId,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        @Schema(description = "Valor visible en el medidor (m³)", example = "1250.50", minimum = "0")
        BigDecimal lecturaM3,

        @NotNull
        @PastOrPresent
        @Schema(description = "Fecha en la que se tomó la lectura", example = "2026-01-20")
        LocalDate fechaLectura,

        @Size(max = 255)
        @Schema(description = "Notas del lecturista (medidor empañado, fuga, etc.)", example = "Medidor con vidrio roto", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String observaciones
) {}