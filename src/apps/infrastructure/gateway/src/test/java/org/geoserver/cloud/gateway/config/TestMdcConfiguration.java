/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.gateway.config;

import org.geoserver.cloud.gateway.filter.TestMdcVerificationFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Test configuration to ensure the MDC verification filter is registered.
 * Simplified for Spring Boot 3 migration.
 */
@TestConfiguration
@Profile("test")
public class TestMdcConfiguration {

    /**
     * Test filter for verifying MDC context is correctly propagated.
     */
    @Bean
    TestMdcVerificationFilter testMdcVerificationFilter() {
        return new TestMdcVerificationFilter();
    }
}
