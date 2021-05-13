/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import org.geoserver.catalog.NamespaceInfo;

public interface NamespaceRepository extends CatalogInfoRepository<NamespaceInfo> {
    /**
     * Establishes {@code namespace} as the {@link #getDefaultNamespace() default} on
     *
     * @throws NoSuchElementException if the namespace being set as default does not exist
     */
    void setDefaultNamespace(@NonNull NamespaceInfo namespace);

    /** Unlinks the current default namespace, leaving no default */
    void unsetDefaultNamespace();

    Optional<NamespaceInfo> getDefaultNamespace();

    Optional<NamespaceInfo> findOneByURI(@NonNull String uri);

    Stream<NamespaceInfo> findAllByURI(@NonNull String uri);
}
