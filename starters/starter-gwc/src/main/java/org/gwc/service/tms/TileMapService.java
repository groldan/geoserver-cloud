/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.service.tms;

import java.util.Optional;
import java.util.stream.Stream;

/** @since 1.0 */
public interface TileMapService {

    Stream<TileMapInfo> getTileMapInfos();

    /**
     * @param layerName
     * @param gridsetId
     * @param format
     * @return
     */
    Optional<TileMap> findTileMap(String layerName, String gridsetId, String format);

    /** @param tileMap */
    Stream<TileSet> findTileSets(TileMap tileMap);
}
