/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.config.dto.mapper;

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.Info;
import org.geoserver.cog.CogSettings;
import org.geoserver.cog.CogSettingsStore;
import org.geoserver.config.ContactInfo;
import org.geoserver.config.CoverageAccessInfo;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.JAIInfo;
import org.geoserver.config.LoggingInfo;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.SettingsInfo;
import org.geoserver.config.impl.ServiceInfoImpl;
import org.geoserver.gwc.wmts.WMTSInfo;
import org.geoserver.gwc.wmts.WMTSInfoImpl;
import org.geoserver.jackson.databind.catalog.dto.CatalogInfoDto;
import org.geoserver.jackson.databind.catalog.dto.InfoDto;
import org.geoserver.jackson.databind.catalog.mapper.CatalogInfoMapper;
import org.geoserver.jackson.databind.catalog.mapper.GeoServerValueObjectsMapper;
import org.geoserver.jackson.databind.config.dto.CogSettingsDto;
import org.geoserver.jackson.databind.config.dto.CogSettingsStoreDto;
import org.geoserver.jackson.databind.config.dto.ConfigInfoDto;
import org.geoserver.jackson.databind.config.dto.ContactInfoDto;
import org.geoserver.jackson.databind.config.dto.CoverageAccess;
import org.geoserver.jackson.databind.config.dto.GeoServer;
import org.geoserver.jackson.databind.config.dto.JaiDto;
import org.geoserver.jackson.databind.config.dto.Logging;
import org.geoserver.jackson.databind.config.dto.ServiceInfoDto;
import org.geoserver.jackson.databind.config.dto.Settings;
import org.geoserver.wcs.WCSInfo;
import org.geoserver.wcs.WCSInfoImpl;
import org.geoserver.wfs.WFSInfo;
import org.geoserver.wfs.WFSInfoImpl;
import org.geoserver.wms.WMSInfo;
import org.geoserver.wms.WMSInfoImpl;
import org.geoserver.wps.WPSInfo;
import org.geoserver.wps.WPSInfoImpl;
import org.geotools.util.Version;
import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper to/from GeoServer config objects and their respective DTO
 * representations
 */
@Mapper(config = ConfigInfoMapperConfig.class)
@AnnotateWith(value = Generated.class)
public interface GeoServerConfigMapper {

    CatalogInfoMapper catalogInfoMapper = Mappers.getMapper(CatalogInfoMapper.class);
    GeoServerValueObjectsMapper VALUE_MAPPER = Mappers.getMapper(GeoServerValueObjectsMapper.class);

    @SuppressWarnings("unchecked")
    default <T extends Info> T genericInfoDtoToObject(InfoDto dto) {
        if (dto == null) return null;
        if (dto instanceof ConfigInfoDto configInfo) return toInfo(configInfo);
        if (dto instanceof CatalogInfoDto catalogInfo) return catalogInfoMapper.map(catalogInfo);

        //        // handle other Info types that are not root config objects but value objects
        //        if (dto instanceof ContactInfoDto c) return (T) contactInfo(c);
        //        if (dto instanceof AttributionInfoDto a) return (T) VALUE_MAPPER.dtoToInfo(a);
        //        if (dto instanceof DataLinkInfoDto d) return (T) VALUE_MAPPER.dtoToInfo(d);
        //        if (dto instanceof LegendInfoDto l) return (T) VALUE_MAPPER.dtoToInfo(l);
        //        if (dto instanceof MetadataLinkInfoDto m) return (T) VALUE_MAPPER.dtoToInfo(m);
        //
        //        if (dto instanceof ProcessGroupInfoDto p)
        //            return (T) Mappers.getMapper(WPSMapper.class).map(p);
        //
        //        if (dto instanceof ProcessInfoDto p)
        //            return (T) Mappers.getMapper(WPSMapper.class).map(p);

        throw new IllegalArgumentException(
                "Unknown config DTO type: " + dto.getClass().getCanonicalName());
    }

