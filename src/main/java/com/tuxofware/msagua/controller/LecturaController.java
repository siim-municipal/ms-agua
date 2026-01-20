package com.tuxofware.msagua.controller;

import com.tuxofware.msagua.dto.request.RegistrarLecturaRequest;
import com.tuxofware.msagua.dto.response.BatchResult;
import com.tuxofware.msagua.dto.response.ConsumoResponse;
import com.tuxofware.msagua.service.LecturaBatchService;
import com.tuxofware.msagua.service.LecturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lecturas")
@RequiredArgsConstructor
@Tag(name = "Agua - Lecturas y Consumo", description = "Gestión de lecturas de medidores y cálculo de consumo volumétrico.")
public class LecturaController {

    private final LecturaService lecturaService;
    private final LecturaBatchService batchService;

    @Operation(
            summary = "Registrar lectura de medidor",
            description = "Captura la lectura actual de un contrato. Valida que la nueva lectura no sea menor a la anterior."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lectura registrada exitosamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o lectura menor a la anterior.", content = @Content),
            @ApiResponse(responseCode = "404", description = "El contrato especificado no existe.", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> registrarLectura(
            @Valid @RequestBody RegistrarLecturaRequest request) { // @Valid es crucial
        lecturaService.registrarLectura(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Calcular consumo del periodo",
            description = "Obtiene la diferencia entre la última lectura registrada y la anterior para determinar el consumo en m³."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cálculo realizado correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConsumoResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Contrato no encontrado.", content = @Content)
    })
    @GetMapping("/consumo-bimestre")
    public ResponseEntity<ConsumoResponse> obtenerConsumo(
            @Parameter(description = "UUID del contrato a consultar", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID contratoId) {
        return ResponseEntity.ok(lecturaService.calcularConsumo(contratoId));
    }

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