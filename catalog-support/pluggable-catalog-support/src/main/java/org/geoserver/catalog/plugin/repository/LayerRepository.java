/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StyleInfo;

public interface LayerRepository extends CatalogInfoRepository<LayerInfo> {

    Optional<LayerInfo> findOneByName(@NonNull String possiblyPrefixedName);

    Stream<LayerInfo> findAllByDefaultStyleOrStyles(@NonNull StyleInfo style);

    Stream<LayerInfo> findAllByResource(@NonNull ResourceInfo resource);
}
