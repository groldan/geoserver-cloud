/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.config.catalog.backend.datadirectory;

import lombok.extern.slf4j.Slf4j;

import org.geoserver.platform.resource.DataDirectoryResourceStore;
import org.geoserver.platform.resource.FileSystemResourceStore;

import java.io.File;
import java.util.Objects;

import jakarta.servlet.ServletContext;

/**
 * {@link DataDirectoryResourceStore} that works both for webmvc (servlet) and reactive (WebFlux)
 * configurations.
 *
 * <p>Works just like a {@link FileSystemResourceStore}, without needing a {@code ServletContext} to
 * resolve the base directory, which shall be given to the constructor instead.
 */
@Slf4j
@SuppressWarnings("serial")
public class NoServletContextDataDirectoryResourceStore extends DataDirectoryResourceStore {

    public NoServletContextDataDirectoryResourceStore(File resourceDirectory) {
        Objects.requireNonNull(resourceDirectory, "root resource directory required");

        if (resourceDirectory.isFile()) {
            throw new IllegalArgumentException(
                    "Directory required, file present at this location %s"
                            .formatted(resourceDirectory));
        } else if (!resourceDirectory.isDirectory() && !resourceDirectory.mkdirs()) {
            throw new IllegalArgumentException(
                    "Unable to create directory %s".formatted(resourceDirectory));
        }
        this.setBaseDirectory(resourceDirectory);
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        log.debug("setServletContext(ServletContext) ignored, data directory explicitly provided.");
    }
}
