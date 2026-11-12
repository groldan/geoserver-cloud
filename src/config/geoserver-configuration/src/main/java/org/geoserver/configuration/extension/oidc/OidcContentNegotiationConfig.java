/* (c) 2026 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.configuration.extension.oidc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;

/**
 * Registers the {@code contentNegotiationStrategy} bean under the exact name expected by Spring Security's
 * {@code HttpSecurityConfiguration} autowiring. Pulled in by {@link OidcSecurityInfrastructureImportSelector}.
 */
@Configuration(proxyBeanMethods = false)
class OidcContentNegotiationConfig {

    @Bean
    ContentNegotiationStrategy contentNegotiationStrategy() {
        return new HeaderContentNegotiationStrategy();
    }
}
