package org.geoserver.cloud.rest.backuprestore;

import org.geoserver.catalog.Catalog;
import org.geoserver.config.GeoServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@AutoConfiguration
@EnableAsync
@Import(WebMvcConfiguration.class)
public class JsonBackupRestoreAutoConfiguration {

    //    @Bean
    //    @Primary
    //    ObjectMapper objectMapper() {
    //        return ObjectMapperUtil.newObjectMapper();
    //    }

    //    @Bean
    //    MappingJackson2HttpMessageConverter customJacksonMessageConverter() {
    //        ObjectMapper customMapper = ObjectMapperUtil.newObjectMapper();
    //        return new MappingJackson2HttpMessageConverter(customMapper);
    //    }

    //    @Bean
    //    CustomObjectMapperAdvice jsonBackupRestoreControllerAdvice(MappingJackson2HttpMessageConverter converter) {
    //        return new CustomObjectMapperAdvice(converter);
    //    }

    @Bean
    JsonBackupRestoreController jsonBackupRestoreController(
            GeoServer geoServer, @Qualifier("rawCatalog") Catalog rawCatalog) {
        return new JsonBackupRestoreController(geoServer, rawCatalog);
    }
}
