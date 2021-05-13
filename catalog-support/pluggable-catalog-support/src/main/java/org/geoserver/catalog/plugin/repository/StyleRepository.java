/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;

public interface StyleRepository extends CatalogInfoRepository<StyleInfo> {

    Stream<StyleInfo> findAllByNullWorkspace();

    Stream<StyleInfo> findAllByWorkspace(@NonNull WorkspaceInfo ws);

    Optional<StyleInfo> findByNameAndWordkspaceNull(@NonNull String name);

    Optional<StyleInfo> findByNameAndWorkspace(
            @NonNull String name, @NonNull WorkspaceInfo workspace);
}
