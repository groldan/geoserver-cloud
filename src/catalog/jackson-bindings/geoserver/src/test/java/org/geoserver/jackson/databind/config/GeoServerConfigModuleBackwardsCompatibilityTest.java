package org.geoserver.jackson.databind.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.geoserver.catalog.Info;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.GeoServerInfo.WebUIMode;
import org.geoserver.jackson.databind.catalog.BackwardsCompatibilityTestSupport;
import org.junit.jupiter.api.Test;

public class GeoServerConfigModuleBackwardsCompatibilityTest extends BackwardsCompatibilityTestSupport {

    @Test
    void geoServerInfo() throws Exception {
        String json =
                """
                {
                  "@type" : "GeoServerInfo",
                  "id" : "GeoServer.global",
                  "settings" : {
                    "@type" : "SettingsInfo",
                    "id" : "global-settings-id",
                    "title" : "Global Settings",
                    "contact" : {
                      "id" : "523-82-9527",
                      "address" : "Apt. 770 0313 Eleanore Station, Stehrhaven, NM 64826-2376",
                      "addressCity" : "Eloisborough",
                      "addressCountry" : "Sri Lanka",
                      "addressDeliveryPoint" : "Suite 358",
                      "addressPostalCode" : "86944",
                      "addressState" : "Massachusetts",
                      "contactFacsimile" : "300-571-5601 x1032",
                      "contactOrganization" : "Little-Dickens",
                      "contactPerson" : "Ms. Daniell Marquardt",
                      "contactVoice" : "789-569-8972",
                      "onlineResource" : "www.klingllc.co",
                      "internationalAddress" : {
                        "de" : "Schöffenweg 80b, Schön Ahmaddorf, BB 18558",
                        "it" : "Strada Enzo 233, Appartamento 17, Settimo Renzo, NA 10663"
                      }
                    },
                    "charset" : "UTF-8",
                    "numDecimals" : 9,
                    "onlineResource" : "http://geoserver.org",
                    "proxyBaseUrl" : "http://test.geoserver.org",
                    "schemaBaseUrl" : "file:data/schemas",
                    "verbose" : true,
                    "verboseExceptions" : true,
                    "metadata" : {
                      "MetadataMap" : {
                        "k3" : {
                          "Literal" : {
                            "type" : "java.lang.Boolean",
                            "value" : false
                          }
                        },
                        "k1" : {
                          "Literal" : {
                            "type" : "java.lang.Integer",
                            "value" : 1
                          }
                        },
                        "k2" : {
                          "Literal" : {
                            "type" : "java.lang.String",
                            "value" : "2"
                          }
                        }
                      }
                    },
                    "localWorkspaceIncludesPrefix" : false,
                    "showCreatedTimeColumnsInAdminList" : false,
                    "showModifiedTimeColumnsInAdminList" : false,
                    "useHeadersProxyURL" : false
                  },
                  "coverageAccess" : {
                    "corePoolSize" : 9,
                    "keepAliveTime" : 1000,
                    "maxPoolSize" : 18,
                    "queueType" : "UNBOUNDED",
                    "imageIOCacheThreshold" : 11
                  },
                  "metadata" : {
                    "MetadataMap" : {
                      "k1" : {
                        "Literal" : {
                          "type" : "java.lang.Integer",
                          "value" : 1
                        }
                      },
                      "k2" : {
                        "Literal" : {
                          "type" : "java.lang.String",
                          "value" : "2"
                        }
                      },
                      "k3" : {
                        "Literal" : {
                          "type" : "java.lang.Boolean",
                          "value" : false
                        }
                      }
                    }
                  },
                  "updateSequence" : 582,
                  "adminUsername" : "admin",
                  "adminPassword" : "geoserver",
                  "featureTypeCacheSize" : 1000,
                  "globalServices" : true,
                  "useHeadersProxyURL" : false,
                  "xmlPostRequestLogBufferSize" : 1024,
                  "xmlExternalEntitiesEnabled" : true,
                  "webUIMode" : "DO_NOT_REDIRECT",
                  "allowStoredQueriesPerWorkspace" : true,
                  "resourceErrorHandling" : "OGC_EXCEPTION_REPORT",
                  "trailingSlashMatch" : true
                }
                """;

        assertGeoServerInfo(decode(json, GeoServerInfo.class));
        assertGeoServerInfo(decode(json, Info.class));
    }

