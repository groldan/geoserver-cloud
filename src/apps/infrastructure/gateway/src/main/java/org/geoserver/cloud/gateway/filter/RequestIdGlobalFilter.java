/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.gateway.filter;

import static org.geoserver.cloud.logging.mdc.config.HttpRequestMdcConfigProperties.REQUEST_ID_HEADER;
import static org.geoserver.cloud.logging.mdc.config.HttpRequestMdcConfigProperties.findRequestId;

import org.geoserver.cloud.logging.mdc.config.HttpRequestMdcConfigProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Sets the {@code http.request.id} header to be transmitted downstream to the proxied services.
 *
 * <p>If the client request {@link HttpRequestMdcConfigProperties#findRequestId contains} a request
 * id header (one of {@code trace-id}, {@code http.request.id}, {@code x-request-id}), that's the
 * one used, otherwise a new value is {@link HttpRequestMdcConfigProperties#newRequestId() created}
 * for the header if not provided by the client.
 */
public class RequestIdGlobalFilter implements GlobalFilter, Ordered {

    /**
     * @return {@link Ordered#HIGHEST_PRECEDENCE}
     */
    public @Override int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    public @Override Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Optional<String> providedRequestId = findRequestId(exchange.getRequest()::getHeaders);
        String requestId;
        if (providedRequestId.isEmpty()) {
            requestId = HttpRequestMdcConfigProperties.newRequestId();
        } else {
            requestId = providedRequestId.orElseThrow();
            exchange.getAttributes().put(REQUEST_ID_HEADER, requestId);
        }
        ServerHttpRequest request =
                exchange.getRequest().mutate().header(REQUEST_ID_HEADER, requestId).build();
        exchange = exchange.mutate().request(request).build();
        return chain.filter(exchange);
    }
}
