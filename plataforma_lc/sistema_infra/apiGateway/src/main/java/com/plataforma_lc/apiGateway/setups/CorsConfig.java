
package com.plataforma_lc.apiGateway.setups;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // Solo permite entorno local y cualquier túnel generado por Cloudflare
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOriginPattern("https://*.trycloudflare.com");
        config.addAllowedMethod("");
        config.addAllowedHeader("");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}