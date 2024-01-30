/*
 * (c) 2023 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.catalog.backend.pgsql;

import org.geoserver.cloud.backend.pgsql.config.RequestCacheDispatcherCallbak;
import org.geoserver.cloud.config.catalog.backend.core.CatalogProperties;
import org.geoserver.cloud.config.catalog.backend.pgsql.PgsqlBackendConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * @since 1.4
 */
@AutoConfiguration(after = PgsqlMigrationAutoConfiguration.class)
@ConditionalOnPgsqlBackendEnabled
@EnableConfigurationProperties(CatalogProperties.class)
@Import(PgsqlBackendConfiguration.class)
public class PgsqlBackendAutoConfiguration {

    @Bean
    RequestCacheDispatcherCallbak pgconfigCacheDispatcherCallbak() {
        return new RequestCacheDispatcherCallbak();
    }
}
