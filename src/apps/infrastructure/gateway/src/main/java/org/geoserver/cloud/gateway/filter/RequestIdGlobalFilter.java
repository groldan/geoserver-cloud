package org.geoserver.cloud.gateway.filter;

import static org.geoserver.cloud.logging.mdc.config.HttpRequestMdcConfigProperties.REQUEST_ID_HEADER;
import static org.geoserver.cloud.logging.mdc.config.HttpRequestMdcConfigProperties.findRequestId;

import java.util.Optional;

import org.geoserver.cloud.logging.mdc.config.HttpRequestMdcConfigProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * A new value is created for the header if not provided by the client.
 */
public class RequestIdGlobalFilter implements GlobalFilter, Ordered {

    /**
     * @return {@link Ordered#HIGHEST_PRECEDENCE}
     */
    public @Override int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Makes sure both the request and response have the same
     * {@literal X-Request-ID} header.
     * <p>
     * A new value is created for the header if not provided by the client.
     */
    public @Override Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Optional<String> providedRequestId = findRequestId(exchange.getRequest()::getHeaders);
        String requestId;
        final ServerHttpRequest request;
        if (providedRequestId.isEmpty()) {
            requestId = HttpRequestMdcConfigProperties.newRequestId();
            request = exchange.getRequest().mutate().header(REQUEST_ID_HEADER, requestId).build();
            exchange = exchange.mutate().request(request).build();
        } else {
            requestId = providedRequestId.orElseThrow();
        }

        ServerHttpResponse response = exchange.getResponse();
        response.beforeCommit(() -> Mono.fromRunnable(() -> setResponseHeader(requestId, response)));

        return chain.filter(exchange);
    }

    private void setResponseHeader(String requestId, ServerHttpResponse response) {
        response.getHeaders().set(REQUEST_ID_HEADER, requestId);
    }

}
