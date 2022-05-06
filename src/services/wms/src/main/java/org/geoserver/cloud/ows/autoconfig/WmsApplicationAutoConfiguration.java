/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.ows.autoconfig;

import org.geoserver.cloud.ows.config.wms.WmsApplicationConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @since 1.0
 */
@Configuration
@Import(WmsApplicationConfiguration.class)
@ConditionalOnProperty(name = "geoserver.wms.enabled", matchIfMissing = false)
public class WmsApplicationAutoConfiguration {}
