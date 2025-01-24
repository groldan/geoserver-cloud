package org.geoserver.jackson.databind.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.geoserver.catalog.Info;
import org.geoserver.catalog.LayerInfo.WMSInterpolation;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.plugin.Patch;
import org.geoserver.catalog.plugin.Query;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.PropertyIsEqualTo;
import org.geotools.api.filter.sort.SortOrder;
import org.geotools.util.Version;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.MethodName.class)
class PatchSerializationBackwardsCompatibilityTest extends BackwardsCompatibilityTestSupport {

    @Test
    void queryFilterInclude() {
        final String json =
                """
                {
                  "type" : "org.geoserver.catalog.WorkspaceInfo",
                  "filter" : {
                    "Include" : { }
                  }
                }
                """;
        Query<WorkspaceInfo> expected = Query.valueOf(WorkspaceInfo.class, Filter.INCLUDE);
        assertThat(decode(json, Query.class)).isEqualTo(expected);
    }

    @Test
    void queryWithFilter() {
        final String json =
                """
                {
                  "type" : "org.geoserver.catalog.WorkspaceInfo",
                  "filter" : {
                    "PropertyIsEqualTo" : {
                      "matchAction" : "ANY",
                      "expression1" : {
                        "PropertyName" : {
                          "propertyName" : "some.property.name"
                        }
                      },
                      "expression2" : {
                        "Literal" : {
                          "type" : "java.util.List",
                          "contentType" : "java.lang.String",
                          "value" : [ "some literal 1", "some literal 2" ]
                        }
                      },
                      "matchingCase" : true
                    }
                  },
                  "sortBy" : [ {
                    "propertyName" : {
                      "PropertyName" : {
                        "propertyName" : "name"
                      }
                    },
                    "sortOrder" : "ASCENDING"
                  }, {
                    "propertyName" : {
                      "PropertyName" : {
                        "propertyName" : "type"
                      }
                    },
                    "sortOrder" : "DESCENDING"
                  } ],
                  "offset" : 2000,
                  "count" : 1000
                }
                """;

        PropertyIsEqualTo filter = equalsTo("some.property.name", List.of("some literal 1", "some literal 2"));
        Query<Info> expected = Query.valueOf(
                WorkspaceInfo.class,
                filter,
                2000,
                1000,
                ff.sort("name", SortOrder.ASCENDING),
                ff.sort("type", SortOrder.DESCENDING));

        // filter.equals is not working
        Query<?> actual = decode(json, Query.class);
        assertThat(actual)
                .hasFieldOrPropertyWithValue("type", expected.getType())
                .hasFieldOrPropertyWithValue("count", expected.getCount())
                .hasFieldOrPropertyWithValue("offset", expected.getOffset())
                .hasFieldOrPropertyWithValue("sortBy", expected.getSortBy());
        assertEqualsFilter(filter, actual.getFilter());
    }

