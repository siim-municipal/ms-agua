package com.tuxofware.msagua.controller;

import com.tuxofware.msagua.dto.response.ConsumoPeriodoResponse;
import com.tuxofware.msagua.service.CalculoConsumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contratos")
@RequiredArgsConstructor
public class ContratoConsumoController {

    private final CalculoConsumoService calculoService;

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
