package org.geoserver.jackson.databind.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.assertj.core.api.ObjectAssert;
import org.geoserver.catalog.AttributeTypeInfo;
import org.geoserver.catalog.AuthorityURLInfo;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.CoverageDimensionInfo;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.DataLinkInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.DimensionDefaultValueSetting;
import org.geoserver.catalog.DimensionDefaultValueSetting.Strategy;
import org.geoserver.catalog.DimensionInfo;
import org.geoserver.catalog.DimensionPresentation;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.HTTPStoreInfo;
import org.geoserver.catalog.Info;
import org.geoserver.catalog.Keyword;
import org.geoserver.catalog.KeywordInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerIdentifierInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.LegendInfo;
import org.geoserver.catalog.MetadataLinkInfo;
import org.geoserver.catalog.MetadataMap;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.PublishedInfo;
import org.geoserver.catalog.PublishedType;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.SLDHandler;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.catalog.WMSStoreInfo;
import org.geoserver.catalog.WMTSLayerInfo;
import org.geoserver.catalog.WMTSStoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.AttributionInfoImpl;
import org.geoserver.catalog.impl.LayerGroupStyle;
import org.geoserver.catalog.impl.LayerGroupStyleImpl;
import org.geoserver.catalog.impl.LayerIdentifier;
import org.geoserver.catalog.impl.LegendInfoImpl;
import org.geoserver.catalog.impl.ModificationProxy;
import org.geoserver.cog.CogSettings;
import org.geoserver.cog.CogSettings.RangeReaderType;
import org.geoserver.ows.util.OwsUtils;
import org.geotools.api.coverage.grid.GridEnvelope;
import org.geotools.api.coverage.grid.GridGeometry;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.jdbc.VirtualTable;
import org.geotools.measure.Measure;
import org.geotools.referencing.CRS;
import org.geotools.util.NumberRange;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import si.uom.SI;

@TestMethodOrder(MethodOrderer.MethodName.class)
@SuppressWarnings("java:1854")
class GeoServerCatalogModuleBackwardsCompatibilityTest extends BackwardsCompatibilityTestSupport {

    @Test
    void measure() {
        assertThat(decode("\"100.5m\"", Measure.class)).isEqualTo(new Measure(100.5, SI.METRE));
        assertThat(decode("\"0.75rad/s\"", Measure.class)).isEqualTo(new Measure(.75, SI.RADIAN_PER_SECOND));
    }

    @Test
    void measureAsFilterLiteral() {
        final String json =
                """
                {
                  "PropertyIsEqualTo" : {
                    "matchAction" : "ANY",
                    "expression1" : {
                      "PropertyName" : {
                        "propertyName" : "prop"
                      }
                    },
                    "expression2" : {
                      "Literal" : {
                        "type" : "org.geotools.measure.Measure",
                        "value" : "1000m"
                      }
                    },
                    "matchingCase" : true
                  }
                }
                """;

        testFilterLiteral(new Measure(1000, SI.METRE), json);
    }

    @Test
    void measureListAsFilterLiteral() {
        final String json =
                """
                {
                  "PropertyIsEqualTo" : {
                    "matchAction" : "ANY",
                    "expression1" : {
                      "PropertyName" : {
                        "propertyName" : "prop"
                      }
                    },
                    "expression2" : {
                      "Literal" : {
                        "type" : "java.util.List",
                        "contentType" : "org.geotools.measure.Measure",
                        "value" : [ "1000m", "100m" ]
                      }
                    },
                    "matchingCase" : true
                  }
                }
                """;

        testFilterLiteral(List.of(new Measure(1000, SI.METRE), new Measure(100, SI.METRE)), json);
    }

    @Test
    void coverageDimensionInfo() {
        final String json =
                """
                {
                  "id" : "802-68-9238",
                  "name" : "bergstrom.biz",
                  "description" : "evolve next-generation models",
                  "range" : {
                    "min" : 0.0,
                    "max" : 255.0,
                    "minIncluded" : true,
                    "maxIncluded" : true
                  },
                  "nullValues" : [ 0.0 ],
                  "unit" : "unit",
                  "dimensionType" : "UNSIGNED_1BIT"
                }
                """;
        CoverageDimensionInfo cdi = data.faker().coverageDimensionInfo();
        OwsUtils.set(cdi, "id", "802-68-9238");
        cdi.setName("bergstrom.biz");
        cdi.setDescription("evolve next-generation models");
        CoverageDimensionInfo actual = decode(json, CoverageDimensionInfo.class);
        assertThat(actual).isEqualTo(cdi);
    }

    @Test
    void coverageDimensionInfoAsFilterLiteral() {
        final String json =
                """
                {
                  "PropertyIsEqualTo" : {
                    "matchAction" : "ANY",
                    "expression1" : {
                      "PropertyName" : {
                        "propertyName" : "prop"
                      }
                    },
                    "expression2" : {
                      "Literal" : {
                        "type" : "java.util.List",
                        "contentType" : "org.geoserver.catalog.CoverageDimensionInfo",
                        "value" : [ {
                          "id" : "802-68-9238",
                          "name" : "bergstrom.biz",
                          "description" : "evolve next-generation models",
                          "range" : {
                            "min" : 0.0,
                            "max" : 255.0,
                            "minIncluded" : true,
                            "maxIncluded" : true
                          },
                          "nullValues" : [ 0.0 ],
                          "unit" : "unit",
                          "dimensionType" : "UNSIGNED_1BIT"
                        }, {
                          "id" : "802-68-9238",
                          "name" : "bergstrom.biz",
                          "description" : "evolve next-generation models",
                          "range" : {
                            "min" : 0.0,
                            "max" : 255.0,
                            "minIncluded" : true,
                            "maxIncluded" : true
                          },
                          "nullValues" : [ 0.0 ],
                          "unit" : "unit",
                          "dimensionType" : "UNSIGNED_1BIT"
                        } ]
                      }
                    },
                    "matchingCase" : true
                  }
                }
                """;
        CoverageDimensionInfo cdi = data.faker().coverageDimensionInfo();
        OwsUtils.set(cdi, "id", "802-68-9238");
        cdi.setName("bergstrom.biz");
        cdi.setDescription("evolve next-generation models");
        testFilterLiteral(List.of(cdi, cdi), json);
    }

