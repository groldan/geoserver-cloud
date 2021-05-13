/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.heap;

import java.util.Arrays;
import java.util.List;
import org.geoserver.catalog.plugin.repository.WorkspaceRepository;
import org.geoserver.catalog.plugin.repository.WorkspaceRepositoryConformanceTest;

public class WorkspaceInfoLookupConformanceTest extends WorkspaceRepositoryConformanceTest {

    protected @Override WorkspaceRepository createRepository() {
        return new WorkspaceInfoLookup();
    }

    protected List<String> getSortablePropertyNames() {
        return Arrays.asList("id", "name", "isolated");
    }
}
