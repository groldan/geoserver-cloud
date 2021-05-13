/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.heap;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.plugin.repository.LayerGroupRepository;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

public class LayerGroupInfoLookup extends CatalogInfoLookup<LayerGroupInfo>
        implements LayerGroupRepository {

    /**
     * The name uses the workspace id as it does not need to be updated when the workspace is
     * renamed
     */
    static final Function<LayerGroupInfo, Name> LAYERGROUP_NAME_MAPPER =
            lg ->
                    new NameImpl(
                            lg.getWorkspace() != null ? lg.getWorkspace().getId() : null,
                            lg.getName());

    public LayerGroupInfoLookup() {
        super(LayerGroupInfo.class, LAYERGROUP_NAME_MAPPER);
    }

    public @Override Stream<LayerGroupInfo> findAllByWorkspaceIsNull() {
        return list(LayerGroupInfo.class, lg -> lg.getWorkspace() == null);
    }

    public @Override Stream<LayerGroupInfo> findAllByWorkspace(WorkspaceInfo workspace) {
        requireNonNull(workspace);
        return list(
                LayerGroupInfo.class,
                lg ->
                        lg.getWorkspace() != null
                                && lg.getWorkspace().getId().equals(workspace.getId()));
    }

    public @Override Optional<LayerGroupInfo> findByNameAndWorkspaceIsNull(String name) {
        requireNonNull(name);
        return findFirstByName(new NameImpl(null, name), LayerGroupInfo.class);
    }

    public @Override Optional<LayerGroupInfo> findByNameAndWorkspace(
            String name, WorkspaceInfo workspace) {
        requireNonNull(name);
        requireNonNull(workspace);
        return findFirstByName(new NameImpl(workspace.getId(), name), LayerGroupInfo.class);
    }
}
