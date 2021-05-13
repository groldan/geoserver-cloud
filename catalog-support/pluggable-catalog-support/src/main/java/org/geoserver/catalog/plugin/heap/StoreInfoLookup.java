/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.heap;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Stream;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.plugin.repository.StoreRepository;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

public class StoreInfoLookup extends CatalogInfoLookup<StoreInfo> implements StoreRepository {

    /**
     * The name uses the workspace id as it does not need to be updated when the workspace is
     * renamed
     */
    static final Function<StoreInfo, Name> STORE_NAME_MAPPER =
            s -> new NameImpl(s.getWorkspace().getId(), s.getName());

    /** The default store keyed by workspace id */
    protected ConcurrentMap<String, DataStoreInfo> defaultStores = new ConcurrentHashMap<>();

    public StoreInfoLookup() {
        super(StoreInfo.class, STORE_NAME_MAPPER);
    }

    public @Override void setDefaultDataStore(WorkspaceInfo workspace, DataStoreInfo store) {
        requireNonNull(workspace);
        requireNonNull(store);
        requireNonNull(store.getWorkspace());
        if (!Objects.equals(workspace.getId(), store.getWorkspace().getId())) {
            throw new IllegalArgumentException(
                    "Can't assign a default store that belongs to another workspace");
        }
        String wsId = workspace.getId();
        final DataStoreInfo localStore =
                super.findById(store.getId(), DataStoreInfo.class)
                        .orElseThrow(NoSuchElementException::new);
        defaultStores.compute(wsId, (ws, oldDefaultStore) -> localStore);
    }

    public @Override void unsetDefaultDataStore(WorkspaceInfo workspace) {
        requireNonNull(workspace);
        defaultStores.remove(workspace.getId());
    }

    public @Override Optional<DataStoreInfo> getDefaultDataStore(WorkspaceInfo workspace) {
        return Optional.ofNullable(defaultStores.get(workspace.getId()));
    }

    public @Override Stream<DataStoreInfo> getDefaultDataStores() {
        return defaultStores.values().stream();
    }

    public @Override void dispose() {
        super.dispose();
        defaultStores.clear();
    }

    public @Override <T extends StoreInfo> Stream<T> findAllByWorkspace(
            WorkspaceInfo workspace, Class<T> clazz) {
        requireNonNull(workspace);
        requireNonNull(clazz);
        return list(clazz, s -> workspace.getId().equals(s.getWorkspace().getId()));
    }

    public @Override <T extends StoreInfo> Stream<T> findAllByType(Class<T> clazz) {
        requireNonNull(clazz);
        return list(clazz, CatalogInfoLookup.alwaysTrue());
    }

    public @Override <T extends StoreInfo> Optional<T> findByNameAndWorkspace(
            String name, WorkspaceInfo workspace, Class<T> clazz) {
        requireNonNull(name);
        requireNonNull(workspace);
        requireNonNull(clazz);
        return findFirstByName(new NameImpl(workspace.getId(), name), clazz);
    }
}
