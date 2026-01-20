package com.tuxofware.msagua.service.impl;

import com.tuxofware.msagua.client.PadronClient;
import com.tuxofware.msagua.dto.request.CrearContratoRequest;
import com.tuxofware.msagua.persistence.entity.ContratoAgua;
import com.tuxofware.msagua.persistence.repository.ContratoAguaRepository;
import com.tuxofware.msagua.service.ContratoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContratoServiceImpl implements ContratoService {

    private final ContratoAguaRepository repository;
    private final PadronClient padronClient;

    @Override
    @Transactional
    public UUID crearContrato(CrearContratoRequest request) {
        // 1. Validar Medidor Único
        if (repository.existsByNumeroMedidor(request.numeroMedidor())) {
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
                .tipoToma(request.tipoToma())
                .build();

        return repository.save(contrato).getId();
    }
}
