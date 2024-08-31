/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.logging.accesslog;

import java.net.URI;

import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AccessLogWebFilter implements OrderedWebFilter {

    private final @NonNull AccessLogFilterConfig config;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (config.shouldLog(exchange.getRequest().getURI())) {
            exchange.getResponse().beforeCommit(() -> logAfter(exchange));
        }

        return chain.filter(exchange);
    }

    /**
     * @param exchange
     */
    private Mono<Void> logAfter(ServerWebExchange exchange) {
        return Mono.fromRunnable(
                () -> {
                    ServerHttpRequest request = exchange.getRequest();
                    ServerHttpResponse response = exchange.getResponse();
                    URI uri = request.getURI();
                    String method = request.getMethodValue();
                    Integer statusCode = response.getRawStatusCode();
                    config.log("{} {} {} ", method, statusCode, uri);
                });
    }
}
