/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.cloud.config.catalog.backend.pgconfig;

import java.io.File;
import lombok.NonNull;
import org.geoserver.cloud.backend.pgconfig.resource.PgconfigResourceStore;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.platform.resource.Resource;

/**
 * @since 1.4
 */
public class PgconfigGeoServerResourceLoader extends GeoServerResourceLoader {

    /**
     * @param resourceStore
     */
    public PgconfigGeoServerResourceLoader(@NonNull PgconfigResourceStore resourceStore) {
        super(resourceStore);
        Resource root = resourceStore.get("");
        File baseDirectory = root.dir();
        setBaseDirectory(baseDirectory);
    }
}