    private void assertGeoServerInfo(Info actual) {
        assertThat(actual)
                .isNotNull()
                .isInstanceOf(GeoServerInfo.class)
                .hasFieldOrPropertyWithValue("id", "GeoServer.global")
                .hasFieldOrPropertyWithValue("settings.id", "global-settings-id")
                .hasFieldOrPropertyWithValue(
                        "settings.contact.address", "Apt. 770 0313 Eleanore Station, Stehrhaven, NM 64826-2376")
                .hasFieldOrPropertyWithValue("numDecimals", 9)
                .hasFieldOrPropertyWithValue("settings.metadata.k1", 1)
                .hasFieldOrPropertyWithValue("settings.metadata.k2", "2")
                .hasFieldOrPropertyWithValue("settings.metadata.k3", false)
                .hasFieldOrPropertyWithValue("metadata.k1", 1)
                .hasFieldOrPropertyWithValue("metadata.k2", "2")
                .hasFieldOrPropertyWithValue("metadata.k3", false)
                .hasFieldOrPropertyWithValue("updateSequence", 582L)
                .hasFieldOrPropertyWithValue("webUIMode", WebUIMode.DO_NOT_REDIRECT);
    }

    @Test
    void wcsServiceInfo() throws Exception {
        String json =
                """
                {
                  "@type" : "WCSInfo",
                  "id" : "wcs-id",
                  "name" : "wcs",
                  "citeCompliant" : true,
                  "enabled" : true,
                  "onlineResource" : "http://geoserver.org/wcs",
                  "title" : "wcs Title",
                  "maintainer" : "Claudious whatever",
                  "fees" : "NONE",
                  "accessConstraints" : "NONE",
                  "versions" : [ "1.0.0", "2.0.0" ],
                  "keywords" : [ {
                    "value" : "Chuck Norris writes code that optimizes itself.",
                    "language" : "eng",
                    "vocabulary" : "watchit"
                  }, {
                    "value" : "Chuck Norris can divide by zero.",
                    "language" : "eng",
                    "vocabulary" : "watchit"
                  } ],
                  "exceptionFormats" : [ "fake-wcs-exception-format" ],
                  "metadataLink" : {
                    "id" : "medatata-link-wcs",
                    "type" : "void",
                    "about" : "about",
                    "metadataType" : "fake",
                    "content" : "content"
                  },
                  "outputStrategy" : "SPEED",
                  "schemaBaseURL" : "file:data/wcs",
                  "verbose" : true,
                  "metadata" : {
                    "MetadataMap" : {
                      "wcs" : {
                        "Literal" : {
                          "type" : "java.lang.String",
                          "value" : "something"
                        }
                      }
                    }
                  },
                  "internationalTitle" : {
                    "en" : "wcs english title",
                    "fr-CA" : "wcstitre anglais"
                  },
                  "internationalAbstract" : {
                    "en" : "wcs english abstract",
                    "fr-CA" : "wcsrésumé anglais"
                  },
                  "maxInputMemory" : -1,
                  "maxOutputMemory" : -1,
                  "overviewPolicy" : "QUALITY",
                  "subsamplingEnabled" : true,
                  "maxRequestedDimensionValues" : 10,
                  "defaultDeflateCompressionLevel" : 9,
                  "gmlprefixing" : false,
                  "latLon" : false,
                  "abstract" : "wcs Abstract"
                }
                """;
        Object expected = null;
        decode(json, null);
    }

    @Test
    void loggingInfo() throws Exception {
        String json =
                """
                {
                  "@type" : "LoggingInfo",
                  "id" : "weird-this-has-id",
                  "level" : "super",
                  "location" : "there",
                  "stdOutLogging" : true
                }
                """;
        Object expected = null;
        decode(json, null);
    }

