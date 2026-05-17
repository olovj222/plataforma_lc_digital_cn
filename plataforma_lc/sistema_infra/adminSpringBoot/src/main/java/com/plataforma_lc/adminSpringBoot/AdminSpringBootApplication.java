package com.plataforma_lc.adminSpringBoot;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableScheduling
@EnableAdminServer
@SpringBootApplication
public class AdminSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminSpringBootApplication.class, args);
	}

}
