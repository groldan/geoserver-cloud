/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.heap;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.plugin.repository.NamespaceRepository;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

public class NamespaceInfoLookup extends CatalogInfoLookup<NamespaceInfo>
        implements NamespaceRepository {

    static final Function<NamespaceInfo, Name> NAMESPACE_NAME_MAPPER =
            n -> new NameImpl(n.getPrefix());

    private NamespaceInfo defaultNamespace;

    public NamespaceInfoLookup() {
        super(NamespaceInfo.class, NAMESPACE_NAME_MAPPER);
    }

    public @Override void setDefaultNamespace(NamespaceInfo namespace) {
        requireNonNull(namespace);
        this.defaultNamespace =
                findById(namespace.getId(), NamespaceInfo.class)
                        .orElseThrow(NoSuchElementException::new);
    }

    public @Override Optional<NamespaceInfo> getDefaultNamespace() {
        return Optional.ofNullable(defaultNamespace);
    }

    public @Override Optional<NamespaceInfo> findOneByURI(String uri) {
        requireNonNull(uri);
        return findFirst(NamespaceInfo.class, ns -> uri.equals(ns.getURI()));
    }

    public @Override Stream<NamespaceInfo> findAllByURI(String uri) {
        requireNonNull(uri);
        return list(NamespaceInfo.class, ns -> ns.getURI().equals(uri));
    }

    public @Override void unsetDefaultNamespace() {
        defaultNamespace = null;
    }
}
