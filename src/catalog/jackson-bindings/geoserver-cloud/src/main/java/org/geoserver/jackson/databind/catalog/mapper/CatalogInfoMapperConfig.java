/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.catalog.mapper;

import org.geoserver.jackson.databind.mapper.InfoReferenceMapper;
import org.geoserver.jackson.databind.mapper.PatchMapper;
import org.geotools.jackson.databind.filter.mapper.GeoToolsValueMappers;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
        componentModel = "default",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {
            ObjectFacotries.class,
            GeoToolsValueMappers.class,
            QueryMapper.class,
            InfoReferenceMapper.class,
            PatchMapper.class
        })
public class CatalogInfoMapperConfig {}
