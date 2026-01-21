package com.tuxofware.msagua.service.impl;

import com.tuxofware.msagua.client.PadronClient;
import com.tuxofware.msagua.dto.request.CambioEstatusRequest;
import com.tuxofware.msagua.dto.request.CrearContratoRequest;
import com.tuxofware.msagua.dto.response.ConsumoPeriodoResponse;
import com.tuxofware.msagua.dto.response.ContratoResumenResponse;
import com.tuxofware.msagua.persistence.entity.ContratoAgua;
import com.tuxofware.msagua.persistence.entity.LecturaAgua;
import com.tuxofware.msagua.persistence.repository.ContratoAguaRepository;
import com.tuxofware.msagua.persistence.repository.LecturaAguaRepository;
import com.tuxofware.msagua.service.ContratoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContratoServiceImpl implements ContratoService {

    private final ContratoAguaRepository contratoAguaRepository;
    private final LecturaAguaRepository lecturaAguaRepository;
    private final PadronClient padronClient;


    @Override
    @Transactional(readOnly = true)
    public List<ContratoResumenResponse> listarContratos() {
        List<ContratoAgua> contratos = contratoAguaRepository.findAll();

        return contratos.stream()
                .map(this::mapToResumen)
                .toList();
    }

    private ContratoResumenResponse mapToResumen(ContratoAgua c) {
        // 1. Obtener última lectura (Optimizada)
        // Usamos una consulta ligera que retorne solo el valor numérico, no toda la entidad
        BigDecimal ultimaLectura = lecturaAguaRepository
                .findUltimaLecturaValorByContratoId(c.getId())
                .orElse(c.getLecturaInicial());

        // 2. Mapeo de datos externos (Predio/Propietario)
        // NOTA: Para evitar llamar a ms-padron N veces (lentitud),
        // temporalmente enviamos placeholders o el ID.
        // La solución ideal es agregar columnas 'clave_catastral_snapshot' en la tabla contrato_agua.
        String claveDisplay = "PREDIO-" + c.getPredioId().toString().substring(0, 8).toUpperCase();
        String propietarioDisplay = "Ver en Padrón";

        return new ContratoResumenResponse(
                c.getId(),
                c.getPredioId(),
                claveDisplay,       // Aquí iría c.getClaveCatastralSnapshot()
                propietarioDisplay, // Aquí iría c.getPropietarioSnapshot()
                c.getNumeroMedidor(),
                c.getTipoToma().name(),
                c.getEstatus(),
                ultimaLectura
        );
    }

    @Override
    @Transactional
    public UUID crearContrato(CrearContratoRequest request) {
        // 1. Validar Medidor Único
        if (contratoAguaRepository.existsByNumeroMedidor(request.numeroMedidor())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El medidor ya está registrado en otro contrato.");
        }

        // 2. Validar Existencia de Predio (Comunicación Síncrona)
        try {
            boolean existePredio = padronClient.verificarExistenciaPredio(request.predioId());
            if (!existePredio) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El predio indicado no existe en Padrón.");
            }
        } catch (Exception e) {
            // Manejo de caída de ms-padron
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo validar el predio con ms-padron.");
        }

        // 3. Persistir
        var contrato = ContratoAgua.builder()
                .predioId(request.predioId())
                .numeroMedidor(request.numeroMedidor())
                .lecturaInicial(request.lecturaInicial())
                .tipoToma(request.tipoToma())
                .build();

        return contratoAguaRepository.save(contrato).getId();
    }

    @Override
    @Transactional
    public void cambiarEstatus(UUID id, CambioEstatusRequest request) {
        var contrato = contratoAguaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato no encontrado"));

        // Validación simple de idempotencia (si ya tiene ese estatus, no hacemos nada)
        if (contrato.getEstatus() == request.estatus()) {
            return;
        }

        // TODO: Aquí podrías agregar reglas de negocio complejas.
        // Ej: No se puede "ACTIVAR" un contrato si el predio tiene deuda (requeriría consultar ms-tesoreria)

        contrato.setEstatus(request.estatus());

        // TODO: Si tuvieras un campo de "motivo" o "observaciones" en la entidad, lo actualizarías aquí:
        // if (request.motivo() != null) contrato.setObservaciones(request.motivo());

        contratoAguaRepository.save(contrato);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumoPeriodoResponse obtenerConsumoPeriodo(UUID contratoId, int mes, int anio) {
        ContratoAgua contrato = contratoAguaRepository.findById(contratoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato no encontrado"));

        // 1. Intentar buscar lectura real
        return lecturaAguaRepository.findByPeriodo(contratoId, mes, anio)
                .map(lecturaActual -> calcularConsumoReal(contrato, lecturaActual))
                .orElseGet(() -> estimarConsumo(contrato, mes, anio));
    }

    @Override
    public boolean existsByPredioId(UUID predioId) {
        return contratoAguaRepository.existsByPredioId(predioId);
    }

    private ConsumoPeriodoResponse calcularConsumoReal(ContratoAgua contrato, LecturaAgua actual) {
        // Buscar lectura anterior a la actual
        BigDecimal lecturaAnterior = lecturaAguaRepository.findFirstByContratoIdAndFechaLecturaBeforeOrderByFechaLecturaDesc(
                        contrato.getId(), actual.getFechaLectura())
                .map(LecturaAgua::getLecturaM3)
                .orElse(contrato.getLecturaInicial()); // Fallback a lectura inicial del contrato

        BigDecimal consumo;

        // MANEJO DE CASO BORDE: Cambio de Medidor o Vuelta de contador
        if (actual.getLecturaM3().compareTo(lecturaAnterior) < 0) {
            // Asumimos que es un medidor nuevo que empezó en 0.
            // Consumo = Lectura Actual (neto).
            consumo = actual.getLecturaM3();
        } else {
            consumo = actual.getLecturaM3().subtract(lecturaAnterior);
        }

        return new ConsumoPeriodoResponse(
                contrato.getId(),
                contrato.getPredioId(),
                consumo,
                contrato.getTipoToma(),
                String.format("%d-%02d", actual.getFechaLectura().getYear(), actual.getFechaLectura().getMonthValue()),
                false
        );
    }

    private ConsumoPeriodoResponse estimarConsumo(ContratoAgua contrato, int mes, int anio) {
        LocalDate fechaCorte = LocalDate.of(anio, mes, 1);

        // Obtener últimas 3 lecturas para promediar
        List<LecturaAgua> historial = lecturaAguaRepository.findTop3ByContratoIdAndFechaLecturaBeforeOrderByFechaLecturaDesc(
                contrato.getId(), fechaCorte);

        BigDecimal consumoEstimado;

        if (historial.size() < 2) {
            // Si no hay suficiente histórico, retornar 0 o un mínimo vital configurado
            // Aquí retornamos 0 para no cobrar sin pruebas, o lanzar 404 según regla de negocio estricta.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Insuficiente histórico para estimar consumo.");
        } else {
            // Calcular diferencias entre las lecturas encontradas para sacar un promedio de consumo mensual
            BigDecimal totalDiferencias = BigDecimal.ZERO;
            int count = 0;

            for (int i = 0; i < historial.size() - 1; i++) {
                BigDecimal actual = historial.get(i).getLecturaM3();
                BigDecimal anterior = historial.get(i+1).getLecturaM3();

                // Ignorar periodos con cambio de medidor para no ensuciar el promedio con negativos
                if (actual.compareTo(anterior) >= 0) {
                    totalDiferencias = totalDiferencias.add(actual.subtract(anterior));
                    count++;
                }
            }

            if (count == 0) consumoEstimado = BigDecimal.ZERO;
            else consumoEstimado = totalDiferencias.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        }

        return new ConsumoPeriodoResponse(
                contrato.getId(),
                contrato.getPredioId(),
                consumoEstimado,
                contrato.getTipoToma(),
                String.format("%d-%02d", anio, mes),
                true // Es estimado
        );
    }

}
