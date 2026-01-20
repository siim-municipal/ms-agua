package com.tuxofware.msagua.service;

import com.tuxofware.msagua.dto.request.CrearContratoRequest;

import java.util.UUID;

public interface ContratoService {

    UUID crearContrato(CrearContratoRequest request);
}
