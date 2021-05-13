/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.heap;

import java.util.function.Function;
import org.geoserver.catalog.MapInfo;
import org.geoserver.catalog.plugin.repository.MapRepository;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

public class MapInfoLookup extends CatalogInfoLookup<MapInfo> implements MapRepository {

    /**
     * Name mapper for {@link MapInfo}, uses simple name mapping on {@link MapInfo#getName()} as it
     * doesn't have a namespace component
     */
    static final Function<MapInfo, Name> MAP_NAME_MAPPER = m -> new NameImpl(m.getName());

    public MapInfoLookup() {
        super(MapInfo.class, MAP_NAME_MAPPER);
    }
}
