/*
 * (c) 2023 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.backend.pgsql.config;

import lombok.NonNull;

import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.plugin.RepositoryGeoServerFacadeImpl;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

/**
 * @since 1.4
 */
public class PgsqlGeoServerFacade extends RepositoryGeoServerFacadeImpl {

    public PgsqlGeoServerFacade(@NonNull JdbcTemplate template) {
        super(new PgsqlConfigRepository(template));
    }

    public PgsqlGeoServerFacade(@NonNull PgsqlConfigRepository repo) {
        super(repo);
    }

    @Override
    public GeoServerInfo getGlobal() {
        GeoServerInfo g =
                RequestCache.get()
                        .flatMap(cache -> cache.getGlobal(repository::getGlobal))
                        .orElse(null);
        return g == null ? super.getGlobal() : wrap(resolve(g), GeoServerInfo.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ServiceInfo> T getService(Class<T> clazz) {
        Optional<T> cached =
                RequestCache.get()
                        .map(cache -> cache.getService(clazz, c -> super.getService((Class<T>) c)));
        return cached.orElseGet(() -> super.getService(clazz));
    }
}
