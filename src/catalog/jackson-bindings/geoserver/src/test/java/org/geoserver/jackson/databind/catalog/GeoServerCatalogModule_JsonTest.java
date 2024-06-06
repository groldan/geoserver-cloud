/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.geotools.jackson.databind.util.ObjectMapperUtil;
import org.junit.jupiter.api.Test;

/**
 * @since 1.0
 */
class GeoServerCatalogModule_JsonTest extends GeoServerCatalogModuleTest {

    protected @Override ObjectMapper newObjectMapper() {
        return ObjectMapperUtil.newObjectMapper();
    }

    @Test
    void storeParameterScalars() {
        super.storeParameterScalars();
    }

    @Test
    void storeParameterPrimitives() {
        super.storeParameterPrimitives();
    }

    @Test
    void storeParameterReferencedEnvelope() {
        super.storeParameterReferencedEnvelope();
    }
}
