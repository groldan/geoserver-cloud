/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.tiling.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.geowebcache.layer.TileLayer;
import org.gwc.tiling.model.MetaTileIdentifier;

/**
 * Blocking worker for (re)seeding/truncating a {@link TileLayer}
 *
 * @since 1.0
 */
@RequiredArgsConstructor
public abstract class TileLayerSeeder {

    protected final @NonNull TileLayer layer;

    /**
     * @return
     */
    public int getMetaWidth() {
        return layer.getMetaTilingFactors()[0];
    }

    /**
     * @return
     */
    public int getMetaHeight() {
        return layer.getMetaTilingFactors()[1];
    }

    /**
     * Ensures all tiles in the meta-tile {@link MetaTileIdentifier#getTiles() range} exists in the
     * cache, possibly disregarding the creation of tiles that already exist
     *
     * @param metatile
     */
    public abstract void seed(MetaTileRequest metatile);

    /**
     * Forces re-creating tiles in the meta-tile {@link MetaTileIdentifier#getTiles() range}
     *
     * @param metatile
     */
    public abstract void reseed(MetaTileRequest metatile);

    /**
     * @param metatile
     */
    public abstract void truncate(MetaTileRequest metatile);
}