    @Test
    void wpsServiceInfo() throws Exception {
        String json =
                """
                {
                  "@type" : "WPSInfo",
                  "id" : "wps-id",
                  "name" : "wps",
                  "citeCompliant" : true,
                  "enabled" : true,
                  "onlineResource" : "http://geoserver.org/wps",
                  "title" : "wps Title",
                  "maintainer" : "Claudious whatever",
                  "fees" : "NONE",
                  "accessConstraints" : "NONE",
                  "versions" : [ "1.0.0", "2.0.0" ],
                  "keywords" : [ {
                    "value" : "No statement can catch the ChuckNorrisException.",
                    "language" : "eng",
                    "vocabulary" : "watchit"
                  }, {
                    "value" : "Chuck Norris can write multi-threaded applications with a single thread.",
                    "language" : "eng",
                    "vocabulary" : "watchit"
                  } ],
                  "exceptionFormats" : [ "fake-wps-exception-format" ],
                  "metadataLink" : {
                    "id" : "medatata-link-wps",
                    "type" : "void",
                    "about" : "about",
                    "metadataType" : "fake",
                    "content" : "content"
                  },
                  "outputStrategy" : "SPEED",
                  "schemaBaseURL" : "file:data/wps",
                  "verbose" : true,
                  "metadata" : {
                    "MetadataMap" : {
                      "wps" : {
                        "Literal" : {
                          "type" : "java.lang.String",
                          "value" : "something"
                        }
                      }
                    }
                  },
                  "internationalTitle" : {
                    "en" : "wps english title",
                    "fr-CA" : "wpstitre anglais"
                  },
                  "internationalAbstract" : {
                    "en" : "wps english abstract",
                    "fr-CA" : "wpsrésumé anglais"
                  },
                  "connectionTimeout" : 1000.0,
                  "resourceExpirationTimeout" : 2000,
                  "maxSynchronousProcesses" : 4,
                  "maxAsynchronousProcesses" : 16,
                  "processGroups" : [ {
                    "factoryClass" : "org.geotools.process.factory.AnnotationDrivenProcessFactory",
                    "enabled" : true
                  } ],
                  "catalogMode" : "CHALLENGE",
                  "maxComplexInputSize" : 1024,
                  "maxAsynchronousExecutionTime" : 1,
                  "maxAsynchronousTotalTime" : 2,
                  "maxSynchronousExecutionTime" : 3,
                  "maxSynchronousTotalTime" : 4,
                  "remoteInputDisabled" : false,
                  "abstract" : "wps Abstract"
                }
                """;
        Object expected = null;
        decode(json, null);
    }

    @Test
    void wmsServiceInfo() throws Exception {
        String json =
                """
                {
                  "@type" : "WMSInfo",
                  "id" : "wms-id",
                  "name" : "wms",
                  "citeCompliant" : true,
                  "enabled" : true,
                  "onlineResource" : "http://geoserver.org/wms",
                  "title" : "wms Title",
                  "maintainer" : "Claudious whatever",
                  "fees" : "NONE",
                  "accessConstraints" : "NONE",
                  "versions" : [ "1.0.0", "2.0.0" ],
                  "keywords" : [ {
                    "value" : "Chuck Norris doesn't believe in floating point numbers because they can't be typed on his binary keyboard.",
                    "language" : "eng",
                    "vocabulary" : "watchit"
                  }, {
                    "value" : "Chuck Norris doesn't have performance bottlenecks. He just makes the universe wait its turn.",
                    "language" : "eng",
                    "vocabulary" : "watchit"
                  } ],
                  "exceptionFormats" : [ "fake-wms-exception-format" ],
                  "metadataLink" : {
                    "id" : "medatata-link-wms",
                    "type" : "void",
                    "about" : "about",
                    "metadataType" : "fake",
                    "content" : "content"
                  },
                  "outputStrategy" : "SPEED",
                  "schemaBaseURL" : "file:data/wms",
                  "verbose" : true,
                  "metadata" : {
                    "MetadataMap" : {
                      "wms" : {
                        "Literal" : {
                          "type" : "java.lang.String",
                          "value" : "something"
                        }
                      }
                    }
                  },
                  "internationalTitle" : {
                    "en" : "wms english title",
                    "fr-CA" : "wmstitre anglais"
                  },
                  "internationalAbstract" : {
                    "en" : "wms english abstract",
                    "fr-CA" : "wmsrésumé anglais"
                  },
                  "watermark" : {
                    "enabled" : false,
                    "position" : "BOT_RIGHT",
                    "transparency" : 100
                  },
                  "interpolation" : "Nearest",
                  "maxBuffer" : 0,
                  "maxRequestMemory" : 0,
                  "maxRenderingTime" : 0,
                  "maxRenderingErrors" : 0,
                  "dynamicStylingDisabled" : false,
                  "featuresReprojectionDisabled" : false,
                  "maxRequestedDimensionValues" : 100,
                  "cacheConfiguration" : {
                    "enabled" : false,
                    "maxEntries" : 1000,
                    "maxEntrySize" : 51200
                  },
                  "remoteStyleMaxRequestTime" : 2000,
                  "remoteStyleTimeout" : 500,
                  "defaultGroupStyleEnabled" : true,
                  "autoEscapeTemplateValues" : false,
                  "transformFeatureInfoDisabled" : false,
                  "bboxforEachCRS" : false,
                  "getMapMimeTypeCheckingEnabled" : false,
                  "getFeatureInfoMimeTypeCheckingEnabled" : false,
                  "abstract" : "wms Abstract"
                }
                """;
        Object expected = null;
        decode(json, null);
    }

