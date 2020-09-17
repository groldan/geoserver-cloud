/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.catalog.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class HTTPStore extends Store {
    private String capabilitiesURL;
    private String username;
    private String password;
    private int maxConnections;
    private int readTimeout;
    private int connectTimeout;
    private boolean useConnectionPooling;
}
