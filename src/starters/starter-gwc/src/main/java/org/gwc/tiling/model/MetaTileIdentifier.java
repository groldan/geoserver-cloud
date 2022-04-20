/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.tiling.model;

import lombok.NonNull;
import lombok.Value;

import java.util.stream.Stream;

/**
 * @since 1.0
 */
public @Value class MetaTileIdentifier {

    private @NonNull CacheIdentifier cache;
    private @NonNull TileRange3D tiles;

    public Stream<TileIdentifier> asTiles() {
        return tiles.asTiles().map(index -> new TileIdentifier(cache, index));
    }
}
