/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.config.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.geoserver.catalog.LayerInfo.WMSInterpolation;
import org.geoserver.catalog.impl.AuthorityURL;
import org.geoserver.catalog.impl.LayerIdentifier;
import org.geoserver.config.ServiceInfo;
import org.geoserver.gwc.wmts.WMTSInfo;
import org.geoserver.jackson.databind.catalog.dto.Keyword;
import org.geoserver.jackson.databind.catalog.dto.MetadataLinkInfoDto;
import org.geoserver.jackson.databind.catalog.dto.MetadataMapDto;
import org.geoserver.security.CatalogMode;
import org.geoserver.wfs.GMLInfoImpl;
import org.geoserver.wfs.WFSInfo.ServiceLevel;
import org.geoserver.wfs.WFSInfo.Version;
import org.geoserver.wms.CacheConfiguration;
import org.geoserver.wms.WatermarkInfoImpl;
import org.geotools.coverage.grid.io.OverviewPolicy;

/** DTO for {@link ServiceInfo} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ServiceInfoDto.WmsService.class),
    @JsonSubTypes.Type(value = ServiceInfoDto.WfsService.class),
    @JsonSubTypes.Type(value = ServiceInfoDto.WcsService.class),
    @JsonSubTypes.Type(value = ServiceInfoDto.WpsService.class),
    @JsonSubTypes.Type(value = ServiceInfoDto.WmtsService.class),
    @JsonSubTypes.Type(value = ServiceInfoDto.GenericService.class)
})
public abstract class ServiceInfoDto extends ConfigInfoDto {
    private String name;
    private String workspace;
    private boolean citeCompliant;
    private boolean enabled;
    private String onlineResource;
    private String title;
    private String Abstract;
    private String maintainer;
    private String fees;
    private String accessConstraints;
    private List<String> versions;
    private List<Keyword> keywords;
    private List<String> exceptionFormats;
    private MetadataLinkInfoDto metadataLink;
    private String outputStrategy;
    private String schemaBaseURL;
    private boolean verbose;
    private MetadataMapDto metadata;

    /**
     * @since geoserver 2.20.0
     */
    private Locale defaultLocale;

    /**
     * @since geoserver 2.20.0
     */
    private Map<String, String> internationalTitle;

    /**
     * @since geoserver 2.20.0
     */
    private Map<String, String> internationalAbstract;

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @JsonTypeName("ServiceInfo")
    public static class GenericService extends ServiceInfoDto {}

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @JsonTypeName("WMSInfo")
    public static class WmsService extends ServiceInfoDto {
        // Works well as POJO, no need to create a separate DTO
        private WatermarkInfoImpl watermark;
        // enum, direct use
        private WMSInterpolation interpolation;
        private List<String> SRS;
        private Set<String> GetMapMimeTypes;
        private boolean GetMapMimeTypeCheckingEnabled;
        private Set<String> GetFeatureInfoMimeTypes;
        private boolean GetFeatureInfoMimeTypeCheckingEnabled;
        private Boolean BBOXForEachCRS;
        private int maxBuffer;
        private int maxRequestMemory;
        private int maxRenderingTime;
        private int maxRenderingErrors;
        private List<AuthorityURL> authorityURLs;
        private List<LayerIdentifier> identifiers;
        private String rootLayerTitle;
        private String rootLayerAbstract;
        private Boolean dynamicStylingDisabled;
        private boolean featuresReprojectionDisabled;
        private int maxRequestedDimensionValues;
        // CacheConfiguration is a POJO, use it directly
        private CacheConfiguration cacheConfiguration;
        private int remoteStyleMaxRequestTime;
        private int remoteStyleTimeout;

        /**
         * @since geoserver 2.19.2
         */
        private boolean defaultGroupStyleEnabled;

        /**
         * @since geoserver 2.20.0
         */
        private Map<String, String> internationalRootLayerTitle;

        /**
         * @since geoserver 2.20.0
         */
        private Map<String, String> internationalRootLayerAbstract;

        /**
         * @since geoserver 2.22.0
         */
        private List<String> allowedURLsForAuthForwarding;

        /**
         * @since geoserver 2.22.0
         */
        private boolean autoEscapeTemplateValues;

        /**
         * @since geoserver 2.24.0
         */
        private boolean transformFeatureInfoDisabled;

        /**
         * @since geoserver 2.24.2
         */
        private Boolean exceptionOnInvalidDimension;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @JsonTypeName("WFSInfo")
    public static class WfsService extends ServiceInfoDto {
        private Map<Version, GMLInfoImpl> GML;
        private int maxFeatures;
        private ServiceLevel serviceLevel;
        private boolean featureBounding;
        private boolean canonicalSchemaLocation;
        private boolean encodeFeatureMember;
        private boolean hitsIgnoreMaxFeatures;
        private boolean includeWFSRequestDumpFile;
        private Integer maxNumberOfFeaturesForPreview;
        private List<String> SRS;
        private Boolean allowGlobalQueries;
        private boolean simpleConversionEnabled;

        /**
         * @since geoserver 2.22.0
         */
        private boolean getFeatureOutputTypeCheckingEnabled;

        /**
         * @since geoserver 2.22.0
         */
        private Set<String> getFeatureOutputTypes = new HashSet<>();

        /**
         * @since geoserver 2.24.0
         */
        private String csvDateFormat;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @JsonTypeName("WCSInfo")
    public static class WcsService extends ServiceInfoDto {
        private boolean GMLPrefixing;
        private long maxInputMemory;
        private long maxOutputMemory;
        private OverviewPolicy overviewPolicy;
        private boolean subsamplingEnabled;
        private boolean LatLon;
        private List<String> SRS;
        private int maxRequestedDimensionValues;
        private int defaultDeflateCompressionLevel;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @JsonTypeName("WPSInfo")
    public static class WpsService extends ServiceInfoDto {
        private double connectionTimeout;
        private int resourceExpirationTimeout;
        private int maxSynchronousProcesses;
        private int maxAsynchronousProcesses;
        private List<ProcessGroupInfoDto> processGroups;
        private String storageDirectory;
        private CatalogMode catalogMode;
        private int maxComplexInputSize;
        private int maxAsynchronousExecutionTime;
        private Integer maxAsynchronousTotalTime;
        private int maxSynchronousExecutionTime;
        private Integer maxSynchronousTotalTime;

        private String externalOutputDirectory;
        private boolean remoteInputDisabled;
    }

    /** DTO for {@link WMTSInfo} */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @JsonTypeName("WMTSInfo")
    public static class WmtsService extends ServiceInfoDto {}
}
