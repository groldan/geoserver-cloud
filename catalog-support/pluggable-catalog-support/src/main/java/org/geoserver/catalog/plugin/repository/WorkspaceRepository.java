/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.NonNull;
import org.geoserver.catalog.WorkspaceInfo;

public interface WorkspaceRepository extends CatalogInfoRepository<WorkspaceInfo> {
    /** Unlinks the current default workspace, leaving no default */
    void unsetDefaultWorkspace();

    /**
     * Establishes {@code workspace} as the {@link #getDefaultWorkspace() default} on
     *
     * @throws NoSuchElementException if the workspace being set as default does not exist
     */
    void setDefaultWorkspace(@NonNull WorkspaceInfo workspace);

    Optional<WorkspaceInfo> getDefaultWorkspace();
}
