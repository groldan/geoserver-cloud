/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.catalog.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("AttributionInfo")
public class AttributionInfoDto extends ValueObjectInfoDto {
    private String title;
    private String href;
    private String logoURL;
    private int logoWidth;
    private int logoHeight;
    private String logoType;
}
