/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.catalog.dto;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LayerGroup extends Published {

    public enum Mode {
        SINGLE,
        OPAQUE_CONTAINER,
        NAMED,
        CONTAINER,
        EO
    }

    protected Mode mode = Mode.SINGLE;
    protected Boolean queryDisabled;
    protected InfoReference workspace;
    protected InfoReference rootLayer;
    protected InfoReference rootLayerStyle;
    protected List<InfoReference> layers;
    protected List<InfoReference> styles;
    protected List<MetadataLink> metadataLinks;
    protected Envelope bounds;
    private List<Keyword> keywords;
}
