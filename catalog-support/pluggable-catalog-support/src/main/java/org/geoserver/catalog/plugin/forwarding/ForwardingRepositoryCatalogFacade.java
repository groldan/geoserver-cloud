/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.forwarding;

import org.geoserver.catalog.CatalogFacade;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.plugin.repository.CatalogInfoRepository;
import org.geoserver.catalog.plugin.repository.LayerGroupRepository;
import org.geoserver.catalog.plugin.repository.LayerRepository;
import org.geoserver.catalog.plugin.repository.MapRepository;
import org.geoserver.catalog.plugin.repository.NamespaceRepository;
import org.geoserver.catalog.plugin.repository.RepositoryCatalogFacade;
import org.geoserver.catalog.plugin.repository.ResourceRepository;
import org.geoserver.catalog.plugin.repository.StoreRepository;
import org.geoserver.catalog.plugin.repository.StyleRepository;
import org.geoserver.catalog.plugin.repository.WorkspaceRepository;

/**
 * {@link CatalogFacade} which forwards all its method calls to another {@code CatalogFacade} aiding
 * in implementing a decorator.
 *
 * <p>Subclasses should override one or more methods to modify the behavior of the backing facade as
 * needed.
 */
public class ForwardingRepositoryCatalogFacade extends ForwardingExtendedCatalogFacade
        implements RepositoryCatalogFacade {

    public ForwardingRepositoryCatalogFacade(RepositoryCatalogFacade facade) {
        super(facade);
    }

    public @Override void setNamespaceRepository(NamespaceRepository namespaces) {
        facade().setNamespaceRepository(namespaces);
    }

    public @Override void setWorkspaceRepository(WorkspaceRepository workspaces) {
        facade().setWorkspaceRepository(workspaces);
    }

    public @Override void setStoreRepository(StoreRepository stores) {
        facade().setStoreRepository(stores);
    }

    public @Override void setResourceRepository(ResourceRepository resources) {
        facade().setResourceRepository(resources);
    }

    public @Override void setLayerRepository(LayerRepository layers) {
        facade().setLayerRepository(layers);
    }

    public @Override void setLayerGroupRepository(LayerGroupRepository layerGroups) {
        facade().setLayerGroupRepository(layerGroups);
    }

    public @Override void setStyleRepository(StyleRepository styles) {
        facade().setStyleRepository(styles);
    }

    public @Override void setMapRepository(MapRepository maps) {
        facade().setMapRepository(maps);
    }

    public @Override NamespaceRepository getNamespaceRepository() {
        return facade().getNamespaceRepository();
    }

    public @Override WorkspaceRepository getWorkspaceRepository() {
        return facade().getWorkspaceRepository();
    }

    public @Override StoreRepository getStoreRepository() {
        return facade().getStoreRepository();
    }

    public @Override ResourceRepository getResourceRepository() {
        return facade().getResourceRepository();
    }

    public @Override LayerRepository getLayerRepository() {
        return facade().getLayerRepository();
    }

    public @Override LayerGroupRepository getLayerGroupRepository() {
        return facade().getLayerGroupRepository();
    }

    public @Override StyleRepository getStyleRepository() {
        return facade().getStyleRepository();
    }

    public @Override MapRepository getMapRepository() {
        return facade().getMapRepository();
    }

    protected RepositoryCatalogFacade facade() {
        return (RepositoryCatalogFacade) facade;
    }

    public @Override <T extends CatalogInfo, R extends CatalogInfoRepository<T>> R repository(
            Class<T> of) {
        return facade().repository(of);
    }

    public @Override <T extends CatalogInfo, R extends CatalogInfoRepository<T>> R repositoryFor(
            T info) {
        return facade().repositoryFor(info);
    }
}
