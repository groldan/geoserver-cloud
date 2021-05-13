/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.WorkspaceInfo;

public interface StoreRepository extends CatalogInfoRepository<StoreInfo> {

    void setDefaultDataStore(@NonNull WorkspaceInfo workspace, @NonNull DataStoreInfo dataStore);

    void unsetDefaultDataStore(@NonNull WorkspaceInfo workspace);

    Optional<DataStoreInfo> getDefaultDataStore(@NonNull WorkspaceInfo workspace);

    Stream<DataStoreInfo> getDefaultDataStores();

    <T extends StoreInfo> Stream<T> findAllByWorkspace(
            @NonNull WorkspaceInfo workspace, @NonNull Class<T> clazz);

    <T extends StoreInfo> Stream<T> findAllByType(@NonNull Class<T> clazz);

    <T extends StoreInfo> Optional<T> findByNameAndWorkspace(
            @NonNull String name, @NonNull WorkspaceInfo workspace, @NonNull Class<T> clazz);
}
