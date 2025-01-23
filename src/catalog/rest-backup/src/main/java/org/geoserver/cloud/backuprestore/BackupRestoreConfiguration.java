package org.geoserver.cloud.backuprestore;

import org.geoserver.catalog.Catalog;
import org.geoserver.config.GeoServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = true)
public class BackupRestoreConfiguration implements WebMvcConfigurer {

    @Bean
    BackupRestoreService backupRestoreService(GeoServer geoServer, @Qualifier("rawCatalog") Catalog rawCatalog) {
        return new BackupRestoreService(geoServer, rawCatalog);
    }
}
