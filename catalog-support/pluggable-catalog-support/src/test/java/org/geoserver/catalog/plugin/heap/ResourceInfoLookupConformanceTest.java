/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.heap;

import org.geoserver.catalog.plugin.repository.NamespaceRepository;
import org.geoserver.catalog.plugin.repository.ResourceRepository;
import org.geoserver.catalog.plugin.repository.ResourceRepositoryConformanceTest;
import org.geoserver.catalog.plugin.repository.StoreRepository;
import org.geoserver.catalog.plugin.repository.WorkspaceRepository;

public class ResourceInfoLookupConformanceTest extends ResourceRepositoryConformanceTest {

    protected @Override ResourceRepository createRepository() {
        LayerInfoLookup layerInfoLookup = new LayerInfoLookup();
        return new ResourceInfoLookup(layerInfoLookup);
    }

    protected @Override NamespaceRepository createNamespacesRepository() {
        return new NamespaceInfoLookup();
    }

    protected @Override WorkspaceRepository createWorkspacesRepository() {
        return new WorkspaceInfoLookup();
    }

    protected @Override StoreRepository createStoresRepository() {
        return new StoreInfoLookup();
    }
}
