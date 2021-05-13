/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.impl.NamespaceInfoImpl;
import org.geoserver.catalog.plugin.Patch;
import org.geoserver.catalog.plugin.PropertyDiff;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/** */
public abstract class NamespaceRepositoryConformanceTest
        extends CatalogInfoRepositoryConformanceTest<NamespaceInfo, NamespaceRepository> {

    private final @Getter Class<NamespaceInfo> infoType = NamespaceInfo.class;

    protected @Override NamespaceInfo createOne(String name) {
        return testData.createNamespace(name, "http://test.com/" + name);
    }

    protected NamespaceInfo createOneIsolated(String name) {
        NamespaceInfo ns = createOne(name);
        ns.setIsolated(true);
        return ns;
    }

    protected @Override List<String> getSortablePropertyNames() {
        return Arrays.asList("id", "prefix", "URI", "isolated");
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.CatalogInfoRepository#update(org.geoserver.catalog.CatalogInfo,
     * org.geoserver.catalog.plugin.Patch)}.
     *
     * <p>Overridden because {@link NamespaceInfoImpl#setDateModified setDateModified} is a no-op
     */
    @Override
    public @Test void update() {
        NamespaceInfo info1 = add(createOne("1"));

        String newPrefix = info1.getPrefix() + "_modified";
        Patch patch1 = PropertyDiff.builder(info1).with("prefix", newPrefix).build().toPatch();
        repository.update(info1, patch1);
        NamespaceInfo updated = repository.findById(info1.getId(), getInfoType()).orElseThrow();
        assertEquals(info1.getId(), updated.getId());
        assertEquals(newPrefix, updated.getPrefix());
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.NamespaceRepository#setDefaultNamespace} and {@link
     * org.geoserver.catalog.plugin.repository.NamespaceRepository#getDefaultNamespace
     */
    public @Test void testSetDefaultNamespace() {
        NamespaceInfo ns1 = add(createOne("ns1"));
        NamespaceInfo ns2 = add(createOne("ns2"));

        assertFalse(repository.getDefaultNamespace().isPresent());

        repository.setDefaultNamespace(ns1);
        assertEquals(ns1.getId(), repository.getDefaultNamespace().orElse(null).getId());

        repository.setDefaultNamespace(ns2);
        assertEquals(ns2.getId(), repository.getDefaultNamespace().orElse(null).getId());
    }

    @Test(expected = NoSuchElementException.class)
    public void testSetDefaultNamespace_non_existent() {
        NamespaceInfo unsaved = createOne("ns1");
        repository.setDefaultNamespace(unsaved);
    }

    @Test(expected = NullPointerException.class)
    public void testSetDefaultNamespace_null() {
        repository.setDefaultNamespace(null);
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.NamespaceRepository#unsetDefaultNamespace} and {@link
     * org.geoserver.catalog.plugin.repository.NamespaceRepository#getDefaultNamespace}.
     */
    public @Test void testUnsetDefaultNamespace() {
        NamespaceInfo ns1 = add(createOne("ns1"));
        add(createOne("ns2"));

        assertFalse(repository.getDefaultNamespace().isPresent());
        repository.setDefaultNamespace(ns1);
        assertEquals(ns1.getId(), repository.getDefaultNamespace().orElse(null).getId());
        repository.unsetDefaultNamespace();
        assertFalse(repository.getDefaultNamespace().isPresent());
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.NamespaceRepository#findOneByURI(java.lang.String)}.
     */
    public @Test void testFindOneByURI() {
        final String sameUri = "http://same.uri";
        NamespaceInfo ns1 = createOne("ns1");
        NamespaceInfo ns2 = createOne("ns2");
        NamespaceInfo ns3 = createOne("ns3");

        ns1.setIsolated(true);
        ns2.setIsolated(true);
        ns1.setURI(sameUri);
        ns2.setURI(sameUri);

        add(ns1);
        add(ns2);
        add(ns3);

        assertFalse(repository.findOneByURI("http://not.found").isPresent());

        assertEquals(ns3.getId(), repository.findOneByURI(ns3.getURI()).orElse(null).getId());

        Optional<NamespaceInfo> found = repository.findOneByURI(sameUri);
        assertTrue(found.isPresent());
        MatcherAssert.assertThat(
                found.get().getId(), Matchers.in(Arrays.asList(ns1.getId(), ns2.getId())));
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.NamespaceRepository#findAllByURI(java.lang.String)}.
     */
    public @Test void testFindAllByURI() {
        final String sameUri = "http://same.uri";
        NamespaceInfo ns1 = createOne("ns1");
        NamespaceInfo ns2 = createOne("ns2");
        NamespaceInfo ns3 = createOne("ns3");

        ns1.setIsolated(true);
        ns2.setIsolated(true);
        ns1.setURI(sameUri);
        ns2.setURI(sameUri);

        add(ns1);
        add(ns2);
        add(ns3);

        try (Stream<NamespaceInfo> found = repository.findAllByURI(sameUri)) {
            Set<String> expected = Set.of(ns1.getId(), ns2.getId());
            Set<String> actual = found.map(NamespaceInfo::getId).collect(Collectors.toSet());
            assertEquals(expected, actual);
        }
    }
}
