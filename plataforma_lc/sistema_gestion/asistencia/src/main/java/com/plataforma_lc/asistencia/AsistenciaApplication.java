package com.plataforma_lc.asistencia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class AsistenciaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsistenciaApplication.class, args);
	}

        @Bean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}
}