    @SuppressWarnings("unchecked")
    default <T extends InfoDto> T genericInfoToDto(Info info) {
        if (info == null) return null;
        if (info instanceof CatalogInfo catInfo) return (T) catalogInfoMapper.map(catInfo);
        if (info instanceof GeoServerInfo gs) return (T) toDto(gs);
        if (info instanceof SettingsInfo settings) return (T) toDto(settings);
        if (info instanceof LoggingInfo logging) return (T) toDto(logging);
        if (info instanceof ServiceInfo service) return (T) toDto(service);

        // handle other Info types that are not root config objects but value objects
        //        if (info instanceof ContactInfo c) return (T) contactInfo(c);
        //
        //        if (info instanceof org.geoserver.catalog.AttributionInfo a) return (T) VALUE_MAPPER.infoToDto(a);
        //
        //        if (info instanceof org.geoserver.catalog.DataLinkInfo d) return (T) VALUE_MAPPER.infoToDto(d);
        //
        //        if (info instanceof org.geoserver.catalog.LegendInfo l) return (T) VALUE_MAPPER.infoToDto(l);
        //
        //        if (info instanceof org.geoserver.catalog.MetadataLinkInfo m) return (T) VALUE_MAPPER.infoToDto(m);
        //
        //        if (info instanceof ProcessGroupInfo p)
        //            return (T) Mappers.getMapper(WPSMapper.class).map(p);
        //
        //        if (info instanceof ProcessInfo p)
        //            return (T) Mappers.getMapper(WPSMapper.class).map(p);

        throw new IllegalArgumentException(
                "Unknown config info type: " + info.getClass().getCanonicalName());
    }

    @SuppressWarnings("unchecked")
    default <T extends Info> T toInfo(ConfigInfoDto dto) {
        if (dto == null) return null;
        if (dto instanceof GeoServer gs) return (T) toInfo(gs);
        if (dto instanceof Settings settings) return (T) toInfo(settings);
        if (dto instanceof Logging logging) return (T) toInfo(logging);
        if (dto instanceof ServiceInfoDto service) return (T) toInfo(service);

        throw new IllegalArgumentException(
                "Unknown config DTO type: " + dto.getClass().getCanonicalName());
    }

    @Mapping(target = "id", ignore = true) // set by factory method
    @Mapping(target = "clientProperties", ignore = true)
    GeoServerInfo toInfo(GeoServer dto);

    GeoServer toDto(GeoServerInfo info);

    @Mapping(target = "id", ignore = true) // set by factory method
    @Mapping(target = "clientProperties", ignore = true)
    SettingsInfo toInfo(Settings dto);

    Settings toDto(SettingsInfo info);

    @Mapping(target = "tileCache", ignore = true)
    @Mapping(target = "JAI", ignore = true)
    JAIInfo jaiInfo(JaiDto dto);

    JaiDto jaiInfo(JAIInfo info);

    @Mapping(target = "id", ignore = true) // set by factory method
    LoggingInfo toInfo(Logging dto);

    Logging toDto(LoggingInfo info);

    @Mapping(target = "threadPoolExecutor", ignore = true)
    CoverageAccessInfo coverageAccessInfo(CoverageAccess dto);

    CoverageAccess coverageAccessInfo(CoverageAccessInfo info);

    @Mapping(target = "id", ignore = true) // set by factory method
    ContactInfo contactInfo(ContactInfoDto dto);

    ContactInfoDto contactInfo(ContactInfo info);

