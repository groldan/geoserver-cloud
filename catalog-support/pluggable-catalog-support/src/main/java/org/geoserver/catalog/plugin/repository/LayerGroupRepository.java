/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.WorkspaceInfo;

public interface LayerGroupRepository extends CatalogInfoRepository<LayerGroupInfo> {

    Optional<LayerGroupInfo> findByNameAndWorkspaceIsNull(@NonNull String name);

    Optional<LayerGroupInfo> findByNameAndWorkspace(
            @NonNull String name, @NonNull WorkspaceInfo workspace);

    Stream<LayerGroupInfo> findAllByWorkspaceIsNull();

    Stream<LayerGroupInfo> findAllByWorkspace(WorkspaceInfo workspace);
}
