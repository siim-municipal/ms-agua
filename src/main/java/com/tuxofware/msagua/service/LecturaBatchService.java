package com.tuxofware.msagua.service;

import com.tuxofware.msagua.dto.response.BatchResult;
import org.springframework.web.multipart.MultipartFile;

public interface LecturaBatchService {
    BatchResult procesarArchivo(MultipartFile file);
}
