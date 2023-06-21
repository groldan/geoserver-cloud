/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.web.core;

import org.apache.wicket.protocol.http.WicketServlet;
import org.geoserver.cloud.config.factory.FilteringXmlBeanDefinitionReader;
import org.geoserver.web.GeoServerBasePage;
import org.geoserver.web.GeoServerWicketServlet;
import org.geoserver.web.HeaderContribution;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.vfny.geoserver.wfs.servlets.TestWfsPost;

@Configuration(proxyBeanMethods = true)
@ImportResource( //
        reader = FilteringXmlBeanDefinitionReader.class, //
        locations = { //
            "jar:gs-wfs-.*!/applicationContext.xml",
            "jar:gs-web-core-.*!/applicationContext.xml#name="
                    + WebCoreConfiguration.EXCLUDED_BEANS_PATTERN //
        })
public class WebCoreConfiguration {

    static final String EXCLUDED_BEANS_PATTERN = "^(?!logsPage).*$";

    public @Bean GeoServerWicketServlet geoServerWicketServlet() {
        return new GeoServerWicketServlet();
    }

    public @Bean TestWfsPost testWfsPostServlet() {
        return new TestWfsPost();
    }

    /** Register the {@link WicketServlet} */
    public @Bean ServletRegistrationBean<GeoServerWicketServlet>
            geoServerWicketServletRegistration() {
        GeoServerWicketServlet servlet = geoServerWicketServlet();
        ServletRegistrationBean<GeoServerWicketServlet> registration;
        registration =
                new ServletRegistrationBean<GeoServerWicketServlet>(servlet, "/web", "/web/*");

        return registration;
    }

    /** Register the {@link TestWfsPost servlet} */
    public @Bean ServletRegistrationBean<TestWfsPost> wfsTestServletRegistration() {
        TestWfsPost servlet = testWfsPostServlet();
        ServletRegistrationBean<TestWfsPost> registration;
        registration = new ServletRegistrationBean<TestWfsPost>(servlet, "/TestWfsPost");

        return registration;
    }

    public @Bean HeaderContribution geoserverCloudCssTheme() {
        HeaderContribution contribution = new HeaderContribution();
        contribution.setScope(GeoServerBasePage.class);
        contribution.setCSSFilename("geoserver-cloud.css");
        // contribution.setFaviconFilename("favicon.ico");
        return contribution;
    }
}
