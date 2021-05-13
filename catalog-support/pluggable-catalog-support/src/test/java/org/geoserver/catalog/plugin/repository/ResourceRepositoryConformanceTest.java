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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.catalog.WMTSLayerInfo;
import org.geoserver.catalog.impl.ResourceInfoImpl;
import org.geoserver.catalog.plugin.Patch;
import org.geoserver.catalog.plugin.PropertyDiff;
import org.junit.Test;
import org.springframework.lang.Nullable;

/** */
public abstract class ResourceRepositoryConformanceTest
        extends CatalogInfoRepositoryConformanceTest<ResourceInfo, ResourceRepository> {

    private final @Getter Class<ResourceInfo> infoType = ResourceInfo.class;

    private NamespaceRepository namespaces;
    private WorkspaceRepository workspaces;
    private StoreRepository stores;

    private NamespaceInfo ns1, ns2;
    private DataStoreInfo ds1, ds2;
    private CoverageStoreInfo cs1, cs2;

    private void setUpAuxiliaryRepos() {
        this.namespaces = createNamespacesRepository();
        this.workspaces = createWorkspacesRepository();
        this.stores = createStoresRepository();

        workspaces.add(testData.workspaceA);
        workspaces.add(testData.workspaceB);

        ns1 = testData.namespaceA;
        ns2 = testData.namespaceB;
        ds1 = testData.createDataStore("ds1", testData.workspaceA);
        ds2 = testData.createDataStore("ds2", testData.workspaceB);
        cs1 = testData.createCoverageStore("cs1", testData.workspaceA);
        cs2 = testData.createCoverageStore("cs2", testData.workspaceB);

        namespaces.add(ns1);
        namespaces.add(ns2);
        stores.add(ds1);
        stores.add(ds2);
        stores.add(cs1);
        stores.add(cs2);
    }

    protected abstract NamespaceRepository createNamespacesRepository();

    protected abstract WorkspaceRepository createWorkspacesRepository();

    protected abstract StoreRepository createStoresRepository();

    protected @Override ResourceInfo createOne(String name) {
        return testData.createFeatureType(name);
    }

    protected @Override <S extends ResourceInfo> S createOne(String name, Class<S> type) {
        if (type.equals(CoverageInfo.class)) {
            return type.cast(testData.createCoverage(name));
        } else if (type.equals(FeatureTypeInfo.class)) {
            return type.cast(testData.createFeatureType(name));
        } else if (type.equals(WMSLayerInfo.class)) {
            return type.cast(testData.createWMSLayer(name));
        } else if (type.equals(WMTSLayerInfo.class)) {
            return type.cast(testData.createWMTSLayer(name));
        }
        return type.cast(super.createOne(name, type));
    }

    protected @Override List<String> getSortablePropertyNames() {
        return Arrays.asList(
                "id", "name", "nativeName", "title", "description", "advertised", "enabled");
    }

    /**
     * Test method for {@link ResourceRepository#update(ResourceInfo, Patch)}.
     *
     * <p>Overridden because {@link ResourceInfoImpl#setDateModified setDateModified} is a no-op
     */
    @Override
    public @Test void update() {
        setUpAuxiliaryRepos();
        FeatureTypeInfo ft1 = add(testData.createFeatureType("ft", ds1, ns1));
        CoverageInfo c1 = add(testData.createCoverage("cov", cs1, ns1));
        testUpdate(ft1);
        testUpdate(c1);
    }

    private void testUpdate(ResourceInfo r) {
        r.setTitle("new title");
        r.setAbstract("new abstract");
        r.setEnabled(false);

        Patch patch =
                PropertyDiff.builder(r)
                        .with("title", "new title")
                        .with("abstract", "new abstract")
                        .with("enabled", false)
                        .build()
                        .toPatch();

        repository.update(r, patch);
        ResourceInfo updated = repository.findById(r.getId(), getInfoType()).orElseThrow();
        assertEquals(r.getId(), updated.getId());
        assertFalse(r.isEnabled());
        assertEquals("new title", r.getTitle());
        assertEquals("new abstract", r.getAbstract());
    }

    @Test(expected = NullPointerException.class)
    public void testFindByNameAndNamespace_null_name() {
        repository.findByNameAndNamespace(null, ns1, ResourceInfo.class);
    }

    @Test(expected = NullPointerException.class)
    public void testFindByNameAndNamespace_null_namespace() {
        repository.findByNameAndNamespace("name", null, ResourceInfo.class);
    }

    @Test(expected = NullPointerException.class)
    public void testFindByNameAndNamespace_null_type() {
        repository.findByNameAndNamespace("name", ns1, null);
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.ResourceRepository#findByNameAndNamespace(java.lang.String,
     * org.geoserver.catalog.NamespaceInfo, java.lang.Class)}.
     */
    public @Test void testFindByNameAndNamespace() {
        setUpAuxiliaryRepos();
        FeatureTypeInfo ft1 = add(testData.createFeatureType("ft", ds1, ns1));
        FeatureTypeInfo ft2 = add(testData.createFeatureType("ft", ds2, ns2));

        CoverageInfo c1 = add(testData.createCoverage("cov", cs1, ns1));
        CoverageInfo c2 = add(testData.createCoverage("cov", cs2, ns2));

        assertFindByNameAndNamespace("ft", ns1, ResourceInfo.class, ft1);
        assertFindByNameAndNamespace("ft", ns1, FeatureTypeInfo.class, ft1);
        assertFindByNameAndNamespace("ft", ns1, CoverageInfo.class, null);

        assertFindByNameAndNamespace("ft", ns2, ResourceInfo.class, ft2);
        assertFindByNameAndNamespace("ft", ns2, FeatureTypeInfo.class, ft2);
        assertFindByNameAndNamespace("ft", ns2, CoverageInfo.class, null);

        assertFindByNameAndNamespace("cov", ns1, ResourceInfo.class, c1);
        assertFindByNameAndNamespace("cov", ns1, FeatureTypeInfo.class, null);
        assertFindByNameAndNamespace("cov", ns1, CoverageInfo.class, c1);

        assertFindByNameAndNamespace("cov", ns2, ResourceInfo.class, c2);
        assertFindByNameAndNamespace("cov", ns2, FeatureTypeInfo.class, null);
        assertFindByNameAndNamespace("cov", ns2, CoverageInfo.class, c2);
    }

    private <R extends ResourceInfo> void assertFindByNameAndNamespace(
            String name, NamespaceInfo ns, Class<R> type, @Nullable R expected) {

        Optional<R> found = repository.findByNameAndNamespace(name, ns, type);
        if (expected == null) {
            assertFalse(found.isPresent());
        } else {
            assertTrue(found.isPresent());
            assertEquals(expected.getId(), found.get().getId());
            assertEquals(ns.getId(), found.get().getNamespace().getId());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFindAllByType_null_type() {
        repository.findAllByType(null);
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.ResourceRepository#findAllByType(java.lang.Class)}.
     */
    public @Test void testFindAllByType() {
        setUpAuxiliaryRepos();

        FeatureTypeInfo ds1ft1 = add(testData.createFeatureType("ft1", ds1, ns1));
        FeatureTypeInfo ds1ft2 = add(testData.createFeatureType("ft2", ds1, ns1));
        FeatureTypeInfo ds2ft1 = add(testData.createFeatureType("ft1", ds2, ns2));

        CoverageInfo cs1c1 = add(testData.createCoverage("cov1", cs1, ns1));
        CoverageInfo cs1c2 = add(testData.createCoverage("cov2", cs1, ns1));
        CoverageInfo cs2c1 = add(testData.createCoverage("cov1", cs2, ns2));

        assertFindAllByType(ResourceInfo.class, ds1ft1, ds1ft2, ds2ft1, cs1c1, cs1c2, cs2c1);
        assertFindAllByType(FeatureTypeInfo.class, ds1ft1, ds1ft2, ds2ft1);
        assertFindAllByType(CoverageInfo.class, cs1c1, cs1c2, cs2c1);
    }

    private void assertFindAllByType(Class<? extends ResourceInfo> type, ResourceInfo... expected) {
        final Set<String> ids =
                Arrays.stream(expected).map(ResourceInfo::getId).collect(Collectors.toSet());

        try (Stream<? extends ResourceInfo> found = repository.findAllByType(type)) {
            Map<String, ? extends ResourceInfo> res =
                    found.collect(Collectors.toMap(ResourceInfo::getId, Function.identity()));
            assertEquals(ids, res.keySet());
        }
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.ResourceRepository#findAllByNamespace(org.geoserver.catalog.NamespaceInfo,
     * java.lang.Class)}.
     */
    public @Test void testFindAllByNamespace() {
        setUpAuxiliaryRepos();
        FeatureTypeInfo ft1 = add(testData.createFeatureType("ft", ds1, ns1));
        FeatureTypeInfo ft2 = add(testData.createFeatureType("ft", ds2, ns2));

        CoverageInfo c1 = add(testData.createCoverage("cov", cs1, ns1));
        CoverageInfo c2 = add(testData.createCoverage("cov", cs2, ns2));

        assertFindAllByNamespace(ns1, ResourceInfo.class, ft1, c1);
        assertFindAllByNamespace(ns1, FeatureTypeInfo.class, ft1);
        assertFindAllByNamespace(ns1, CoverageInfo.class, c1);

        assertFindAllByNamespace(ns2, ResourceInfo.class, ft2, c2);
        assertFindAllByNamespace(ns2, FeatureTypeInfo.class, ft2);
        assertFindAllByNamespace(ns2, CoverageInfo.class, c2);
    }

    private void assertFindAllByNamespace(
            NamespaceInfo ns, Class<? extends ResourceInfo> type, ResourceInfo... expected) {

        try (Stream<? extends ResourceInfo> found = repository.findAllByNamespace(ns, type)) {
            List<? extends ResourceInfo> all = found.collect(Collectors.toList());

            Set<String> ids =
                    Arrays.stream(expected).map(ResourceInfo::getId).collect(Collectors.toSet());

            Set<String> actual = all.stream().map(ResourceInfo::getId).collect(Collectors.toSet());

            assertEquals(ids, actual);
            all.forEach(r -> assertEquals(ns.getId(), r.getNamespace().getId()));
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFindByStoreAndName_null_store() {
        repository.findByStoreAndName(null, "name", ResourceInfo.class);
    }

    @Test(expected = NullPointerException.class)
    public void testFindByStoreAndName_null_name() {
        repository.findByStoreAndName(ds1, null, ResourceInfo.class);
    }

    @Test(expected = NullPointerException.class)
    public void testFindByStoreAndName_null_type() {
        repository.findByStoreAndName(ds1, "name", null);
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.ResourceRepository#findByStoreAndName(org.geoserver.catalog.StoreInfo,
     * java.lang.String, java.lang.Class)}.
     */
    public @Test void testFindByStoreAndName() {
        setUpAuxiliaryRepos();

        FeatureTypeInfo ds1ft1 = add(testData.createFeatureType("ft1", ds1, ns1));
        FeatureTypeInfo ds1ft2 = add(testData.createFeatureType("ft2", ds1, ns1));
        FeatureTypeInfo ds2ft1 = add(testData.createFeatureType("ft1", ds2, ns2));

        CoverageInfo cs1c1 = add(testData.createCoverage("cov1", cs1, ns1));
        CoverageInfo cs1c2 = add(testData.createCoverage("cov2", cs1, ns1));
        CoverageInfo cs2c1 = add(testData.createCoverage("cov1", cs2, ns2));

        assertFindByStoreAndName(ds1, "ft1", ResourceInfo.class, ds1ft1);
        assertFindByStoreAndName(ds1, "ft1", FeatureTypeInfo.class, ds1ft1);
        assertFindByStoreAndName(ds1, "ft1", CoverageInfo.class, null);

        assertFindByStoreAndName(ds1, "ft2", ResourceInfo.class, ds1ft2);
        assertFindByStoreAndName(ds1, "ft2", FeatureTypeInfo.class, ds1ft2);
        assertFindByStoreAndName(ds1, "ft2", CoverageInfo.class, null);

        assertFindByStoreAndName(ds2, "ft1", ResourceInfo.class, ds2ft1);
        assertFindByStoreAndName(ds2, "ft1", FeatureTypeInfo.class, ds2ft1);
        assertFindByStoreAndName(ds2, "ft1", CoverageInfo.class, null);

        assertFindByStoreAndName(cs1, "cov1", ResourceInfo.class, cs1c1);
        assertFindByStoreAndName(cs1, "cov1", FeatureTypeInfo.class, null);
        assertFindByStoreAndName(cs1, "cov1", CoverageInfo.class, cs1c1);

        assertFindByStoreAndName(cs1, "cov2", ResourceInfo.class, cs1c2);
        assertFindByStoreAndName(cs1, "cov2", FeatureTypeInfo.class, null);
        assertFindByStoreAndName(cs1, "cov2", CoverageInfo.class, cs1c2);

        assertFindByStoreAndName(cs2, "cov1", ResourceInfo.class, cs2c1);
        assertFindByStoreAndName(cs2, "cov1", FeatureTypeInfo.class, null);
        assertFindByStoreAndName(cs2, "cov1", CoverageInfo.class, cs2c1);
    }

    private void assertFindByStoreAndName(
            StoreInfo store,
            String name,
            Class<? extends ResourceInfo> type,
            @Nullable ResourceInfo expected) {

        Optional<? extends ResourceInfo> found = repository.findByStoreAndName(store, name, type);
        if (expected == null) {
            assertFalse(found.isPresent());
        } else {
            ResourceInfo res = found.get();
            assertTrue(type.isInstance(res));
            assertEquals(expected.getId(), res.getId());
            assertNotNull(res.getStore());
            assertEquals(store.getId(), res.getStore().getId());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFindAllByStore_null_store() {
        repository.findAllByStore(null, ResourceInfo.class);
    }

    @Test(expected = NullPointerException.class)
    public void testFindAllByStore_null_type() {
        repository.findAllByStore(ds1, null);
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.ResourceRepository#findAllByStore(org.geoserver.catalog.StoreInfo,
     * java.lang.Class)}.
     */
    public @Test void testFindAllByStore() {
        setUpAuxiliaryRepos();

        FeatureTypeInfo ds1ft1 = add(testData.createFeatureType("ft1", ds1, ns1));
        FeatureTypeInfo ds1ft2 = add(testData.createFeatureType("ft2", ds1, ns1));

        FeatureTypeInfo ds2ft1 = add(testData.createFeatureType("ft1", ds2, ns2));

        CoverageInfo cs1c1 = add(testData.createCoverage("cov1", cs1, ns1));
        CoverageInfo cs1c2 = add(testData.createCoverage("cov2", cs1, ns1));

        CoverageInfo cs2c1 = add(testData.createCoverage("cov1", cs2, ns2));

        assertFindAllByStore(ds1, ResourceInfo.class, ds1ft1, ds1ft2);
        assertFindAllByStore(ds1, FeatureTypeInfo.class, ds1ft1, ds1ft2);
        assertFindAllByStore(ds1, CoverageInfo.class);

        assertFindAllByStore(cs1, ResourceInfo.class, cs1c1, cs1c2);
        assertFindAllByStore(cs1, FeatureTypeInfo.class);
        assertFindAllByStore(cs1, CoverageInfo.class, cs1c1, cs1c2);

        assertFindAllByStore(ds2, ResourceInfo.class, ds2ft1);
        assertFindAllByStore(ds2, FeatureTypeInfo.class, ds2ft1);
        assertFindAllByStore(ds2, CoverageInfo.class);

        assertFindAllByStore(cs2, ResourceInfo.class, cs2c1);
        assertFindAllByStore(cs2, FeatureTypeInfo.class);
        assertFindAllByStore(cs2, CoverageInfo.class, cs2c1);
    }

    private void assertFindAllByStore(
            StoreInfo store, Class<? extends ResourceInfo> type, ResourceInfo... expected) {

        final Set<String> ids =
                Arrays.stream(expected).map(ResourceInfo::getId).collect(Collectors.toSet());
        try (Stream<? extends ResourceInfo> found = repository.findAllByStore(store, type)) {
            Map<String, ? extends ResourceInfo> res =
                    found.collect(Collectors.toMap(ResourceInfo::getId, Function.identity()));
            assertEquals(ids, res.keySet());
        }
    }
}
