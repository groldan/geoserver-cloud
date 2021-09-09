/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.catalog.client.repository;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.plugin.repository.NamespaceRepository;
import org.springframework.lang.Nullable;

public class CatalogClientNamespaceRepository extends CatalogClientRepository<NamespaceInfo>
        implements NamespaceRepository {

    private final @Getter Class<NamespaceInfo> contentType = NamespaceInfo.class;

    public @Override void setDefaultNamespace(@NonNull NamespaceInfo namespace) {
        Objects.requireNonNull(namespace.getId(), "provided null namespace id");
        blockAndReturn(client().setDefaultNamespace(namespace.getId()));
    }

    public @Override <U extends NamespaceInfo> Optional<U> findFirstByName(
            @NonNull String name, @NonNull Class<U> infoType) {
        // geoserver has this tendency to loose method contracts...
        if (name.indexOf(':') > -1) {
            return Optional.empty();
        }
        return super.findFirstByName(name, infoType);
    }

    public @Override void unsetDefaultNamespace() {
        block(client().unsetDefaultNamespace());
    }

    public @Override @Nullable Optional<NamespaceInfo> getDefaultNamespace() {
        return blockAndReturn(client().getDefaultNamespace());
    }

    public @Override @Nullable Optional<NamespaceInfo> findOneByURI(@NonNull String uri) {
        return blockAndReturn(client().findOneNamespaceByURI(uri));
    }

    public @Override Stream<NamespaceInfo> findAllByURI(@NonNull String uri) {
        return toStream(client().findAllNamespacesByURI(uri));
    }
}
