package com.tuxofware.msagua.service.impl;

import com.opencsv.bean.CsvToBeanBuilder;
import com.tuxofware.msagua.dto.projection.UltimaLecturaProjection;
import com.tuxofware.msagua.dto.request.LecturaCsvRecordRequest;
import com.tuxofware.msagua.dto.response.BatchResult;
import com.tuxofware.msagua.persistence.entity.ContratoAgua;
import com.tuxofware.msagua.persistence.entity.LecturaAgua;
import com.tuxofware.msagua.persistence.repository.ContratoAguaRepository;
import com.tuxofware.msagua.persistence.repository.LecturaAguaRepository;
import com.tuxofware.msagua.service.LecturaBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LecturaBatchServiceImpl implements LecturaBatchService {

    private final ContratoAguaRepository contratoRepository;
    private final LecturaAguaRepository lecturaRepository;

    @Override
    @Transactional
    public BatchResult procesarArchivo(MultipartFile file) {
        List<String> errores = new ArrayList<>();
        List<LecturaAgua> lecturasParaGuardar = new ArrayList<>();

        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // 1. Parsear CSV a Beans
            List<LecturaCsvRecordRequest> inputs = new CsvToBeanBuilder<LecturaCsvRecordRequest>(reader)
                    .withType(LecturaCsvRecordRequest.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            if (inputs.isEmpty()) return new BatchResult(0, 0, 0, List.of("Archivo vacío"));

            // 2. Pre-cargar datos (Bulk Fetch) para evitar N+1
            Set<String> medidores = inputs.stream()
                    .map(LecturaCsvRecordRequest::numeroMedidor)
                    .collect(Collectors.toSet());

            // Mapa: NumeroMedidor -> Contrato
            Map<String, ContratoAgua> mapaContratos = contratoRepository.findByNumeroMedidorIn(medidores)
                    .stream()
                    .collect(Collectors.toMap(ContratoAgua::getNumeroMedidor, Function.identity()));

            // Mapa: ContratoID -> UltimaLecturaValor
            Set<UUID> contratoIds = mapaContratos.values().stream().map(ContratoAgua::getId).collect(Collectors.toSet());

            Map<UUID, BigDecimal> mapaUltimasLecturas = new HashMap<>();
            if (!contratoIds.isEmpty()) {
                mapaUltimasLecturas = lecturaRepository.findUltimasLecturasBatch(contratoIds)
                        .stream()
                        .collect(Collectors.toMap(UltimaLecturaProjection::contratoId, UltimaLecturaProjection::ultimaLectura));
            }

            // 3. Procesamiento y Validación In-Memory
            for (LecturaCsvRecordRequest fila : inputs) {
                try {
                    // Validación A: Contrato existe
                    ContratoAgua contrato = mapaContratos.get(fila.numeroMedidor());
                    if (contrato == null) {
                        errores.add("Medidor " + fila.numeroMedidor() + ": No existe contrato activo.");
                        continue;
                    }

                    // Validación B: Consumo positivo (Actual >= Anterior)
                    BigDecimal lecturaAnterior = mapaUltimasLecturas.getOrDefault(contrato.getId(), contrato.getLecturaInicial());

                    if (fila.lecturaM3().compareTo(lecturaAnterior) < 0) {
                        errores.add("Medidor " + fila.numeroMedidor() + ": Lectura actual (" + fila.lecturaM3() +
                                ") menor a anterior (" + lecturaAnterior + ").");
                        continue;
                    }

                    // Construir Entidad
                    var nuevaLectura = LecturaAgua.builder()
                            .contrato(contrato)
                            .fechaLectura(fila.fechaLectura())
                            .lecturaM3(fila.lecturaM3())
                            .observaciones(fila.observaciones())
                            .build();

                    lecturasParaGuardar.add(nuevaLectura);

                } catch (Exception e) {
                    errores.add("Error procesando medidor " + fila.numeroMedidor() + ": " + e.getMessage());
                }
            }

            // 4. Guardado Batch
            if (!lecturasParaGuardar.isEmpty()) {
                lecturaRepository.saveAll(lecturasParaGuardar);
            }

            return new BatchResult(inputs.size(), lecturasParaGuardar.size(), errores.size(), errores);

        } catch (Exception e) {
            log.error("Error crítico procesando batch", e);
            throw new RuntimeException("Error procesando archivo CSV", e);
        }
    }
}
