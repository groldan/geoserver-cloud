/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.heap;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.plugin.Patch;
import org.geoserver.catalog.plugin.repository.ResourceRepository;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

/**
 * CatalogInfoLookup specialization for {@code ResourceInfo} that encapsulates the logic to update
 * the name lookup for the linked {@code LayerInfo} given that {@code LayerInfo.getName() ==
 * LayerInfo.getResource().getName()}
 */
public final class ResourceInfoLookup extends CatalogInfoLookup<ResourceInfo>
        implements ResourceRepository {
    /**
     * The name uses the namspace id as it does not need to be updated when the namespace is renamed
     */
    protected static final Function<ResourceInfo, Name> RESOURCE_NAME_MAPPER =
            r -> new NameImpl(r.getNamespace().getId(), r.getName());

    private final LayerInfoLookup layers;

    public ResourceInfoLookup(LayerInfoLookup layers) {
        super(ResourceInfo.class, ResourceInfoLookup.RESOURCE_NAME_MAPPER);
        this.layers = layers;
    }

    public @Override <R extends ResourceInfo> R update(R value, Patch patch) {
        requireNonNull(value);
        requireNonNull(patch);
        Name oldName = getMapForValue(idToMameMultiMap, value).get(value.getId());
        R updated = super.update(value, patch);
        Name newName = nameMapper.apply(value);
        if (!newName.equals(oldName)) {
            layers.updateName(oldName, newName);
        }
        return updated;
    }

    public @Override <T extends ResourceInfo> Stream<T> findAllByType(Class<T> clazz) {
        requireNonNull(clazz);
        return list(clazz, CatalogInfoLookup.alwaysTrue());
    }

    public @Override <T extends ResourceInfo> Stream<T> findAllByNamespace(
            NamespaceInfo ns, Class<T> clazz) {
        requireNonNull(ns);
        requireNonNull(clazz);
        return list(clazz, r -> ns.equals(r.getNamespace()));
    }

    public @Override <T extends ResourceInfo> Optional<T> findByStoreAndName(
            StoreInfo store, String name, Class<T> clazz) {
        requireNonNull(store);
        requireNonNull(name);
        requireNonNull(clazz);
        return findFirst(
                clazz, r -> name.equals(r.getName()) && store.getId().equals(r.getStore().getId()));
    }

    public @Override <T extends ResourceInfo> Stream<T> findAllByStore(
            StoreInfo store, Class<T> clazz) {
        requireNonNull(store);
        requireNonNull(clazz);
        return list(clazz, r -> store.equals(r.getStore()));
    }

    public @Override <T extends ResourceInfo> Optional<T> findByNameAndNamespace(
            String name, NamespaceInfo namespace, Class<T> clazz) {
        requireNonNull(name);
        requireNonNull(namespace);
        requireNonNull(clazz);
        return findFirstByName(new NameImpl(namespace.getId(), name), clazz);
    }
}
