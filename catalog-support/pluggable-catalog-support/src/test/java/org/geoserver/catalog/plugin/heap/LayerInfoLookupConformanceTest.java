/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.heap;

import org.geoserver.catalog.plugin.repository.LayerRepository;
import org.geoserver.catalog.plugin.repository.LayerRepositoryConformanceTest;
import org.geoserver.catalog.plugin.repository.ResourceRepository;
import org.geoserver.catalog.plugin.repository.StyleRepository;
import org.junit.After;

public class LayerInfoLookupConformanceTest extends LayerRepositoryConformanceTest {

    private LayerInfoLookup layers;

    public @After void clear() {
        this.layers = null;
    }

    protected @Override LayerRepository createLayerRepository() {
        return layers;
    }

    protected @Override ResourceRepository createResourceRepository() {
        this.layers = new LayerInfoLookup();
        return new ResourceInfoLookup(this.layers);
    }

    protected @Override StyleRepository createStylesRepository() {
        return new StyleInfoLookup();
    }
}
