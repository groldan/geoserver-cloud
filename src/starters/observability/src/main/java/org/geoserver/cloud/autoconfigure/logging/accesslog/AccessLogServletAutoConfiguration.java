/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.logging.accesslog;

import org.geoserver.cloud.logging.accesslog.AccessLogFilterConfig;
import org.geoserver.cloud.logging.accesslog.AccessLogServletFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@AutoConfiguration
@EnableConfigurationProperties(AccessLogFilterConfig.class)
@ConditionalOnWebApplication(type = Type.SERVLET)
public class AccessLogServletAutoConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    AccessLogServletFilter accessLogFilter(AccessLogFilterConfig conf) {
        return new AccessLogServletFilter(conf);
    }
}
