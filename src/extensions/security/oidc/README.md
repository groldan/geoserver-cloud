# OIDC / OAuth2 Login Extension

Auto-configuration for the GeoServer OpenID Connect / OAuth2 Login extension.

## Overview

Integrates the upstream GeoServer OpenID Connect extension
([`gs-sec-oidc-core` and `gs-sec-oidc-web`](https://github.com/geoserver/geoserver/tree/main/src/extension/security),
groupId `org.geoserver.extension`) into GeoServer Cloud. When enabled, the OIDC authentication
provider becomes available to configure from the GeoServer admin UI (Security > Authentication),
with support for Google, GitHub, Microsoft Azure and any generic OpenID Connect provider.

The extension is **disabled by default**. It must be turned on explicitly because operation requires
deployment-specific credentials (client id/secret, issuer URIs, etc.) that have no sensible default.

## Features

- Four providers out of the box: Google, GitHub, Microsoft, generic OIDC
- Role extraction from ID token, access token, userinfo endpoint, or Microsoft Graph API
- Standard GeoServer XStream-persisted filter configuration: provider settings are stored in the
  data directory under `security/filter/<filtername>/config.xml` and edited via the Wicket admin UI
- Bearer token (resource server) mode for API clients, see [Bearer tokens](#bearer-tokens)
- Cross-pod propagation of browser logins via the existing
  [`gateway-shared-auth`](../gateway-shared-auth/README.md) extension

## Configuration

The extension can be enabled with:

```yaml
geoserver:
  extension:
    security:
      oidc:
        enabled: true  # Enable/disable OIDC extension (default: false)
```

Backward-compatible placeholder form:

```yaml
geoserver:
  security:
    oidc:
      enabled: true
```

The default `config/geoserver.yml` also imports an optional `config/geoserver_oidc.yml` file where
the `enabled` flag (and future provider-binding properties) can live separately from the main config.

### Admin UI walkthrough

Once the extension is enabled, an operator configures a working OIDC login as follows:

1. Log into the webui as `admin`.
2. Go to **Security > Authentication**, create a new filter of type
   **"GeoServer OAuth2 Login"**, name it (e.g. `openid-connect`) and fill in the provider details.
3. In the same page, create a **new filter chain** named `oauth-callback`:
   - Ant patterns: `/oauth2/authorization/**,/login/oauth2/code/**` (no space between)
   - Chain type: HTTP
   - Add the `openid-connect` filter to this chain
   - Make sure the new chain is positioned **before** `webLogin`
4. Open the existing `web` filter chain and add the `openid-connect` filter before `anonymous`.
5. If the role source is a user-group service, create matching users/roles.
6. Save. Log out and click the new provider button on the login page.

See the [upstream OIDC documentation](https://github.com/geoserver/geoserver/tree/main/doc/en/user/extensions/oidc)
for role sources, token validation and provider-specific details.

### Deployment notes

- **The authentication provider is registered in every service** through the security starter,
  the admin panel and login buttons only in the webui. Browser logins happen in the webui, and
  `gateway-shared-auth` forwards the username and roles to the other services as internal headers.
- **Gateway routing**: the OAuth2 callback paths (`/web/oauth2/authorization/**` and
  `/web/login/oauth2/code/**`) are covered by the default gateway route for the webui
  (`Path=${basepath}/**`). No additional gateway configuration is required.
- **`PROXY_BASE_URL`**: if the deployment is behind a reverse proxy, set `PROXY_BASE_URL`
  (or `geoserver.proxy.baseUrl`) so the upstream module computes correct redirect URIs.

### Bearer tokens

The OIDC filter has an "Enable Resource Server (Bearer JWT)" option, on by default. With it, the
same filter accepts `Authorization: Bearer <JWT>` requests from API clients, validates the token
with the provider configuration of the filter, and resolves roles with the role source, role
converter and "only listed roles" settings of the filter. Browser and API clients get the same
roles from the same configuration.

The service receiving the request validates the token. The filter therefore has to be in the
`default` chain (or the other API chains) and not only in the `web` chain. The gateway forwards the
`Authorization` header untouched and needs no token filter of its own. `gateway-shared-auth` only
applies to browser sessions.

## Implementation Details

### Key classes

- `OidcAutoConfiguration`: entry point, splits into two inner configurations:
  - `Enabled`: activated by `@ConditionalOnOidc`; imports the transpiled core Spring config
  - `WebUI`: additionally requires `@ConditionalOnGeoServerWebUI` and
    `@ConditionalOnClass(AuthenticationFilterPanelInfo.class)`; imports the transpiled webui Spring
    config (panel info + login-button beans)
- `ConditionalOnOidc`: composite conditional: `@ConditionalOnGeoServer` +
  `@ConditionalOnClass(GeoServerOAuth2LoginAuthenticationFilter.class)` + property check
- `OidcConfigProperties`: `@ConfigurationProperties(prefix = "geoserver.extension.security.oidc")`;
  exposes the single `enabled` flag (`DEFAULT = false`)

### Transpiled Spring configuration

The upstream `applicationContext.xml` files are transpiled at build time by
[`gs-spring-config-transpiler`](../../../build-tools/spring-config-transpiler/) into Java
`@Configuration` classes under
`src/config/geoserver-configuration/src/main/java/org/geoserver/configuration/extension/oidc/`:

- `OidcCoreConfiguration`: transpiles `gs-sec-oidc-core/applicationContext.xml`. It excludes the
  Spring Security internal `@Configuration` beans because the upstream XML registers them as plain
  `<bean>`s and relies on Spring's XML parser to process their own `@Bean` methods. A transpiled
  Java `@Bean` method returns a bare instance instead.
- `OidcWebUIConfiguration`: transpiles `gs-sec-oidc-web/applicationContext.xml` (panel info, four
  login-button `LoginFormInfo` beans, the button manager).
- `OidcSecurityInfrastructureImportSelector`: pulls in the three Spring Security internal
  configuration classes by FQN string. Two of them (`HttpSecurityConfiguration`,
  `ObjectPostProcessorConfiguration`) are package-private and cannot be referenced by class
  literal from another package; the `ImportSelector` sidesteps that restriction.
- `OidcContentNegotiationConfig`: registers the `contentNegotiationStrategy` bean under the exact
  name expected by Spring Security's `HttpSecurityConfiguration` for autowiring.
- `OidcDependsOnAliasConfig`: registers a placeholder bean named `oidcHttpSecurityConfiguration`
  to satisfy the `@DependsOn` ordering hint on the transpiled `oauth2LoginAuthenticationProvider`.

### Dependency versions

- GeoServer manages `com.nimbusds:nimbus-jose-jwt` at 10.10, above the 10.9.1 declared by Spring
  Security 7.1. GeoServer Cloud pins 10.10 in the `dependencyConvergence` profile of `src/pom.xml`,
  as it does for the other versions managed by upstream GeoServer.

## Testing

- `OidcAutoConfigurationTest`: slice test using `WebApplicationContextRunner`; mocks `GeoServer`
  and `GeoServerSecurityManager` (registered under the bean name `authenticationManager` to match
  the transpiled `@Qualifier`). No Docker required.
- `OidcWebUIApplicationIT`: full-stack integration test in the webui app module. Boots the
  `WebUIApplication` with `geoserver.extension.security.oidc.enabled=true` and a real Keycloak
  Testcontainer, asserts that the OIDC beans are registered and the container is reachable. Runs
  under Maven Failsafe (`mvn verify`); skips gracefully when Docker is unavailable.

## Known Limitations

- **Per-provider configuration is operator-driven, not property-driven.** Client id/secret,
  authorization/token/userinfo endpoints, role-source settings, etc. are configured through the
  Wicket admin UI and persisted via XStream in the data directory. A follow-up will add
  `@ConfigurationProperties`-based binding so those settings can be provided entirely via
  `application.yml` / environment variables.
- **Session affinity.** OIDC produces an `OAuth2AuthenticationToken` in the webui's servlet session
  (`HttpSessionSecurityContextRepository`). Cross-pod propagation is handled by `gateway-shared-auth`;
  the token itself is not replicated across webui replicas. For horizontally scaled webui, rely on
  sticky sessions at the ingress until a DB/Redis-backed `SecurityContextRepository` is available
  (requires an upstream change to make the repository injectable on
  `GeoServerOAuth2LoginAuthenticationFilterBuilder`).

## Logging

```yaml
logging:
  level:
    org.geoserver.cloud.autoconfigure.extensions.security.oidc: DEBUG
    org.geoserver.security.oauth2: DEBUG
    org.springframework.security.oauth2: DEBUG
```

The upstream module ships an `OIDC_LOGGING` template that can be enabled from the GeoServer admin
UI (Settings > Global). When selected, GeoServer switches its logging configuration to a
preset tuned for debugging OAuth2 flows.
