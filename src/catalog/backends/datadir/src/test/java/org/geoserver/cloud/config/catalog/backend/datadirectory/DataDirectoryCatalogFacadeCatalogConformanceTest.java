/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.config.catalog.backend.datadirectory;

import org.geoserver.catalog.impl.CatalogImpl;
import org.geoserver.catalog.plugin.CatalogConformanceTest;
import org.geoserver.catalog.plugin.CatalogPlugin;
import org.geoserver.catalog.plugin.DefaultMemoryCatalogFacade;

class DataDirectoryCatalogFacadeCatalogConformanceTest extends CatalogConformanceTest {

    @Override
    protected CatalogImpl createCatalog() {
        var facade = new DataDirectoryCatalogFacade(new DefaultMemoryCatalogFacade());
        return new CatalogPlugin(facade);
    }
}
