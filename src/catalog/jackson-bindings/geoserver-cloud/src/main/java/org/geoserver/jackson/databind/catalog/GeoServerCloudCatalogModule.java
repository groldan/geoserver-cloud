/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.catalog;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.plugin.Patch;
import org.geoserver.catalog.plugin.Query;
import org.geoserver.jackson.databind.catalog.dto.PatchDto;
import org.geoserver.jackson.databind.catalog.dto.QueryDto;
import org.geoserver.jackson.databind.catalog.mapper.QueryMapper;
import org.geoserver.jackson.databind.config.GeoServerConfigModule;
import org.geoserver.jackson.databind.mapper.PatchMapper;
import org.geotools.jackson.databind.filter.GeoToolsFilterModule;
import org.geotools.jackson.databind.geojson.GeoToolsGeoJsonModule;
import org.geotools.jackson.databind.util.MapperDeserializer;
import org.geotools.jackson.databind.util.MapperSerializer;
import org.mapstruct.factory.Mappers;

/**
 * Jackson {@link Module} to handle GeoServer-Cloud specific {@link CatalogInfo}
 * bindings.
 *
 * <p>
 * Registers serializers and deserializers for {@link Patch} and {@link Query}.
 *
 * <p>
 * Depends on {@link GeoServerCatalogModule}, {@link GeoServerConfigModule} ,
 * {@link GeoToolsGeoJsonModule} and {@link GeoToolsFilterModule}.
 *
 * <p>
 * To register the module for a specific {@link ObjectMapper}, either:
 *
 * <pre>
 * <code>
 * ObjectMapper objectMapper = ...
 * objectMapper.findAndRegisterModules();
 * </code>
 * </pre>
 *
 * Or:
 *
 * <pre>
 * <code>
 * ObjectMapper objectMapper = ...
 * objectMapper.registerModule(new GeoToolsGeoJsonModule());
 * objectMapper.registerModule(new GeoToolsFilterModule());
 * objectMapper.registerModule(new GeoServerCatalogModule());
 * objectMapper.registerModule(new GeoServerConfigModule());
 * objectMapper.registerModule(new GeoServerCloudCatalogModule());
 * </code>
 * </pre>
 */
@SuppressWarnings("serial")
@Slf4j(topic = "org.geoserver.jackson.databind.catalog")
public class GeoServerCloudCatalogModule extends SimpleModule {

    static final PatchMapper PATCH_MAPPER = Mappers.getMapper(PatchMapper.class);
    static final QueryMapper QUERY_MAPPER = Mappers.getMapper(QueryMapper.class);

    public GeoServerCloudCatalogModule() {
        super(GeoServerCloudCatalogModule.class.getSimpleName(), new Version(1, 0, 0, null, null, null));

        log.debug("registering jackson de/serializers GeoServer Cloud specific Catalog types");

        addMapperSerializer(Patch.class, PATCH_MAPPER::patchToDto, PatchDto.class, PATCH_MAPPER::dtoToPatch);
        addMapperSerializer(Query.class, QUERY_MAPPER::queryToDto, QueryDto.class, QUERY_MAPPER::dtoToQuery);
    }

    /**
     * @param <T> object model type
     * @param <D> DTO type
     */
    private <T, D> void addMapperSerializer(
            Class<T> type, Function<T, D> serializerMapper, Class<D> dtoType, Function<D, T> deserializerMapper) {

        MapperSerializer<T, D> serializer = new MapperSerializer<>(type, serializerMapper);
        MapperDeserializer<D, T> deserializer = new MapperDeserializer<>(dtoType, deserializerMapper);
        super.addSerializer(type, serializer);
        super.addDeserializer(type, deserializer);
    }
}
