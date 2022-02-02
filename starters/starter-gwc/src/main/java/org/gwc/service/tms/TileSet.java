/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.service.tms;

import lombok.Data;

/** @since 1.0 */
public @Data class TileSet {

    private String name;
    private int order;
    private double unitsPerPixel;
}