    @Test
    void metadataLinkInfo() {
        final String json =
                """
                {
                  "id" : "898-34-7145",
                  "type" : "type",
                  "about" : "matrix",
                  "metadataType" : "metadataType",
                  "content" : "www.rempel-anderson.com"
                }
                """;
        MetadataLinkInfo expected = data.faker().metadataLink();
        OwsUtils.set(expected, "id", "898-34-7145");
        expected.setContent("www.rempel-anderson.com");
        expected.setAbout("matrix");

        Object actual = decode(json, MetadataLinkInfo.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void metadataLinkInfoAsFilterLiteral() {
        final String json =
                """
                {
                  "PropertyIsEqualTo" : {
                    "matchAction" : "ANY",
                    "expression1" : {
                      "PropertyName" : {
                        "propertyName" : "prop"
                      }
                    },
                    "expression2" : {
                      "Literal" : {
                        "type" : "org.geoserver.catalog.MetadataLinkInfo",
                        "value" : {
                          "id" : "898-34-7145",
                          "type" : "type",
                          "about" : "matrix",
                          "metadataType" : "metadataType",
                          "content" : "www.rempel-anderson.com"
                        }
                      }
                    },
                    "matchingCase" : true
                  }
                }
                """;
        MetadataLinkInfo expected = data.faker().metadataLink();
        OwsUtils.set(expected, "id", "898-34-7145");
        expected.setContent("www.rempel-anderson.com");
        expected.setAbout("matrix");

        testFilterLiteral(expected, json);
    }

    @Test
    void numberRange() {
        final String json =
                """
                {
                  "min" : 4.9E-324,
                  "max" : 0.0,
                  "minIncluded" : true,
                  "maxIncluded" : true
                }
                """;
        NumberRange<Double> expected = NumberRange.create(Double.MIN_VALUE, 0d);
        assertThat(decode(json, NumberRange.class)).isEqualTo(expected);
    }

    @Test
    void numberRangeAsFilterLiteral() {
        final String json =
                """
                {
                  "PropertyIsEqualTo" : {
                    "matchAction" : "ANY",
                    "expression1" : {
                      "PropertyName" : {
                        "propertyName" : "prop"
                      }
                    },
                    "expression2" : {
                      "Literal" : {
                        "type" : "org.geotools.util.NumberRange",
                        "value" : {
                          "min" : 4.9E-324,
                          "max" : 0.0,
                          "minIncluded" : true,
                          "maxIncluded" : true
                        }
                      }
                    },
                    "matchingCase" : true
                  }
                }
                """;
        NumberRange<Double> expected = NumberRange.create(Double.MIN_VALUE, 0d);
        testFilterLiteral(expected, json);
    }

    @Test
    void layerIdentifierInfo() {
        final String json =
                """
                {
                  "authority" : "www.goldnerinc.co",
                  "identifier" : "881-86-0744"
                }
                """;
        LayerIdentifierInfo expected = new LayerIdentifier();
        expected.setAuthority("www.goldnerinc.co");
        expected.setIdentifier("881-86-0744");
        assertThat(decode(json, LayerIdentifierInfo.class)).isEqualTo(expected);
    }

    @Test
    void layerIdentifierInfoAsFilterLiteral() {
        final String json =
                """
                {
                  "PropertyIsEqualTo" : {
                    "matchAction" : "ANY",
                    "expression1" : {
                      "PropertyName" : {
                        "propertyName" : "prop"
                      }
                    },
                    "expression2" : {
                      "Literal" : {
                        "type" : "org.geoserver.catalog.LayerIdentifierInfo",
                        "value" : {
                          "authority" : "www.goldnerinc.co",
                          "identifier" : "881-86-0744"
                        }
                      }
                    },
                    "matchingCase" : true
                  }
                }
                """;
        LayerIdentifierInfo expected = new LayerIdentifier();
        expected.setAuthority("www.goldnerinc.co");
        expected.setIdentifier("881-86-0744");
        testFilterLiteral(expected, json);
    }

    @Test
    void layerInfo() {
        final String json =
                """
                {
                  "@type" : "LayerInfo",
                  "id" : "layer1",
                  "name" : "ftName",
                  "title" : "Layer1",
                  "enabled" : true,
                  "advertised" : true,
                  "attribution" : {
                    "logoWidth" : 10,
                    "logoHeight" : 12
                  },
                  "defaultStyle" : "style1",
                  "styles" : [ "style1", "style2" ],
                  "resource" : "ft1",
                  "type" : "VECTOR",
                  "queryable" : true,
                  "opaque" : false,
                  "abstract" : "ftAbstract",
                  "dateCreated" : 1738432853803,
                  "dateModified" : 1738432853803
                }
                """;

        FeatureTypeInfo resource = data.featureTypeA;

        LayerInfo expected = data.faker()
                .layerInfo(
                        "layer1",
                        resource,
                        "Layer1",
                        true,
                        proxy("style1", StyleInfo.class),
                        proxy("style1", StyleInfo.class),
                        proxy("style2", StyleInfo.class));
        expected.setType(PublishedType.VECTOR);
        expected.setDateCreated(new Date(1738432853803L));
        expected.setDateModified(new Date(1738432853803L));
        expected.setAttribution(new AttributionInfoImpl());
        expected.getAttribution().setLogoWidth(10);
        expected.getAttribution().setLogoHeight(12);

        assertLayerInfo(expected, decode(json, LayerInfo.class));
        assertLayerInfo(expected, decode(json, PublishedInfo.class));
        assertLayerInfo(expected, decode(json, CatalogInfo.class));
        assertLayerInfo(expected, decode(json, Info.class));
    }

    private void assertLayerInfo(LayerInfo expected, Info actual) {
        assertPublishedInfo(expected, actual)
                .isInstanceOf(LayerInfo.class)
                .hasFieldOrPropertyWithValue("path", expected.getPath())
                .hasFieldOrPropertyWithValue(
                        "defaultStyle.id", expected.getDefaultStyle().getId())
                .hasFieldOrPropertyWithValue(
                        "resource.id", expected.getResource().getId())
                .hasFieldOrPropertyWithValue("legend", expected.getLegend())
                .hasFieldOrPropertyWithValue("queryable", expected.isQueryable())
                .hasFieldOrPropertyWithValue("opaque", expected.isOpaque())
                .hasFieldOrPropertyWithValue(
                        "defaultWMSInterpolationMethod", expected.getDefaultWMSInterpolationMethod());

        Set<String> expectedStyles =
                expected.getStyles().stream().map(StyleInfo::getId).collect(Collectors.toSet());
        Set<String> actualStyles =
                ((LayerInfo) actual).getStyles().stream().map(StyleInfo::getId).collect(Collectors.toSet());

        assertThat(actualStyles).isEqualTo(expectedStyles);
    }

    private ObjectAssert<Info> assertPublishedInfo(PublishedInfo expected, Info actual) {
        return assertCatalogInfo(expected, actual)
                .isInstanceOf(PublishedInfo.class)
                .hasFieldOrPropertyWithValue("type", expected.getType())
                .hasFieldOrPropertyWithValue("metadata", expected.getMetadata())
                .hasFieldOrPropertyWithValue("attribution", expected.getAttribution())
                .hasFieldOrPropertyWithValue("authorityURLs", expected.getAuthorityURLs())
                .hasFieldOrPropertyWithValue("internationalTitle", expected.getInternationalTitle())
                .hasFieldOrPropertyWithValue("internationalAbstract", expected.getInternationalAbstract());
    }

    @Test
    void featureTypeInfo() {
        final String json =
                """
                {
                  "@type" : "FeatureTypeInfo",
                  "id" : "ft1",
                  "name" : "ftName",
                  "namespace" : "ns1",
                  "store" : "ds1",
                  "nativeName" : "ftName",
                  "title" : "Title",
                  "description" : "ftDescription",
                  "keywords" : [ {
                    "value" : "value",
                    "language" : "es"
                  } ],
                  "enabled" : true,
                  "advertised" : true,
                  "serviceConfiguration" : false,
                  "simpleConversionEnabled" : false,
                  "internationalTitle" : {
                    "en" : "english title",
                    "fr-CA" : "titre anglais"
                  },
                  "internationalAbstract" : {
                    "en" : "english abstract",
                    "fr-CA" : "résumé anglais"
                  },
                  "maxFeatures" : 0,
                  "numDecimals" : 0,
                  "padWithZeros" : false,
                  "forcedDecimal" : false,
                  "overridingServiceSRS" : false,
                  "skipNumberMatched" : false,
                  "circularArcPresent" : false,
                  "encodeMeasures" : false,
                  "abstract" : "abstract"
                }
                """;
        FeatureTypeInfo expected = createTestFeatureType();
        expected.getAttributes().clear();

        assertFeatureTypeInfo(expected, decode(json, FeatureTypeInfo.class));
        assertFeatureTypeInfo(expected, decode(json, ResourceInfo.class));
        assertFeatureTypeInfo(expected, decode(json, CatalogInfo.class));
        assertFeatureTypeInfo(expected, decode(json, Info.class));
    }

    @Test
    void featureTypeInfofeatureTypeInfoWithAttributesList() {
        final String json =
                """
                {
                  "@type" : "FeatureTypeInfo",
                  "id" : "ft1",
                  "name" : "ftName",
                  "namespace" : "ns1",
                  "store" : "ds1",
                  "nativeName" : "ftName",
                  "title" : "Title",
                  "description" : "ftDescription",
                  "keywords" : [ {
                    "value" : "value",
                    "language" : "es"
                  } ],
                  "enabled" : true,
                  "advertised" : true,
                  "serviceConfiguration" : false,
                  "simpleConversionEnabled" : false,
                  "internationalTitle" : {
                    "en" : "english title",
                    "fr-CA" : "titre anglais"
                  },
                  "internationalAbstract" : {
                    "en" : "english abstract",
                    "fr-CA" : "résumé anglais"
                  },
                  "maxFeatures" : 0,
                  "numDecimals" : 0,
                  "padWithZeros" : false,
                  "forcedDecimal" : false,
                  "overridingServiceSRS" : false,
                  "skipNumberMatched" : false,
                  "circularArcPresent" : false,
                  "encodeMeasures" : false,
                  "abstract" : "abstract",
                  "attributes" : [ {
                    "name" : "name",
                    "featureType" : "ft1",
                    "minOccurs" : 0,
                    "maxOccurs" : 1,
                    "nillable" : true,
                    "binding" : "java.lang.String",
                    "source" : "name"
                  }, {
                    "name" : "id",
                    "featureType" : "ft1",
                    "minOccurs" : 0,
                    "maxOccurs" : 1,
                    "nillable" : true,
                    "binding" : "java.lang.String",
                    "source" : "\\\"id\\\""
                  }, {
                    "name" : "polygonProperty",
                    "featureType" : "ft1",
                    "minOccurs" : 0,
                    "maxOccurs" : 1,
                    "nillable" : true,
                    "binding" : "org.locationtech.jts.geom.Polygon",
                    "source" : "polygonProperty"
                  }, {
                    "name" : "centroid",
                    "featureType" : "ft1",
                    "minOccurs" : 0,
                    "maxOccurs" : 1,
                    "nillable" : true,
                    "binding" : "org.locationtech.jts.geom.Point",
                    "source" : "centroid"
                  }, {
                    "name" : "url",
                    "featureType" : "ft1",
                    "minOccurs" : 0,
                    "maxOccurs" : 1,
                    "nillable" : true,
                    "binding" : "java.net.URL",
                    "source" : "url"
                  }, {
                    "name" : "uuid",
                    "featureType" : "ft1",
                    "minOccurs" : 0,
                    "maxOccurs" : 1,
                    "nillable" : true,
                    "binding" : "java.util.UUID",
                    "source" : "uuid"
                  }, {
                    "name" : "boola",
                    "featureType" : "ft1",
                    "minOccurs" : 0,
                    "maxOccurs" : 1,
                    "nillable" : true,
                    "binding" : "boolean[]",
                    "source" : "boola"
                  }, {
                    "name" : "bytea",
                    "featureType" : "ft1",
                    "minOccurs" : 0,
                    "maxOccurs" : 1,
                    "nillable" : true,
                    "binding" : "byte[]",
                    "source" : "bytea"
                  }, {
                    "name" : "shorta",
                    "featureType" : "ft1",
                    "minOccurs" : 0,
                    "maxOccurs" : 1,
                    "nillable" : true,
                    "binding" : "short[]",
                    "source" : "shorta"
                  }, {
                    "name" : "inta",
                    "featureType" : "ft1",
                    "minOccurs" : 0,
                    "maxOccurs" : 1,
                    "nillable" : true,
                    "binding" : "int[]",
                    "source" : "inta"
                  }, {
                    "name" : "longa",
                    "featureType" : "ft1",
                    "minOccurs" : 0,
                    "maxOccurs" : 1,
                    "nillable" : true,
                    "binding" : "long[]",
                    "source" : "longa"
                  }, {
                    "name" : "floata",
                    "featureType" : "ft1",
                    "minOccurs" : 0,
                    "maxOccurs" : 1,
                    "nillable" : true,
                    "binding" : "float[]",
                    "source" : "floata"
                  }, {
                    "name" : "doublea",
                    "featureType" : "ft1",
                    "minOccurs" : 0,
                    "maxOccurs" : 1,
                    "nillable" : true,
                    "binding" : "double[]",
                    "source" : "doublea"
                  }, {
                    "name" : "stringa",
                    "featureType" : "ft1",
                    "minOccurs" : 0,
                    "maxOccurs" : 1,
                    "nillable" : true,
                    "binding" : "java.lang.String[]",
                    "source" : "stringa"
                  } ]
                }
                """;
        FeatureTypeInfo expected = createTestFeatureType();

        assertFeatureTypeInfo(expected, decode(json, FeatureTypeInfo.class));
        assertFeatureTypeInfo(expected, decode(json, ResourceInfo.class));
        assertFeatureTypeInfo(expected, decode(json, CatalogInfo.class));
        assertFeatureTypeInfo(expected, decode(json, Info.class));
    }

    private void assertFeatureTypeInfo(FeatureTypeInfo expected, Info actual) {
        assertResourceInfo(expected, actual)
                .isInstanceOf(FeatureTypeInfo.class)
                .hasFieldOrPropertyWithValue("cqlFilter", expected.getCqlFilter())
                .hasFieldOrPropertyWithValue("maxFeatures", expected.getMaxFeatures())
                .hasFieldOrPropertyWithValue("numDecimals", expected.getNumDecimals())
                .hasFieldOrPropertyWithValue("padWithZeros", expected.getPadWithZeros())
                .hasFieldOrPropertyWithValue("forcedDecimal", expected.getForcedDecimal())
                .hasFieldOrPropertyWithValue("responseSRS", expected.getResponseSRS())
                .hasFieldOrPropertyWithValue("overridingServiceSRS", expected.isOverridingServiceSRS())
                .hasFieldOrPropertyWithValue("skipNumberMatched", expected.getSkipNumberMatched())
                .hasFieldOrPropertyWithValue("circularArcPresent", expected.isCircularArcPresent())
                .hasFieldOrPropertyWithValue("encodeMeasures", expected.getEncodeMeasures())
                .hasFieldOrPropertyWithValue("linearizationTolerance", expected.getLinearizationTolerance());

        List<AttributeTypeInfo> expectedAttributes = expected.getAttributes();
        List<AttributeTypeInfo> actualAttributes = ((FeatureTypeInfo) actual).getAttributes();
        assertThat(actualAttributes).hasSameSizeAs(expectedAttributes);

        for (int i = 0; i < expectedAttributes.size(); i++) {
            assertAttribute(expectedAttributes.get(i), actualAttributes.get(i));
        }
    }

    private void assertAttribute(AttributeTypeInfo expected, AttributeTypeInfo actual) {

        assertThat(actual)
                .hasFieldOrPropertyWithValue("binding", expected.getBinding())
                .hasFieldOrPropertyWithValue("length", expected.getLength())
                .hasFieldOrPropertyWithValue("name", expected.getName())
                .hasFieldOrPropertyWithValue("source", expected.getSource())
                .hasFieldOrPropertyWithValue("description", expected.getDescription())
                .hasFieldOrPropertyWithValue("maxOccurs", expected.getMaxOccurs())
                .hasFieldOrPropertyWithValue("minOccurs", expected.getMinOccurs())
                .hasFieldOrPropertyWithValue("metadata", expected.getMetadata());
    }

    private FeatureTypeInfo createTestFeatureType() {
        KeywordInfo k = new Keyword("value");
        k.setLanguage("es");
        FeatureTypeInfo ft = ModificationProxy.unwrap(data.featureTypeA);
        ft.getKeywords().add(k);
        ft.setTitle("Title");
        ft.setAbstract("abstract");
        ft.setInternationalTitle(data.faker()
                .internationalString(Locale.ENGLISH, "english title", Locale.CANADA_FRENCH, "titre anglais"));
        ft.setInternationalAbstract(data.faker()
                .internationalString(Locale.ENGLISH, "english abstract", Locale.CANADA_FRENCH, "résumé anglais"));

        List<AttributeTypeInfo> attributes = GeoServerCatalogModuleTest.createTestAttributes(ft);
        ft.getAttributes().addAll(attributes);

        return ft;
    }

    @Test
    void wmsLayerInfo() {
        final String json =
                """
                {
                  "@type" : "WMSLayerInfo",
                  "id" : "wmsl-1",
                  "name" : "wmsLayer1",
                  "namespace" : "ns1",
                  "store" : "wms1",
                  "nativeName" : "native-name",
                  "enabled" : true,
                  "advertised" : true,
                  "serviceConfiguration" : false,
                  "simpleConversionEnabled" : false,
                  "preferredFormat" : "image/png",
                  "metadataBBoxRespected" : false
                }
                """;
        WMSLayerInfo expected = data.faker()
                .wmsLayerInfo(
                        "wmsl-1",
                        proxy("wms1", WMSStoreInfo.class),
                        proxy("ns1", NamespaceInfo.class),
                        "wmsLayer1",
                        true);
        expected.setNativeName("native-name");

        assertWmsLayer(expected, decode(json, WMSLayerInfo.class));
        assertWmsLayer(expected, decode(json, ResourceInfo.class));
        assertWmsLayer(expected, decode(json, CatalogInfo.class));
        assertWmsLayer(expected, decode(json, Info.class));
    }

    private void assertWmsLayer(WMSLayerInfo expected, Info actual) {
        assertResourceInfo(expected, actual)
                .isInstanceOf(WMSLayerInfo.class)
                .hasFieldOrPropertyWithValue("forcedRemoteStyle", expected.getForcedRemoteStyle())
                .hasFieldOrPropertyWithValue("preferredFormat", expected.getPreferredFormat())
                .hasFieldOrPropertyWithValue("minScale", expected.getMinScale())
                .hasFieldOrPropertyWithValue("maxScale", expected.getMaxScale())
                .hasFieldOrPropertyWithValue("metadataBBoxRespected", expected.isMetadataBBoxRespected())
                .hasFieldOrPropertyWithValue("selectedRemoteFormats", expected.getSelectedRemoteFormats())
                .hasFieldOrPropertyWithValue("selectedRemoteStyles", expected.getSelectedRemoteStyles());
    }

    @Test
    void coordinateReferenceSystemCustomCRS() {
        String json =
                """
                {
                  "wkt" : "PROJCS[\\\"UTM Zone 10, Northern Hemisphere\\\", GEOGCS[\\\"GRS 1980(IUGG, 1980)\\\", DATUM[\\\"unknown\\\", SPHEROID[\\\"GRS80\\\", 6378137.0, 298.257222101], TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]], PRIMEM[\\\"Greenwich\\\", 0.0], UNIT[\\\"degree\\\", 0.017453292519943295], AXIS[\\\"Longitude\\\", EAST], AXIS[\\\"Latitude\\\", NORTH]], PROJECTION[\\\"Transverse_Mercator\\\"], PARAMETER[\\\"central_meridian\\\", -123.0], PARAMETER[\\\"latitude_of_origin\\\", 0.0], PARAMETER[\\\"scale_factor\\\", 0.9996], PARAMETER[\\\"false_easting\\\", 1640419.947506562], PARAMETER[\\\"false_northing\\\", 0.0], UNIT[\\\"ft\\\", 0.3048], AXIS[\\\"X\\\", EAST], AXIS[\\\"Y\\\", NORTH]]"
                }
                """;
        CoordinateReferenceSystem crs = createCustomCrs();
        CoordinateReferenceSystem actual = decode(json, CoordinateReferenceSystem.class);
        assertThat(actual).isEqualTo(crs);
    }

    @SneakyThrows(FactoryException.class)
    private CoordinateReferenceSystem createCustomCrs() {
        String customWKT =
                """
                PROJCS[ "UTM Zone 10, Northern Hemisphere",
                  GEOGCS["GRS 1980(IUGG, 1980)",
                    DATUM["unknown",
                       SPHEROID["GRS80",6378137,298.257222101],
                       TOWGS84[0,0,0,0,0,0,0]
                    ],
                    PRIMEM["Greenwich",0],
                    UNIT["degree",0.0174532925199433]
                  ],
                  PROJECTION["Transverse_Mercator"],
                  PARAMETER["latitude_of_origin",0],
                  PARAMETER["central_meridian",-123],
                  PARAMETER["scale_factor",0.9996],
                  PARAMETER["false_easting",1640419.947506562],
                  PARAMETER["false_northing",0],
                  UNIT["Foot (International)",0.3048]
                ]""";

        CoordinateReferenceSystem crs = CRS.parseWKT(customWKT);
        return crs;
    }

    @Test
    void coordinateReferenceSystemCustomCRSAsFilterLiteral() {
        final String json =
                """
                {
                  "PropertyIsEqualTo" : {
                    "matchAction" : "ANY",
                    "expression1" : {
                      "PropertyName" : {
                        "propertyName" : "prop"
                      }
                    },
                    "expression2" : {
                      "Literal" : {
                        "type" : "org.geotools.api.referencing.crs.CoordinateReferenceSystem",
                        "value" : {
                           "wkt" : "PROJCS[\\\"UTM Zone 10, Northern Hemisphere\\\", GEOGCS[\\\"GRS 1980(IUGG, 1980)\\\", DATUM[\\\"unknown\\\", SPHEROID[\\\"GRS80\\\", 6378137.0, 298.257222101], TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]], PRIMEM[\\\"Greenwich\\\", 0.0], UNIT[\\\"degree\\\", 0.017453292519943295], AXIS[\\\"Longitude\\\", EAST], AXIS[\\\"Latitude\\\", NORTH]], PROJECTION[\\\"Transverse_Mercator\\\"], PARAMETER[\\\"central_meridian\\\", -123.0], PARAMETER[\\\"latitude_of_origin\\\", 0.0], PARAMETER[\\\"scale_factor\\\", 0.9996], PARAMETER[\\\"false_easting\\\", 1640419.947506562], PARAMETER[\\\"false_northing\\\", 0.0], UNIT[\\\"ft\\\", 0.3048], AXIS[\\\"X\\\", EAST], AXIS[\\\"Y\\\", NORTH]]"
                        }
                      }
                    },
                    "matchingCase" : true
                  }
                }
                """;
        CoordinateReferenceSystem crs = createCustomCrs();
        testFilterLiteral(crs, json);
    }

    @Test
    void coordinateReferenceSystemCustomCRSList() {
        final String json =
                """
                {
                  "PropertyIsEqualTo" : {
                    "matchAction" : "ANY",
                    "expression1" : {
                      "PropertyName" : {
                        "propertyName" : "prop"
                      }
                    },
                    "expression2" : {
                      "Literal" : {
                        "type" : "java.util.List",
                        "contentType" : "org.geotools.api.referencing.crs.CoordinateReferenceSystem",
                        "value" : [ {
                          "wkt" : "PROJCS[\\\"UTM Zone 10, Northern Hemisphere\\\", GEOGCS[\\\"GRS 1980(IUGG, 1980)\\\", DATUM[\\\"unknown\\\", SPHEROID[\\\"GRS80\\\", 6378137.0, 298.257222101], TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]], PRIMEM[\\\"Greenwich\\\", 0.0], UNIT[\\\"degree\\\", 0.017453292519943295], AXIS[\\\"Longitude\\\", EAST], AXIS[\\\"Latitude\\\", NORTH]], PROJECTION[\\\"Transverse_Mercator\\\"], PARAMETER[\\\"central_meridian\\\", -123.0], PARAMETER[\\\"latitude_of_origin\\\", 0.0], PARAMETER[\\\"scale_factor\\\", 0.9996], PARAMETER[\\\"false_easting\\\", 1640419.947506562], PARAMETER[\\\"false_northing\\\", 0.0], UNIT[\\\"ft\\\", 0.3048], AXIS[\\\"X\\\", EAST], AXIS[\\\"Y\\\", NORTH]]"
                        }, {
                          "wkt" : "PROJCS[\\\"UTM Zone 10, Northern Hemisphere\\\", GEOGCS[\\\"GRS 1980(IUGG, 1980)\\\", DATUM[\\\"unknown\\\", SPHEROID[\\\"GRS80\\\", 6378137.0, 298.257222101], TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]], PRIMEM[\\\"Greenwich\\\", 0.0], UNIT[\\\"degree\\\", 0.017453292519943295], AXIS[\\\"Longitude\\\", EAST], AXIS[\\\"Latitude\\\", NORTH]], PROJECTION[\\\"Transverse_Mercator\\\"], PARAMETER[\\\"central_meridian\\\", -123.0], PARAMETER[\\\"latitude_of_origin\\\", 0.0], PARAMETER[\\\"scale_factor\\\", 0.9996], PARAMETER[\\\"false_easting\\\", 1640419.947506562], PARAMETER[\\\"false_northing\\\", 0.0], UNIT[\\\"ft\\\", 0.3048], AXIS[\\\"X\\\", EAST], AXIS[\\\"Y\\\", NORTH]]"
                        } ]
                      }
                    },
                    "matchingCase" : true
                  }
                }
                """;
        CoordinateReferenceSystem crs = createCustomCrs();
        testFilterLiteral(List.of(crs, crs), json);
    }

    @Test
    void styleInfo() {
        final String json =
                """
                {
                  "@type" : "StyleInfo",
                  "id" : "style1",
                  "name" : "style1",
                  "workspace" : "ws1",
                  "format" : "sld",
                  "formatVersion" : {
                    "value" : "1.0.0"
                  },
                  "filename" : "styleFilename"
                }
                """;
        StyleInfo expected = data.style1;
        expected.setFormatVersion(SLDHandler.VERSION_10);
        expected.setFormat(SLDHandler.FORMAT);
        expected.setWorkspace(proxy("ws1", WorkspaceInfo.class));
        assertStyleInfo(expected, decode(json, StyleInfo.class));
    }

    private void assertStyleInfo(StyleInfo expected, Info actual) {
        assertCatalogInfo(expected, actual)
                .isInstanceOf(StyleInfo.class)
                .hasFieldOrPropertyWithValue("name", expected.getName())
                .hasFieldOrPropertyWithValue("filename", expected.getFilename())
                .hasFieldOrPropertyWithValue("format", expected.getFormat())
                .hasFieldOrPropertyWithValue("formatVersion", expected.getFormatVersion())
                .hasFieldOrPropertyWithValue("metadata", expected.getMetadata())
                .hasFieldOrPropertyWithValue(
                        "workspace.id", expected.getWorkspace().getId());
    }

    @Test
    void layerGroupInfo() {
        final String json =
                """
                {
                  "@type" : "LayerGroupInfo",
                  "id" : "lg1",
                  "name" : "layerGroup",
                  "workspace" : "ws3",
                  "rootLayer" : "L1",
                  "rootLayerStyle": "S1",
                  "title" : "LG Title",
                  "enabled" : true,
                  "advertised" : true,
                  "mode" : "SINGLE",
                  "queryDisabled" : false,
                  "layers" : [ "layer1" ],
                  "styles" : [ "style1" ],
                  "internationalTitle" : {
                    "en" : "english title",
                    "fr-CA" : "titre anglais"
                  },
                  "internationalAbstract" : {
                    "en" : "english abstract",
                    "fr-CA" : "résumé anglais"
                  },
                  "layerGroupStyles" : [ {
                    "id" : "lgsid",
                    "layers" : [ null ],
                    "styles" : [ "test-style-id" ],
                    "title" : "Lgs Title",
                    "internationalTitle" : {
                      "fr" : "French title",
                      "it" : "Italian title"
                    },
                    "internationalAbstract" : {
                      "fr" : "French abstract",
                      "it" : "Italian abstract"
                    },
                    "abstract" : "Lgs Abstract"
                  } ],
                  "abstract" : "LG abstract"
                }
                """;

        LayerGroupInfo expected = data.layerGroup1;
        expected.setWorkspace(proxy("ws3", WorkspaceInfo.class));
        expected.setRootLayer(proxy("L1", LayerInfo.class));
        expected.setRootLayerStyle(proxy("S1", StyleInfo.class));
        expected.setTitle("LG Title");
        expected.setAbstract("LG abstract");
        expected.setInternationalTitle(data.faker()
                .internationalString(Locale.ENGLISH, "english title", Locale.CANADA_FRENCH, "titre anglais"));
        expected.setInternationalAbstract(data.faker()
                .internationalString(Locale.ENGLISH, "english abstract", Locale.CANADA_FRENCH, "résumé anglais"));

        LayerGroupStyle lgs = new LayerGroupStyleImpl();
        lgs.setId("lgsid");
        lgs.setTitle("Lgs Title");
        lgs.setAbstract("Lgs Abstract");
        lgs.setInternationalTitle(
                data.faker().internationalString(Locale.ITALIAN, "Italian title", Locale.FRENCH, "French title"));
        lgs.setInternationalAbstract(
                data.faker().internationalString(Locale.ITALIAN, "Italian abstract", Locale.FRENCH, "French abstract"));

        lgs.setLayers(Arrays.asList(data.createLayer(data.coverageA, data.style1)));
        lgs.setStyles(Arrays.asList(data.createStyle("test-style")));

        expected.setLayerGroupStyles(Arrays.asList(lgs));

        assertLayerGroupInfo(expected, decode(json, LayerGroupInfo.class));
        assertLayerGroupInfo(expected, decode(json, PublishedInfo.class));
        assertLayerGroupInfo(expected, decode(json, CatalogInfo.class));
        assertLayerGroupInfo(expected, decode(json, Info.class));
    }

    private void assertLayerGroupInfo(LayerGroupInfo expected, Info actual) {
        assertPublishedInfo(expected, actual)
                .isInstanceOf(LayerGroupInfo.class)
                .hasFieldOrPropertyWithValue("name", expected.getName())
                .hasFieldOrPropertyWithValue("title", expected.getTitle())
                .hasFieldOrPropertyWithValue("abstract", expected.getAbstract())
                .hasFieldOrPropertyWithValue("enabled", expected.isEnabled())
                .hasFieldOrPropertyWithValue("advertised", expected.isAdvertised())
                .hasFieldOrPropertyWithValue("mode", expected.getMode())
                .hasFieldOrPropertyWithValue("queryDisabled", expected.isQueryDisabled())
                .hasFieldOrPropertyWithValue(
                        "workspace.id", expected.getWorkspace().getId())
                .hasFieldOrPropertyWithValue(
                        "rootLayer.id", expected.getRootLayer().getId())
                .hasFieldOrPropertyWithValue(
                        "rootLayerStyle.id", expected.getRootLayerStyle().getId())
                .hasFieldOrPropertyWithValue("bounds", expected.getBounds())
                .hasFieldOrPropertyWithValue("keywords", expected.getKeywords());
        LayerGroupInfo actualLayerGroup = (LayerGroupInfo) actual;
        actualLayerGroup.getLayers();
        actualLayerGroup.getStyles();
    }

    @Test
    void authorityURLInfo() {
        final String json =
                """
                {
                  "name" : "name",
                  "href" : "href"
                }
                """;

        AuthorityURLInfo expected = data.faker().authorityURLInfo();
        expected.setName("name");
        expected.setHref("href");

        assertThat(decode(json, AuthorityURLInfo.class)).isEqualTo(expected);
    }

    @Test
    void authorityURLInfoAsFilterLiteral() {
        final String json =
                """
                {
                  "PropertyIsEqualTo" : {
                    "matchAction" : "ANY",
                    "expression1" : {
                      "PropertyName" : {
                        "propertyName" : "prop"
                      }
                    },
                    "expression2" : {
                      "Literal" : {
                        "type" : "org.geoserver.catalog.AuthorityURLInfo",
                        "value" : {
                          "name" : "name",
                          "href" : "href"
                        }
                      }
                    },
                    "matchingCase" : true
                  }
                }
                """;
        AuthorityURLInfo literal = data.faker().authorityURLInfo();
        literal.setName("name");
        literal.setHref("href");
        testFilterLiteral(literal, json);
    }

    @Test
    void authorityURLInfoListFilterLiteral() {
        final String json =
                """
                {
                  "PropertyIsEqualTo" : {
                    "matchAction" : "ANY",
                    "expression1" : {
                      "PropertyName" : {
                        "propertyName" : "prop"
                      }
                    },
                    "expression2" : {
                      "Literal" : {
                        "type" : "java.util.List",
                        "contentType" : "org.geoserver.catalog.AuthorityURLInfo",
                        "value" : [ {
                          "name" : "name",
                          "href" : "href"
                        }, {
                          "name" : "name",
                          "href" : "href"
                        } ]
                      }
                    },
                    "matchingCase" : true
                  }
                }
                """;
        AuthorityURLInfo literal = data.faker().authorityURLInfo();
        literal.setName("name");
        literal.setHref("href");
        testFilterLiteral(List.of(literal, literal), json);
    }

    @Test
    void dataLinkInfo() {
        final String json =
                """
                {
                  "id" : "725-06-2339",
                  "about" : "Truly wonderful, the mind of a child is.",
                  "type" : "heidenreich",
                  "content" : "www.derrick-johns.biz"
                }
                """;

        DataLinkInfo expected = data.faker()
                .dataLinkInfo(
                        "725-06-2339",
                        "Truly wonderful, the mind of a child is.",
                        "heidenreich",
                        "www.derrick-johns.biz");
        DataLinkInfo actual = decode(json, DataLinkInfo.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @SneakyThrows(FactoryException.class)
    void referencedEnvelope() {
        final String json =
                """
                {
                  "crs" : {
                    "srs" : "EPSG:4326"
                  },
                  "coordinates" : [ -180.0, 180.0, -90.0, 90.0 ]
                }
                """;

        CoordinateReferenceSystem wgs84LonLat = CRS.decode("EPSG:4326", true);
        ReferencedEnvelope expected = new ReferencedEnvelope(-180, 180, -90, 90, wgs84LonLat);

        assertThat(decode(json, ReferencedEnvelope.class)).isEqualTo(expected);
    }

    @Test
    @SneakyThrows(FactoryException.class)
    void referencedEnvelopeAsFilterLiteral() {
        final String json =
                """
                {
                  "PropertyIsEqualTo" : {
                    "matchAction" : "ANY",
                    "expression1" : {
                      "PropertyName" : {
                        "propertyName" : "prop"
                      }
                    },
                    "expression2" : {
                      "Literal" : {
                        "type" : "org.geotools.geometry.jts.ReferencedEnvelope",
                        "value" : {
                          "crs" : {
                            "srs" : "EPSG:4326"
                          },
                          "coordinates" : [ -180.0, 180.0, -90.0, 90.0 ]
                        }
                      }
                    },
                    "matchingCase" : true
                  }
                }
                """;
        CoordinateReferenceSystem wgs84LonLat = CRS.decode("EPSG:4326", true);
        ReferencedEnvelope expected = new ReferencedEnvelope(-180, 180, -90, 90, wgs84LonLat);
        testFilterLiteral(expected, json);
    }

    @Test
    void coverageInfo() {
        final String json =
                """
                {
                  "@type" : "CoverageInfo",
                  "id" : "cov1",
                  "name" : "cvName",
                  "namespace" : "ns1",
                  "store" : "cs1",
                  "nativeName" : "cvName",
                  "enabled" : false,
                  "advertised" : true,
                  "serviceConfiguration" : false,
                  "simpleConversionEnabled" : false
                }
                """;
        CoverageInfo expected = data.faker()
                .coverageInfo(
                        "cov1", proxy("ns1", NamespaceInfo.class), proxy("cs1", CoverageStoreInfo.class), "cvName");
        expected.setAdvertised(true);

        assertCoverageInfo(expected, decode(json, CoverageInfo.class));
        assertCoverageInfo(expected, decode(json, ResourceInfo.class));
        assertCoverageInfo(expected, decode(json, CatalogInfo.class));
        assertCoverageInfo(expected, decode(json, Info.class));
    }

    private void assertCoverageInfo(CoverageInfo expected, Info actual) {
        assertResourceInfo(expected, actual)
                .isInstanceOf(CoverageInfo.class)
                .hasFieldOrPropertyWithValue("nativeFormat", expected.getNativeFormat())
                .hasFieldOrPropertyWithValue("nativeCoverageName", expected.getNativeCoverageName())
                .hasFieldOrPropertyWithValue("grid", expected.getGrid())
                .hasFieldOrPropertyWithValue("supportedFormats", expected.getSupportedFormats())
                .hasFieldOrPropertyWithValue("interpolationMethods", expected.getInterpolationMethods())
                .hasFieldOrPropertyWithValue("defaultInterpolationMethod", expected.getDefaultInterpolationMethod())
                .hasFieldOrPropertyWithValue("requestSRS", expected.getRequestSRS())
                .hasFieldOrPropertyWithValue("responseSRS", expected.getResponseSRS())
                .hasFieldOrPropertyWithValue("parameters", expected.getParameters());
    }

    private ObjectAssert<Info> assertResourceInfo(ResourceInfo expected, Info actual) {
        return assertCatalogInfo(expected, actual)
                .isInstanceOf(ResourceInfo.class)
                .hasFieldOrPropertyWithValue("name", expected.getName())
                .hasFieldOrPropertyWithValue(
                        "namespace.id", expected.getNamespace().getId())
                .hasFieldOrPropertyWithValue("store.id", expected.getStore().getId())
                .hasFieldOrPropertyWithValue("nativeName", expected.getNativeName())
                .hasFieldOrPropertyWithValue("enabled", expected.isEnabled())
                .hasFieldOrPropertyWithValue("advertised", expected.isAdvertised())
                .hasFieldOrPropertyWithValue("title", expected.getTitle())
                .hasFieldOrPropertyWithValue("abstract", expected.getAbstract())
                .hasFieldOrPropertyWithValue("internationalAbstract", expected.getInternationalAbstract())
                .hasFieldOrPropertyWithValue("internationalTitle", expected.getInternationalTitle());
    }

    @Test
    void gridGeometry2D() throws FactoryException {
        final String json =
                """
                {
                  "low" : [ 0, 0 ],
                  "high" : [ 1024, 768 ],
                  "transform" : [ 0.3515625, 0.0, 0.0, -0.234375, -179.82421875, 89.8828125 ],
                  "crs" : {
                    "srs" : "EPSG:4326"
                  }
                }
                """;

        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        ReferencedEnvelope env = new ReferencedEnvelope(-180, 180, -90, 90, crs);
        GridEnvelope range = new GeneralGridEnvelope(new int[] {0, 0}, new int[] {1024, 768});

        GridGeometry2D expected = new GridGeometry2D(range, env);

        GridGeometry actual = decode(json, GridGeometry.class);
        assertThat(actual).isEqualTo(expected);

        actual = decode(json, GridGeometry2D.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void metadataMap() {
        final String json =
                """
                {
                  "MetadataMap" : {
                    "k1" : {
                      "Literal" : {
                        "type" : "java.lang.String",
                        "value" : "v1"
                      }
                    },
                    "k2" : {
                      "Literal" : {
                        "type" : "java.lang.Integer",
                        "value" : 1000
                      }
                    },
                    "k3" : {
                      "Literal" : {
                        "value" : null
                      }
                    }
                  }
                }
                """;
        MetadataMap expected = new MetadataMap();
        expected.put("k1", "v1");
        expected.put("k2", 1000);
        expected.put("k3", null);
        assertThat(decode(json, MetadataMap.class)).isEqualTo(expected);
    }

    void metadataMapAsFilterLiteral() {
        final String json =
                """
                {
                  "PropertyIsEqualTo" : {
                    "matchAction" : "ANY",
                    "expression1" : {
                      "PropertyName" : {
                        "propertyName" : "literalTestProp"
                      }
                    },
                    "expression2" : {
                      "Literal" : {
                        "type" : "java.util.Map",
                        "value" : {
                          "k1" : {
                            "Literal" : {
                              "type" : "java.lang.String",
                              "value" : "v1"
                            }
                          },
                          "k2" : {
                            "Literal" : {
                              "type" : "java.lang.String",
                              "value" : "v2"
                            }
                          },
                          "k3" : {
                            "Literal" : {
                              "value" : null
                            }
                          }
                        }
                      }
                    },
                    "matchingCase" : true
                  }
                }
                """;
        Object expected = null;
        decode(json, null);
    }

    @Test
    void keywordInfo() {
        final String json =
                """
                {
                  "value" : "value",
                  "language" : "en",
                  "vocabulary" : "bad"
                }
                """;
        KeywordInfo expected = new org.geoserver.catalog.Keyword("value");
        expected.setLanguage("en");
        expected.setVocabulary("bad");

        assertThat(decode(json, KeywordInfo.class)).isEqualTo(expected);
    }

    @Test
    void dimensionInfo() {
        final String json =
                """
                {
                  "enabled" : true,
                  "attribute" : "attribute",
                  "presentation" : "DISCRETE_INTERVAL",
                  "resolution" : 766.7141,
                  "units" : "metre",
                  "unitSymbol" : "m",
                  "nearestMatchEnabled" : true,
                  "rawNearestMatchEnabled" : false,
                  "acceptableInterval" : "searchRange",
                  "defaultValueStrategy" : "MAXIMUM",
                  "defaultValueReferenceValue" : "referenceValue"
                }
                """;

        DimensionInfo di = data.faker().dimensionInfo();
        di.setAcceptableInterval("searchRange");
        di.setAttribute("attribute");
        di.setEnabled(true);
        di.setNearestMatchEnabled(true);
        di.setResolution(BigDecimal.valueOf(766.7141));
        di.setUnits("metre");
        di.setUnitSymbol("m");
        di.setPresentation(DimensionPresentation.DISCRETE_INTERVAL);

        DimensionDefaultValueSetting defaultValue = new DimensionDefaultValueSetting();
        defaultValue.setReferenceValue("referenceValue");
        defaultValue.setStrategyType(Strategy.MAXIMUM);
        di.setDefaultValue(defaultValue);

        // DimensionDefaultValueSetting does not implement equals()
        DimensionInfo actual = decode(json, DimensionInfo.class);
        assertThat(actual)
                .hasFieldOrPropertyWithValue("acceptableInterval", di.getAcceptableInterval())
                .hasFieldOrPropertyWithValue("attribute", di.getAttribute())
                .hasFieldOrPropertyWithValue("endAttribute", di.getEndAttribute())
                .hasFieldOrPropertyWithValue("endValue", di.getEndValue())
                .hasFieldOrPropertyWithValue("startValue", di.getStartValue())
                .hasFieldOrPropertyWithValue("units", di.getUnits())
                .hasFieldOrPropertyWithValue("unitSymbol", di.getUnitSymbol())
                .hasFieldOrPropertyWithValue(
                        "defaultValue.referenceValue", di.getDefaultValue().getReferenceValue())
                .hasFieldOrPropertyWithValue(
                        "defaultValue.strategyType", di.getDefaultValue().getStrategyType())
                .hasFieldOrPropertyWithValue("nearestFailBehavior", di.getNearestFailBehavior())
                .hasFieldOrPropertyWithValue("presentation", di.getPresentation())
                .hasFieldOrPropertyWithValue("resolution", di.getResolution());
    }

    @Test
    void virtualTable() {
        final String json =
                """
                {
                  "name" : "testvt",
                  "sql" : "select * from test;\\n",
                  "escapeSql" : true
                }
                """;

        VirtualTable vt = new VirtualTable("testvt", "select * from test;", true);
        VirtualTable actual = decode(json, VirtualTable.class);
        assertThat(actual).isEqualTo(vt);
    }

    @Test
    void coverageStoreInfoCOG() {
        final String json =
                """
                {
                  "@type" : "CoverageStoreInfo",
                  "id" : "cs1",
                  "name" : "csName",
                  "workspace" : "ws1",
                  "type" : "fakeCoverageType",
                  "enabled" : false,
                  "metadata" : {
                    "MetadataMap" : {
                      "cogSettings" : {
                        "Literal" : {
                          "type" : "org.geoserver.cog.CogSettingsStore",
                          "value" : {
                            "CogSettingsStore" : {
                              "rangeReaderSettings" : "Azure",
                              "useCachingStream" : true
                            }
                          }
                        }
                      }
                    }
                  },
                  "disableOnConnFailure" : false,
                  "url" : "file://fake"
                }
                """;
        CoverageStoreInfo expected = data.faker()
                .coverageStoreInfo(
                        "cs1", proxy("ws1", WorkspaceInfo.class), "csName", "fakeCoverageType", "file://fake");

        CogSettings cogSettings = new CogSettings();
        cogSettings.setUseCachingStream(true);
        cogSettings.setRangeReaderSettings(RangeReaderType.Azure);
        expected.getMetadata().put("cogSettings", cogSettings);

        assertCoverageStore(expected, decode(json, CoverageStoreInfo.class));
        assertCoverageStore(expected, decode(json, StoreInfo.class));
        assertCoverageStore(expected, decode(json, CatalogInfo.class));
        assertCoverageStore(expected, decode(json, Info.class));
    }

    @Test
    void legendInfo() {
        final String json =
                """
                {
                  "id" : "id",
                  "width" : 20,
                  "height" : 10,
                  "format" : "format",
                  "onlineResource" : "onlineResource"
                }
                """;
        LegendInfoImpl expected = new LegendInfoImpl();
        expected.setFormat("format");
        expected.setHeight(10);
        expected.setWidth(20);
        expected.setId("id");
        expected.setOnlineResource("onlineResource");

        // LegendInfoImpl does not implement equals
        assertThat(decode(json, LegendInfo.class))
                .isInstanceOf(LegendInfo.class)
                .hasFieldOrPropertyWithValue("id", expected.getId())
                .hasFieldOrPropertyWithValue("width", expected.getWidth())
                .hasFieldOrPropertyWithValue("height", expected.getHeight())
                .hasFieldOrPropertyWithValue("format", expected.getFormat())
                .hasFieldOrPropertyWithValue("onlineResource", expected.getOnlineResource());
    }

    @Test
    void dataStoreInfo() {
        final String json =
                """
                {
                  "@type" : "DataStoreInfo",
                  "id" : "ds1",
                  "name" : "dsName",
                  "workspace" : "ws1",
                  "description" : "dsDescription",
                  "enabled" : true,
                  "connectionParameters" : {
                    "param1" : "test value",
                    "param2" : 1000
                  },
                  "disableOnConnFailure" : true,
                         "dateCreated" : 1738432853803,
                         "dateModified" : 1738432853803
                }
                """;
        DataStoreInfo expected =
                data.faker().dataStoreInfo("ds1", proxy("ws1", WorkspaceInfo.class), "dsName", "dsDescription", true);
        expected.getConnectionParameters().put("param1", "test value");
        expected.getConnectionParameters().put("param2", 1000);
        expected.setDisableOnConnFailure(true);
        expected.setDateCreated(new Date(1738432853803L));
        expected.setDateModified(new Date(1738432853803L));

        assertDataStore(expected, decode(json, DataStoreInfo.class));
        assertDataStore(expected, decode(json, StoreInfo.class));
        assertDataStore(expected, decode(json, CatalogInfo.class));
        assertDataStore(expected, decode(json, Info.class));
    }

    private void assertDataStore(DataStoreInfo expected, Info actual) {
        assertStore(expected, actual).isInstanceOf(DataStoreInfo.class);
    }

    @Test
    void coverageStoreInfo() {
        final String json =
                """
                {
                  "@type" : "CoverageStoreInfo",
                  "id" : "cs1",
                  "name" : "csName",
                  "workspace" : "ws1",
                  "type" : "fakeCoverageType",
                  "enabled" : false,
                  "disableOnConnFailure" : false,
                  "url" : "file://fake"
                }
                """;
        CoverageStoreInfo expected = data.faker()
                .coverageStoreInfo(
                        "cs1", proxy("ws1", WorkspaceInfo.class), "csName", "fakeCoverageType", "file://fake");

        assertCoverageStore(expected, decode(json, CoverageStoreInfo.class));
        assertCoverageStore(expected, decode(json, StoreInfo.class));
        assertCoverageStore(expected, decode(json, CatalogInfo.class));
        assertCoverageStore(expected, decode(json, Info.class));
    }

    private void assertCoverageStore(CoverageStoreInfo expected, Info actual) {
        assertStore(expected, actual)
                .isInstanceOf(CoverageStoreInfo.class)
                .hasFieldOrPropertyWithValue("type", expected.getType())
                .hasFieldOrPropertyWithValue("url", expected.getURL());
    }

    @Test
    void wmsStoreInfo() {
        final String json =
                """
                {
                  "@type" : "WMSStoreInfo",
                  "id" : "wms1",
                  "name" : "wmsName",
                  "workspace" : "ws1",
                  "type" : "WMS",
                  "enabled" : true,
                  "disableOnConnFailure" : true,
                  "capabilitiesURL" : "http://fake.url",
                  "maxConnections" : 10,
                  "readTimeout" : 100,
                  "connectTimeout" : 50,
                  "useConnectionPooling" : true,
                  "headerName": "hname",
                  "headerValue": "hvalue",
                  "authKey": "hkvalue"
                }
                """;
        WMSStoreInfo expected = data.faker()
                .wmsStoreInfo("wms1", proxy("ws1", WorkspaceInfo.class), "wmsName", "http://fake.url", true);
        expected.setDisableOnConnFailure(true);
        expected.setMaxConnections(10);
        expected.setReadTimeout(100);
        expected.setConnectTimeout(50);
        expected.setUseConnectionPooling(true);
        expected.setHeaderName("hname");
        expected.setHeaderValue("hvalue");
        expected.setAuthKey("hkvalue");

        assertWmsStore(expected, decode(json, WMSStoreInfo.class));
        assertWmsStore(expected, decode(json, HTTPStoreInfo.class));
        assertWmsStore(expected, decode(json, StoreInfo.class));
        assertWmsStore(expected, decode(json, CatalogInfo.class));
        assertWmsStore(expected, decode(json, Info.class));
    }

    private void assertWmsStore(WMSStoreInfo expected, Info actual) {

        assertHttpStore(expected, actual)
                .isInstanceOf(WMSStoreInfo.class)
                .hasFieldOrPropertyWithValue("headerName", expected.getHeaderName())
                .hasFieldOrPropertyWithValue("headerValue", expected.getHeaderValue())
                .hasFieldOrPropertyWithValue("authKey", expected.getAuthKey());
    }

    @Test
    void wmtsStoreInfo() {
        final String json =
                """
                {
                  "@type" : "WMTSStoreInfo",
                  "id" : "wmts1",
                  "name" : "wmtsName",
                  "workspace" : "ws1",
                  "type" : "WMTS",
                  "enabled" : true,
                  "disableOnConnFailure" : true,
                  "capabilitiesURL" : "http://fake.wmts.url",
                  "maxConnections" : 10,
                  "readTimeout" : 100,
                  "connectTimeout" : 50,
                  "useConnectionPooling" : true,
                  "headerName": "hname",
                  "headerValue": "hvalue",
                  "authKey": "hkvalue"
                }
                """;
        WMTSStoreInfo expected = data.faker()
                .wmtsStoreInfo("wmts1", proxy("ws1", WorkspaceInfo.class), "wmtsName", "http://fake.wmts.url", true);
        expected.setDisableOnConnFailure(true);
        expected.setMaxConnections(10);
        expected.setReadTimeout(100);
        expected.setConnectTimeout(50);
        expected.setUseConnectionPooling(true);
        expected.setHeaderName("hname");
        expected.setHeaderValue("hvalue");
        expected.setAuthKey("hkvalue");

        assertWmtsStore(expected, decode(json, WMTSStoreInfo.class));
        assertWmtsStore(expected, decode(json, HTTPStoreInfo.class));
        assertWmtsStore(expected, decode(json, StoreInfo.class));
        assertWmtsStore(expected, decode(json, CatalogInfo.class));
        assertWmtsStore(expected, decode(json, Info.class));
    }

    private void assertWmtsStore(WMTSStoreInfo expected, Info actual) {

        assertHttpStore(expected, actual)
                .isInstanceOf(WMTSStoreInfo.class)
                .hasFieldOrPropertyWithValue("headerName", expected.getHeaderName())
                .hasFieldOrPropertyWithValue("headerValue", expected.getHeaderValue())
                .hasFieldOrPropertyWithValue("authKey", expected.getAuthKey());
    }

    private ObjectAssert<Info> assertHttpStore(HTTPStoreInfo expected, Info actual) {

        return assertStore(expected, actual)
                .isInstanceOf(HTTPStoreInfo.class)
                .hasFieldOrPropertyWithValue("type", expected.getType())
                .hasFieldOrPropertyWithValue("enabled", expected.isEnabled())
                .hasFieldOrPropertyWithValue("disableOnConnFailure", expected.isDisableOnConnFailure())
                .hasFieldOrPropertyWithValue("capabilitiesURL", expected.getCapabilitiesURL())
                .hasFieldOrPropertyWithValue("maxConnections", expected.getMaxConnections())
                .hasFieldOrPropertyWithValue("readTimeout", expected.getReadTimeout())
                .hasFieldOrPropertyWithValue("connectTimeout", expected.getConnectTimeout())
                .hasFieldOrPropertyWithValue("useConnectionPooling", expected.isUseConnectionPooling());
    }

    private ObjectAssert<Info> assertStore(StoreInfo expected, Info actual) {
        return assertCatalogInfo(expected, actual)
                .isInstanceOf(StoreInfo.class)
                .hasFieldOrPropertyWithValue("enabled", expected.isEnabled())
                .hasFieldOrPropertyWithValue("connectionParameters", expected.getConnectionParameters())
                .hasFieldOrPropertyWithValue("disableOnConnFailure", expected.isDisableOnConnFailure())
                .hasFieldOrPropertyWithValue(
                        "workspace.id", expected.getWorkspace().getId());
    }

    @Test
    void workspaceInfo() {
        final String json =
                """
                {
                  "@type" : "WorkspaceInfo",
                  "id" : "ws1",
                  "name" : "wsName",
                  "isolated" : true,
                  "dateCreated" : 1738432853803,
                  "dateModified" : 1738432853803
                }
                """;
        WorkspaceInfo expected = data.faker().workspaceInfo("ws1", "wsName");
        expected.setDateCreated(new Date(1738432853803L));
        expected.setDateModified(new Date(1738432853803L));

        assertThat(decode(json, WorkspaceInfo.class)).isEqualTo(expected);
        assertThat(decode(json, CatalogInfo.class)).isEqualTo(expected);
        assertThat(decode(json, Info.class)).isEqualTo(expected);
    }

    @Test
    void namespaceInfo() {
        final String json =
                """
                {
                  "@type" : "NamespaceInfo",
                  "id" : "ns1",
                  "name" : "wsName",
                  "isolated" : false,
                  "uri" : "nsURI"
                }
                """;

        NamespaceInfo expected = data.faker().namespace("ns1", "wsName", "nsURI");

        assertThat(decode(json, NamespaceInfo.class)).isEqualTo(expected);
        assertThat(decode(json, CatalogInfo.class)).isEqualTo(expected);
        assertThat(decode(json, Info.class)).isEqualTo(expected);
    }

    @Test
    void wmtsLayerInfo() {
        final String json =
                """
                {
                  "@type" : "WMTSLayerInfo",
                  "id" : "wmtsl1",
                  "name" : "wmtsLayer",
                  "namespace" : "ns1",
                  "store" : "wmts1",
                  "nativeName" : "native-name",
                  "enabled" : true,
                  "advertised" : true,
                  "serviceConfiguration" : false,
                  "simpleConversionEnabled" : false
                }
                """;

        WMTSLayerInfo expected = data.faker()
                .wmtsLayerInfo(
                        "wmtsl1",
                        proxy("wmts1", WMTSStoreInfo.class),
                        proxy("ns1", NamespaceInfo.class),
                        "wmtsLayer",
                        true);
        expected.setNativeName("native-name");

        assertWmtsLayer(expected, decode(json, WMTSLayerInfo.class));
        assertWmtsLayer(expected, decode(json, ResourceInfo.class));
        assertWmtsLayer(expected, decode(json, CatalogInfo.class));
        assertWmtsLayer(expected, decode(json, Info.class));
    }

    private void assertWmtsLayer(WMTSLayerInfo expected, Info actual) {
        assertResourceInfo(expected, actual).isInstanceOf(WMTSLayerInfo.class);
    }

    private ObjectAssert<Info> assertCatalogInfo(CatalogInfo expected, Info actual) {
        return assertThat(actual)
                .isInstanceOf(CatalogInfo.class)
                .hasFieldOrPropertyWithValue("id", expected.getId())
                .hasFieldOrPropertyWithValue("dateCreated", expected.getDateCreated())
                .hasFieldOrPropertyWithValue("dateModified", expected.getDateModified());
    }
}
