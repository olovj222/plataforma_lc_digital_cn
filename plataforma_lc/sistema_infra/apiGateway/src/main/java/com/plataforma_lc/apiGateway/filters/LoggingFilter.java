/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.apiGateway.filters;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        return chain.filter(exchange)
            .doOnError(throwable -> {
                log.error("[MICROSERVICIO CAÍDO] {} {} - Error: {} - Timestamp: {}",
                    method,
                    path,
                    throwable.getMessage(),
                        LocalDateTime.now()
                );
            })
            .then(Mono.fromRunnable(() -> {
                int statusCode = exchange.getResponse().getStatusCode().value();
                if (statusCode == 503 || statusCode == 502) {
                    log.error("[MICROSERVICIO NO DISPONIBLE] {} {} - Status: {} - Timestamp: {}",
                        method,
                        path,
                        statusCode,
                        LocalDateTime.now()
                    );
                }
            }));
    }

    @Override
    public int getOrder() {
        return -1; 
    }
}