/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.tiling.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.layer.TileLayer;

import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @since 1.0
 */
@Accessors(chain = true, fluent = true)
public class TilePyramidBuilder {

    private @Setter @Getter TileLayer layer;
    private @Setter @Getter String gridsetId;
    private @Setter @Getter BoundingBox bounds;

    public TilePyramid build() {
        Objects.requireNonNull(layer, "layer can't be null");
        Objects.requireNonNull(gridsetId, "gridsetId can't be null");

        final @NonNull GridSubset gridSubset = layer.getGridSubset(gridsetId);
        long[][] coveredGridLevels = resolveCoverageGridLevels(layer, bounds, gridSubset);

        SortedSet<TileRange3D> levelRanges =
                Stream.of(coveredGridLevels)
                        // could be null in case calling code is only interested in a subset of zoom
                        // levels
                        .filter(Objects::nonNull)
                        .map(TilePyramidBuilder::gridCoverageToTileRange)
                        .collect(Collectors.toCollection(TreeSet::new));

        return new TilePyramid(levelRanges);
    }

    private static TileRange3D gridCoverageToTileRange(long[] levelCoverage) {
        int z = (int) levelCoverage[4];
        long minx = levelCoverage[0];
        long miny = levelCoverage[1];
        long maxx = levelCoverage[2];
        long maxy = levelCoverage[3];
        return TileRange3D.of(z, minx, miny, maxx, maxy);
    }

    private static long[][] resolveCoverageGridLevels(
            @NonNull TileLayer layer, BoundingBox boundingBox, GridSubset gridSubset) {

        long[][] coveredGridLevels;
        if (boundingBox == null) {
            coveredGridLevels = gridSubset.getCoverages();
        } else {
            coveredGridLevels = gridSubset.getCoverageIntersections(boundingBox);
        }
        int[] metaTilingFactors = layer.getMetaTilingFactors();
        coveredGridLevels = gridSubset.expandToMetaFactors(coveredGridLevels, metaTilingFactors);
        return coveredGridLevels;
    }
}
