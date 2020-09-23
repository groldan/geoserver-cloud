/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.catalog.client.repository;

import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.plugin.CatalogInfoRepository.WorkspaceRepository;
import org.springframework.lang.Nullable;

public class CloudWorkspaceRepository extends CatalogServiceClientRepository<WorkspaceInfo>
        implements WorkspaceRepository {

    private final @Getter Class<WorkspaceInfo> infoType = WorkspaceInfo.class;

    public @Override void setDefaultWorkspace(@NonNull WorkspaceInfo workspace) {
        Objects.requireNonNull(workspace.getId(), "workspace id can't be null");
        callAndBlock(() -> client().setDefaultWorkspace(workspace.getId()));
    }

    public @Override @Nullable WorkspaceInfo getDefaultWorkspace() {
        return callAndReturn(client()::getDefaultWorkspace);
    }
}
