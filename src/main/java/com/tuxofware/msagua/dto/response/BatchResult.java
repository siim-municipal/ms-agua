package com.tuxofware.msagua.dto.response;

import java.util.List;

public record BatchResult(
        int totalProcesados,
        int exitosos,
        int fallidos,
        List<String> errores
) {}