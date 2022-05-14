/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.gwc.tiling;

import lombok.extern.slf4j.Slf4j;

import org.geoserver.cloud.autoconfigure.gwc.ConditionalOnGeoWebCacheEnabled;
import org.geoserver.cloud.autoconfigure.gwc.core.GwcCoreAutoConfiguration;
import org.gwc.tiling.integration.local.GeoWebCacheJobsConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

/**
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnGeoWebCacheEnabled
@AutoConfigureAfter(GwcCoreAutoConfiguration.class)
@Import({GeoWebCacheJobsConfiguration.class})
@Slf4j(topic = "org.geoserver.cloud.autoconfigure.gwc.tiling")
public class TilingServiceAutoConfiguration {

    public @PostConstruct void log() {
        log.info("GeoWebCache tile cache jobs enabled");
    }
}