    @Test
    void contactInfo() throws Exception {
        String json =
                """
                {
                  "id" : "209-94-3770",
                  "address" : "5778 Hessel Oval, North Olivastad, GA 40100",
                  "addressCity" : "South Waynebury",
                  "addressCountry" : "Bolivia",
                  "addressDeliveryPoint" : "Suite 368",
                  "addressPostalCode" : "84279-9940",
                  "addressState" : "Alaska",
                  "contactFacsimile" : "802.545.0054",
                  "contactOrganization" : "Cassin, Gislason and Sauer",
                  "contactPerson" : "Cary McCullough",
                  "contactVoice" : "1-036-025-5927",
                  "onlineResource" : "www.johnson-rath.biz",
                  "internationalAddress" : {
                    "de" : "Goetheplatz 07a, Schön Eileen, NI 67547",
                    "it" : "Appartamento 90 Piazza Davide 45, Rizzo laziale, AQ 12798"
                  }
                }
                """;
        Object expected = null;
        decode(json, null);
    }

    @Test
    void settingsInfo() throws Exception {
        String json =
                """
                {
                  "@type" : "SettingsInfo",
                  "id" : "wsName-settings-id",
                  "workspace" : "ws1",
                  "title" : "wsName Settings",
                  "contact" : {
                    "id" : "586-80-8557",
                    "address" : "216 Heathcote Orchard, Ankundingberg, VA 09841",
                    "addressCity" : "Alphonsomouth",
                    "addressCountry" : "Israel",
                    "addressDeliveryPoint" : "Apt. 092",
                    "addressPostalCode" : "01596-2719",
                    "addressState" : "California",
                    "contactFacsimile" : "787-760-6377",
                    "contactOrganization" : "Watsica, Bins and O'Connell",
                    "contactPerson" : "Dallas Murphy",
                    "contactVoice" : "304-433-9441",
                    "onlineResource" : "www.gerholdrunolfsdottirandschowalter.org",
                    "internationalAddress" : {
                      "de" : "Am Hang 18b, Neu Abdul, SL 23814",
                      "it" : "Appartamento 17 Rotonda De Angelis 433, Piano 9, Borgo Leone, SO 89794"
                    }
                  },
                  "charset" : "UTF-8",
                  "numDecimals" : 9,
                  "onlineResource" : "http://geoserver.org",
                  "proxyBaseUrl" : "http://test.geoserver.org",
                  "schemaBaseUrl" : "file:data/schemas",
                  "verbose" : true,
                  "verboseExceptions" : true,
                  "metadata" : {
                    "MetadataMap" : {
                      "k3" : {
                        "Literal" : {
                          "type" : "java.lang.Boolean",
                          "value" : false
                        }
                      },
                      "k1" : {
                        "Literal" : {
                          "type" : "java.lang.Integer",
                          "value" : 1
                        }
                      },
                      "k2" : {
                        "Literal" : {
                          "type" : "java.lang.String",
                          "value" : "2"
                        }
                      }
                    }
                  },
                  "localWorkspaceIncludesPrefix" : false,
                  "showCreatedTimeColumnsInAdminList" : false,
                  "showModifiedTimeColumnsInAdminList" : false,
                  "useHeadersProxyURL" : false
                }
                """;
        Object expected = null;
        decode(json, null);
    }

