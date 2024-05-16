/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.backend.pgconfig.catalog.repository;

import static org.geoserver.catalog.Predicates.and;
import static org.geoserver.catalog.Predicates.isInstanceOf;

import lombok.NonNull;

import org.geoserver.catalog.Info;
import org.geoserver.catalog.PublishedInfo;
import org.geotools.api.filter.Filter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @since 1.9
 */
public class PgconfigPublishedInfoRepository<P extends PublishedInfo>
        extends PgconfigCatalogInfoRepository<P> {

    protected final PgconfigStyleRepository styleLoader;

    private final @NonNull Class<P> type;

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
    protected String getQueryTable() {
        return "publishedinfos";
    }

    @Override
    protected RowMapper<P> newRowMapper() {
        return CatalogInfoRowMapper.published(styleLoader::findById);
    }

    @Override
    protected <S extends Info> Filter applyTypeFilter(Filter filter, Class<S> type) {
        if (!PublishedInfo.class.equals(type)) {
            filter = and(isInstanceOf(type), filter);
        }
        return filter;
    }
}
