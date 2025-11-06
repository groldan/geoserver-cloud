/* (c) 2025 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.cloud.config.catalog.backend.datadirectory;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogTestData;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.PublishedInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.plugin.CatalogPlugin;
import org.geoserver.catalog.plugin.CatalogPluginStyleResourcePersister;
import org.geoserver.cloud.autoconfigure.catalog.backend.datadir.DataDirectoryTestConfiguration;
import org.geoserver.cloud.catalog.backend.datadir.EventualConsistencyEnforcer;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.config.util.XStreamPersisterFactory;
import org.geoserver.platform.GeoServerResourceLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Integration test for {@link CloudDataDirectoryGeoServerLoader} that verifies catalog loading
 * behavior with dangling references.
 *
 * <p>This test reproduces the scenario where a catalog is loaded from disk with pre-existing
 * dangling references (e.g., LayerGroup pointing to deleted Layer, Layer pointing to deleted Style).
 * The test verifies that:
 * <ul>
 *   <li>The catalog loads successfully despite dangling references
 *   <li>The {@link EventualConsistencyEnforcer} converges (pending queue is empty)
 *   <li>Query operations don't waste time retrying when the catalog is converged
 * </ul>
 */
@SpringBootTest(
        classes = DataDirectoryTestConfiguration.class,
        properties = {
            "geoserver.backend.dataDirectory.enabled=true",
            "geoserver.backend.data-directory.eventual-consistency.enabled=true",
            "geoserver.backend.data-directory.eventual-consistency.retries=25,25,50"
        })
@ActiveProfiles("test")
class CloudDataDirectoryGeoServerLoaderTest {

    static @TempDir Path datadir;

    @DynamicPropertySource
    static void setUpDataDir(DynamicPropertyRegistry registry) {
        registry.add("geoserver.backend.data-directory.location", datadir::toAbsolutePath);
    }

    /**
     * Populates the data directory with a catalog containing dangling references.
     * Uses the same approach as CloudDataDirectoryGeoServerLoader to write XML files.
     * <p>
     * This happens before {@link #setUpDataDir(DynamicPropertyRegistry)}
     */
    @BeforeAll
    static void populateDataDirectoryWithDanglingReferences() throws Exception {
        // Create a temporary catalog to generate XML files
        CatalogPlugin tempCatalog = new CatalogPlugin();
        GeoServerResourceLoader resourceLoader = new GeoServerResourceLoader(datadir.toFile());
        tempCatalog.setResourceLoader(resourceLoader);

        // Create persisters that will write XML files
        XStreamPersisterFactory xpf = new XStreamPersisterFactory();
        XStreamPersister persister = xpf.createXMLPersister();
        GeoServerDataDirectory dataDirectory = new GeoServerDataDirectory(resourceLoader);

        CatalogPluginGeoServerConfigPersister configPersister =
                new CatalogPluginGeoServerConfigPersister(resourceLoader, persister);
        CatalogPluginStyleResourcePersister stylePersister = new CatalogPluginStyleResourcePersister(tempCatalog);

        tempCatalog.addListener(configPersister);
        tempCatalog.addListener(stylePersister);

        // Use CatalogTestData to create a realistic catalog
        CatalogTestData testData =
                CatalogTestData.empty(() -> tempCatalog, () -> null).initialize();

        // Scenario 1: LayerGroup with missing layer
        LayerGroupInfo layerGroup = testData.layerGroup1;
        LayerInfo layerToRemove = testData.layerFeatureTypeA;

        // Verify layer is in the layer group
        assertThat(layerGroup.getLayers()).contains(layerToRemove);

        // Remove the layer to create a dangling reference
        tempCatalog.remove(layerToRemove);

        // Scenario 2: Layer with missing style
        LayerInfo layerWithMissingStyle = testData.layerFeatureTypeA;
        StyleInfo styleToRemove = layerWithMissingStyle.getDefaultStyle();

        // Remove the style to create a dangling reference
        tempCatalog.remove(styleToRemove);

        // Scenario 3: Resource with missing store
        ResourceInfo resourceWithMissingStore = testData.coverageA;
        StoreInfo storeToRemove = resourceWithMissingStore.getStore();

        // Remove the store to create a dangling reference
        tempCatalog.remove(storeToRemove);

        // The XML files now have dangling references
        // When Spring loads the catalog, these dangling refs will be ResolvingProxy instances
    }

