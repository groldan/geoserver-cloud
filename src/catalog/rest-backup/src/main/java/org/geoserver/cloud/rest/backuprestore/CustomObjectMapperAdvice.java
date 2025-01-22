package org.geoserver.cloud.rest.backuprestore;

import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

// @RestControllerAdvice(assignableTypes = {JsonBackupRestoreController.class})
class CustomObjectMapperAdvice {

    private final MappingJackson2HttpMessageConverter converter;

    public CustomObjectMapperAdvice(MappingJackson2HttpMessageConverter converter) {
        this.converter = converter;
    }

    @Bean
    MappingJackson2HttpMessageConverter messageConverter() {
        return converter;
    }
}
