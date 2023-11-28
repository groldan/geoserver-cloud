/*
 * (c) 2023 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.wfs.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.client.AclClient;
import org.geoserver.acl.client.AclClientAdaptor;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.geoserver.acl.testcontainer.GeoServerAclContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = WfsApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = RabbitAutoConfiguration.class)
@ActiveProfiles("test")
@Testcontainers
class WfsApplicationACL_IT {

    static @Container GeoServerAclContainer acl =
            GeoServerAclContainer.currentVersion().withDevMode();

    @DynamicPropertySource
    static void setUpAcl(DynamicPropertyRegistry registry) {
        registry.add("geoserver.acl.enabled", Boolean.TRUE::booleanValue);
        registry.add("geoserver.acl.client.basePath", acl::apiUrl);
        registry.add("geoserver.acl.client.username", acl::devAdminUser);
        registry.add("geoserver.acl.client.password", acl::devAdminPassword);
        registry.add("geoserver.acl.client.debug", Boolean.TRUE::booleanValue);
    }

    private RuleRepository aclRules;
    private AuthorizationService aclAuthorization;

    @BeforeEach
    void beforeEach() {
        assertTrue(acl.isRunning());

        final String apiUrl = acl.apiUrl();
        final String username = acl.devAdminUser();
        final String password = acl.devAdminPassword();

        AclClient client =
                new AclClient() //
                        .setBasePath(apiUrl) //
                        .setUsername(username) //
                        .setPassword(password);

        AclClientAdaptor adaptor = new AclClientAdaptor(client);
        aclRules = adaptor.getRuleRepository();
        aclAuthorization = adaptor.getAuthorizationService();
    }

    @Test
    @Order(1)
    public void contextLoads() {}
}
