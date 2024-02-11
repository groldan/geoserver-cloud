/*
 * (c) 2023 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.event.remote.datadir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;

import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.CatalogTestData;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.Info;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.LayerGroupStyle;
import org.geoserver.catalog.impl.ResolvingProxy;
import org.geoserver.catalog.plugin.AbstractCatalogVisitor;
import org.geoserver.catalog.plugin.CatalogPlugin;
import org.geoserver.catalog.plugin.ExtendedCatalogFacade;
import org.geoserver.cloud.event.info.ConfigInfoType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class EventualConsistencyEnforcerTest {

    CatalogTestData data;
    EventualConsistencyEnforcer executor;
    CatalogPlugin catalog;
    ExtendedCatalogFacade rawFacade;

    NamespaceInfo namespace;
    WorkspaceInfo workspace;

    /** Has {@link DataStoreInfo#setWorkspace WorkspaceInfo} {@link #proxy proxyfied} */
    DataStoreInfo dataStore;

    /**
     * Has {@link FeatureTypeInfo#setNamespace NamespaceInfo} and {@link FeatureTypeInfo#setStore
     * StoreInfo} {@link #proxy proxyfied}
     */
    FeatureTypeInfo featureType;

    StyleInfo style;

    /** Has {@link StyleInfo#setWorkspace WorkspaceInfo} {@link #proxy proxyfied} */
    StyleInfo styleWs;

    /**
     * Has {@link LayerInfo#setResource ResourceInfo}, {@link LayerInfo#setDefaultStyle
     * defaultStyle} and {@link LayerInfo#getStyles() styles} {@link #proxy proxyfied} ({@code
     * getStyles()} contains {@link #styleWs})
     */
    LayerInfo layer;

    /**
     * Has {@link LayerGroupInfo#getWorkspace workspace} ({@link #workspace}), {@link
     * LayerGroupInfo#getLayers layers} ({@link #layer}), {@link LayerGroupInfo#getStyles() styles}
     * ({@link #style}) proxyfied
     */
    LayerGroupInfo lg;

    @BeforeEach
    void setUp() throws Exception {
        catalog = new CatalogPlugin();
        rawFacade = catalog.getFacade();
        // do not call afterPropertiesSet() so the monitor/convergence thread doesn't
        // run
        executor = new EventualConsistencyEnforcer(rawFacade);
        data = CatalogTestData.empty(() -> catalog, () -> null);
        data.createCatalogObjects();

        namespace = proxyfyRefs(data.namespaceA);
        workspace = proxyfyRefs(data.workspaceA);
        dataStore = proxyfyRefs(data.dataStoreA);
        featureType = proxyfyRefs(data.featureTypeA);
        style = proxyfyRefs(data.style1);

        data.style2.setWorkspace(workspace);
        styleWs = proxyfyRefs(data.style2);

        data.layerFeatureTypeA.setDefaultStyle(style);
        data.layerFeatureTypeA.getStyles().add(styleWs);
        layer = proxyfyRefs(data.layerFeatureTypeA);

        lg = proxyfyRefs(data.layerGroup1);
    }

    /**
     * Put the enforced in a non-converged state, then let it run the monitor/convergence thread and
     * verify the catalog converged, meaning there are no pending catalog modifications, while the
     * catalog stayed consistent all the time, despite being out of sync with the event producer
     * (i.e. other services issuing remote catalog mutating events that arrive out of order)
     */
    @Test
    void testMonitoringAndConvergenceThread() {
        try {
            data.featureTypeA.setStore(proxy(data.dataStoreA));
            executor.add(data.featureTypeA);

            data.dataStoreA.setWorkspace(proxy(data.workspaceA));
            executor.add(data.dataStoreA);

            assertThat(find(data.featureTypeA)).isEmpty();
            assertThat(find(data.dataStoreA)).isEmpty();

            assertThat(executor.isConverged()).isFalse();

            executor.monitorExecutionPeriodSeconds = 1;
            executor.afterPropertiesSet();

            // add missing obejcts through the catalog and not through the enforcer for the
            // pending
            // ops to be resolved by the monitor thread and not by the executor.add() op
            catalog.add(data.namespaceA);
            catalog.add(data.workspaceA);

            await().atMost(Duration.ofSeconds(2))
                    .untilAsserted(
                            () -> {
                                assertThat(find(data.dataStoreA)).isPresent();
                                assertThat(find(data.featureTypeA)).isPresent();
                            });

            assertConverged();
        } finally {
            executor.destroy();
        }
    }

    @Test
    void testAdd() {
        // add ft with a proxy ref to the store and namespace
        featureType.setStore(proxy(dataStore));
        featureType.setNamespace(proxy(namespace));
        executor.add(featureType);
        assertNotConverged();
        assertThat(find(featureType)).isEmpty();

        // add store with a proxy ref to workspace
        dataStore.setWorkspace(proxy(workspace));
        executor.add(dataStore);
        assertNotConverged();
        assertThat(find(featureType)).isEmpty();
        assertThat(find(dataStore)).isEmpty();

        // add layer with proxy refs to resource and default style
        layer.setResource(proxy(featureType));
        layer.setDefaultStyle(proxy(style));
        executor.add(layer);
        assertNotConverged();
        assertThat(find(featureType)).isEmpty();
        assertThat(find(dataStore)).isEmpty();
        assertThat(find(layer)).isEmpty();

        // add workspace, only the store should converge
        executor.add(workspace);
        assertThat(find(featureType)).isPresent();
        assertNotConverged();
        assertThat(find(dataStore)).isPresent();
        assertThat(find(featureType)).isEmpty();
        assertThat(find(layer)).isEmpty();

        // assertConverged();
    }

    @Test
    void testRemove() {
        fail("Not yet implemented");
    }

    @Test
    void testUpdate() {
        fail("Not yet implemented");
    }

    @Test
    void testSetDefaultWorkspace() {
        fail("Not yet implemented");
    }

    @Test
    void testSetDefaultNamespace() {
        fail("Not yet implemented");
    }

    @Test
    void testSetDefaultDataStore() {
        fail("Not yet implemented");
    }

    private <T extends Info> T proxy(T info) {
        if (null == info) return info;
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) ConfigInfoType.valueOf(info).getType();
        return ResolvingProxy.create(info.getId(), type);
    }

    private <T extends CatalogInfo> Optional<T> find(T testdata) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) ConfigInfoType.valueOf(testdata).getType();
        return catalog.findById(testdata.getId(), type);
    }

    private void assertConverged() {
        assertThat(executor.isConverged()).isTrue();
    }

    private void assertNotConverged() {
        assertThat(executor.isConverged()).isFalse();
    }

    /**
     * Converts all {@link CatalogInfo} properties in {@code info} to {@link ResolvingProxy} refs
     *
     * @return {@code info}
     */
    private <T extends CatalogInfo> T proxyfyRefs(T info) {
        info.accept(
                new AbstractCatalogVisitor() {
                    @Override
                    protected void visit(StoreInfo store) {
                        store.setWorkspace(proxy(store.getWorkspace()));
                    }

                    @Override
                    public void visit(FeatureTypeInfo featureType) {
                        featureType.setNamespace(proxy(featureType.getNamespace()));
                        featureType.setStore(proxy(featureType.getStore()));
                    }

                    @Override
                    protected void visit(ResourceInfo resource) {
                        resource.setStore(proxy(resource.getStore()));
                        resource.setNamespace(proxy(resource.getNamespace()));
                    }

                    @Override
                    public void visit(LayerInfo layer) {
                        layer.setResource(proxy(layer.getResource()));
                        layer.setDefaultStyle(proxy(layer.getDefaultStyle()));
                        Set<StyleInfo> styles = Set.copyOf(layer.getStyles());
                        layer.getStyles().clear();
                        styles.forEach(s -> layer.getStyles().add(proxy(s)));
                    }

                    @Override
                    public void visit(LayerGroupInfo lg) {
                        lg.setWorkspace(proxy(lg.getWorkspace()));
                        lg.setRootLayer(proxy(lg.getRootLayer()));
                        lg.setRootLayerStyle(proxy(lg.getRootLayerStyle()));

                        proxify(lg.getLayers());
                        proxify(lg.getStyles());
                        proxify(lg.getLayerGroupStyles());
                    }

                    private LayerGroupStyle proxify(LayerGroupStyle lgs) {
                        StyleInfo name = lgs.getName();
                        if (null != name && null != name.getId()) {
                            lgs.setName(proxy(name));
                        }
                        proxify(lgs.getStyles());
                        proxify(lgs.getLayers());
                        return null;
                    }

                    @SuppressWarnings("unchecked")
                    private <C extends Info> void proxify(List<C> list) {
                        for (int i = 0; i < list.size(); i++) {
                            C info = list.get(i);
                            C proxied =
                                    switch (info) {
                                        case CatalogInfo c -> (C) proxy(c);
                                        case LayerGroupStyle lgs -> (C) proxify(lgs);
                                        default -> throw new IllegalArgumentException();
                                    };
                            list.set(i, proxied);
                        }
                    }

                    @Override
                    public void visit(StyleInfo style) {
                        style.setWorkspace(proxy(style.getWorkspace()));
                    }
                });
        return info;
    }
}
