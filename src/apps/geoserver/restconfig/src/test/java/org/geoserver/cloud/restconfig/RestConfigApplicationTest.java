/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.restconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_HTML;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.SLDHandler;
import org.geoserver.catalog.event.CatalogListener;
import org.geoserver.gwc.GWC;
import org.geoserver.gwc.layer.CatalogLayerEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RestConfigApplicationTest {

    @Autowired private TestRestTemplate restTemplate;

    @BeforeEach
    void before() {
        restTemplate = restTemplate.withBasicAuth("admin", "geoserver");
    }

    @Test
    void testDefaultContentType() {

        testPathExtensionContentType("/rest/workspaces", APPLICATION_JSON);
        testPathExtensionContentType("/rest/layers", APPLICATION_JSON);
    }

    @Test
    void testPathExtensionContentNegotiation() {

        testPathExtensionContentType("/rest/styles/line.json", APPLICATION_JSON);
        testPathExtensionContentType("/rest/styles/line.xml", APPLICATION_XML);
        testPathExtensionContentType("/rest/styles/line.html", TEXT_HTML);
        testPathExtensionContentType(
                "/rest/styles/line.sld", MediaType.valueOf(SLDHandler.MIMETYPE_10));

        testPathExtensionContentType("/rest/workspaces.html", TEXT_HTML);
        testPathExtensionContentType("/rest/workspaces.xml", APPLICATION_XML);
        testPathExtensionContentType("/rest/workspaces.json", APPLICATION_JSON);
    }

    protected void testPathExtensionContentType(String uri, MediaType expected) {
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(expected);
    }

    @Test
    void gwcIntegrationTest(@LocalServerPort int servicePort, ApplicationContext context) {
        assertThat(GWC.get()).isNotNull();
        var catalog = context.getBean("rawCatalog", Catalog.class);
        Optional<CatalogListener> listener =
                catalog.getListeners().stream()
                        .filter(CatalogLayerEventListener.class::isInstance)
                        .findFirst();
        assertThat(listener)
                .as(
                        "Expected Catalog to have an org.geoserver.gwc.layer.CatalogLayerEventListener listener")
                .isPresent();
    }
}