    default ServiceInfo toInfo(ServiceInfoDto dto) {
        if (dto == null) return null;
        if (dto instanceof ServiceInfoDto.WmsService wms) return toInfo(wms);
        if (dto instanceof ServiceInfoDto.WfsService wfs) return toInfo(wfs);
        if (dto instanceof ServiceInfoDto.WcsService wcs) return toInfo(wcs);
        if (dto instanceof ServiceInfoDto.WpsService wps) return toInfo(wps);
        if (dto instanceof ServiceInfoDto.WmtsService wmts) return toInfo(wmts);
        if (dto instanceof ServiceInfoDto.GenericService s) return toInfo(s);

        throw new IllegalArgumentException(
                "Unknown ServiceInfo type: " + dto.getClass().getCanonicalName());
    }

    default ServiceInfoDto toDto(ServiceInfo info) {
        if (info == null) return null;
        if (info instanceof WMSInfo wms) return toDto(wms);
        if (info instanceof WFSInfo wfs) return toDto(wfs);
        if (info instanceof WCSInfo wcs) return toDto(wcs);
        if (info instanceof WPSInfo wps) return toDto(wps);
        if (info instanceof WMTSInfo wmts) return toDto(wmts);
        if (info.getClass().equals(ServiceInfoImpl.class)) return toGenericService(info);

        throw new IllegalArgumentException(
                "Unknown ServiceInfo type: " + info.getClass().getCanonicalName());
    }

    /**
     * {@link ServiceInfo#getVersions()} does not parameterize the list, hence
     * Mapstruct assigns the {@code List<String>} as is
     */
    default List<org.geotools.util.Version> stringListToVersionList(List<String> list) {
        return list == null ? null : list.stream().map(Version::new).collect(toCollection(ArrayList::new));
    }

    @Mapping(target = "clientProperties", ignore = true)
    @Mapping(target = "geoServer", ignore = true)
    @Mapping(target = "versions", expression = "java(stringListToVersionList(dto.getVersions()))")
    WMSInfoImpl toInfo(ServiceInfoDto.WmsService dto);

    ServiceInfoDto.WmsService toDto(WMSInfo info);

    @Mapping(target = "clientProperties", ignore = true)
    @Mapping(target = "geoServer", ignore = true)
    @Mapping(target = "versions", expression = "java(stringListToVersionList(dto.getVersions()))")
    WFSInfoImpl toInfo(ServiceInfoDto.WfsService dto);

    ServiceInfoDto.WfsService toDto(WFSInfo info);

    @Mapping(target = "clientProperties", ignore = true)
    @Mapping(target = "geoServer", ignore = true)
    @Mapping(target = "versions", expression = "java(stringListToVersionList(dto.getVersions()))")
    WCSInfoImpl toInfo(ServiceInfoDto.WcsService dto);

    ServiceInfoDto.WcsService toDto(WCSInfo info);

    @Mapping(target = "clientProperties", ignore = true)
    @Mapping(target = "geoServer", ignore = true)
    @Mapping(target = "versions", expression = "java(stringListToVersionList(dto.getVersions()))")
    WPSInfoImpl toInfo(ServiceInfoDto.WpsService dto);

    ServiceInfoDto.WpsService toDto(WPSInfo info);

    @Mapping(target = "clientProperties", ignore = true)
    @Mapping(target = "geoServer", ignore = true)
    @Mapping(target = "versions", expression = "java(stringListToVersionList(dto.getVersions()))")
    WMTSInfoImpl toInfo(ServiceInfoDto.WmtsService dto);

    @Mapping(target = "clientProperties", ignore = true)
    @Mapping(target = "geoServer", ignore = true)
    @Mapping(target = "versions", expression = "java(stringListToVersionList(dto.getVersions()))")
    ServiceInfoImpl toInfo(ServiceInfoDto.GenericService dto);

    ServiceInfoDto.GenericService toGenericService(ServiceInfo info);

    ServiceInfoDto.WmtsService toDto(WMTSInfo info);

    CogSettings cogSettings(CogSettingsDto dto);

    CogSettingsDto cogSettings(CogSettings info);

    CogSettingsStore cogSettingsStore(CogSettingsStoreDto dto);

    CogSettingsStoreDto cogSettingsStore(CogSettingsStore info);
}
