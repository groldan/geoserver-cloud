/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.heap;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.plugin.repository.StyleRepository;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

public class StyleInfoLookup extends CatalogInfoLookup<StyleInfo> implements StyleRepository {

    static final Function<StyleInfo, Name> STYLE_NAME_MAPPER =
            s ->
                    new NameImpl(
                            s.getWorkspace() != null ? s.getWorkspace().getId() : null,
                            s.getName());

    public StyleInfoLookup() {
        super(StyleInfo.class, STYLE_NAME_MAPPER);
    }

    public @Override Stream<StyleInfo> findAllByNullWorkspace() {
        return list(StyleInfo.class, s -> s.getWorkspace() == null);
    }

    public @Override Stream<StyleInfo> findAllByWorkspace(WorkspaceInfo ws) {
        requireNonNull(ws);
        return list(
                StyleInfo.class,
                s -> s.getWorkspace() != null && s.getWorkspace().getId().equals(ws.getId()));
    }

    public @Override Optional<StyleInfo> findByNameAndWordkspaceNull(String name) {
        requireNonNull(name);
        return findFirstByName(new NameImpl(null, name), StyleInfo.class);
    }

    public @Override Optional<StyleInfo> findByNameAndWorkspace(
            String name, WorkspaceInfo workspace) {
        requireNonNull(name);
        requireNonNull(workspace);
        return findFirstByName(new NameImpl(workspace.getId(), name), StyleInfo.class);
    }
}
