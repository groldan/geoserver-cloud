/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.CatalogTestData;
import org.geoserver.catalog.Info;
import org.geoserver.catalog.MetadataMap;
import org.geoserver.catalog.Predicates;
import org.geoserver.catalog.impl.ClassMappings;
import org.geoserver.catalog.plugin.Patch;
import org.geoserver.catalog.plugin.PropertyDiff;
import org.geoserver.catalog.plugin.Query;
import org.geoserver.ows.util.OwsUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

/** */
public abstract class CatalogInfoRepositoryConformanceTest<
        T extends CatalogInfo, R extends CatalogInfoRepository<T>> {

    protected R repository;

    public @Rule CatalogTestData testData = CatalogTestData.empty();
    public @Rule TestName testName = new TestName();

    @Before
    public final void setUp() throws Exception {
        repository = createRepository();
    }

    protected abstract R createRepository() throws Exception;

    protected abstract Class<T> getInfoType();

    protected abstract T createOne(String name);

    @SuppressWarnings("unchecked")
    protected <D extends T> D add(D obj) {
        assertNotNull(obj.getId());
        repository.add(obj);
        return (D)
                repository
                        .findById(obj.getId(), getInfoType())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                String.format(
                                                        "%s(%s) not found once added",
                                                        getInfoType().getSimpleName(),
                                                        obj.getId())));
    }

    protected <S extends T> S createOne(String name, Class<S> type) {
        if (type.equals(getInfoType())) {
            return type.cast(createOne(name));
        }
        throw new UnsupportedOperationException(
                "Override to support creating instanceof of " + type.getSimpleName());
    }

    protected abstract List<String> getSortablePropertyNames();

    protected List<String> getExampleUnsortablePropertyNames() {
        return Arrays.asList("metadata");
    }

    /**
     * To be implemented for object types that support multiple instances with the same name (e.g.
     * same name, different workspace)
     */
    protected void testFindFirstByName() {
        final String namePrefix = this.testName.getMethodName() + "_";
        String name1 = namePrefix + "1";
        String name2 = namePrefix + "2";

        assertFalse(repository.findFirstByName(name1, getInfoType()).isPresent());
        assertFalse(repository.findFirstByName(name2, getInfoType()).isPresent());

        T info1 = createOne(name1);
        T info2 = createOne(name2);
        repository.add(info1);
        repository.add(info2);

        T found = repository.findFirstByName(name1, getInfoType()).orElseThrow();
        assertEquals(info1.getId(), found.getId());

        found = repository.findFirstByName(name2, getInfoType()).orElseThrow();
        assertEquals(info2.getId(), found.getId());
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.CatalogInfoRepository#findFirstByName(java.lang.String,
     * java.lang.Class)}.
     */
    public final @Test void findFirstByName() {
        testFindFirstByName();
    }

    /**
     * To be overridden for {@link CatalogInfo} types whose {@code equals()} implementation is
     * broken
     */
    protected void assertInfoEquals(T expected, T actual) {
        assertEquals(expected, actual);
    }

    protected T createOne() {
        return createOne(testName.getMethodName());
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.CatalogInfoRepository#getContentType()}.
     */
    public @Test void getContentType() {
        assertEquals(getInfoType(), repository.getContentType());
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.CatalogInfoRepository#add(org.geoserver.catalog.CatalogInfo)}.
     */
    public @Test void add_null() {
        assertThrows(NullPointerException.class, () -> repository.add(null));
    }

    public @Test void add_null_id() {
        T info = createOne();
        OwsUtils.set(info, "id", null);
        assertNull(info.getId());
        assertThrows(NullPointerException.class, () -> repository.add(info));
    }

    public @Test void add() {
        T info1 = createOne("1");
        T info2 = createOne("2");

        assertIds(repository);

        repository.add(info1);
        assertIds(repository, info1.getId());

        repository.add(info2);
        assertIds(repository, info1.getId(), info2.getId());
    }

    @Ignore("Legacy catalog expects object to be replaced")
    @Test(expected = IllegalArgumentException.class)
    public void add_duplicate_id() {
        final String dupId = "dupId";
        T info1 = createOne("1");
        T info2 = createOne("2");
        OwsUtils.set(info1, "id", dupId);
        OwsUtils.set(info2, "id", dupId);

        repository.add(info1);
        repository.add(info2);
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.CatalogInfoRepository#remove(org.geoserver.catalog.CatalogInfo)}.
     */
    public @Test void remove() {
        T info = createOne("remove");
        repository.add(info);

        assertTrue(repository.findById(info.getId(), getInfoType()).isPresent());
        repository.remove(info);
        assertFalse(repository.findById(info.getId(), getInfoType()).isPresent());
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.CatalogInfoRepository#update(org.geoserver.catalog.CatalogInfo,
     * org.geoserver.catalog.plugin.Patch)}.
     */
    public @Test void update() {
        T info1 = createOne("1");

        info1.setDateModified(null);
        repository.add(info1);

        Date dateModified = new Date();
        Patch patch1 =
                PropertyDiff.builder(info1).with("dateModified", dateModified).build().toPatch();
        repository.update(info1, patch1);
        T updated = repository.findById(info1.getId(), getInfoType()).orElseThrow();
        assertEquals(info1.getId(), updated.getId());
        assertEquals(dateModified, updated.getDateModified());
    }

    public @Test void update_metadataMap() {
        T info = createOne();
        MetadataMap metadataMap = OwsUtils.property(info, "metadata", MetadataMap.class);
        Assume.assumeTrue(null != metadataMap);

        metadataMap.put("stringKey", "string value");
        metadataMap.put("intKey", Integer.valueOf(1000));
        metadataMap.put("doubleKey", Double.MIN_VALUE);
        metadataMap.put("boolKey", Boolean.FALSE);
        repository.add(info);

        MetadataMap patchValue = new MetadataMap(metadataMap);
        patchValue.put("stringKey", "string value");
        patchValue.put("doubleKey", Double.MAX_VALUE);
        patchValue.put("boolKey", Boolean.TRUE);
        patchValue.put("newKey", "new key value");

        Patch patch = PropertyDiff.builder(info).with("metadata", patchValue).build().toPatch();
        repository.update(info, patch);
        T updated = repository.findById(info.getId(), getInfoType()).orElseThrow();
        assertEquals(info.getId(), updated.getId());

        MetadataMap updatedMap = OwsUtils.property(updated, "metadata", MetadataMap.class);
        assertEquals(patchValue, updatedMap);
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.CatalogInfoRepository#dispose()}.
     */
    public @Test void dispose() {
        repository.add(createOne());
        repository.dispose();
        repository.dispose(); // check for method's idempotency
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.CatalogInfoRepository#findAll()}.
     */
    public @Test void findAll() {
        try (Stream<T> s = repository.findAll()) {
            assertEquals(0, s.count());
        }
        T info1 = createOne("1");
        T info2 = createOne("2");
        T info3 = createOne("3");
        repository.add(info1);

        try (Stream<T> s = repository.findAll()) {
            assertEquals(1, s.count());
        }
        repository.add(info2);
        try (Stream<T> s = repository.findAll()) {
            assertEquals(2, s.count());
        }

        repository.add(info3);
        try (Stream<T> s = repository.findAll()) {
            assertEquals(3, s.count());
        }
        assertIds(repository, info1.getId(), info2.getId(), info3.getId());
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.CatalogInfoRepository#findAll(org.geoserver.catalog.plugin.Query)}.
     */
    public @Test void findAll_Query_All() {
        final Query<T> query = Query.all(getInfoType());
        T info1 = createOne("1");
        T info2 = createOne("2");
        T info3 = createOne("3");

        assertIds(repository);
        repository.add(info1);
        assertIds(repository.findAll(query), info1.getId());

        repository.add(info2);
        repository.add(info3);
        assertIds(repository.findAll(query), info1.getId(), info2.getId(), info3.getId());
    }

    public @Test void findAll_AllQuery() {
        try (Stream<T> found = repository.findAll(Query.all(getInfoType()))) {
            assertEquals(0L, found.count());
        }

        final String type = getInfoType().getSimpleName();
        T o1 = add(createOne(String.format("%s-1", type)));
        T o2 = add(createOne(String.format("%s-2", type)));
        T o3 = add(createOne(String.format("%s-3", type)));

        try (Stream<T> found = repository.findAll(Query.all(getInfoType()))) {
            List<String> ids = found.map(T::getId).collect(Collectors.toList());
            assertEquals(3, ids.size());
            assertEquals(Set.of(o1.getId(), o2.getId(), o3.getId()), Set.copyOf(ids));
        }
    }

    public @Test void findAll_Query_Filter() {
        final String type = getInfoType().getSimpleName();
        T o1 = add(createOne(String.format("%s-1", type)));
        T o2 = add(createOne(String.format("%s-2", type)));
        @SuppressWarnings("unused")
        T o3 = add(createOne(String.format("%s-3", type)));

        Filter filter =
                Predicates.or(
                        Predicates.equal("id", o1.getId()), Predicates.equal("id", o2.getId()));

        Query<T> query = Query.valueOf(getInfoType(), filter);

        try (Stream<T> found = repository.findAll(query)) {
            List<String> ids = found.map(T::getId).collect(Collectors.toList());
            assertEquals(2, ids.size());
            assertEquals(Set.of(o1.getId(), o2.getId()), Set.copyOf(ids));
        }
    }

    public @Test void findAll_Query_Filter_InstanceOf() {
        final Class<? extends Info>[] concreteTypes =
                ClassMappings.fromInterface(getInfoType()).concreteInterfaces();

        for (Class<? extends Info> c : concreteTypes) {
            @SuppressWarnings("unchecked")
            Class<? extends T> s = (Class<? extends T>) c;
            String name1 = s.getSimpleName() + "-1";
            String name2 = s.getSimpleName() + "-2";
            add(createOne(name1, s));
            add(createOne(name2, s));
        }

        for (Class<? extends Info> c : concreteTypes) {
            @SuppressWarnings("unchecked")
            Class<? extends T> s = (Class<? extends T>) c;
            Filter filter = Predicates.isInstanceOf(s);
            Query<? extends T> query = Query.valueOf(s, filter);
            try (Stream<? extends T> stream = repository.findAll(query)) {
                assertEquals(2, stream.filter(s::isInstance).count());
            }
        }

        Filter filter = Predicates.isInstanceOf(getInfoType());
        Query<T> query = Query.valueOf(getInfoType(), filter);
        try (Stream<T> stream = repository.findAll(query)) {
            assertEquals(2 * concreteTypes.length, stream.count());
        }
    }

    @Ignore("Implement findAll_Query_SortBy_SupportedProperties!!!")
    public @Test void findAll_Query_SortBy_SupportedProperties() {
        final List<String> sortablePropertyNames = this.getSortablePropertyNames();
        assertTrue(sortablePropertyNames.contains("id"));

        T info1 = add(createOne("1"));
        T info2 = add(createOne("2"));
        T info3 = add(createOne("3"));

        try (Stream<T> s = repository.findAll()) {
            s.collect(Collectors.toList());
        }

        for (String propName : sortablePropertyNames) {}

        SortBy[] sortOrder = null;
        Query<Info> valueOf = Query.valueOf(getInfoType(), null, null, null, sortOrder);

        fail("Not yet implemented");
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.CatalogInfoRepository#count(java.lang.Class,
     * org.opengis.filter.Filter)}.
     */
    public @Test void count() {
        T info1 = add(createOne("1"));
        assertIds(repository, info1.getId());

        T info2 = add(createOne("2"));
        assertIds(repository, info1.getId(), info2.getId());
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.CatalogInfoRepository#findById(java.lang.String,
     * java.lang.Class)}.
     */
    public @Test void findById() {
        T i1 = createOne("1");
        T i2 = createOne("2");
        T i3 = createOne("3");

        assertFalse(repository.findById(i1.getId(), getInfoType()).isPresent());

        i1 = add(i1);
        i2 = add(i2);
        i3 = add(i3);

        assertEquals(
                i1.getId(), repository.findById(i1.getId(), getInfoType()).orElseThrow().getId());
        assertEquals(
                i2.getId(), repository.findById(i2.getId(), getInfoType()).orElseThrow().getId());
        assertEquals(
                i3.getId(), repository.findById(i3.getId(), getInfoType()).orElseThrow().getId());
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.CatalogInfoRepository#canSortBy(java.lang.String)}.
     */
    public @Test void canSortBy() {
        for (String pname : getSortablePropertyNames()) {
            assertTrue(repository.canSortBy(pname));
        }
        for (String pname : getExampleUnsortablePropertyNames()) {
            assertFalse(repository.canSortBy(pname));
        }
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.CatalogInfoRepository#syncTo(org.geoserver.catalog.plugin.repository.CatalogInfoRepository)}.
     */
    public @Test void syncTo() throws Exception {
        T info1 = add(createOne("1"));
        T info2 = add(createOne("2"));
        T info3 = add(createOne("3"));

        CatalogInfoRepository<T> repository2 = createRepository();
        repository.syncTo(repository2);

        assertIds(repository2, info1.getId(), info2.getId(), info3.getId());
        assertIds(repository, info1.getId(), info2.getId(), info3.getId());
    }

    private void assertIds(CatalogInfoRepository<T> repository, @NonNull String... ids) {
        assertIds(repository.findAll(), ids);
    }

    private void assertIds(Stream<T> stream, @NonNull String... ids) {
        Set<String> expected = Arrays.stream(ids).collect(Collectors.toSet());
        Set<String> actual;
        try (Stream<T> s = stream) {
            actual = s.map(T::getId).collect(Collectors.toSet());
        }
        assertEquals(expected, actual);
    }
}
