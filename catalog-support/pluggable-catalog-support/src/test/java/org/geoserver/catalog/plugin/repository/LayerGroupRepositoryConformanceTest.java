/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.junit.Test;

/** */
public abstract class LayerGroupRepositoryConformanceTest
        extends CatalogInfoRepositoryConformanceTest<LayerGroupInfo, LayerGroupRepository> {

    private final @Getter Class<LayerGroupInfo> infoType = LayerGroupInfo.class;

    protected @Override LayerGroupInfo createOne(String name) {
        LayerGroupInfo lg = testData.createLayerGroup(name);
        lg.setWorkspace(null);
        return lg;
    }

    protected LayerGroupInfo createOne(String name, WorkspaceInfo ws) {
        LayerGroupInfo lg = createOne(name);
        lg.setWorkspace(ws);
        return lg;
    }

    protected LayerGroupInfo createAndAdd(String name, WorkspaceInfo ws) {
        LayerGroupInfo lg = createOne(name, ws);
        assertNotNull(lg.getId());
        return add(lg);
    }

    protected @Override List<String> getSortablePropertyNames() {
        return Arrays.asList(
                "id",
                "abstract",
                "title",
                "name",
                "type",
                "advertised",
                "enabled",
                "mode",
                "queryDisabled");
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.LayerGroupRepository#findByNameAndWorkspaceIsNull(java.lang.String)}.
     */
    public @Test void testFindByNameAndWorkspaceIsNull() {
        final String sameName = testName.getMethodName();
        LayerGroupInfo lgNoWorkspace = createAndAdd(sameName, null);
        LayerGroupInfo lgWs = createAndAdd(sameName, testData.workspaceB);

        assertNotEquals(lgNoWorkspace.getId(), lgWs.getId());
        Optional<LayerGroupInfo> found = repository.findByNameAndWorkspaceIsNull(sameName);
        assertEquals(lgNoWorkspace.getId(), found.orElse(null).getId());
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.LayerGroupRepository#findByNameAndWorkspace(java.lang.String,
     * org.geoserver.catalog.WorkspaceInfo)}.
     */
    public @Test void testFindByNameAndWorkspace() {
        final String sameName = testName.getMethodName();
        LayerGroupInfo lg1 = createAndAdd(sameName, testData.workspaceA);
        LayerGroupInfo lg2 = createAndAdd(sameName, testData.workspaceB);

        assertNotEquals(lg1.getId(), lg2.getId());
        assertEquals(
                lg1.getId(),
                repository
                        .findByNameAndWorkspace(sameName, testData.workspaceA)
                        .orElse(null)
                        .getId());
        assertEquals(
                lg2.getId(),
                repository
                        .findByNameAndWorkspace(sameName, testData.workspaceB)
                        .orElse(null)
                        .getId());
    }

    @Test(expected = NullPointerException.class)
    public void testFindByNameAndWorkspace_null_workspace() {
        repository.findByNameAndWorkspace("name", null);
    }

    @Test(expected = NullPointerException.class)
    public void testFindByNameAndWorkspace_null_name() {
        repository.findByNameAndWorkspace(null, testData.workspaceA);
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.LayerGroupRepository#findAllByWorkspaceIsNull()}.
     */
    public @Test void testFindAllByWorkspaceIsNull() {
        final String name1 = testName.getMethodName() + "-1";
        final String name2 = testName.getMethodName() + "-2";

        LayerGroupInfo lgNoWs1 = createAndAdd(name1, null);
        LayerGroupInfo lgNoWs2 = createAndAdd(name2, null);

        @SuppressWarnings("unused")
        LayerGroupInfo lgWs1 = createAndAdd(name1, testData.workspaceA);
        @SuppressWarnings("unused")
        LayerGroupInfo lgWs2 = createAndAdd(name2, testData.workspaceB);

        try (Stream<LayerGroupInfo> stream = repository.findAllByWorkspaceIsNull()) {
            List<String> ids = stream.map(LayerGroupInfo::getId).collect(Collectors.toList());
            assertEquals(2, ids.size());
            assertTrue(ids.contains(lgNoWs1.getId()));
            assertTrue(ids.contains(lgNoWs2.getId()));
        }
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.LayerGroupRepository#findAllByWorkspace(org.geoserver.catalog.WorkspaceInfo)}.
     */
    public @Test void testFindAllByWorkspace() {
        final String name1 = testName.getMethodName() + "-1";
        final String name2 = testName.getMethodName() + "-2";

        LayerGroupInfo lgNoWs1 = createAndAdd(name1, null);
        LayerGroupInfo lgNoWs2 = createAndAdd(name2, null);

        createAndAdd(name1, testData.workspaceA);
        createAndAdd(name2, testData.workspaceA);
        createAndAdd(name1, testData.workspaceB);
        createAndAdd(name2, testData.workspaceB);

        try (Stream<LayerGroupInfo> stream = repository.findAllByWorkspaceIsNull()) {
            List<String> ids = stream.map(LayerGroupInfo::getId).collect(Collectors.toList());
            assertEquals(2, ids.size());
            assertTrue(ids.contains(lgNoWs1.getId()));
            assertTrue(ids.contains(lgNoWs2.getId()));
        }
    }
}
