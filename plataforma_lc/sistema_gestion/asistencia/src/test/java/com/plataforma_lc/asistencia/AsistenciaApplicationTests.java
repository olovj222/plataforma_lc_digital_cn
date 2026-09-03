package com.plataforma_lc.asistencia;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false"
})
class AsistenciaApplicationTests {

	@Test
	void contextLoads() {
	}

}
