package com.plataforma_lc.apiGateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
        "eureka.client.enabled=false",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8090/realms/plataforma-lc"
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ApiGatewayApplicationTests {
    @Test
    void contextLoads() {
    }
}
