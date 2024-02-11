/*
 * (c) 2023 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.event.remote.datadir;

import lombok.NonNull;

import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.MapInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.ResolvingProxy;
import org.geoserver.catalog.plugin.ExtendedCatalogFacade;
import org.geoserver.catalog.plugin.Patch;
import org.geoserver.catalog.plugin.forwarding.ForwardingExtendedCatalogFacade;

/**
 *
 *
 * <ul>
 *   <li>All {@code add(@NonNull )} methods check if the {@link CatalogInfo} being added have
 *       unresolved ({@link ResolvingProxy}) references
 *   <li>If so, the object is put in a pending list and not added
 *   <li>Conversely, if during {@code add(@NonNull )}, there's a pending add waiting for this new
 *       object, the {@code add(@NonNull )} proceeds and then the pending object is added
 * </ul>
 *
 * @since 1.7
 * @see EventualConsistencyEnforcer
 * @see RemoteEventDataDirectoryProcessor
 */
public class EventuallyConsistentCatalogFacade extends ForwardingExtendedCatalogFacade {

    private final EventualConsistencyEnforcer enforcer;

    public EventuallyConsistentCatalogFacade(
            @NonNull ExtendedCatalogFacade facade, @NonNull EventualConsistencyEnforcer tracker) {
        super(facade);
        this.enforcer = tracker;
    }

    @Override
    public <T extends CatalogInfo> T add(@NonNull T info) {
        return enforcer.add(info);
    }

    @Override
    public WorkspaceInfo add(@NonNull WorkspaceInfo info) {
        return enforcer.add(info);
    }

    @Override
    public NamespaceInfo add(@NonNull NamespaceInfo info) {
        return enforcer.add(info);
    }

    @Override
    public LayerGroupInfo add(@NonNull LayerGroupInfo info) {
        return enforcer.add(info);
    }

    @Override
    public LayerInfo add(@NonNull LayerInfo info) {
        return enforcer.add(info);
    }

    @Override
    public ResourceInfo add(@NonNull ResourceInfo info) {
        return enforcer.add(info);
    }

    @Override
    public StoreInfo add(@NonNull StoreInfo info) {
        return enforcer.add(info);
    }

    @Override
    public StyleInfo add(@NonNull StyleInfo info) {
        return enforcer.add(info);
    }

    @Override
    public <I extends CatalogInfo> I update(@NonNull I info, @NonNull Patch patch) {
        return enforcer.update(info, patch);
    }

    @Override
    public void remove(@NonNull CatalogInfo info) {
        enforcer.remove(info);
    }

    @Override
    public void remove(@NonNull WorkspaceInfo info) {
        enforcer.remove(info);
    }

    @Override
    public void remove(@NonNull NamespaceInfo info) {
        enforcer.remove(info);
    }

    @Override
    public void remove(@NonNull StoreInfo info) {
        enforcer.remove(info);
    }

    @Override
    public void remove(@NonNull ResourceInfo info) {
        enforcer.remove(info);
    }

    @Override
    public void remove(@NonNull LayerInfo info) {
        enforcer.remove(info);
    }

    @Override
    public void remove(@NonNull LayerGroupInfo info) {
        enforcer.remove(info);
    }

    @Override
    public void remove(@NonNull StyleInfo info) {
        enforcer.remove(info);
    }

    @Override
    public void remove(@NonNull MapInfo info) {
        enforcer.remove(info);
    }

    @Override
    public void setDefaultWorkspace(WorkspaceInfo workspace) {
        enforcer.setDefaultWorkspace(workspace);
    }

    @Override
    public void setDefaultNamespace(NamespaceInfo defaultNamespace) {
        enforcer.setDefaultNamespace(defaultNamespace);
    }

    @Override
    public void setDefaultDataStore(WorkspaceInfo workspace, DataStoreInfo store) {
        enforcer.setDefaultDataStore(workspace, store);
    }
}
