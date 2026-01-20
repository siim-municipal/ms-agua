package com.tuxofware.msagua.service;

import com.tuxofware.msagua.dto.request.RegistrarLecturaRequest;
import com.tuxofware.msagua.dto.response.ConsumoResponse;

import java.util.UUID;

public interface LecturaService {

    void registrarLectura(RegistrarLecturaRequest request);

    ConsumoResponse calcularConsumo(UUID contratoId);

}
