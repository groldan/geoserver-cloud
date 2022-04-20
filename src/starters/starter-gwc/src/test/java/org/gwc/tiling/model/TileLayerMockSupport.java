/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.tiling.model;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.NonNull;

import org.geowebcache.config.DefaultGridsets;
import org.geowebcache.grid.GridSet;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.grid.GridSubsetFactory;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.mime.MimeType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @since 1.0
 */
public class TileLayerMockSupport {

    public final GridSet worldEpsg3857 = new DefaultGridsets(false, false).worldEpsg3857();
    public final GridSet worldEpsg4326 = new DefaultGridsets(false, false).worldEpsg4326();

    public final GridSubset subset3857 = fullSubset(worldEpsg3857);
    public final GridSubset subset4326 = fullSubset(worldEpsg4326);

    public GridSubset fullSubset(@NonNull GridSet gridset) {
        int zoomStart = 0;
        int zoomStop = gridset.getNumLevels() - 1;
        return GridSubsetFactory.createGridSubSet(
                gridset, gridset.getBounds(), zoomStart, zoomStop);
    }

    public TileLayer mockLayer(
            @NonNull String name, @NonNull GridSubset gridSubset, @NonNull MimeType mimeType) {

        return this.mockLayer(name, gridSubset, mimeType, null);
    }

    public TileLayer mockLayer(
            @NonNull String name,
            @NonNull GridSubset gridSubset,
            @NonNull MimeType mimeType,
            String parameterIds) {

        return this.mockLayer(
                name,
                Set.of(gridSubset),
                List.of(mimeType),
                parameterIds == null ? List.of() : List.of(parameterIds));
    }

    public TileLayer mockLayer(
            String name,
            Set<GridSubset> gridSubsets,
            List<MimeType> mimeTypes,
            List<String> parameterIds) {

        TileLayer l = mock(TileLayer.class);
        when(l.getName()).thenReturn(name);
        when(l.getGridSubsets())
                .thenReturn(
                        gridSubsets.stream().map(GridSubset::getName).collect(Collectors.toSet()));
        when(l.getMimeTypes()).thenReturn(mimeTypes);

        for (GridSubset gs : gridSubsets) {
            when(l.getGridSubset(eq(gs.getName()))).thenReturn(gs);
        }

        when(l.getMetaTilingFactors()).thenReturn(new int[] {4, 4});
        return l;
    }
}
