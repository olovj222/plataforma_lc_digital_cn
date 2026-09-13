package com.plataforma_lc.apiGateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
        "eureka.client.enabled=false",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://login.microsoftonline.com/2dcf78c8-4359-4115-8b06-50c5a455e4e0/v2.0"
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ApiGatewayApplicationTests {
    @Test
    void contextLoads() {
    }
}
