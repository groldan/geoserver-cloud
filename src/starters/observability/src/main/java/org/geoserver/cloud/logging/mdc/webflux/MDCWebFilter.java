/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.logging.mdc.webflux;

import java.security.Principal;
import java.util.Optional;
import java.util.function.Supplier;

import org.geoserver.cloud.logging.mdc.config.HttpRequestMdcConfigProperties;
import org.geoserver.cloud.logging.mdc.config.MDCConfigProperties;
import org.slf4j.MDC;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Logging MDC filter for Webflux
 *
 * @see MDCConfigProperties
 */
@RequiredArgsConstructor
public class MDCWebFilter implements OrderedWebFilter {

    private final @NonNull MDCConfigProperties config;
    private final @NonNull Environment env;
    private final @NonNull Optional<BuildProperties> buildProperties;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        exchange.getResponse().beforeCommit(() -> log(exchange));

        return chain.filter(exchange);
    }

    private static final Principal ANNON = () -> "anonymous";

    /**
     * @param exchange
     */
    private Mono<Void> log(ServerWebExchange exchange) {
        return exchange.getPrincipal().switchIfEmpty(Mono.just(ANNON)).doOnNext(p -> setMdcAttributes(p, exchange))
                .then();
    }

    private void setMdcAttributes(Principal principal, ServerWebExchange exchange) {

        config.getApplication().addEnvironmentProperties(env, buildProperties);
        setHttpMdcAttributes(exchange);

        if (config.getUser().isId() && principal != null && principal != ANNON) {
            MDC.put("enduser.id", principal.getName());
        }
    }

    private void setHttpMdcAttributes(ServerWebExchange exchange) {
        ServerHttpRequest req = exchange.getRequest();
        HttpRequestMdcConfigProperties httpConfig = this.config.getHttp();
        httpConfig.id(req::getHeaders).remoteAddr(req.getRemoteAddress())
                // .remoteHost(req.)
                .method(req::getMethodValue).url(uri(req)).queryString(queryString(req)).parameters(req::getQueryParams)
                // .sessionId(sessionId(exchange))
                .headers(req::getHeaders).cookies(req::getCookies);
    }

    private Supplier<String> uri(ServerHttpRequest req) {
        return () -> req.getURI().getRawPath();
    }

    private Supplier<String> queryString(ServerHttpRequest req) {
        return () -> req.getURI().getRawQuery();
    }
}
