/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;

public interface ResourceRepository extends CatalogInfoRepository<ResourceInfo> {

    <T extends ResourceInfo> Optional<T> findByNameAndNamespace(
            @NonNull String name, @NonNull NamespaceInfo namespace, @NonNull Class<T> clazz);

    <T extends ResourceInfo> Stream<T> findAllByType(@NonNull Class<T> clazz);

    <T extends ResourceInfo> Stream<T> findAllByNamespace(
            @NonNull NamespaceInfo ns, @NonNull Class<T> clazz);

    <T extends ResourceInfo> Optional<T> findByStoreAndName(
            @NonNull StoreInfo store, @NonNull String name, @NonNull Class<T> clazz);

    <T extends ResourceInfo> Stream<T> findAllByStore(StoreInfo store, Class<T> clazz);
}
