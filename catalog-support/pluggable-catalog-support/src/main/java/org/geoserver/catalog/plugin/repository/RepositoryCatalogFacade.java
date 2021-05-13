/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import org.geoserver.catalog.CatalogFacade;
import org.geoserver.catalog.plugin.ExtendedCatalogFacade;

/** {@link CatalogFacade} extension backed by {@link CatalogInfoRepository} repositories */
public interface RepositoryCatalogFacade
        extends ExtendedCatalogFacade, CatalogInfoRepositoryHolder {}
