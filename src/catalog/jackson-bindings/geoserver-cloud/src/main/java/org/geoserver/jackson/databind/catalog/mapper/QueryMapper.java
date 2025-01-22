/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.catalog.mapper;

import lombok.Generated;
import org.geoserver.catalog.plugin.Query;
import org.geoserver.jackson.databind.catalog.dto.QueryDto;
import org.geoserver.jackson.databind.mapper.InfoReferenceMapper;
import org.geotools.jackson.databind.filter.mapper.GeoToolsValueMappers;
import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "default",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {GeoToolsValueMappers.class, ObjectFacotries.class, InfoReferenceMapper.class})
@AnnotateWith(value = Generated.class)
public interface QueryMapper {

    @Mapping(target = "withFilter", ignore = true)
    @SuppressWarnings("rawtypes")
    Query dtoToQuery(QueryDto dto);

    QueryDto queryToDto(@SuppressWarnings("rawtypes") Query query);
}
