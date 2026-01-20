package com.tuxofware.msagua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsAguaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAguaApplication.class, args);
	}

}
