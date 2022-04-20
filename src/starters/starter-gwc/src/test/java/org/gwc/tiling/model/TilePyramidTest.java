/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.tiling.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.NonNull;

import org.geowebcache.grid.GridSubset;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.mime.ImageMime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @since 1.0
 */
class TilePyramidTest {

    private final TileLayerMockSupport support = new TileLayerMockSupport();

    @BeforeEach
    void setUp() throws Exception {}

    TilePyramid full3857() {
        return full(support.subset3857);
    }

    TilePyramid full4326() {
        return full(support.subset4326);
    }

    TilePyramid full(GridSubset subset) {
        return builder(subset).build();
    }

    TilePyramidBuilder builder(GridSubset subset) {
        TileLayer layer = support.mockLayer("layer", subset, ImageMime.png24);
        String gridsetId = subset.getName();
        return TilePyramid.builder().layer(layer).gridsetId(gridsetId);
    }

    @Test
    void testRangesOrder() {
        GridSubset subset = support.subset3857;
        TilePyramid pyramid = full(subset);

        final int numLevels = subset.getGridSet().getNumLevels();
        final SortedSet<TileRange3D> ranges = pyramid.getRanges();

        assertEquals(numLevels, pyramid.getRanges().size());
        assertEquals(subset.getZoomStart(), pyramid.getMinZoomLevel());
        assertEquals(subset.getZoomStop(), pyramid.getMaxZoomLevel());

        List<Integer> expected = IntStream.range(0, numLevels).mapToObj(Integer::valueOf).toList();
        List<Integer> actual = ranges.stream().map(TileRange3D::getZoomLevel).toList();
        assertEquals(expected, actual);
    }

    @Test
    void testFullBounds_3857() {
        testFullBounds(support.subset3857);
    }

    @Test
    void testFullBounds_4326() {
        testFullBounds(support.subset4326);
    }

    void testFullBounds(@NonNull GridSubset subset) {
        TilePyramidBuilder builder = builder(subset);
        TileLayer layer = builder.layer();
        TilePyramid pyramid = builder.build();
        assertEquals(pyramid, builder(subset).bounds(null).build());

        long[][] coverages = subset.getCoverages();
        int[] metaTilingFactors = layer.getMetaTilingFactors();
        // {z}{minx,miny,maxx,maxy,z}
        long[][] coveredGridLevels = subset.expandToMetaFactors(coverages, metaTilingFactors);

        for (int z = subset.getZoomStart(); z <= subset.getZoomStop(); z++) {
            long[] coverage = coveredGridLevels[z];

            TileRange3D range = pyramid.getRange(z).orElseThrow();
            assertEquals(z, range.getZoomLevel());
            TileIndex2D lowerLeft = range.getTiles().getLowerLeft();
            TileIndex2D upperRight = range.getTiles().getUpperRight();
            assertEquals(coverage[0], lowerLeft.getX(), () -> "at z level " + coverage[4]);
            assertEquals(coverage[1], lowerLeft.getY(), () -> "at z level " + coverage[4]);
            assertEquals(coverage[2], upperRight.getX(), () -> "at z level " + coverage[4]);
            assertEquals(coverage[3], upperRight.getY(), () -> "at z level " + coverage[4]);
        }
    }

    @Test
    void testAsMetaTiles() {
        final TilePyramid pyramid = full4326().toLevel(11);
        testAsMetaTiles(pyramid, 1, 1);
        testAsMetaTiles(pyramid, 2, 2);
        testAsMetaTiles(pyramid, 1, 3);
        testAsMetaTiles(pyramid, 3, 1);
        testAsMetaTiles(pyramid, 8, 8);
        testAsMetaTiles(pyramid, 21, 32);

        IllegalArgumentException err;
        err = assertThrows(IllegalArgumentException.class, () -> testAsMetaTiles(pyramid, -1, 1));
        assertThat(err.getMessage()).contains("width must be > 0");

        err = assertThrows(IllegalArgumentException.class, () -> testAsMetaTiles(pyramid, 1, -1));
        assertThat(err.getMessage()).contains("height must be > 0");
    }

    protected void testAsMetaTiles(final TilePyramid pyramid, int metax, int metay) {
        long metaTileCount = pyramid.asMetaTiles(metax, metay).count();
        long fastCount = pyramid.countMetaTiles(metax, metay).longValue();
        assertEquals(metaTileCount, fastCount);

        Stream<TileRange3D> asMetaTiles = pyramid.asMetaTiles(metax, metay);

        BigInteger tilesFromMetaTiles =
                asMetaTiles.map(TileRange3D::count).reduce(BigInteger::add).orElseThrow();

        assertEquals(pyramid.count(), tilesFromMetaTiles);
    }

    @Test
    void testAsTiles() {
        final TilePyramid pyramid = full4326().toLevel(12);
        Stream<TileIndex3D> tiles = pyramid.asTiles();
        assertEquals(pyramid.count().longValue(), tiles.count());
    }

    @Test
    @Disabled
    void testTraverseFullMetaTiles() {
        // e.g.: Count: 1,832,603,271, time: 36s, throughput: 50,905,646/s
        final TilePyramid pyramid = full4326().toLevel(18);
        long start = System.currentTimeMillis();
        long count = pyramid.asMetaTiles(10, 10).count();
        long ts = (System.currentTimeMillis() - start) / 1000;
        long thrpt = count / ts;
        System.err.printf("Count: %,d, time: %,ds, throughput: %,d/s", count, ts, thrpt);
    }
}
