/* (c) 2026 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.cloud.web.app;

import static org.assertj.core.api.Assertions.assertThat;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.geoserver.platform.ModuleStatus;
import org.geoserver.security.oauth2.login.GeoServerOAuth2LoginAuthenticationProvider;
import org.geoserver.web.security.oauth2.login.OAuth2LoginAuthProviderPanelInfo;
import org.geoserver.web.security.oauth2.login.OAuth2LoginButtonManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Full-stack integration test that boots the {@link WebUIApplication} with the OIDC extension enabled and a Keycloak
 * Testcontainer running. Verifies that the autoconfig wires cleanly in a real Spring Boot app, both the core OIDC beans
 * and the Wicket UI beans, and that the Keycloak container's discovery endpoint is reachable.
 *
 * <p>The login flow itself (browser redirect, Keycloak authentication, callback) is covered upstream; this IT verifies
 * only the Cloud integration plumbing.
 *
 * @since 3.1.0
 */
@SpringBootTest(
        properties = {
            "spring.cloud.bus.enabled=false",
            "spring.cloud.config.enabled=false",
            "spring.cloud.config.discovery.enabled=false",
            "spring.cloud.consul.enabled=false",
            "spring.cloud.consul.discovery.enabled=false",
            "geoserver.extension.security.oidc.enabled=true"
        })
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class OidcWebUIApplicationIT {

    @Container
    static final KeycloakContainer KEYCLOAK = new KeycloakContainer()
            .withRealmImportFile("/keycloak/master-realm.json")
            .withRealmImportFile("/keycloak/gs-realm-realm.json");

    static @TempDir Path tmpdir;
    static Path datadir;

    @DynamicPropertySource
    static void setUpDataDir(DynamicPropertyRegistry registry) throws IOException {
        datadir = tmpdir.resolve("datadir");
        Path gwcdir = datadir.resolve("gwc");
        if (!Files.isDirectory(datadir)) {
            Files.createDirectory(datadir);
        }
        if (!Files.isDirectory(gwcdir)) {
            Files.createDirectory(gwcdir);
        }
        registry.add("geoserver.backend.data-directory.location", datadir::toAbsolutePath);
        registry.add("gwc.cache-directory", gwcdir::toAbsolutePath);
    }

    @Autowired
    ApplicationContext appContext;

    @Test
    void oidcBeansRegisteredWhenEnabled() {
        assertThat(appContext.getBeansOfType(GeoServerOAuth2LoginAuthenticationProvider.class))
                .as("OIDC authentication provider must be registered when the extension is enabled")
                .hasSize(1);
        assertThat(appContext.getBean("openIdConnectCoreExtension", ModuleStatus.class))
                .hasFieldOrPropertyWithValue("module", "gs-sec-oidc-core");
    }

    @Test
    void oidcWebUIBeansRegisteredInWebUI() {
        assertThat(appContext.getBeansOfType(OAuth2LoginAuthProviderPanelInfo.class))
                .as("OIDC admin-UI panel info must be registered in the webui service")
                .hasSize(1);
        assertThat(appContext.getBeansOfType(OAuth2LoginButtonManager.class))
                .as("OIDC login button manager must be registered in the webui service")
                .hasSize(1);
        assertThat(appContext.getBean("openIdConnectWebExtension", ModuleStatus.class))
                .hasFieldOrPropertyWithValue("module", "gs-sec-oidc-web");
    }

    @Test
    void keycloakContainerIsReachable() {
        assertThat(KEYCLOAK.isRunning()).isTrue();
        assertThat(KEYCLOAK.getAuthServerUrl()).startsWith("http");
    }
}
