/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.gwc.core;

import org.geoserver.cloud.autoconfigure.gwc.ConditionalOnGeoWebCacheEnabled;
import org.geoserver.cloud.gwc.config.core.GeoWebCacheConfigurationProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * This auto-configuration only integrates the minimal components to have gwc integrated with
 * GeoServer, while allowing to disable certain components through {@link
 * GeoWebCacheConfigurationProperties configuration properties}.
 *
 * @see GeoWebCacheCoreAutoConfiguration
 * @see GeoServerIntegrationAutoConfiguration
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnGeoWebCacheEnabled
@AutoConfigureAfter(CacheSeedingWebMapServiceAutoConfiguration.class)
@Import({ //
    GeoWebCacheCoreAutoConfiguration.class, //
    GeoServerIntegrationAutoConfiguration.class
})
public class GeoWebCacheAutoConfiguration {}
