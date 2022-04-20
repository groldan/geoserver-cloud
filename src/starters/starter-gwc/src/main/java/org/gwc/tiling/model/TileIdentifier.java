/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.tiling.model;

import lombok.NonNull;
import lombok.Value;

/**
 * @since 1.0
 */
public @Value class TileIdentifier {

    private @NonNull CacheIdentifier cache;
    private @NonNull TileIndex3D tileIndex;

    public long x() {
        return tileIndex.getX();
    }

    public long y() {
        return tileIndex.getY();
    }

    public int z() {
        return tileIndex.getZ();
    }
}
