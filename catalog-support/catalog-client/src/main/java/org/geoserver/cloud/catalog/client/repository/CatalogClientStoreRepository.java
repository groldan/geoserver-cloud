/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.catalog.client.repository;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.ClassMappings;
import org.geoserver.catalog.plugin.repository.StoreRepository;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;

public class CatalogClientStoreRepository extends CatalogClientRepository<StoreInfo>
        implements StoreRepository {

    private final @Getter Class<StoreInfo> contentType = StoreInfo.class;

    public @Override void setDefaultDataStore(
            @NonNull WorkspaceInfo workspace, @NonNull DataStoreInfo dataStore) {

        String workspaceId = workspace.getId();
        String dataStoreId = dataStore.getId();
        blockAndReturn(client().setDefaultDataStoreByWorkspaceId(workspaceId, dataStoreId));
    }

    public @Override void unsetDefaultDataStore(@NonNull WorkspaceInfo workspace) {
        String workspaceId = workspace.getId();
        blockAndReturn(client().unsetDefaultDataStore(workspaceId));
    }

    public @Override Optional<DataStoreInfo> getDefaultDataStore(@NonNull WorkspaceInfo workspace) {
        String workspaceId = workspace.getId();
        return blockAndReturn(client().findDefaultDataStoreByWorkspaceId(workspaceId));
    }

    public @Override Stream<DataStoreInfo> getDefaultDataStores() {
        return toStream(client().getDefaultDataStores());
    }

    public @Override <T extends StoreInfo> Stream<T> findAllByWorkspace(
            @NonNull WorkspaceInfo workspace, @Nullable Class<T> clazz) {

        String workspaceId = workspace.getId();
        ClassMappings type = typeEnum(clazz);

        Flux<T> flux = client().findStoresByWorkspaceId(workspaceId, type);
        return toStream(flux);
    }

    public @Override <T extends StoreInfo> Stream<T> findAllByType(@NonNull Class<T> clazz) {

        return toStream(client().findAll(endpoint(), typeEnum(clazz)).map(clazz::cast));
    }

    public @Override <T extends StoreInfo> Optional<T> findByNameAndWorkspace(
            @NonNull String name, @NonNull WorkspaceInfo workspace, @NonNull Class<T> clazz) {

        String workspaceId = workspace.getId();
        ClassMappings type = typeEnum(clazz);
        return blockAndReturn(client().findStoreByWorkspaceIdAndName(workspaceId, name, type));
    }
}
