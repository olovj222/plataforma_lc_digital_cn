package com.plataforma_lc.apiGateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class UserContextFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
            .cast(JwtAuthenticationToken.class)
            .map(auth -> {
                Jwt jwt = (Jwt) auth.getPrincipal();
                String userId = jwt.getSubject();

                List<String> roles = jwt.getClaimAsStringList("roles");
                String rolesHeader = (roles == null || roles.isEmpty())
                    ? ""
                    : String.join(",", roles);

                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Roles", rolesHeader)
                    .build();

                return exchange.mutate().request(mutatedRequest).build();
            })
            .defaultIfEmpty(exchange) // por si algún endpoint quedara público en el futuro
            .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        return 0; // corre después del LoggingFilter (-1), antes de reenviar la petición
    }
}