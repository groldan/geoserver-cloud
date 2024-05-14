/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.backend.pgconfig.catalog.repository;

import lombok.NonNull;

import org.geoserver.catalog.PublishedInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @since 1.9
 */
public abstract class PgconfigPublishedInfoRepository<P extends PublishedInfo>
        extends PgconfigCatalogInfoRepository<P> {

    protected final PgconfigStyleRepository styleLoader;
    private final Class<P> type;

    protected PgconfigPublishedInfoRepository(
            @NonNull Class<P> type,
            @NonNull JdbcTemplate template,
            @NonNull PgconfigStyleRepository styleLoader) {
        super(template);
        this.type = type;
        this.styleLoader = styleLoader;
    }

    @Override
    public Class<P> getContentType() {
        return type;
    }

    @Override
    protected RowMapper<P> newRowMapper() {
        return CatalogInfoRowMapper.published(styleLoader::findById);
    }
}
