/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.config.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
    @JsonSubTypes.Type(value = GeoServer.class, name = "GeoServerInfo"),
    @JsonSubTypes.Type(value = Logging.class, name = "LoggingInfo"),
    @JsonSubTypes.Type(value = Settings.class, name = "SettingsInfo"),
    @JsonSubTypes.Type(value = Service.class)
})
public @Data class ConfigInfoDto {
    private String id;
}
