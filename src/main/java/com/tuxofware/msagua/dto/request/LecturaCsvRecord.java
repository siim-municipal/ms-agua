package com.tuxofware.msagua.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LecturaCsvRecord(
        String numeroMedidor,
        BigDecimal lecturaM3,
        LocalDate fechaLectura, // Formato esperado en CSV: yyyy-MM-dd
        String observaciones
) {}
