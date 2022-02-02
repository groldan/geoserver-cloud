/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.service.tms;

import lombok.Data;
import org.geowebcache.grid.BoundingBox;

/** @since 1.0 */
public @Data class TileMap {

    private String title;

    private String srs;

    private String profile = "local";

    /** tile map identifier, usually {@code <layerName>@<gridsetId>@<imageFormat>} */
    private String identifier;

    private String Abstract;

    private BoundingBox boundingBox;

    private double originX;
    private double originY;
    private int tileWidth;
    private int tileHeight;
    private String mimeType;
    private String fileNameExtension;
}
