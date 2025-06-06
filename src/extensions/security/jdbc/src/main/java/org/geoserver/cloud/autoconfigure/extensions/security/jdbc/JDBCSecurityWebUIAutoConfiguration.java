/* (c) 2025 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.cloud.autoconfigure.extensions.security.jdbc;

import org.geoserver.cloud.config.factory.ImportFilteredResource;
import org.geoserver.security.web.auth.AuthenticationFilterPanelInfo;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * Auto-configuration for the GeoServer JDBC security web UI components.
 *
 * <p>This configuration is only active when:
 * <ul>
 *   <li>The JDBC security extension is enabled
 *   <li>The GeoServer web UI classes are available on the classpath
 * </ul>
 *
 * <p>It registers the JDBC security web UI components like panel info classes
 * for configuration through the GeoServer web admin interface.
 *
 * @since 2.27.0.0
 */
@AutoConfiguration
@SuppressWarnings("java:S1118") // Suppress SonarLint warning, constructor needs to be public
@ConditionalOnJDBC
@ConditionalOnClass(AuthenticationFilterPanelInfo.class)
@ImportFilteredResource("jar:gs-web-sec-jdbc-.*!/applicationContext.xml")
public class JDBCSecurityWebUIAutoConfiguration {}