    @Autowired
    Catalog catalog;

    @Autowired
    EventualConsistencyEnforcer enforcer;

    @Test
    void testCatalogLoadsWithDanglingReferences() {
        // Verify catalog loaded successfully
        assertThat(catalog.getWorkspaces())
                .as("Catalog should load workspaces despite dangling references")
                .isNotEmpty();

        assertThat(catalog.getNamespaces())
                .as("Catalog should load namespaces despite dangling references")
                .isNotEmpty();

        assertThat(catalog.getStyles())
                .as("Catalog should load styles despite dangling references")
                .isNotEmpty();
    }

    @Test
    void testCatalogConvergesDespiteDanglingReferences() {
        // The key test: verify the enforcer converged
        // Dangling references should NOT prevent convergence
        assertThat(enforcer.isConverged())
                .as("Catalog should converge despite dangling references. "
                        + "Operations with dangling refs should not remain in pending queue indefinitely.")
                .isTrue();
    }

    @Test
    void testLayerGroupWithMissingLayerLoaded() {
        // Find the layer group that has the dangling reference
        LayerGroupInfo loadedLayerGroup = catalog.getLayerGroups().stream()
                .filter(lg -> lg.getName().equals("layerGroup1"))
                .findFirst()
                .orElse(null);

        assertThat(loadedLayerGroup)
                .as("LayerGroup with dangling reference should be loaded")
                .isNotNull();

        // The layer group's layers list will contain a ResolvingProxy for the missing layer
        // This is expected behavior - the object loads with unresolved references
        assertThat(loadedLayerGroup.getLayers())
                .as("LayerGroup should have layers (including unresolved proxies)")
                .isNotEmpty();
    }

    @Test
    void testLayerWithMissingStyleLoaded() {
        assertThat(enforcer.isConverged())
        .as("Catalog should converge despite dangling references. "
                + "Operations with dangling refs should not remain in pending queue indefinitely.")
        .isTrue();
        // Find the layer that has the dangling style reference
        LayerInfo loadedLayer = catalog.getLayers().stream()
                .filter(l -> l.getResource().getName().equals("ft2"))
                .findFirst()
                .orElse(null);

        assertThat(loadedLayer)
                .as("Layer with dangling style reference should be loaded")
                .isNotNull();

        // The layer's default style will be a ResolvingProxy
        StyleInfo defaultStyle = loadedLayer.getDefaultStyle();
        assertThat(defaultStyle)
                .as("Layer should have a default style (may be unresolved proxy)")
                .isNotNull();
    }

    @Test
    void testQueryPerformanceWhenConverged() {
        // Verify the catalog is converged
        assertThat(enforcer.isConverged()).isTrue();

        // Query for a non-existent workspace
        // This should return quickly (not waste 100ms retrying)
        long startTime = System.currentTimeMillis();
        WorkspaceInfo notFound = catalog.getWorkspaceByName("does-not-exist");
        long elapsed = System.currentTimeMillis() - startTime;

        assertThat(notFound).isNull();

        // Should be very fast (< 10ms) because catalog is converged
        // The old bug would waste ~100ms retrying even when converged
        assertThat(elapsed)
                .as("Query should be fast when catalog is converged. " + "Should not waste time in retry logic.")
                .isLessThan(50);
    }

    @Test
    void testObjectsWithDanglingReferencesAreAccessible() {
        // Verify we can query for objects that have dangling references
        // They should be in the catalog, just with unresolved proxies

        for (WorkspaceInfo ws : catalog.getWorkspaces()) {
            assertThat(ws).isNotNull();
            assertThat(ws.getName()).isNotBlank();
        }

        for (NamespaceInfo ns : catalog.getNamespaces()) {
            assertThat(ns).isNotNull();
            assertThat(ns.getPrefix()).isNotBlank();
        }

        for (StyleInfo style : catalog.getStyles()) {
            assertThat(style).isNotNull();
            assertThat(style.getName()).isNotBlank();
        }

        for (LayerInfo layer : catalog.getLayers()) {
            assertThat(layer).isNotNull();
            assertThat(layer.getName()).isNotBlank();
            // Resource might be a ResolvingProxy, but should not be null
            assertThat(layer.getResource()).isNotNull();
        }

        for (PublishedInfo published : catalog.getLayers()) {
            assertThat(published).isNotNull();
        }
    }
}
