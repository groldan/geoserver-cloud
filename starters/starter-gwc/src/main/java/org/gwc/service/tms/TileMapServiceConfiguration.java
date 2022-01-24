/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.service.tms;

import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.layer.TileLayerDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** @since 1.0 */
@Configuration
public class TileMapServiceConfiguration {

    @Autowired
    public @Bean TileMapService gwcTileMapService(TileLayerDispatcher tld, GridSetBroker gridsets) {
        return new TileMapServiceImpl(tld, gridsets);
    }
}
