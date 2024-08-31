package org.geoserver.cloud.autoconfigure.logging.mdc;

import java.util.Optional;

import org.geoserver.cloud.logging.mdc.config.MDCConfigProperties;
import org.geoserver.cloud.logging.mdc.webflux.MDCWebFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnWebApplication(type = Type.REACTIVE)
@EnableConfigurationProperties({ MDCConfigProperties.class })
public class LoggingMDCWebFluxAutoConfiguration {

    @Bean
    MDCWebFilter mdcWebFilter(MDCConfigProperties config, Environment env, Optional<BuildProperties> buildProps) {
        return new MDCWebFilter(config, env, buildProps);
    }
}
