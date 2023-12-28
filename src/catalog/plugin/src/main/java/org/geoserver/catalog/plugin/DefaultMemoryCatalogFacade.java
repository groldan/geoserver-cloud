/*
 * (c) 2014 - 2015 Open Source Geospatial Foundation - all rights reserved (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogFacade;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.CatalogRepository;
import org.geoserver.catalog.plugin.CatalogInfoLookup.LayerGroupInfoLookup;
import org.geoserver.catalog.plugin.CatalogInfoLookup.LayerInfoLookup;
import org.geoserver.catalog.plugin.CatalogInfoLookup.MapInfoLookup;
import org.geoserver.catalog.plugin.CatalogInfoLookup.NamespaceInfoLookup;
import org.geoserver.catalog.plugin.CatalogInfoLookup.ResourceInfoLookup;
import org.geoserver.catalog.plugin.CatalogInfoLookup.StoreInfoLookup;
import org.geoserver.catalog.plugin.CatalogInfoLookup.StyleInfoLookup;
import org.geoserver.catalog.plugin.CatalogInfoLookup.WorkspaceInfoLookup;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Default catalog facade implementation using in-memory {@link CatalogRepository repositories} to
 * store the {@link CatalogInfo}
 */
public class DefaultMemoryCatalogFacade extends RepositoryCatalogFacadeImpl
        implements CatalogFacade {

    public DefaultMemoryCatalogFacade() {
        this(null);
    }

    public DefaultMemoryCatalogFacade(Catalog catalog) {
        super(catalog);
        resolve();
    }

    @Override
    public void resolve() {
        // JD creation checks are done here b/c when xstream depersists
        // some members may be left null
        setWorkspaceRepository(resolve(workspaces, WorkspaceInfoLookup::new));
        setNamespaceRepository(resolve(namespaces, NamespaceInfoLookup::new));
        setStoreRepository(resolve(stores, StoreInfoLookup::new));
        setStyleRepository(resolve(styles, StyleInfoLookup::new));
        setLayerRepository(resolve(layers, LayerInfoLookup::new));
        setResourceRepository(
                resolve(resources, () -> new ResourceInfoLookup((LayerInfoLookup) layers)));
        setLayerGroupRepository(resolve(layerGroups, LayerGroupInfoLookup::new));
        setMapRepository(resolve(maps, MapInfoLookup::new));
    }

    private <I extends CatalogInfo, R extends CatalogInfoRepository<I>> R resolve(
            R current, Supplier<R> factory) {
        return Optional.ofNullable(current).orElseGet(factory::get);
    }
}
