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
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.StyleInfo;
import org.junit.Test;

/** */
public abstract class LayerRepositoryConformanceTest
        extends CatalogInfoRepositoryConformanceTest<LayerInfo, LayerRepository> {

    private final @Getter Class<LayerInfo> infoType = LayerInfo.class;
    private ResourceRepository resources;
    private StyleRepository styles;

    protected @Override final LayerRepository createRepository() {
        this.resources = createResourceRepository();
        this.styles = createStylesRepository();
        return createLayerRepository();
    }

    protected abstract LayerRepository createLayerRepository();

    protected abstract ResourceRepository createResourceRepository();

    protected abstract StyleRepository createStylesRepository();

    protected @Override LayerInfo createOne(String name) {
        FeatureTypeInfo resource = testData.createFeatureType(name);
        resources.add(resource);
        resource =
                resources
                        .findById(resource.getId(), FeatureTypeInfo.class)
                        .orElseThrow(IllegalStateException::new);

        return testData.createLayer(resource);
    }

    protected LayerInfo createAndAdd(
            String name, StyleInfo defaultStyle, StyleInfo... additionalStyles) {
        LayerInfo l = createOne(name);
        assertNotNull(l.getId());
        l.setDefaultStyle(defaultStyle);
        if (null != additionalStyles) {
            Arrays.asList(additionalStyles).forEach(s -> l.getStyles().add(s));
        }
        return add(l);
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
                "opaque",
                "queryable",
                "path");
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.LayerRepository#findOneByName(java.lang.String)}.
     */
    public @Test void testFindOneByName() {
        LayerInfo l1 = testData.layerFeatureTypeA;
        assertFalse(repository.findOneByName(l1.getName()).isPresent());
        l1 = add(l1);
        Optional<LayerInfo> found = repository.findOneByName(l1.getName());
        assertTrue(found.isPresent());
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.LayerRepository#findAllByDefaultStyleOrStyles(org.geoserver.catalog.StyleInfo)}.
     */
    public @Test void testFindAllByDefaultStyleOrStyles() {
        StyleInfo style1 = testData.style1;
        StyleInfo style2 = testData.style2;
        styles.add(style1);
        styles.add(style2);

        LayerInfo l1 = createAndAdd("l1", style1);
        LayerInfo l2 = createAndAdd("l2", style2, style1);

        try (Stream<String> found =
                repository.findAllByDefaultStyleOrStyles(style1).map(LayerInfo::getId)) {
            Set<String> expected = Set.of(l1.getId(), l2.getId());
            Set<String> actual = found.collect(Collectors.toSet());
            assertEquals(expected, actual);
        }

        try (Stream<String> found =
                repository.findAllByDefaultStyleOrStyles(style2).map(LayerInfo::getId)) {
            Set<String> expected = Set.of(l2.getId());
            Set<String> actual = found.collect(Collectors.toSet());
            assertEquals(expected, actual);
        }
    }

    /**
     * Test method for {@link
     * org.geoserver.catalog.plugin.repository.LayerRepository#findAllByResource(org.geoserver.catalog.ResourceInfo)}.
     */
    public @Test void testFindAllByResource() {
        LayerInfo l1 = add(createOne("l1"));
        LayerInfo l2 = add(createOne("l2"));
        // there's a 1-1 relationship between layerinfo and resourceinfo anyway
        Set<String> actual =
                repository
                        .findAllByResource(l1.getResource())
                        .map(LayerInfo::getId)
                        .collect(Collectors.toSet());
        assertEquals(Set.of(l1.getId()), actual);

        actual =
                repository
                        .findAllByResource(l2.getResource())
                        .map(LayerInfo::getId)
                        .collect(Collectors.toSet());
        assertEquals(Set.of(l2.getId()), actual);
    }

    @Test(expected = NullPointerException.class)
    public void testFindAllByResource_null() {
        repository.findAllByResource(null);
    }
}
