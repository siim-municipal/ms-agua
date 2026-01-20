package com.tuxofware.msagua.controller;

import com.tuxofware.msagua.dto.request.CrearContratoRequest;
import com.tuxofware.msagua.dto.response.ConsumoPeriodoResponse;
import com.tuxofware.msagua.service.CalculoConsumoService;
import com.tuxofware.msagua.service.ContratoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contratos")
@RequiredArgsConstructor
@Tag(name = "Agua - Contratos", description = "Gestión de altas y bajas de contratos de servicio de agua potable.")
public class ContratoController {
    private final ContratoService service;
    private final CalculoConsumoService calculoService;

    @Operation(
            summary = "Dar de alta un nuevo contrato",
            description = "Registra un nuevo servicio de agua vinculado a un predio existente. Valida la existencia del predio en Padrón y la unicidad del medidor."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Contrato creado exitosamente. Se devuelve la URI del recurso en el header 'Location'.",
                    headers = @Header(name = "Location", description = "URI del nuevo contrato", schema = @Schema(type = "string"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos (faltan campos o formato incorrecto).",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El Predio especificado (predioId) no existe en el sistema de Padrón.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto: El número de medidor ya está registrado en otro contrato activo.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Error de comunicación con el microservicio de Padrón.",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping
    public ResponseEntity<Void> crearContrato(@Valid @RequestBody CrearContratoRequest request) {
        UUID id = service.crearContrato(request);
        return ResponseEntity.created(URI.create("/api/v1/contratos/" + id)).build();
    }

    @Operation(summary = "Obtener consumo calculado del periodo",
            description = "Devuelve el consumo en m3. Si no existe lectura, realiza una estimación basada en el promedio de los últimos 3 meses.")
    @GetMapping("/{id}/consumo-periodo")
    public ResponseEntity<ConsumoPeriodoResponse> obtenerConsumoPeriodo(
            @PathVariable UUID id,
            @Parameter(description = "Mes (1-12)", example = "1") @RequestParam int mes,
            @Parameter(description = "Año (YYYY)", example = "2025") @RequestParam int anio) {

        return ResponseEntity.ok(calculoService.obtenerConsumoPeriodo(id, mes, anio));
    }
}
