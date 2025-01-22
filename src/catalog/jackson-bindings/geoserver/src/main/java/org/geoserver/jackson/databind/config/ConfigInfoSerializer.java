/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.config;

import org.geoserver.catalog.Info;
import org.geoserver.jackson.databind.config.dto.ConfigInfoDto;
import org.geoserver.jackson.databind.config.dto.mapper.GeoServerConfigMapper;
import org.geotools.jackson.databind.util.MapperSerializer;
import org.mapstruct.factory.Mappers;

@SuppressWarnings("serial")
public class ConfigInfoSerializer<T extends Info> extends MapperSerializer<T, ConfigInfoDto> {

    private static final GeoServerConfigMapper mapper = Mappers.getMapper(GeoServerConfigMapper.class);

    public ConfigInfoSerializer(Class<T> type) {
        super(type, mapper::toDto);
    }
}
