/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.vector.formats;

import org.geotools.data.graticule.GraticuleDataStoreFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Configuration to enable graticule support as vector data format. */
@Configuration
@Import(value = {GraticuleWebUIAutoConfiguration.class})
public class GraticuleConfiguration {

    @Bean
    public GraticuleDataStoreFactory graticuleDataStoreFactory() {
        return new GraticuleDataStoreFactory();
    }
}