    @Test
    void wmtsServiceInfo() throws Exception {
        String json =
                """
                {
                  "@type" : "WMTSInfo",
                  "id" : "wmts-id",
                  "name" : "wmts",
                  "citeCompliant" : true,
                  "enabled" : true,
                  "onlineResource" : "http://geoserver.org/wmts",
                  "title" : "wmts Title",
                  "maintainer" : "Claudious whatever",
                  "fees" : "NONE",
                  "accessConstraints" : "NONE",
                  "versions" : [ "1.0.0", "2.0.0" ],
                  "keywords" : [ {
                    "value" : "Chuck Norris' keyboard doesn't have a F1 key, the computer asks him for help.",
                    "language" : "eng",
                    "vocabulary" : "watchit"
                  }, {
                    "value" : "Chuck Norris doesn't need the cloud to scale his applications, he uses his laptop.",
                    "language" : "eng",
                    "vocabulary" : "watchit"
                  } ],
                  "exceptionFormats" : [ "fake-wmts-exception-format" ],
                  "metadataLink" : {
                    "id" : "medatata-link-wmts",
                    "type" : "void",
                    "about" : "about",
                    "metadataType" : "fake",
                    "content" : "content"
                  },
                  "outputStrategy" : "SPEED",
                  "schemaBaseURL" : "file:data/wmts",
                  "verbose" : true,
                  "metadata" : {
                    "MetadataMap" : {
                      "wmts" : {
                        "Literal" : {
                          "type" : "java.lang.String",
                          "value" : "something"
                        }
                      }
                    }
                  },
                  "internationalTitle" : {
                    "en" : "wmts english title",
                    "fr-CA" : "wmtstitre anglais"
                  },
                  "internationalAbstract" : {
                    "en" : "wmts english abstract",
                    "fr-CA" : "wmtsrésumé anglais"
                  },
                  "abstract" : "wmts Abstract"
                }
                """;
        Object expected = null;
        decode(json, null);
    }

    @Test
    void wfsServiceInfo() throws Exception {
        String json =
                """
                {
                  "@type" : "WFSInfo",
                  "id" : "wfs-id",
                  "name" : "wfs",
                  "citeCompliant" : true,
                  "enabled" : true,
                  "onlineResource" : "http://geoserver.org/wfs",
                  "title" : "wfs Title",
                  "maintainer" : "Claudious whatever",
                  "fees" : "NONE",
                  "accessConstraints" : "NONE",
                  "versions" : [ "1.0.0", "2.0.0" ],
                  "keywords" : [ {
                    "value" : "Chuck Norris doesn't have performance bottlenecks. He just makes the universe wait its turn.",
                    "language" : "eng",
                    "vocabulary" : "watchit"
                  }, {
                    "value" : "Chuck Norris can binary search unsorted data.",
                    "language" : "eng",
                    "vocabulary" : "watchit"
                  } ],
                  "exceptionFormats" : [ "fake-wfs-exception-format" ],
                  "metadataLink" : {
                    "id" : "medatata-link-wfs",
                    "type" : "void",
                    "about" : "about",
                    "metadataType" : "fake",
                    "content" : "content"
                  },
                  "outputStrategy" : "SPEED",
                  "schemaBaseURL" : "file:data/wfs",
                  "verbose" : true,
                  "metadata" : {
                    "MetadataMap" : {
                      "wfs" : {
                        "Literal" : {
                          "type" : "java.lang.String",
                          "value" : "something"
                        }
                      },
                      "maxNumberOfFeaturesForPreview" : {
                        "Literal" : {
                          "type" : "java.lang.Integer",
                          "value" : 10
                        }
                      }
                    }
                  },
                  "internationalTitle" : {
                    "en" : "wfs english title",
                    "fr-CA" : "wfstitre anglais"
                  },
                  "internationalAbstract" : {
                    "en" : "wfs english abstract",
                    "fr-CA" : "wfsrésumé anglais"
                  },
                  "maxFeatures" : 50,
                  "serviceLevel" : "COMPLETE",
                  "featureBounding" : true,
                  "canonicalSchemaLocation" : false,
                  "encodeFeatureMember" : false,
                  "hitsIgnoreMaxFeatures" : false,
                  "includeWFSRequestDumpFile" : true,
                  "maxNumberOfFeaturesForPreview" : 10,
                  "allowGlobalQueries" : true,
                  "simpleConversionEnabled" : false,
                  "getFeatureOutputTypeCheckingEnabled" : false,
                  "gml" : {
                    "V_20" : {
                      "srsNameStyle" : "URN",
                      "overrideGMLAttributes" : true,
                      "mimeTypeToForce" : "application/gml;test=true"
                    },
                    "V_10" : {
                      "srsNameStyle" : "URN",
                      "overrideGMLAttributes" : true,
                      "mimeTypeToForce" : "application/gml;test=true"
                    }
                  },
                  "abstract" : "wfs Abstract"
                }
                """;
        Object expected = null;
        decode(json, null);
    }

