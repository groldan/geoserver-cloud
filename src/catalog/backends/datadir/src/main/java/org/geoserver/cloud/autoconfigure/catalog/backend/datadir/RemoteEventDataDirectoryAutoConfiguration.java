/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.catalog.backend.datadir;

import org.geoserver.cloud.autoconfigure.catalog.event.ConditionalOnCatalogEvents;
import org.geoserver.cloud.event.remote.datadir.RemoteEventDataDirectoryConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * {@link AutoConfiguration @AutoConfiguration} to contribute beans related to handling remotely
 * produced catalog and config events
 *
 * @see RemoteEventDataDirectoryConfiguration
 */
@AutoConfiguration
@ConditionalOnDataDirectoryEnabled
@ConditionalOnCatalogEvents
@Import(RemoteEventDataDirectoryConfiguration.class)
public class RemoteEventDataDirectoryAutoConfiguration {}
