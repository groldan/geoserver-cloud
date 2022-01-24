/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.service.tms;

import com.google.common.collect.Streams;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;
import org.geowebcache.grid.GridSet;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.layer.TileLayerDispatcher;
import org.geowebcache.layer.meta.LayerMetaInformation;
import org.geowebcache.mime.MimeType;

/** @since 1.0 */
public class TileMapServiceImpl implements TileMapService {

    private TileLayerDispatcher layers;
    private GridSetBroker gridsets;

    public TileMapServiceImpl(TileLayerDispatcher layers, GridSetBroker gridsets) {
        this.layers = layers;
        this.gridsets = gridsets;
    }

    @Override
    public Stream<TileMapInfo> getTileMapInfos() {
        Iterable<TileLayer> layerList = layers.getLayerList();
        return Streams.stream(layerList).flatMap(this::tileMapsOf);
    }

    @Override
    public Optional<TileMap> findTileMap(String layerName, String gridsetId, String format) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Stream<TileSet> findTileSets(TileMap tileMap) {
        return Stream.empty();
    }

    private Stream<TileMapInfo> tileMapsOf(TileLayer layer) {
        Stream<GridSubset> gridSubsets = layer.getGridSubsets().stream().map(layer::getGridSubset);
        return gridSubsets.flatMap(gs -> this.tileMapsOf(layer, gs));
    }

    private Stream<TileMapInfo> tileMapsOf(TileLayer layer, GridSubset gridSubset) {
        return layer.getMimeTypes().stream().map(mime -> toTileMapInfo(layer, gridSubset, mime));
    }

    private TileMapInfo toTileMapInfo(TileLayer layer, GridSubset gridSubset, MimeType mime) {
        final GridSet gridSet = gridSubset.getGridSet();

        String title = tileMapTitle(layer);
        String srs = gridSubset.getSRS().toString();
        String identifier = tileMapName(layer, gridSubset, mime);
        String profile = profileForGridSet(gridSet);

        TileMapInfo info = new TileMapInfo();
        info.setTitle(title);
        info.setIdentifier(identifier);
        info.setSrs(srs);
        info.setProfile(profile);
        return info;
    }

    protected String tileMapName(TileLayer tl, GridSubset gridSub, MimeType mimeType) {
        String name = URLEncoder.encode(tl.getName(), StandardCharsets.UTF_8);
        String gridSubset = URLEncoder.encode(gridSub.getName(), StandardCharsets.UTF_8);
        String fileExtension = mimeType.getFileExtension();
        return String.format("%s@%s@%s", name, gridSubset, fileExtension);
    }

    protected String profileForGridSet(GridSet gridSet) {
        if (gridSet == gridsets.getWorldEpsg4326()) {
            return "global-geodetic";
        } else if (gridSet == gridsets.getWorldEpsg3857()) {
            return "global-mercator";
        }
        return "local";
    }

    protected String tileMapTitle(TileLayer tl) {
        LayerMetaInformation metaInfo = tl.getMetaInformation();
        if (metaInfo != null && metaInfo.getTitle() != null) {
            return metaInfo.getTitle();
        }

        return tl.getName();
    }
}
