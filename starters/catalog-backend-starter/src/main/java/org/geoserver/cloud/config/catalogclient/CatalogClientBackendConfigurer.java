/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.config.catalogclient;

import java.io.File;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.catalog.plugin.ExtendedCatalogFacade;
import org.geoserver.cloud.catalog.client.impl.CatalogClientCatalogFacade;
import org.geoserver.cloud.catalog.client.impl.CatalogClientConfiguration;
import org.geoserver.cloud.catalog.client.impl.CatalogClientGeoServerFacade;
import org.geoserver.cloud.catalog.client.impl.CatalogClientResourceStore;
import org.geoserver.cloud.catalog.client.reactivefeign.ResourceStoreFallbackFactory;
import org.geoserver.cloud.config.catalog.GeoServerBackendConfigurer;
import org.geoserver.cloud.config.catalog.GeoServerBackendProperties;
import org.geoserver.config.GeoServerFacade;
import org.geoserver.config.GeoServerLoader;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.platform.resource.FileSystemResourceStore;
import org.geoserver.platform.resource.ResourceStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CatalogClientConfiguration.class)
@Slf4j
public class CatalogClientBackendConfigurer implements GeoServerBackendConfigurer {

    private @Autowired @Getter ApplicationContext context;

    private @Autowired CatalogClientCatalogFacade catalogClientFacade;
    private @Autowired CatalogClientGeoServerFacade configClientFacade;
    private @Autowired CatalogClientResourceStore catalogServiceResourceStore;
    private @Autowired ResourceStoreFallbackFactory resourceStoreFallbackFactory;

    private @Autowired GeoServerBackendProperties configProps;

    public @Override @Bean ExtendedCatalogFacade catalogFacade() {
        return catalogClientFacade;
    }

    public @Override @Bean GeoServerFacade geoserverFacade() {
        return configClientFacade;
    }

    public @Override @Bean CatalogClientResourceStore resourceStoreImpl() {
        CatalogClientResourceStore store = catalogServiceResourceStore;
        File cacheDirectory = configProps.getCatalogService().getCacheDirectory();
        if (null != cacheDirectory) {
            store.setLocalCacheDirectory(cacheDirectory);
        }
        return store;
    }

    public @Override @Bean GeoServerLoader geoServerLoaderImpl() {
        return new CatalogClientGeoServerLoader(resourceLoader());
    }

    public @Override @Bean GeoServerResourceLoader resourceLoader() {
        ResourceStore fallbackResourceStore = catalogServiceFallbackResourceStore();
        if (fallbackResourceStore != null) {
            log.info(
                    "Using fallback ResourceStore {}",
                    fallbackResourceStore.getClass().getCanonicalName());
            resourceStoreFallbackFactory.setFallback(fallbackResourceStore);
        }
        CatalogClientResourceStore resourceStore = resourceStoreImpl();
        GeoServerResourceLoader resourceLoader = new GeoServerResourceLoader(resourceStore);
        File cacheDirectory = configProps.getCatalogService().getCacheDirectory();
        if (null != cacheDirectory) {
            resourceLoader.setBaseDirectory(cacheDirectory);
        }
        return resourceLoader;
    }

    public @Bean ResourceStore catalogServiceFallbackResourceStore() {
        File dir =
                getContext()
                        .getEnvironment()
                        .getProperty(
                                "geoserver.backend.catalog-service.fallback-resource-directory",
                                File.class);
        if (dir == null) return null;
        dir.mkdirs();
        return new FileSystemResourceStore(dir);
    }
}
