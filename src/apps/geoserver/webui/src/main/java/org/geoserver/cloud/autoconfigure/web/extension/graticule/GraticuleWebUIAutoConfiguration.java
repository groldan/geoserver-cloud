/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.web.extension.graticule;

import org.geoserver.cloud.config.factory.FilteringXmlBeanDefinitionReader;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/** Auto configuration to enable the graticule customized store panel when the web-ui is present. */
@Configuration
@ImportResource( //
        reader = FilteringXmlBeanDefinitionReader.class, //
        locations = {"jar:gs-graticule-.*!/applicationContext.xml"})
public class GraticuleWebUIAutoConfiguration {}
