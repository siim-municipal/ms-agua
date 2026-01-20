package com.tuxofware.msagua.service.impl;

import com.tuxofware.msagua.dto.request.RegistrarLecturaRequest;
import com.tuxofware.msagua.dto.response.ConsumoResponse;
import com.tuxofware.msagua.persistence.entity.ContratoAgua;
import com.tuxofware.msagua.persistence.entity.LecturaAgua;
import com.tuxofware.msagua.persistence.repository.ContratoAguaRepository;
import com.tuxofware.msagua.persistence.repository.LecturaAguaRepository;
import com.tuxofware.msagua.service.LecturaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LecturaServiceImpl implements LecturaService {

    private final ContratoAguaRepository contratoRepository;
    private final LecturaAguaRepository lecturaRepository;

    @Transactional
    @Override
    public void registrarLectura(RegistrarLecturaRequest request) {
        var contrato = contratoRepository.findById(request.contratoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato no encontrado"));

        // 1. Obtener lectura anterior (o inicial del contrato)
        BigDecimal valorAnterior = obtenerUltimaLecturaValor(contrato);

        // 2. Validar consistencia
        if (request.lecturaM3().compareTo(valorAnterior) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La nueva lectura no puede ser menor a la anterior (" + valorAnterior + ")");
        }

        // 3. Guardar
        var nuevaLectura = LecturaAgua.builder()
                .contrato(contrato)
                .fechaLectura(request.fechaLectura())
                .lecturaM3(request.lecturaM3())
                .observaciones(request.observaciones())
                .build();

        lecturaRepository.save(nuevaLectura);
    }

    @Transactional(readOnly = true)
    @Override
    public ConsumoResponse calcularConsumo(UUID contratoId) {
        // 1. Validar existencia del contrato
        var contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato no encontrado"));

        // 2. Obtener historial reciente (Máximo 2 registros)
        List<LecturaAgua> historial = lecturaRepository.findTop2ByContratoIdOrderByFechaLecturaDesc(contratoId);

        BigDecimal lecturaActual;
        BigDecimal lecturaAnterior;
        String periodoDesc;

        if (historial.isEmpty()) {
            // ESCENARIO A: Contrato nuevo, nunca se ha medido
            lecturaActual = contrato.getLecturaInicial();
            lecturaAnterior = contrato.getLecturaInicial();
            periodoDesc = "Sin lecturas registradas";

        } else if (historial.size() == 1) {
            // ESCENARIO B: Primera lectura del mes. Se compara vs. Lectura Inicial del contrato
            lecturaActual = historial.getFirst().getLecturaM3();
            lecturaAnterior = contrato.getLecturaInicial();
            periodoDesc = "Inicial vs Primera Lectura";

        } else {
            // ESCENARIO C: Ya hay historial. Comparamos la última (0) vs. la penúltima (1)
            lecturaActual = historial.get(0).getLecturaM3();
            lecturaAnterior = historial.get(1).getLecturaM3();
            periodoDesc = "Periodo Regular";
        }

        // 3. Cálculo Aritmético Seguro
        BigDecimal consumo = lecturaActual.subtract(lecturaAnterior);

        // Validación de seguridad (por si cambiaron el medidor y no reiniciaron el ciclo)
        if (consumo.compareTo(BigDecimal.ZERO) < 0) {
            // En un sistema real, aquí se lanzaría una alerta de "Medidor Reiniciado" o "Error de Captura"
            consumo = BigDecimal.ZERO;
            periodoDesc = "Error: Lectura actual menor a anterior";
        }

        return new ConsumoResponse(
                contratoId,
                lecturaAnterior,
                lecturaActual,
                consumo,
                periodoDesc
        );
    }

    private BigDecimal obtenerUltimaLecturaValor(ContratoAgua contrato) {
        return lecturaRepository.findFirstByContratoIdOrderByFechaLecturaDesc(contrato.getId())
                .map(LecturaAgua::getLecturaM3)
                .orElse(contrato.getLecturaInicial());
    }
}
