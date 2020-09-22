/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.catalog;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.geoserver.jackson.databind.catalog.dto.VersionDto;
import org.geotools.util.Version;

public class VersionDeserializer extends JsonDeserializer<Version> {

    public @Override Version deserialize(JsonParser parser, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        VersionDto dto = parser.readValueAs(VersionDto.class);
        return VersionSerializer.mapper.dtoToVersion(dto);
    }
}
