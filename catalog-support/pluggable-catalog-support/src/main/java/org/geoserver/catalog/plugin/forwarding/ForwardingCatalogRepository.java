/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.forwarding;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.plugin.Patch;
import org.geoserver.catalog.plugin.Query;
import org.geoserver.catalog.plugin.repository.CatalogInfoRepository;
import org.opengis.filter.Filter;

public abstract class ForwardingCatalogRepository<
                I extends CatalogInfo, S extends CatalogInfoRepository<I>>
        implements CatalogInfoRepository<I> {

    protected S subject;

    public ForwardingCatalogRepository(S subject) {
        this.subject = subject;
    }

    public @Override Class<I> getContentType() {
        return subject.getContentType();
    }

    public @Override boolean canSortBy(@NonNull String propertyName) {
        return subject.canSortBy(propertyName);
    }

    public @Override void add(I value) {
        subject.add(value);
    }

    public @Override void remove(I value) {
        subject.remove(value);
    }

    public @Override <T extends I> T update(T value, Patch patch) {
        return subject.update(value, patch);
    }

    public @Override void dispose() {
        subject.dispose();
    }

    public @Override Stream<I> findAll() {
        return subject.findAll();
    }

    public @Override <U extends I> Stream<U> findAll(Query<U> query) {
        return subject.findAll(query);
    }

    public @Override <U extends I> long count(final Class<U> of, final Filter filter) {
        return subject.count(of, filter);
    }

    public @Override <U extends I> Optional<U> findById(String id, Class<U> clazz) {
        return subject.findById(id, clazz);
    }

    public @Override <U extends I> Optional<U> findFirstByName(
            @NonNull String name, Class<U> clazz) {
        return subject.findFirstByName(name, clazz);
    }

    public @Override void syncTo(CatalogInfoRepository<I> target) {
        subject.syncTo(target);
    }
}
