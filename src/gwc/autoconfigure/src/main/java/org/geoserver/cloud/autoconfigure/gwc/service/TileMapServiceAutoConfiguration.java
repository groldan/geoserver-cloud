/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.gwc.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.geoserver.cloud.gwc.config.core.GeoWebCacheConfigurationProperties;
import org.geoserver.cloud.gwc.config.services.TileMapServiceConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

/**
 * @since 1.0
 */
@AutoConfiguration
@ConditionalOnProperty(
        name = GeoWebCacheConfigurationProperties.SERVICE_TMS_ENABLED,
        havingValue = "true",
        matchIfMissing = false)
@ConditionalOnClass(TileMapServiceConfiguration.class)
@Import(TileMapServiceConfiguration.class)
@Slf4j(topic = "org.geoserver.cloud.autoconfigure.gwc.service")
public class TileMapServiceAutoConfiguration {

    public @PostConstruct void log() {
        log.info("{} enabled", GeoWebCacheConfigurationProperties.SERVICE_TMS_ENABLED);
    }
}