    @Test
    void infoReferenceWmsStoreInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "wms",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "WMSSTORE",
                            "id" : "wms1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void jaiInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "jaiInfo",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.config.JAIInfo",
                          "value" : {
                            "allowInterpolation" : true,
                            "recycling" : true,
                            "tilePriority" : 1,
                            "tileThreads" : 7,
                            "memoryCapacity" : 4096.0,
                            "memoryThreshold" : 0.75,
                            "pngEncoderType" : "PNGJ",
                            "jpegAcceleration" : true,
                            "allowNativeMosaic" : true,
                            "allowNativeWarp" : true,
                            "jaiextinfo" : {
                              "jaiextoperations" : [ "categorize" ],
                              "jaioperations" : [ "band" ]
                            }
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void publishedType() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "publishedType",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.catalog.PublishedType",
                          "value" : "REMOTE"
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void dataLinkInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "dl",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.catalog.DataLinkInfo",
                          "value" : {
                            "id" : "657-78-2534",
                            "about" : "Death is a natural part of life. Rejoice for those around you who transform into the Force. Mourn them do not. Miss them do not. Attachment leads to jealously. The shadow of greed, that is.",
                            "type" : "bernier",
                            "content" : "www.shirley-bogisich.co"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceLayerInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "layer",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "LAYER",
                            "id" : "layer1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void loggingInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "logging",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.config.LoggingInfo",
                          "value" : {
                            "@type" : "LoggingInfo",
                            "id" : "weird-this-has-id",
                            "level" : "super",
                            "location" : "there",
                            "stdOutLogging" : true
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceCoverageInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "coverage",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "COVERAGE",
                            "id" : "cov1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void metadataMap() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "metadata",
                      "value" : {
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
                                "type" : "java.lang.Integer",
                                "value" : 2
                              }
                            }
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void simpleTypesNull() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "nullvalue",
                      "value" : {
                        "Literal" : {
                          "value" : null
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void simpleTypesInteger() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "int",
                      "value" : {
                        "Literal" : {
                          "type" : "java.lang.Integer",
                          "value" : 2147483647
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void simpleTypesLong() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "long",
                      "value" : {
                        "Literal" : {
                          "type" : "java.lang.Long",
                          "value" : 9223372036854775807
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void simpleTypesDate() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "date",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.Date",
                          "value" : 10000000
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void simpleTypesString() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "string",
                      "value" : {
                        "Literal" : {
                          "type" : "java.lang.String",
                          "value" : "string value"
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void keywordInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "kw",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.catalog.KeywordInfo",
                          "value" : {
                            "value" : "There is no need to try catching Chuck Norris' exceptions for recovery; every single throw he does is fatal.",
                            "language" : "eng",
                            "vocabulary" : "watchit"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void arrayTypes_scalar_byte_array() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "bytea",
                      "value" : {
                        "Literal" : {
                          "type" : "byte[]",
                          "value" : "AQID"
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void arrayTypes_scalar_boolean_array() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "booleana",
                      "value" : {
                        "Literal" : {
                          "type" : "boolean[]",
                          "value" : [ false, true, false ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void arrayTypes_scalar_char_array() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "chararray",
                      "value" : {
                        "Literal" : {
                          "type" : "char[]",
                          "value" : [ "a", "b", "c", "d" ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void arrayTypes_non_scalar() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "ns_array",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.catalog.NamespaceInfo[]",
                          "value" : [ {
                            "@type" : "NamespaceInfo",
                            "id" : "ns1",
                            "name" : "wsName",
                            "isolated" : false,
                            "uri" : "nsURI"
                          }, {
                            "@type" : "NamespaceInfo",
                            "id" : "ns2",
                            "name" : "aaa",
                            "isolated" : false,
                            "uri" : "nsURIaaa"
                          }, {
                            "@type" : "NamespaceInfo",
                            "id" : "ns3",
                            "name" : "bbb",
                            "isolated" : false,
                            "uri" : "nsURIbbb"
                          } ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void coordinateReferenceSystem() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "crs",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geotools.api.referencing.crs.CoordinateReferenceSystem",
                          "value" : {
                            "srs" : "EPSG:3857"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void attributionInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "attribution",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.catalog.impl.AttributionInfoImpl",
                          "value" : {
                            "id" : "010-87-4039",
                            "title" : "recontextualize end-to-end eyeballs",
                            "href" : "www.predovic-hahn.com",
                            "logoWidth" : 384,
                            "logoHeight" : 360
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void wmsLayerInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "wmsl",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "WMSLAYER",
                            "id" : "wmsl-1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void metadataLinkInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "metadataLink",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.catalog.MetadataLinkInfo",
                          "value" : {
                            "id" : "524-93-9700",
                            "type" : "type",
                            "about" : "grid-enabled",
                            "metadataType" : "metadataType",
                            "content" : "www.wiza-reynolds.co"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceCoverageStoreInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "coverageStore",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "COVERAGESTORE",
                            "id" : "cs1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void setPropertyOfInfoReferences() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "workspaces",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.Set",
                          "contentType" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : [ {
                            "type" : "WORKSPACE",
                            "id" : "ws2"
                          }, {
                            "type" : "WORKSPACE",
                            "id" : "ws1"
                          } ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void setPropertyAttributionInfoImpl() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "attribution",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.Set",
                          "contentType" : "org.geoserver.catalog.impl.AttributionInfoImpl",
                          "value" : [ {
                            "id" : "265-45-0195",
                            "title" : "visualize cross-platform methodologies",
                            "href" : "www.daremayertandcorkery.com",
                            "logoWidth" : 239,
                            "logoHeight" : 404
                          }, {
                            "id" : "117-33-7242",
                            "title" : "strategize strategic eyeballs",
                            "href" : "www.sporer-klein.io",
                            "logoWidth" : 211,
                            "logoHeight" : 486
                          } ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void setPropertyContactInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "contact",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.Set",
                          "contentType" : "org.geoserver.config.ContactInfo",
                          "value" : [ {
                            "id" : "176-37-2932",
                            "address" : "Apt. 785 3654 Herman Circles, New Lindsay, MA 78989-8200",
                            "addressCity" : "Port Elliot",
                            "addressCountry" : "Canada",
                            "addressDeliveryPoint" : "Apt. 757",
                            "addressPostalCode" : "11461",
                            "addressState" : "Minnesota",
                            "contactFacsimile" : "(463) 388-1244 x2458",
                            "contactOrganization" : "Kuhic-Kris",
                            "contactPerson" : "Shauna Rowe",
                            "contactVoice" : "(703) 048-6114",
                            "onlineResource" : "www.legros-considine.com",
                            "internationalAddress" : {
                              "de" : "Apt. 234 Lessingstr. 1, Schön Marcushagen, HB 17797",
                              "it" : "Incrocio Ferraro 63, Appartamento 19, Galli lido, RI 21817"
                            }
                          }, {
                            "id" : "190-03-5875",
                            "address" : "00231 Yundt Forges, Claudport, ND 20631",
                            "addressCity" : "Lake Alfredo",
                            "addressCountry" : "Yemen",
                            "addressDeliveryPoint" : "Suite 474",
                            "addressPostalCode" : "16473",
                            "addressState" : "California",
                            "contactFacsimile" : "(226) 038-1173",
                            "contactOrganization" : "Franecki and Sons",
                            "contactPerson" : "Elwood Kutch",
                            "contactVoice" : "(857) 547-4191",
                            "onlineResource" : "www.hessel-langworth.net",
                            "internationalAddress" : {
                              "de" : "Zimmer 894 Heinrich-Hörlein-Str. 720, Bad Selim, SL 85613",
                              "it" : "Appartamento 03 Via Prisca 8, Piano 6, Settimo Augusto lido, IS 31922"
                            }
                          } ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void attributeTypeInfo_list() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "attributes",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.List",
                          "contentType" : "org.geoserver.catalog.AttributeTypeInfo",
                          "value" : [ {
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
                            "source" : "\"id\""
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
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void wmsInterpolation() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "defaultWmsInterpolation",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.catalog.LayerInfo.WMSInterpolation",
                          "value" : "Bicubic"
                        }
                      }
                    } ]
                  }
                }
                """;
        assertPatch(json, "defaultWmsInterpolation", WMSInterpolation.Bicubic);
    }

    @Test
    void dimensionInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "dimensionInfo",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.catalog.DimensionInfo",
                          "value" : {
                            "enabled" : true,
                            "attribute" : "attribute",
                            "presentation" : "DISCRETE_INTERVAL",
                            "resolution" : 183.6448,
                            "units" : "metre",
                            "unitSymbol" : "m",
                            "nearestMatchEnabled" : true,
                            "rawNearestMatchEnabled" : false,
                            "acceptableInterval" : "searchRange",
                            "defaultValueStrategy" : "MAXIMUM",
                            "defaultValueReferenceValue" : "referenceValue"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceLayerGroupInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "layer",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "LAYERGROUP",
                            "id" : "lg1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void layerInfo_value_object_properties() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "legend",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.catalog.LegendInfo",
                          "value" : {
                            "id" : "566-45-0478",
                            "width" : 19,
                            "height" : 18,
                            "format" : "image/png",
                            "onlineResource" : "www.olympia-russel.com"
                          }
                        }
                      }
                    }, {
                      "name" : "attribution",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.catalog.impl.AttributionInfoImpl",
                          "value" : {
                            "id" : "738-05-4217",
                            "title" : "generate 24/365 niches",
                            "href" : "www.hills-mitchell.net",
                            "logoWidth" : 140,
                            "logoHeight" : 287
                          }
                        }
                      }
                    }, {
                      "name" : "authorityURLs",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.List",
                          "contentType" : "org.geoserver.catalog.AuthorityURLInfo",
                          "value" : [ {
                            "name" : "test-auth-url-6488",
                            "href" : "www.grady-hermann.com"
                          }, {
                            "name" : "test-auth-url-3429",
                            "href" : "www.effertzandsons.info"
                          } ]
                        }
                      }
                    }, {
                      "name" : "identifiers",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.List",
                          "contentType" : "org.geoserver.catalog.LayerIdentifierInfo",
                          "value" : [ {
                            "authority" : "www.williamsongroup.io",
                            "identifier" : "129-93-6787"
                          }, {
                            "authority" : "www.morar-collier.name",
                            "identifier" : "335-91-4503"
                          } ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void namespaceInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "namespace",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "NAMESPACE",
                            "id" : "ns1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void gridGeometry() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "gridGeometry",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geotools.coverage.grid.GridGeometry2D",
                          "value" : {
                            "@type" : "GridGeometry2D",
                            "low" : [ 0, 0 ],
                            "high" : [ 1024, 768 ],
                            "transform" : [ 0.3515625, 0.0, 0.0, -0.234375, -179.82421875, 89.8828125 ],
                            "crs" : {
                              "srs" : "EPSG:4326"
                            }
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceFeatureTypeInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "ft",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "FEATURETYPE",
                            "id" : "ft1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceDataStoreInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "dataStore",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "DATASTORE",
                            "id" : "ds1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void version_list() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "version",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.List",
                          "contentType" : "org.geotools.util.Version",
                          "value" : [ {
                            "value" : "1.0.1"
                          }, {
                            "value" : "1.0.2"
                          } ]
                        }
                      }
                    } ]
                  }
                }
                """;
        assertPatch(json, "version", List.of(new Version("1.0.1"), new Version("1.0.2")));
    }

    @Test
    void name() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "name",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geotools.api.feature.type.Name",
                          "value" : {
                            "localPart" : "localname"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void contactInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "contact",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.config.ContactInfo",
                          "value" : {
                            "id" : "654-04-4985",
                            "address" : "Suite 017 424 Fisher Mountains, Howechester, NE 64948-2842",
                            "addressCity" : "New Malcom",
                            "addressCountry" : "Niger",
                            "addressDeliveryPoint" : "Suite 346",
                            "addressPostalCode" : "39045",
                            "addressState" : "Virginia",
                            "contactFacsimile" : "(375) 900-9409",
                            "contactOrganization" : "Jaskolski-Gleason",
                            "contactPerson" : "Gino Cartwright",
                            "contactVoice" : "1-372-071-2456",
                            "onlineResource" : "www.wisozkinc.io",
                            "internationalAddress" : {
                              "de" : "Grunewaldstr. 91b, Groß Curt, BB 25935",
                              "it" : "Via Sabino 8, Appartamento 82, Palumbo lido, CZ 80191"
                            }
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void authorityURLInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "authorityURL",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.catalog.AuthorityURLInfo",
                          "value" : {
                            "name" : "test-auth-url-5879",
                            "href" : "www.borer-schulist.io"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void version() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "version",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geotools.util.Version",
                          "value" : {
                            "value" : "1.0.1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, "version", new Version("1.0.1"));
    }

    @Test
    void referencedEnvelope() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "bounds",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geotools.geometry.jts.ReferencedEnvelope",
                          "value" : {
                            "crs" : {
                              "srs" : "EPSG:3857"
                            },
                            "coordinates" : [ 0.0, 1000.0, -1000.0, -1.0 ]
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void name_with_ns() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "name",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geotools.api.feature.type.Name",
                          "value" : {
                            "namespaceURI" : "http://name.space",
                            "localPart" : "localname"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void metadataMapWithCogSettings() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "metadata",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.Map",
                          "value" : {
                            "cogSettings" : {
                              "Literal" : {
                                "type" : "org.geoserver.cog.CogSettings",
                                "value" : {
                                  "rangeReaderSettings" : "Azure",
                                  "useCachingStream" : true
                                }
                              }
                            },
                            "cogSettingsStore" : {
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
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void listProperty_nulls() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "nullvalue",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.List",
                          "value" : [ null, null ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void listProperty_ints() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "int",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.List",
                          "contentType" : "java.lang.Integer",
                          "value" : [ 2147483647, -2147483648 ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void listProperty_longs() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "long",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.List",
                          "contentType" : "java.lang.Long",
                          "value" : [ 9223372036854775807, -9223372036854775808 ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void listProperty_dates() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "date",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.List",
                          "contentType" : "java.util.Date",
                          "value" : [ 10000000, 11000000 ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void listProperty_strings() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "string",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.List",
                          "contentType" : "java.lang.String",
                          "value" : [ "string1", "string2" ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void listProperty_InfoReference() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "workspaces",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.List",
                          "contentType" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : [ {
                            "type" : "WORKSPACE",
                            "id" : "ws1"
                          }, {
                            "type" : "WORKSPACE",
                            "id" : "ws2"
                          } ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void listProperty_AttributionInfoImpl() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "attribution",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.List",
                          "contentType" : "org.geoserver.catalog.impl.AttributionInfoImpl",
                          "value" : [ {
                            "id" : "433-24-5398",
                            "title" : "cultivate transparent interfaces",
                            "href" : "www.bartongroup.name",
                            "logoWidth" : 487,
                            "logoHeight" : 306
                          }, {
                            "id" : "092-25-9798",
                            "title" : "utilize enterprise eyeballs",
                            "href" : "www.haaggoldnerandbaumbach.name",
                            "logoWidth" : 416,
                            "logoHeight" : 409
                          } ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void listProperty_ContactInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "contact",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.List",
                          "contentType" : "org.geoserver.config.ContactInfo",
                          "value" : [ {
                            "id" : "279-32-1030",
                            "address" : "9535 Clay Skyway, Runolfssonshire, CO 07279-3991",
                            "addressCity" : "South Tawnya",
                            "addressCountry" : "Canada",
                            "addressDeliveryPoint" : "Apt. 837",
                            "addressPostalCode" : "80260-8900",
                            "addressState" : "Arizona",
                            "contactFacsimile" : "127.892.0330",
                            "contactOrganization" : "Windler-Schmidt",
                            "contactPerson" : "Nereida Block",
                            "contactVoice" : "076.871.7940",
                            "onlineResource" : "www.kublittelandkessler.name",
                            "internationalAddress" : {
                              "de" : "Quettinger Str. 51, Süd Enricoland, SH 35467",
                              "it" : "Strada Caruso 25, Bernardi terme, TV 68154"
                            }
                          }, {
                            "id" : "117-70-3401",
                            "address" : "4273 Zboncak Wall, Townechester, RI 86641",
                            "addressCity" : "Port Muimouth",
                            "addressCountry" : "Iceland",
                            "addressDeliveryPoint" : "Apt. 038",
                            "addressPostalCode" : "30730",
                            "addressState" : "Florida",
                            "contactFacsimile" : "1-915-924-3440",
                            "contactOrganization" : "Heidenreich Group",
                            "contactPerson" : "Hector Schaden",
                            "contactVoice" : "1-363-959-8809",
                            "onlineResource" : "www.schneider-heidenreich.org",
                            "internationalAddress" : {
                              "de" : "Zimmer 684 Golo-Mann-Str. 1, Ost Aymanscheid, SL 73183",
                              "it" : "Incrocio Coppola 679, Settimo Raoul, EN 01721"
                            }
                          } ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void settingsInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "settings",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.config.SettingsInfo",
                          "value" : {
                            "@type" : "SettingsInfo",
                            "id" : "global-settings-id",
                            "title" : "Global Settings",
                            "contact" : {
                              "id" : "539-72-1545",
                              "address" : "Suite 109 9637 Anitra Lake, East Shannon, PA 86621",
                              "addressCity" : "Zackstad",
                              "addressCountry" : "Liberia",
                              "addressDeliveryPoint" : "Suite 508",
                              "addressPostalCode" : "19417",
                              "addressState" : "Hawaii",
                              "contactFacsimile" : "(129) 166-0540",
                              "contactOrganization" : "Schaden, Jaskolski and Stamm",
                              "contactPerson" : "Era Hirthe PhD",
                              "contactVoice" : "948-883-7861",
                              "onlineResource" : "www.jacobsllc.info",
                              "internationalAddress" : {
                                "de" : "Friedrich-Engels-Str. 43a, Neu Nikestadt, MV 68400",
                                "it" : "Incrocio Galli 920, Piano 8, Odino a mare, MT 97574"
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
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceGeoServerInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "global",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "GLOBAL",
                            "id" : "GeoServer.global"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void numberRange() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "range",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geotools.util.NumberRange",
                          "value" : {
                            "min" : 4.9E-324,
                            "max" : 1.01,
                            "minIncluded" : true,
                            "maxIncluded" : true
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void keywordInfo_list() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "keywords",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.List",
                          "contentType" : "org.geoserver.catalog.KeywordInfo",
                          "value" : [ {
                            "value" : "Chuck Norris does not use revision control software. None of his code has ever needed revision.",
                            "language" : "eng",
                            "vocabulary" : "watchit"
                          }, null, {
                            "value" : "Chuck Norris doesn't need the cloud to scale his applications, he uses his laptop.",
                            "language" : "eng",
                            "vocabulary" : "watchit"
                          } ]
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void growableInternationalString() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "growableI18n",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geotools.api.util.InternationalString",
                          "value" : {
                            "" : "default lang",
                            "es-AR" : "en argentino",
                            "es" : "en español"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceWmtsStoreInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "wmts",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "WMTSSTORE",
                            "id" : "wmts1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void measure_meters() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "meters",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geotools.measure.Measure",
                          "value" : "1000m"
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void measure_radians() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "radians",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geotools.measure.Measure",
                          "value" : "0.75rad/s"
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceWorkspaceInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "workspace",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "WORKSPACE",
                            "id" : "ws1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void layerIdentifierInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "layerIdentifier",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.catalog.LayerIdentifierInfo",
                          "value" : {
                            "authority" : "www.blickandsons.org",
                            "identifier" : "008-19-0152"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceWcsInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "wcsService",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "SERVICE",
                            "id" : "wcs-id"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceWfsInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "wfsService",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "SERVICE",
                            "id" : "wfs-id"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void class_property() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "binding",
                      "value" : {
                        "Literal" : {
                          "type" : "java.lang.Class",
                          "value" : "org.geotools.util.Version"
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceNamespace() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "namespace",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "NAMESPACE",
                            "id" : "ns1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceWmsInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "wmsService",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "SERVICE",
                            "id" : "wms-id"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceWpsInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "wpsService",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "SERVICE",
                            "id" : "wps-id"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void settingsInfo_workspace() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "settings",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.config.SettingsInfo",
                          "value" : {
                            "@type" : "SettingsInfo",
                            "id" : "wsName-settings-id",
                            "workspace" : "ws1",
                            "title" : "wsName Settings",
                            "contact" : {
                              "id" : "878-69-0885",
                              "address" : "160 Su Ridge, Pricehaven, CA 15257-7447",
                              "addressCity" : "Lake Kimfurt",
                              "addressCountry" : "Turks and Caicos Islands",
                              "addressDeliveryPoint" : "Apt. 577",
                              "addressPostalCode" : "48285-3936",
                              "addressState" : "Utah",
                              "contactFacsimile" : "1-221-721-8402 x4601",
                              "contactOrganization" : "Boyle-Jakubowski",
                              "contactPerson" : "Ms. Taylor McClure",
                              "contactVoice" : "701-003-8710",
                              "onlineResource" : "www.beierkrajcikandhomenick.info",
                              "internationalAddress" : {
                                "de" : "Apt. 601 In der Wasserkuhl 55c, Götzelmannberg, NI 04915",
                                "it" : "Strada Ferrari 26, Appartamento 26, Vania nell'emilia, TE 82933"
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
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void coverageAccessInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "coverageInfo",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.config.CoverageAccessInfo",
                          "value" : {
                            "corePoolSize" : 10,
                            "keepAliveTime" : 30000,
                            "maxPoolSize" : 5,
                            "queueType" : "UNBOUNDED",
                            "imageIOCacheThreshold" : 10240
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceStyleInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "style",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "STYLE",
                            "id" : "style1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void infoReferenceWmtsLayerInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "wmtsl",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "WMTSLAYER",
                            "id" : "wmtsl1"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void coverageDimensionInfo() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "coverageDimensionInfo",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.catalog.CoverageDimensionInfo",
                          "value" : {
                            "id" : "452-31-3561",
                            "name" : "jast.net",
                            "description" : "envisioneer rich platforms",
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
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    @Test
    void layerInfoStyles() {
        String json =
                """
                {
                  "Patch" : {
                    "patches" : [ {
                      "name" : "styles",
                      "value" : {
                        "Literal" : {
                          "type" : "java.util.Set",
                          "contentType" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : [ {
                            "type" : "STYLE",
                            "id" : "s1-id"
                          }, {
                            "type" : "STYLE",
                            "id" : "s2-id"
                          } ]
                        }
                      }
                    }, {
                      "name" : "defaultStyle",
                      "value" : {
                        "Literal" : {
                          "type" : "org.geoserver.jackson.databind.catalog.dto.InfoReference",
                          "value" : {
                            "type" : "STYLE",
                            "id" : "style2"
                          }
                        }
                      }
                    } ]
                  }
                }
                """;

        assertPatch(json, null, null);
    }

    private void assertPatch(String json, String name, Object value) {
        Patch expected = Patch.of(name, value);
        assertThat(decode(json, Patch.class)).isEqualTo(expected);
    }
}
