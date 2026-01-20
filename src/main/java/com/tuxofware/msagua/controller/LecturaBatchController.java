package com.tuxofware.msagua.controller;

import com.tuxofware.msagua.dto.response.BatchResult;
import com.tuxofware.msagua.service.LecturaBatchService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/lecturas")
@RequiredArgsConstructor
public class LecturaBatchController {

    private final LecturaBatchService batchService;

    @Operation(summary = "Carga masiva de lecturas (CSV)", description = "Procesa archivos de handhelds. Valida consistencia y reporta errores por fila.")
    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BatchResult> cargarLecturasBatch(@RequestParam("file") MultipartFile file) {
        // Validación básica de extensión
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(batchService.procesarArchivo(file));
    }
}