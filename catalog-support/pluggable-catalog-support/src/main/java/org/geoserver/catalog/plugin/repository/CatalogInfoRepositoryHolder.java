/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import org.geoserver.catalog.CatalogInfo;

public interface CatalogInfoRepositoryHolder {

    <T extends CatalogInfo, R extends CatalogInfoRepository<T>> R repository(Class<T> of);

    <T extends CatalogInfo, R extends CatalogInfoRepository<T>> R repositoryFor(T info);

    void setNamespaceRepository(NamespaceRepository namespaces);

    NamespaceRepository getNamespaceRepository();

    void setWorkspaceRepository(WorkspaceRepository workspaces);

    WorkspaceRepository getWorkspaceRepository();

    void setStoreRepository(StoreRepository stores);

    StoreRepository getStoreRepository();

    void setResourceRepository(ResourceRepository resources);

    ResourceRepository getResourceRepository();

    void setLayerRepository(LayerRepository layers);

    LayerRepository getLayerRepository();

    void setLayerGroupRepository(LayerGroupRepository layerGroups);

    LayerGroupRepository getLayerGroupRepository();

    void setStyleRepository(StyleRepository styles);

    StyleRepository getStyleRepository();

    void setMapRepository(MapRepository maps);

    MapRepository getMapRepository();
}
