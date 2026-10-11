package com.plataforma_lc.evaluaciones;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = EvaluacionesApplication.class, properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false"
})
class EvaluacionesApplicationTests {

    @Test
    void contextLoads() {
    }

}