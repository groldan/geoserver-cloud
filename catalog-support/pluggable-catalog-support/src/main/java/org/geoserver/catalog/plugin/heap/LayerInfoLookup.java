/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.heap;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Stream;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.impl.LayerInfoImpl;
import org.geoserver.catalog.plugin.repository.LayerRepository;
import org.opengis.feature.type.Name;

public final class LayerInfoLookup extends CatalogInfoLookup<LayerInfo> implements LayerRepository {

    /** Like LayerInfo, actually delegates to the resource logic */
    static final Function<LayerInfo, Name> LAYER_NAME_MAPPER =
            l -> ResourceInfoLookup.RESOURCE_NAME_MAPPER.apply(l.getResource());

    public LayerInfoLookup() {
        super(LayerInfo.class, LAYER_NAME_MAPPER);
    }

    void updateName(Name oldName, Name newName) {
        requireNonNull(oldName);
        requireNonNull(newName);
        ConcurrentMap<Name, LayerInfo> nameLookup =
                getMapForType(nameMultiMap, LayerInfoImpl.class);
        LayerInfo layer = nameLookup.remove(oldName);
        if (layer != null) {
            nameLookup.put(newName, layer);
            getMapForType(idToMameMultiMap, LayerInfoImpl.class).put(layer.getId(), newName);
        }
    }

    /** Override to remove by name instead of by id */
    public @Override void remove(LayerInfo value) {
        requireNonNull(value);
        checkNotAProxy(value);
        ConcurrentMap<Name, LayerInfo> nameMap = getMapForValue(nameMultiMap, value);
        synchronized (nameMap) {
            Name name = nameMapper.apply(value);
            LayerInfo removed = nameMap.remove(name);
            if (removed != null) {
                getMapForValue(idMultiMap, value).remove(value.getId());
                getMapForValue(idToMameMultiMap, value).remove(value.getId());
            }
        }
    }

    public @Override Optional<LayerInfo> findOneByName(String name) {
        requireNonNull(name);
        return findFirst(LayerInfo.class, li -> name.equals(li.getName()));
    }

    public @Override Stream<LayerInfo> findAllByDefaultStyleOrStyles(StyleInfo style) {
        requireNonNull(style);
        return list(
                LayerInfo.class,
                li -> style.equals(li.getDefaultStyle()) || li.getStyles().contains(style));
    }

    public @Override Stream<LayerInfo> findAllByResource(ResourceInfo resource) {
        requireNonNull(resource);
        // in the current setup we cannot have multiple layers associated to the same
        // resource, as they would all share the same name (the one of the resource) so
        // a direct lookup becomes possible
        Name name = ResourceInfoLookup.RESOURCE_NAME_MAPPER.apply(resource);
        return findFirstByName(name, LayerInfo.class).map(Stream::of).orElse(Stream.empty());
    }
}
