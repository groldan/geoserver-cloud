/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.heap;

import org.geoserver.catalog.plugin.repository.StoreRepository;
import org.geoserver.catalog.plugin.repository.StoreRepositoryConformanceTest;

public class StoreInfoLookupConformanceTest extends StoreRepositoryConformanceTest {

    protected @Override StoreRepository createRepository() throws Exception {
        return new StoreInfoLookup();
    }
}
