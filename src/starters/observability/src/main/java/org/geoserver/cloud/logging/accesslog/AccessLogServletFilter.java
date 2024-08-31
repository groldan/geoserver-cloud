/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.logging.accesslog;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.NonNull;

/** Similar to {@link CommonsRequestLoggingFilter} but uses slf4j */
public class AccessLogServletFilter extends OncePerRequestFilter {

    private final @NonNull AccessLogFilterConfig config;

    public AccessLogServletFilter(@NonNull AccessLogFilterConfig conf) {
        this.config = conf;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

            try {
                filterChain.doFilter(request, response);
            }finally {
                String method = request.getMethod();
                int statusCode = response.getStatus();
                String uri = request.getRequestURI();
                config.log("{} {} {} ", method, statusCode, uri);
            }
    }
}