    @Test
    void setingsInfoWithWorkspace() throws Exception {
        String json =
                """
                {
                  "@type" : "SettingsInfo",
                  "id" : "wsName-settings-id",
                  "workspace" : "ws1",
                  "title" : "wsName Settings",
                  "contact" : {
                    "id" : "626-73-6198",
                    "address" : "676 Carmine Islands, Daphnestad, LA 07475",
                    "addressCity" : "East Bomouth",
                    "addressCountry" : "Maldives",
                    "addressDeliveryPoint" : "Suite 504",
                    "addressPostalCode" : "78953-6135",
                    "addressState" : "Oklahoma",
                    "contactFacsimile" : "535.995.2290 x390",
                    "contactOrganization" : "Gutkowski, Goyette and Renner",
                    "contactPerson" : "Ladawn Kris",
                    "contactVoice" : "710-597-9327",
                    "onlineResource" : "www.kautzergroup.co",
                    "internationalAddress" : {
                      "de" : "Reuschenberger Str. 03b, Nussbeckhagen, HB 36385",
                      "it" : "Piano 4 Piazza Lucia 62, Rebecca laziale, PD 36357"
                    }
                  },
                  "charset" : "UTF-8",
                  "numDecimals" : 9,
                  "onlineResource" : "http://geoserver.org",
                  "proxyBaseUrl" : "http://test.geoserver.org",
                  "schemaBaseUrl" : "file:data/schemas",
                  "verbose" : true,
                  "verboseExceptions" : true,
                  "metadata" : {
                    "MetadataMap" : {
                      "k3" : {
                        "Literal" : {
                          "type" : "java.lang.Boolean",
                          "value" : false
                        }
                      },
                      "k1" : {
                        "Literal" : {
                          "type" : "java.lang.Integer",
                          "value" : 1
                        }
                      },
                      "k2" : {
                        "Literal" : {
                          "type" : "java.lang.String",
                          "value" : "2"
                        }
                      }
                    }
                  },
                  "localWorkspaceIncludesPrefix" : false,
                  "showCreatedTimeColumnsInAdminList" : false,
                  "showModifiedTimeColumnsInAdminList" : false,
                  "useHeadersProxyURL" : false
                }
                """;
        Object expected = null;
        decode(json, null);
    }

    @Test
    void setingsInfo() throws Exception {
        String json =
                """
                {
                  "@type" : "SettingsInfo",
                  "id" : "global-settings-id",
                  "title" : "Global Settings",
                  "contact" : {
                    "id" : "792-19-6951",
                    "address" : "1237 O'Kon Corners, Ebertview, GA 27293-8434",
                    "addressCity" : "South Leonardtown",
                    "addressCountry" : "Holy See (Vatican City State)",
                    "addressDeliveryPoint" : "Suite 625",
                    "addressPostalCode" : "22154-7932",
                    "addressState" : "Wyoming",
                    "contactFacsimile" : "713.040.5036 x225",
                    "contactOrganization" : "Muller-Bosco",
                    "contactPerson" : "Cordelia Raynor DDS",
                    "contactVoice" : "(473) 388-1348",
                    "onlineResource" : "www.yostandsons.co",
                    "internationalAddress" : {
                      "de" : "7 OG Walter-Hochapfel-Str. 205, Celinehagen, HB 37230",
                      "it" : "Piano 0 Incrocio Piersilvio 6, Quarto Rebecca ligure, LI 95517"
                    }
                  },
                  "charset" : "UTF-8",
                  "numDecimals" : 9,
                  "onlineResource" : "http://geoserver.org",
                  "proxyBaseUrl" : "http://test.geoserver.org",
                  "schemaBaseUrl" : "file:data/schemas",
                  "verbose" : true,
                  "verboseExceptions" : true,
                  "metadata" : {
                    "MetadataMap" : {
                      "k3" : {
                        "Literal" : {
                          "type" : "java.lang.Boolean",
                          "value" : false
                        }
                      },
                      "k1" : {
                        "Literal" : {
                          "type" : "java.lang.Integer",
                          "value" : 1
                        }
                      },
                      "k2" : {
                        "Literal" : {
                          "type" : "java.lang.String",
                          "value" : "2"
                        }
                      }
                    }
                  },
                  "localWorkspaceIncludesPrefix" : false,
                  "showCreatedTimeColumnsInAdminList" : false,
                  "showModifiedTimeColumnsInAdminList" : false,
                  "useHeadersProxyURL" : false
                }
                """;
        Object expected = null;
        decode(json, null);
    }
}
