package com.plataforma_lc.estudiante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class EstudianteApplication {

	public static void main(String[] args) {
		SpringApplication.run(EstudianteApplication.class, args);
	}
        @Bean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}

}
