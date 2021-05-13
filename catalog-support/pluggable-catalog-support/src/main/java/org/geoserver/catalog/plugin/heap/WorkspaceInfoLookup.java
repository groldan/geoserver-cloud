/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.heap;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.plugin.repository.WorkspaceRepository;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

public class WorkspaceInfoLookup extends CatalogInfoLookup<WorkspaceInfo>
        implements WorkspaceRepository {

    static final Function<WorkspaceInfo, Name> WORKSPACE_NAME_MAPPER =
            w -> new NameImpl(w.getName());

    private WorkspaceInfo defaultWorkspace;

    public WorkspaceInfoLookup() {
        super(WorkspaceInfo.class, WORKSPACE_NAME_MAPPER);
    }

    public @Override void setDefaultWorkspace(WorkspaceInfo workspace) {
        this.defaultWorkspace =
                findById(workspace.getId(), WorkspaceInfo.class)
                        .orElseThrow(NoSuchElementException::new);
    }

    public @Override Optional<WorkspaceInfo> getDefaultWorkspace() {
        return Optional.ofNullable(defaultWorkspace);
    }

    public @Override void unsetDefaultWorkspace() {
        defaultWorkspace = null;
    }
}
