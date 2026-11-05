package com.plataforma_lc.adminKafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AdminKafkaApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminKafkaApplication.class, args);
    }
}

