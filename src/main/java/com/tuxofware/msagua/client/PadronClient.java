package com.tuxofware.msagua.client;

import com.tuxofware.msagua.config.PadronFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(
        name = "ms-padron-unico",
        configuration = PadronFeignConfig.class
)
public interface PadronClient {
    @GetMapping("/api/v1/predios/{id}/exists")
    boolean verificarExistenciaPredio(@PathVariable("id") UUID id);
}