/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.gwc.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.geoserver.gwc.layer.TileLayerCatalogListener;
import org.springframework.context.ApplicationContext;

/**
 * Local {@link ApplicationContext} event issued to replace the tighly coupled {@link
 * TileLayerCatalogListener} by loosely coupled application events
 *
 * @since 1.0
 */
public class TileLayerEvent extends GeoWebCacheEvent {

    private static final long serialVersionUID = 1L;

    private @Getter @Setter String layerId;

    public TileLayerEvent(Object source) {
        super(source);
    }

    public TileLayerEvent(Object source, @NonNull Type eventType, @NonNull String layerId) {
        super(source, eventType);
        this.layerId = layerId;
    }

    public @Override String toString() {
        return String.format("%s[%s]", getClass().getSimpleName(), getLayerId());
    }

    protected @Override String getObjectId() {
        return layerId;
    }
}
