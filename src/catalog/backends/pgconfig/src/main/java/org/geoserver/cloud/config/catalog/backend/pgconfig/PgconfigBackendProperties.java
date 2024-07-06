/*
 * (c) 2023 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.config.catalog.backend.pgconfig;

import lombok.Data;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Configuration properties to set up the PostgreSQL connection pool used by the `pgconfig` Catalog
 * back-end
 *
 * @since 1.4
 */
@Data
@ConfigurationProperties(prefix = "geoserver.backend.pgconfig")
public class PgconfigBackendProperties {

    /**
     * Whether to use the pgconfig catalog back-end to host the GeoServer catalog and services
     * configuration. Only one catalog back-end can be enabled, and it's usually done by enabling
     * the corresponding Spring profile (e.g. runnning the applications with
     * -Dspring.profiles.active=pgconfig,...)
     */
    private boolean enabled;

    /** The PostgreSQL databse schema to use to host the catalog and services configuration. */
    private String schema = "public";

    /**
     * Whether to create the postgres schema if it doesn't exist. Make sure the user configured in
     * the connection pool has enough provileges. Note this is performed by Flyway migrations.
     */
    private boolean createSchema = true;

    /**
     * Whether to initialize the database. I.e. create the tables and views and run database
     * migrations when upgrading to a new version of the database schema. Note this is performed by
     * Flyway migrations.
     */
    private boolean initialize = true;

    private DataSourceProperties datasource;

    public String schema() {
        return StringUtils.hasLength(schema) ? schema : "public";
    }
}
