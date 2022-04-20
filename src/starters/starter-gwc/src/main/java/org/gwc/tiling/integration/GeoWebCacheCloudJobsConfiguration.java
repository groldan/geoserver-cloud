/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.tiling.integration;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.layer.TileLayerDispatcher;
import org.geowebcache.storage.StorageBroker;
import org.geowebcache.storage.StorageException;
import org.gwc.tiling.event.jobs.CacheJobEvent;
import org.gwc.tiling.service.CacheJobManager;
import org.gwc.tiling.service.CacheJobRegistry;
import org.gwc.tiling.service.CacheJobRequestBuilder;
import org.gwc.tiling.service.TileCacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public class GeoWebCacheCloudJobsConfiguration {

    @Bean
    CacheJobRegistry cacheJobRegistry() {
        return new CacheJobRegistry();
    }

    @Bean
    CacheJobManager cacheJobManager(
            CacheJobRegistry registry,
            Supplier<CacheJobRequestBuilder> requestBuilderFactory,
            ApplicationEventPublisher springEventPublisher) {

        Consumer<? super CacheJobEvent> jobEventPublisher = springEventPublisher::publishEvent;
        return new CacheJobManager(registry, requestBuilderFactory, jobEventPublisher);
    }

    @Bean
    TileCacheManager tileCacheManager(TileLayerDispatcher tld) {
        return new TileCacheManager(new DefaultTileLayerSeederResolver(tld));
    }

    @Bean
    Supplier<CacheJobRequestBuilder> cacheJobRequestBuilderFactory(
            TileLayerDispatcher tld, StorageBroker sb) {
        Function<String, TileLayer> tileLayerResolver =
                t -> {
                    try {
                        return tld.getTileLayer(t);
                    } catch (GeoWebCacheException e) {
                        throw new IllegalStateException(e);
                    }
                };

        Function<String, Set<String>> paramsIdsResolver =
                t -> {
                    try {
                        return sb.getCachedParameterIds(t);
                    } catch (StorageException e) {
                        throw new IllegalStateException(e);
                    }
                };
        return () -> new CacheJobRequestBuilder(tileLayerResolver, paramsIdsResolver);
    }
}
