/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.wms.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.geoserver.cloud.autoconfigure.gwc.integration.WMSIntegrationAutoConfiguration.ForwardGetMapToGwcAspect;
import org.geoserver.cloud.virtualservice.VirtualServiceVerifier;
import org.geoserver.cloud.wms.controller.GetMapReflectorController;
import org.geoserver.cloud.wms.controller.WMSController;
import org.geoserver.gwc.wms.CachingExtendedCapabilitiesProvider;
import org.geoserver.ows.FlatKvpParser;
import org.geoserver.ows.kvp.CQLFilterKvpParser;
import org.geoserver.ows.kvp.SortByKvpParser;
import org.geoserver.ows.kvp.ViewParamsKvpParser;
import org.geoserver.ows.util.NumericKvpParser;
import org.geoserver.wfs.kvp.BBoxKvpParser;
import org.geoserver.wfs.xml.v1_1_0.WFSConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

abstract class WmsApplicationTest {

    protected @Autowired ConfigurableApplicationContext context;

    @Test
    void testExpectedBeansFromWmsApplicationAutoConfiguration() {
        expecteBean("wfsConfiguration", WFSConfiguration.class);
        expecteBean("webMapServiceController", WMSController.class);
        expecteBean("virtualServiceVerifier", VirtualServiceVerifier.class);
        expecteBean("getMapReflectorController", GetMapReflectorController.class);
        expecteBean(
                "wms_1_1_1_GetCapabilitiesResponse",
                org.geoserver.wms.capabilities.GetCapabilitiesResponse.class);
    }

    @Test
    void testExpectedBeansFromGsWfsJarFile() {
        expecteBean("bboxKvpParser", BBoxKvpParser.class);
        expecteBean("featureIdKvpParser", FlatKvpParser.class);
        expecteBean("cqlKvpParser", CQLFilterKvpParser.class);
        expecteBean("maxFeatureKvpParser", NumericKvpParser.class);
        expecteBean("sortByKvpParser", SortByKvpParser.class);
        expecteBean("wfsSqlViewKvpParser", ViewParamsKvpParser.class);

        expecteBean("wfsXsd-1.0", org.geoserver.wfs.xml.v1_0_0.WFS.class);
        expecteBean("wfsXmlConfiguration-1.0", org.geoserver.wfs.xml.v1_0_0.WFSConfiguration.class);

        expecteBean("wfsXsd-1.1", org.geoserver.wfs.xml.v1_1_0.WFS.class);
        expecteBean("wfsXmlConfiguration-1.1", org.geoserver.wfs.xml.v1_1_0.WFSConfiguration.class);

        expecteBean("wfsXsd-1.0", org.geoserver.wfs.xml.v1_0_0.WFS.class);
        expecteBean("wfsXmlConfiguration-1.0", org.geoserver.wfs.xml.v1_0_0.WFSConfiguration.class);

        expecteBean("filter1_0_0_KvpParser", org.geoserver.wfs.kvp.Filter_1_0_0_KvpParser.class);
        expecteBean("filter1_1_0_KvpParser", org.geoserver.wfs.kvp.Filter_1_1_0_KvpParser.class);
        expecteBean("filter2_0_0_KvpParser", org.geoserver.wfs.kvp.Filter_2_0_0_KvpParser.class);

        expecteBean("gml2SchemaBuilder", org.geoserver.wfs.xml.FeatureTypeSchemaBuilder.GML2.class);
        expecteBean("gml3SchemaBuilder", org.geoserver.wfs.xml.FeatureTypeSchemaBuilder.GML3.class);

        expecteBean("gml2OutputFormat", org.geoserver.wfs.xml.GML2OutputFormat.class);
        expecteBean("gml3OutputFormat", org.geoserver.wfs.xml.GML3OutputFormat.class);
        expecteBean("gml32OutputFormat", org.geoserver.wfs.xml.GML32OutputFormat.class);
    }

    @Test
    void testGwcWmsIntegration() {
        expecteBean(
                "gwcWMSExtendedCapabilitiesProvider", CachingExtendedCapabilitiesProvider.class);
        expecteBean("gwcGetMapAdvise", ForwardGetMapToGwcAspect.class);
    }

    protected void expecteBean(String name, Class<?> type) {
        assertThat(context.getBean(name)).isInstanceOf(type);
    }
}
