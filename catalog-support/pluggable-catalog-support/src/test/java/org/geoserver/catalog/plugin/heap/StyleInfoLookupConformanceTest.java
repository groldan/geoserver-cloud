/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.heap;

import org.geoserver.catalog.plugin.repository.StyleRepository;
import org.geoserver.catalog.plugin.repository.StyleRepositoryConformanceTest;

public class StyleInfoLookupConformanceTest extends StyleRepositoryConformanceTest {

    protected @Override StyleRepository createRepository() {
        return new StyleInfoLookup();
    }
}
