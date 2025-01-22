/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.catalog.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * DTO for {@link org.geoserver.catalog.impl.LayerGroupStyle}
 *
 * @see org.geoserver.catalog.impl.LayerGroupStyle
 * @since 1.0-RC2 (geoserver 2.21.0)
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("LayerGroupStyle")
public class LayerGroupStyleDto extends ValueObjectInfoDto {

    /** The style name as a StyleInfo. */
    private Style name;

    /** The list of contained PublishedInfo. */
    private List<String> layers;

    /** The List of StyleInfo for {@link #getLayers() the layers} */
    private List<String> styles;

    private String title;
    private Map<String, String> internationalTitle;

    @SuppressWarnings("java:S116")
    private String Abstract;

    private Map<String, String> internationalAbstract;
}
