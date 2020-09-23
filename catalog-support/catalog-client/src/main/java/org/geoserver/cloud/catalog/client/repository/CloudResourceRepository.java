/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.catalog.client.repository;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.impl.ClassMappings;
import org.geoserver.catalog.plugin.CatalogInfoRepository.ResourceRepository;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;

public class CloudResourceRepository extends CatalogServiceClientRepository<ResourceInfo>
        implements ResourceRepository {

    private final @Getter Class<ResourceInfo> infoType = ResourceInfo.class;

    // REVISIT: used to build filters on methods that miss a counterpart on ReactiveCatalogClient
    private final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    public @Override <T extends ResourceInfo> Stream<T> findAllByType(@Nullable Class<T> clazz) {
        return client().findAll(endpoint(), typeEnum(clazz))
                .map(clazz::cast)
                .map(this::resolve)
                .toStream();
    }

    public @Override <T extends ResourceInfo> Stream<T> findAllByNamespace(
            @NonNull NamespaceInfo ns, @Nullable Class<T> clazz) {

        // REVISIT: missed custom method on ReactiveCatalogClient
        Filter filter = ff.equals(ff.property("namespace.id"), ff.literal(ns.getId()));
        return client().query(endpoint(), typeEnum(clazz), filter)
                .map(clazz::cast)
                .map(this::resolve)
                .toStream();
    }

    public @Override @Nullable <T extends ResourceInfo> Optional<T> findByStoreAndName(
            @NonNull StoreInfo store, @NonNull String name, @Nullable Class<T> clazz) {
        // REVISIT: missed custom method on ReactiveCatalogClient
        Filter filter =
                ff.and(
                        ff.equals(ff.property("store.id"), ff.literal(store.getId())),
                        ff.equals(ff.property("name"), ff.literal(name)));

        Flux<T> query = client().query(endpoint(), typeEnum(clazz), filter);
        return query.toStream().findFirst().map(this::resolve);
    }

    public @Override <T extends ResourceInfo> Stream<T> findAllByStore(
            StoreInfo store, Class<T> clazz) {

        // REVISIT: missed custom method on ReactiveCatalogClient
        Filter filter = ff.equals(ff.property("store.id"), ff.literal(store.getId()));
        Flux<T> query = client().query(endpoint(), typeEnum(clazz), filter);
        return query.toStream().map(this::resolve);
    }

    public @Override <T extends ResourceInfo> Optional<T> findByNameAndNamespace(
            @NonNull String name, @NonNull NamespaceInfo namespace, @NonNull Class<T> clazz) {

        String namespaceId = namespace.getId();
        ClassMappings type = typeEnum(clazz);
        return blockAndReturn(client().findResourceByNamespaceIdAndName(namespaceId, name, type));
    }
}
