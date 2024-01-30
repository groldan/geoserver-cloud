/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.restconfig;

import org.geoserver.rest.RequestInfo;
import org.geoserver.rest.RestConfiguration;
import org.geoserver.rest.catalog.AdminRequestCallback;
import org.geoserver.rest.resources.ResourceController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

@Configuration
@ComponentScan(
        basePackageClasses = org.geoserver.rest.AbstractGeoServerController.class, //
        /*
         * Exclude AdminRequestCallback from component-scan. For some reason it's not being loaded in
         * vanilla geoserver (from gs-restconfig's applicationContext.xml) and causes a difference in behavior. At some
         * point it'll have to be fixed upstream and re-enabled here.
         */
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = AdminRequestCallback.class)
        })
@SuppressWarnings("deprecation")
public class RestConfigApplicationConfiguration extends RestConfiguration {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        super.configureContentNegotiation(configurer);
        configurer.favorPathExtension(true);
    }

    /**
     * "Deprecate use of path extensions in request mapping and content negotiation" {@code
     * https://github.com/spring-projects/spring-framework/issues/24179}
     */
    @Bean
    @Override
    public RequestMappingHandlerMapping requestMappingHandlerMapping(
            @Qualifier("mvcContentNegotiationManager")
                    ContentNegotiationManager contentNegotiationManager,
            @Qualifier("mvcConversionService") FormattingConversionService conversionService,
            @Qualifier("mvcResourceUrlProvider") ResourceUrlProvider resourceUrlProvider) {

        RequestMappingHandlerMapping handlerMapping =
                super.requestMappingHandlerMapping(
                        contentNegotiationManager, conversionService, resourceUrlProvider);

        handlerMapping.setUseSuffixPatternMatch(true);
        handlerMapping.setUseRegisteredSuffixPatternMatch(true);

        return handlerMapping;
    }

    @Bean
    SetRequestPathInfoFilter setRequestPathInfoFilter() {
        return new SetRequestPathInfoFilter();
    }

    /**
     * GeoSever REST API always expect the {@link HttpServletRequest#getServletPath()} to be
     * {@literal /rest}, and {@link HttpServletRequest#getPathInfo()} whatever comes after in the
     * request URI.
     *
     * <p>for example: {@link RequestInfo} constructor, {@link ResourceController#resource}, etc.
     */
    static class SetRequestPathInfoFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            request = adaptRequest((HttpServletRequest) request);
            chain.doFilter(request, response);
        }

        protected ServletRequest adaptRequest(HttpServletRequest request) {
            final String requestURI = request.getRequestURI();
            final String restBasePath = "/rest";
            final int restIdx = requestURI.indexOf(restBasePath);
            if (restIdx > -1) {
                final String pathToRest = requestURI.substring(0, restIdx + restBasePath.length());
                final String pathInfo = requestURI.substring(pathToRest.length());

                return new HttpServletRequestWrapper(request) {
                    @Override
                    public String getServletPath() {
                        return restBasePath;
                    }

                    @Override
                    public String getPathInfo() {
                        return pathInfo;
                    }
                };
            }
            return request;
        }
    }
}
