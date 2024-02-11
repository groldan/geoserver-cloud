/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.config.catalog.backend.datadirectory;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.MapInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.plugin.ExtendedCatalogFacade;
import org.geoserver.catalog.plugin.Patch;
import org.geoserver.catalog.plugin.forwarding.ForwardingExtendedCatalogFacade;
import org.geoserver.cloud.event.remote.datadir.HashCode;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class DataDirectoryCatalogFacade extends ForwardingExtendedCatalogFacade {

    private HashCode convergedHash = new HashCode();
    private ReentrantLock hashLock = new ReentrantLock();

    public DataDirectoryCatalogFacade(ExtendedCatalogFacade facade) {
        super(facade);
        Executors.newScheduledThreadPool(1)
                .scheduleAtFixedRate(this::report, 10, 5, TimeUnit.SECONDS);
    }

    private void report() {
        hashLock.lock();
        try {
            log.info("Catalog convergence state: {}", convergedHash);
        } finally {
            hashLock.unlock();
        }
    }

    private <I extends CatalogInfo> I hash(@NonNull I info) {
        hashLock.lock();
        try {
            convergedHash = convergedHash.xor(info);
        } finally {
            hashLock.unlock();
        }
        return info;
    }

    private void removeHash(CatalogInfo info) {
        removeHash(info.getId());
    }

    private void removeHash(String id) {
        hashLock.lock();
        try {
            convergedHash = convergedHash.remove(id);
        } finally {
            hashLock.unlock();
        }
    }

    @Override
    public <I extends CatalogInfo> I update(I info, Patch patch) {
        return hash(super.update(info, patch));
    }

    @Override
    public <T extends CatalogInfo> T add(@NonNull T info) {
        return hash(super.add(info));
    }

    @Override
    public void remove(@NonNull CatalogInfo info) {
        super.remove(info);
        removeHash(info);
    }

    @Override
    public StoreInfo add(StoreInfo store) {
        return hash(facade.add(store));
    }

    @Override
    public void remove(StoreInfo store) {
        facade.remove(store);
        removeHash(store);
    }

    @Override
    public void save(StoreInfo store) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceInfo add(ResourceInfo resource) {
        return hash(facade.add(resource));
    }

    @Override
    public void remove(ResourceInfo resource) {
        facade.remove(resource);
        removeHash(resource);
    }

    @Override
    public void save(ResourceInfo resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LayerInfo add(LayerInfo layer) {
        return hash(facade.add(layer));
    }

    @Override
    public void remove(LayerInfo layer) {
        facade.remove(layer);
        removeHash(layer);
    }

    @Override
    public void save(LayerInfo layer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MapInfo add(MapInfo map) {
        return hash(facade.add(map));
    }

    @Override
    public void remove(MapInfo map) {
        facade.remove(map);
        removeHash(map);
    }

    @Override
    public void save(MapInfo map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LayerGroupInfo add(LayerGroupInfo layerGroup) {
        return hash(facade.add(layerGroup));
    }

    @Override
    public void remove(LayerGroupInfo layerGroup) {
        facade.remove(layerGroup);
        removeHash(layerGroup);
    }

    @Override
    public void save(LayerGroupInfo layerGroup) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamespaceInfo add(NamespaceInfo namespace) {
        return hash(facade.add(namespace));
    }

    @Override
    public void remove(NamespaceInfo namespace) {
        facade.remove(namespace);
        removeHash(namespace);
    }

    @Override
    public void save(NamespaceInfo namespace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WorkspaceInfo add(WorkspaceInfo workspace) {
        return hash(facade.add(workspace));
    }

    @Override
    public void remove(WorkspaceInfo workspace) {
        facade.remove(workspace);
        removeHash(workspace);
    }

    @Override
    public void save(WorkspaceInfo workspace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StyleInfo add(StyleInfo style) {
        return hash(facade.add(style));
    }

    @Override
    public void remove(StyleInfo style) {
        facade.remove(style);
        removeHash(style);
    }

    @Override
    public void save(StyleInfo style) {
        throw new UnsupportedOperationException();
    }
}
