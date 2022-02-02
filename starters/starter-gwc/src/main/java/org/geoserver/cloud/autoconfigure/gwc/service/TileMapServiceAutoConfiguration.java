/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.gwc.service;

import org.gwc.service.tms.TileMapServiceConfiguration;
import org.gwc.web.tms.TileMapServiceWebConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * TODO: make conditional
 *
 * @since 1.0
 */
@Configuration
@Import({TileMapServiceConfiguration.class, TileMapServiceWebConfiguration.class})
public class TileMapServiceAutoConfiguration {}
