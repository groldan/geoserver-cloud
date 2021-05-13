/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.WMSStoreInfo;
import org.geoserver.catalog.WMTSStoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.lang.Nullable;

/** */
public abstract class StoreRepositoryConformanceTest
        extends CatalogInfoRepositoryConformanceTest<StoreInfo, StoreRepository> {

    private final @Getter Class<StoreInfo> infoType = StoreInfo.class;

    protected WorkspaceInfo workspace;

    public @Before void setupTestWorkspace() {
        String name = testName.getMethodName();
        workspace = testData.createWorkspace(name);
    }

    protected @Override List<String> getSortablePropertyNames() {
        return Arrays.asList("id", "name", "type");
    }

    protected @Override StoreInfo createOne(String name) {
        return testData.createDataStore(name, workspace);
    }

    protected @Override <S extends StoreInfo> S createOne(String name, Class<S> type) {
        if (type.equals(CoverageStoreInfo.class)) {
            return type.cast(testData.createCoverageStore(name));
        }
        if (type.equals(DataStoreInfo.class)) {
            return type.cast(testData.createDataStore(name));
        }
        if (type.equals(WMSStoreInfo.class)) {
            return type.cast(testData.createWebMapServer(name));
        }
        if (type.equals(WMTSStoreInfo.class)) {
            return type.cast(testData.createWebMapTileServer(name));
        }
        return type.cast(super.createOne(name, type));
    }

    protected <S extends StoreInfo> S createAndAdd(String name, Class<S> type) {
        return add(createOne(name, type));
    }

    protected <S extends StoreInfo> S createAndAdd(
            String name, Class<S> type, WorkspaceInfo workspace) {
        S store = createOne(name, type);
        store.setWorkspace(workspace);
        return add(store);
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.StoreRepository#setDefaultDataStore(org.geoserver.catalog.WorkspaceInfo,
     * org.geoserver.catalog.DataStoreInfo)} and {@link
     * org.geoserver.catalog.plugin.repository.StoreRepository#getDefaultDataStore(org.geoserver.catalog.WorkspaceInfo)}.
     */
    public @Test void testSetDefaultDataStore() {
        WorkspaceInfo ws1 = testData.workspaceA;
        WorkspaceInfo ws2 = testData.workspaceB;

        DataStoreInfo store1 = createAndAdd("store1", DataStoreInfo.class, ws1);
        DataStoreInfo store2 = createAndAdd("store2", DataStoreInfo.class, ws2);

        assertFalse(repository.getDefaultDataStore(ws1).isPresent());
        repository.setDefaultDataStore(ws1, store1);
        assertEquals(store1.getId(), repository.getDefaultDataStore(ws1).orElse(null).getId());

        assertFalse(repository.getDefaultDataStore(ws2).isPresent());
        repository.setDefaultDataStore(ws2, store2);
        assertEquals(store2.getId(), repository.getDefaultDataStore(ws2).orElse(null).getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDefaultDataStore_store_does_not_belong_to_workspace() {
        WorkspaceInfo ws1 = testData.workspaceA;
        WorkspaceInfo ws2 = testData.workspaceB;
        DataStoreInfo store1 = createAndAdd("store1", DataStoreInfo.class, ws1);

        repository.setDefaultDataStore(ws2, store1);
    }

    @Test(expected = NullPointerException.class)
    public void testSetDefaultDataStore_null_workspace() {
        repository.setDefaultDataStore(null, testData.dataStoreA);
    }

    @Test(expected = NullPointerException.class)
    public void testSetDefaultDataStore_null_store() {
        repository.setDefaultDataStore(testData.workspaceA, null);
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.StoreRepository#unsetDefaultDataStore(org.geoserver.catalog.WorkspaceInfo)}
     * and {@link
     * org.geoserver.catalog.plugin.repository.StoreRepository#getDefaultDataStore(org.geoserver.catalog.WorkspaceInfo)}.
     */
    public @Test void testUnsetDefaultDataStore() {
        WorkspaceInfo ws1 = workspace;
        WorkspaceInfo ws2 = testData.workspaceB;

        DataStoreInfo store1 = createAndAdd("store1", DataStoreInfo.class, ws1);
        DataStoreInfo store2 = createAndAdd("store1", DataStoreInfo.class, ws2);

        repository.setDefaultDataStore(ws1, store1);
        repository.setDefaultDataStore(ws2, store2);

        assertEquals(store1.getId(), repository.getDefaultDataStore(ws1).orElse(null).getId());
        assertEquals(store2.getId(), repository.getDefaultDataStore(ws2).orElse(null).getId());

        repository.unsetDefaultDataStore(ws2);

        assertEquals(store1.getId(), repository.getDefaultDataStore(ws1).orElse(null).getId());

        repository.unsetDefaultDataStore(ws1);

        assertFalse(repository.getDefaultDataStore(ws1).isPresent());
    }

    @Test(expected = NullPointerException.class)
    public void testUnsetDefaultDataStore_null_workspace() {
        repository.unsetDefaultDataStore(null);
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.StoreRepository#getDefaultDataStores()}.
     */
    public @Test void testGetDefaultDataStores() {
        WorkspaceInfo ws1 = testData.workspaceA;
        WorkspaceInfo ws2 = testData.workspaceB;
        DataStoreInfo store1 = createAndAdd("store1", DataStoreInfo.class, ws1);
        DataStoreInfo store2 = createAndAdd("store2", DataStoreInfo.class, ws2);

        assertGetDefaultDataStores();
        repository.setDefaultDataStore(ws1, store1);
        assertGetDefaultDataStores(store1);

        repository.setDefaultDataStore(ws2, store2);
        assertGetDefaultDataStores(store1, store2);

        repository.unsetDefaultDataStore(ws1);
        assertGetDefaultDataStores(store2);

        repository.unsetDefaultDataStore(ws2);
        assertGetDefaultDataStores();
    }

    private void assertGetDefaultDataStores(DataStoreInfo... expected) {
        Set<String> expectedIds =
                Arrays.stream(expected).map(StoreInfo::getId).collect(Collectors.toSet());
        try (Stream<String> ids = repository.getDefaultDataStores().map(StoreInfo::getId)) {
            assertEquals(expectedIds, ids.collect(Collectors.toSet()));
        }
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.StoreRepository#findAllByWorkspace(org.geoserver.catalog.WorkspaceInfo,
     * java.lang.Class)}.
     */
    public @Test void testFindAllByWorkspace() {
        WorkspaceInfo ws1 = testData.workspaceA;
        WorkspaceInfo ws2 = testData.workspaceB;

        final String name1 = "store1";
        final String name2 = "store2";

        testFindAllByWorkspace(ws1, StoreInfo.class);
        testFindAllByWorkspace(ws2, StoreInfo.class);

        DataStoreInfo ws1s1 = createAndAdd(name1, DataStoreInfo.class, ws1);
        CoverageStoreInfo ws1s2 = createAndAdd(name2, CoverageStoreInfo.class, ws1);

        CoverageStoreInfo ws2s1 = createAndAdd(name1, CoverageStoreInfo.class, ws2);
        DataStoreInfo ws2s2 = createAndAdd(name2, DataStoreInfo.class, ws2);

        testFindAllByWorkspace(ws1, StoreInfo.class, ws1s1, ws1s2);
        testFindAllByWorkspace(ws1, DataStoreInfo.class, ws1s1);
        testFindAllByWorkspace(ws1, CoverageStoreInfo.class, ws1s2);

        testFindAllByWorkspace(ws2, StoreInfo.class, ws2s1, ws2s2);
        testFindAllByWorkspace(ws2, DataStoreInfo.class, ws2s2);
        testFindAllByWorkspace(ws2, CoverageStoreInfo.class, ws2s1);
    }

    private void testFindAllByWorkspace(
            WorkspaceInfo ws, Class<? extends StoreInfo> type, StoreInfo... expected) {

        Set<String> expectedIds =
                Arrays.stream(expected).map(StoreInfo::getId).collect(Collectors.toSet());
        try (Stream<String> ids = repository.findAllByWorkspace(ws, type).map(StoreInfo::getId)) {
            assertEquals(expectedIds, ids.collect(Collectors.toSet()));
        }
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.StoreRepository#findAllByType(java.lang.Class)}.
     */
    public @Test void testFindAllByType() {
        WorkspaceInfo ws1 = testData.workspaceA;
        WorkspaceInfo ws2 = testData.workspaceB;

        final String name1 = "store1";
        final String name2 = "store2";

        testFindAllByType(StoreInfo.class);

        DataStoreInfo ws1s1 = createAndAdd(name1, DataStoreInfo.class, ws1);
        CoverageStoreInfo ws1s2 = createAndAdd(name2, CoverageStoreInfo.class, ws1);

        CoverageStoreInfo ws2s1 = createAndAdd(name1, CoverageStoreInfo.class, ws2);
        DataStoreInfo ws2s2 = createAndAdd(name2, DataStoreInfo.class, ws2);

        testFindAllByType(StoreInfo.class, ws1s1, ws1s2, ws2s1, ws2s2);
        testFindAllByType(DataStoreInfo.class, ws1s1, ws2s2);
        testFindAllByType(CoverageStoreInfo.class, ws1s2, ws2s1);
    }

    private void testFindAllByType(Class<? extends StoreInfo> type, StoreInfo... expected) {
        Set<String> expectedIds =
                Arrays.stream(expected).map(StoreInfo::getId).collect(Collectors.toSet());
        try (Stream<String> ids = repository.findAllByType(type).map(StoreInfo::getId)) {
            assertEquals(expectedIds, ids.collect(Collectors.toSet()));
        }
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.StoreRepository#findByNameAndWorkspace(java.lang.String,
     * org.geoserver.catalog.WorkspaceInfo, java.lang.Class)}.
     */
    public @Test void testFindByNameAndWorkspace() {
        WorkspaceInfo ws1 = testData.workspaceA;
        WorkspaceInfo ws2 = testData.workspaceB;

        final String name1 = "store1";
        final String name2 = "store2";

        DataStoreInfo ws1s1 = createAndAdd(name1, DataStoreInfo.class, ws1);
        CoverageStoreInfo ws1s2 = createAndAdd(name2, CoverageStoreInfo.class, ws1);

        CoverageStoreInfo ws2s1 = createAndAdd(name1, CoverageStoreInfo.class, ws2);
        DataStoreInfo ws2s2 = createAndAdd(name2, DataStoreInfo.class, ws2);

        testFindByNameAndWorkspace(name1, ws1, ws1s1, StoreInfo.class);
        testFindByNameAndWorkspace(name1, ws1, ws1s1, DataStoreInfo.class);
        testFindByNameAndWorkspace(name1, ws1, null, CoverageStoreInfo.class);

        testFindByNameAndWorkspace(name1, ws2, ws2s1, StoreInfo.class);
        testFindByNameAndWorkspace(name1, ws2, null, DataStoreInfo.class);
        testFindByNameAndWorkspace(name1, ws2, ws2s1, CoverageStoreInfo.class);

        testFindByNameAndWorkspace(name2, ws1, ws1s2, StoreInfo.class);
        testFindByNameAndWorkspace(name2, ws1, null, DataStoreInfo.class);
        testFindByNameAndWorkspace(name2, ws1, ws1s2, CoverageStoreInfo.class);

        testFindByNameAndWorkspace(name2, ws2, ws2s2, StoreInfo.class);
        testFindByNameAndWorkspace(name2, ws2, ws2s2, DataStoreInfo.class);
        testFindByNameAndWorkspace(name2, ws2, null, CoverageStoreInfo.class);
    }

    private void testFindByNameAndWorkspace(
            String name,
            WorkspaceInfo ws,
            @Nullable StoreInfo expected,
            Class<? extends StoreInfo> queryType) {

        Optional<? extends StoreInfo> found =
                repository.findByNameAndWorkspace(name, ws, queryType);
        if (expected == null) {
            assertFalse(found.isPresent());
        } else {
            assertTrue("expected " + expected.getId(), found.isPresent());
            StoreInfo store = found.get();
            assertEquals(expected.getId(), store.getId());
            assertNotNull(store.getWorkspace());
            assertEquals(ws.getId(), store.getWorkspace().getId());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFindByNameAndWorkspace_null_name() {
        repository.findByNameAndWorkspace(null, workspace, StoreInfo.class);
    }

    @Test(expected = NullPointerException.class)
    public void testFindByNameAndWorkspace_null_workspace() {
        repository.findByNameAndWorkspace("name", null, StoreInfo.class);
    }

    @Test(expected = NullPointerException.class)
    public void testFindByNameAndWorkspace_null_type() {
        repository.findByNameAndWorkspace("name", workspace, null);
    }
}
