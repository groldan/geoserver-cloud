/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.junit.Test;

/** */
public abstract class StyleRepositoryConformanceTest
        extends CatalogInfoRepositoryConformanceTest<StyleInfo, StyleRepository> {

    private final @Getter Class<StyleInfo> infoType = StyleInfo.class;

    protected @Override StyleInfo createOne(String name) {
        return testData.createStyle(name);
    }

    protected StyleInfo createOne(String name, WorkspaceInfo ws) {
        return testData.createStyle(name, ws);
    }

    protected @Override List<String> getSortablePropertyNames() {
        return Arrays.asList("id", "format", "name", "filename");
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.StyleRepository#findAllByNullWorkspace()}.
     */
    public @Test void testFindAllByNullWorkspace() {
        WorkspaceInfo ws1 = testData.workspaceA;

        @SuppressWarnings("unused")
        StyleInfo ws1s1 = add(createOne("s1", ws1));
        @SuppressWarnings("unused")
        StyleInfo ws1s2 = add(createOne("s2", ws1));

        try (Stream<StyleInfo> all = repository.findAllByNullWorkspace()) {
            assertEquals(0L, all.count());
        }

        StyleInfo noWsS1 = add(createOne("s1"));
        StyleInfo noWsS2 = add(createOne("s2"));

        try (Stream<StyleInfo> all = repository.findAllByNullWorkspace()) {
            List<StyleInfo> found = all.collect(Collectors.toList());
            Set<String> ids = Set.of(noWsS1.getId(), noWsS2.getId());
            assertEquals(2, found.size());
            found.forEach(s -> assertTrue(ids.contains(s.getId())));
            found.forEach(s -> assertNull(s.getWorkspace()));
        }
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.StyleRepository#findAllByWorkspace(org.geoserver.catalog.WorkspaceInfo)}.
     */
    public @Test void testFindAllByWorkspace() {
        WorkspaceInfo ws1 = testData.workspaceA;
        WorkspaceInfo ws2 = testData.workspaceB;

        add(createOne("s1"));
        add(createOne("s2"));

        StyleInfo ws1s1 = add(createOne("s1", ws1));
        StyleInfo ws1s2 = add(createOne("s2", ws1));

        try (Stream<StyleInfo> found = repository.findAllByWorkspace(ws2)) {
            assertEquals(0L, found.count());
        }

        try (Stream<StyleInfo> all = repository.findAllByWorkspace(ws1)) {
            List<StyleInfo> found = all.collect(Collectors.toList());
            Set<String> ids = Set.of(ws1s1.getId(), ws1s2.getId());
            found.forEach(s -> assertTrue(ids.contains(s.getId())));
            found.forEach(s -> assertNotNull(s.getWorkspace()));
            found.forEach(s -> assertEquals(ws1.getId(), s.getWorkspace().getId()));
        }
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.StyleRepository#findByNameAndWordkspaceNull(java.lang.String)}.
     */
    public @Test void testFindByNameAndWordkspaceNull() {
        WorkspaceInfo ws1 = testData.workspaceA;

        add(createOne("s1", ws1));
        add(createOne("s2", ws1));

        assertFalse(repository.findByNameAndWordkspaceNull("s1").isPresent());

        StyleInfo noWsS1 = add(createOne("s1"));
        StyleInfo noWsS2 = add(createOne("s2"));

        assertEquals(
                noWsS1.getId(), repository.findByNameAndWordkspaceNull("s1").orElseThrow().getId());
        assertEquals(
                noWsS2.getId(), repository.findByNameAndWordkspaceNull("s2").orElseThrow().getId());
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.StyleRepository#findByNameAndWorkspace(java.lang.String,
     * org.geoserver.catalog.WorkspaceInfo)}.
     */
    public @Test void testFindByNameAndWorkspace() {
        WorkspaceInfo ws1 = testData.workspaceA;
        WorkspaceInfo ws2 = testData.workspaceB;

        @SuppressWarnings("unused")
        StyleInfo noWsS1 = add(createOne("s1"));
        @SuppressWarnings("unused")
        StyleInfo noWsS2 = add(createOne("s2"));

        assertFalse(repository.findByNameAndWorkspace("s1", ws1).isPresent());

        StyleInfo ws1s1 = add(createOne("s1", ws1));
        StyleInfo ws1s2 = add(createOne("s2", ws1));
        StyleInfo ws2s1 = add(createOne("s1", ws2));
        StyleInfo ws2s2 = add(createOne("s2", ws2));

        assertEquals(
                ws1s1.getId(), repository.findByNameAndWorkspace("s1", ws1).orElseThrow().getId());
        assertEquals(
                ws1s2.getId(), repository.findByNameAndWorkspace("s2", ws1).orElseThrow().getId());

        assertEquals(
                ws2s1.getId(), repository.findByNameAndWorkspace("s1", ws2).orElseThrow().getId());
        assertEquals(
                ws2s2.getId(), repository.findByNameAndWorkspace("s2", ws2).orElseThrow().getId());
    }
}
