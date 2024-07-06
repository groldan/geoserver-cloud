# GeoServer Cloud configuration properties
All externalized configuration properties
## Table of Contents
* [**catalog-common**](#catalog-common)
  * [**geoserver.catalog** - `org.geoserver.cloud.config.catalog.backend.core.CatalogProperties`](#geoserver.catalog)

  * [**geoserver.metrics** - `org.geoserver.cloud.autoconfigure.metrics.catalog.GeoSeverMetricsConfigProperties`](#geoserver.metrics)

  * [**geotools.httpclient.proxy** - `org.geoserver.cloud.autoconfigure.geotools.GeoToolsHttpClientProxyConfigurationProperties`](#geotools.httpclient.proxy)

  * [**geotools.httpclient.proxy.http** - `org.geoserver.cloud.autoconfigure.geotools.GeoToolsHttpClientProxyConfigurationProperties$ProxyHostConfig`](#geotools.httpclient.proxy.http)

  * [**geotools.httpclient.proxy.https** - `org.geoserver.cloud.autoconfigure.geotools.GeoToolsHttpClientProxyConfigurationProperties$ProxyHostConfig`](#geotools.httpclient.proxy.https)
* [**catalog-datadir**](#catalog-datadir)
  * [**geoserver.backend.data-directory** - `org.geoserver.cloud.config.catalog.backend.datadirectory.DataDirectoryProperties`](#geoserver.backend.data-directory)

  * [**geoserver.backend.data-directory.eventual-consistency** - `org.geoserver.cloud.config.catalog.backend.datadirectory.DataDirectoryProperties$EventualConsistencyConfig`](#geoserver.backend.data-directory.eventual-consistency)
* [**catalog-pgconfig**](#catalog-pgconfig)
  * [**geoserver.backend.pgconfig** - `org.geoserver.cloud.config.catalog.backend.pgconfig.PgconfigBackendProperties`](#geoserver.backend.pgconfig)
* [**catalog-jdbcconfig**](#catalog-jdbcconfig)
  * [**geoserver.backend.jdbcconfig.datasource.xa** - `org.springframework.boot.autoconfigure.jdbc.DataSourceProperties$Xa`](#geoserver.backend.jdbcconfig.datasource.xa)

  * [**geoserver.backend.jdbcconfig** - `org.geoserver.cloud.config.catalog.backend.jdbcconfig.JdbcConfigConfigurationProperties`](#geoserver.backend.jdbcconfig)

  * [**geoserver.backend.jdbcconfig** - `org.geoserver.cloud.config.catalog.backend.jdbcconfig.CloudJdbcConfigProperties`](#geoserver.backend.jdbcconfig)

  * [**geoserver.backend.jdbcconfig** - `org.geoserver.cloud.config.catalog.backend.jdbcconfig.CloudJdbcStoreProperties`](#geoserver.backend.jdbcconfig)

  * [**geoserver.backend.jdbcconfig.datasource** - `org.geoserver.cloud.config.catalog.backend.jdbcconfig.JDBCConfigBackendConfigurer$ExtendedDataSourceProperties`](#geoserver.backend.jdbcconfig.datasource)
* [**jndi**](#jndi)
  * [**jndi** - `org.geoserver.cloud.config.jndidatasource.JNDIDataSourcesConfigurationProperties`](#jndi)
* [**security**](#security)
  * [**geoserver.security.gateway-shared-auth** - `org.geoserver.cloud.autoconfigure.authzn.GatewaySharedAuthConfigProperties`](#geoserver.security.gateway-shared-auth)
* [**gwc**](#gwc)
  * [**gwc** - `org.geoserver.cloud.gwc.config.core.GeoWebCacheConfigurationProperties`](#gwc)

  * [**gwc.blobstores** - `org.geoserver.cloud.gwc.config.core.GeoWebCacheConfigurationProperties$BlobstoresConfig`](#gwc.blobstores)

  * [**gwc.disk-quota** - `org.geoserver.cloud.gwc.config.core.GeoWebCacheConfigurationProperties$DiskQuotaConfig`](#gwc.disk-quota)

  * [**gwc.services** - `org.geoserver.cloud.gwc.config.core.GeoWebCacheConfigurationProperties$ServicesConfig`](#gwc.services)

## catalog-common
Common Catalog configuration properties
### geoserver.catalog
**Class:** `org.geoserver.cloud.config.catalog.backend.core.CatalogProperties`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| advertised| java.lang.Boolean| | true|  `GEOSERVER_CATALOG_ADVERTISED`|
| isolated| java.lang.Boolean| | true|  `GEOSERVER_CATALOG_ISOLATED`|
| local-workspace| java.lang.Boolean| | true|  `GEOSERVER_CATALOG_LOCALWORKSPACE`|
| secure| java.lang.Boolean| | true|  `GEOSERVER_CATALOG_SECURE`|
### geoserver.metrics
**Class:** `org.geoserver.cloud.autoconfigure.metrics.catalog.GeoSeverMetricsConfigProperties`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| enabled| java.lang.Boolean| | true|  `GEOSERVER_METRICS_ENABLED`|
| instance-id| java.lang.String| | |  `GEOSERVER_METRICS_INSTANCEID`|
### geotools.httpclient.proxy
**Class:** `org.geoserver.cloud.autoconfigure.geotools.GeoToolsHttpClientProxyConfigurationProperties`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| enabled| java.lang.Boolean| | true|  `GEOTOOLS_HTTPCLIENT_PROXY_ENABLED`|
### geotools.httpclient.proxy.http
**Class:** `org.geoserver.cloud.autoconfigure.geotools.GeoToolsHttpClientProxyConfigurationProperties$ProxyHostConfig`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| host| java.lang.String| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTP_HOST`|
| non-proxy-hosts| java.util.List&lt;java.util.regex.Pattern&gt;| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTP_NONPROXYHOSTS`|
| password| java.lang.String| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTP_PASSWORD`|
| port| java.lang.Integer| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTP_PORT`|
| user| java.lang.String| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTP_USER`|
| host| java.lang.String| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTPS_HOST`|
| non-proxy-hosts| java.util.List&lt;java.util.regex.Pattern&gt;| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTPS_NONPROXYHOSTS`|
| password| java.lang.String| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTPS_PASSWORD`|
| port| java.lang.Integer| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTPS_PORT`|
| user| java.lang.String| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTPS_USER`|
### geotools.httpclient.proxy.https
**Class:** `org.geoserver.cloud.autoconfigure.geotools.GeoToolsHttpClientProxyConfigurationProperties$ProxyHostConfig`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| host| java.lang.String| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTPS_HOST`|
| non-proxy-hosts| java.util.List&lt;java.util.regex.Pattern&gt;| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTPS_NONPROXYHOSTS`|
| password| java.lang.String| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTPS_PASSWORD`|
| port| java.lang.Integer| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTPS_PORT`|
| user| java.lang.String| | |  `GEOTOOLS_HTTPCLIENT_PROXY_HTTPS_USER`|

## catalog-datadir
Datadir Catalog back-end configuration properties
### geoserver.backend.data-directory
**Class:** `org.geoserver.cloud.config.catalog.backend.datadirectory.DataDirectoryProperties`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| enabled| java.lang.Boolean| Whether to use the data-directory catalog back-end to host the GeoServer catalog and services configuration. Only one catalog back-end can be enabled, and it&#x27;s usually done by enabling the corresponding Spring profile (e.g. runnning the applications with -Dspring.profiles.active&#x3D;datadir,...)| false|  `GEOSERVER_BACKEND_DATADIRECTORY_ENABLED`|
| location| java.nio.file.Path| | |  `GEOSERVER_BACKEND_DATADIRECTORY_LOCATION`|
| parallel-loader| java.lang.Boolean| | true|  `GEOSERVER_BACKEND_DATADIRECTORY_PARALLELLOADER`|
### geoserver.backend.data-directory.eventual-consistency
**Class:** `org.geoserver.cloud.config.catalog.backend.datadirectory.DataDirectoryProperties$EventualConsistencyConfig`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| enabled| java.lang.Boolean| If enabled, the data directory catalog will be resilient to bus events coming out of order| true|  `GEOSERVER_BACKEND_DATADIRECTORY_EVENTUALCONSISTENCY_ENABLED`|
| retries| java.util.List&lt;java.lang.Integer&gt;| milliseconds to wait before retrying Catalog.getXXX point queries returning null. The list size determines the number of retries. The values the milliseconds to wait| |  `GEOSERVER_BACKEND_DATADIRECTORY_EVENTUALCONSISTENCY_RETRIES`|

## catalog-pgconfig
Configuration properties to set up the PostgreSQL
					  connection pool used by the &#x60;pgconfig&#x60; Catalogback-end
### geoserver.backend.pgconfig
**Class:** `org.geoserver.cloud.config.catalog.backend.pgconfig.PgconfigBackendProperties`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| create-schema| java.lang.Boolean| Whether to create the postgres schema if it doesn&#x27;t exist. Make sure the user configured in the connection pool has enough provileges. Note this is performed by Flyway migrations.| true|  `GEOSERVER_BACKEND_PGCONFIG_CREATESCHEMA`|
| datasource| org.springframework.boot.autoconfigure.jdbc.DataSourceProperties| | |  `GEOSERVER_BACKEND_PGCONFIG_DATASOURCE`|
| enabled| java.lang.Boolean| Whether to use the pgconfig catalog back-end to host the GeoServer catalog and services configuration. Only one catalog back-end can be enabled, and it&#x27;s usually done by enabling the corresponding Spring profile (e.g. runnning the applications with -Dspring.profiles.active&#x3D;pgconfig,...)| false|  `GEOSERVER_BACKEND_PGCONFIG_ENABLED`|
| initialize| java.lang.Boolean| Whether to initialize the database. I.e. create the tables and views and run database migrations when upgrading to a new version of the database schema. Note this is performed by Flyway migrations.| true|  `GEOSERVER_BACKEND_PGCONFIG_INITIALIZE`|
| schema| java.lang.String| The PostgreSQL databse schema to use to host the catalog and services configuration.| public|  `GEOSERVER_BACKEND_PGCONFIG_SCHEMA`|

## catalog-jdbcconfig
Deprecatted &#x60;jdbcconfig&#x60; Catalog back-end configuration properties
### geoserver.backend.jdbcconfig.datasource.xa
**Class:** `org.springframework.boot.autoconfigure.jdbc.DataSourceProperties$Xa`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| data-source-class-name| java.lang.String| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_XA_DATASOURCECLASSNAME`|
| properties| java.util.Map&lt;java.lang.String,java.lang.String&gt;| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_XA_PROPERTIES`|
### geoserver.backend.jdbcconfig
**Class:** `org.geoserver.cloud.config.catalog.backend.jdbcconfig.JdbcConfigConfigurationProperties`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| cache-directory| java.nio.file.Path| | |  `GEOSERVER_BACKEND_JDBCCONFIG_CACHEDIRECTORY`|
| datasource| org.springframework.boot.autoconfigure.jdbc.DataSourceProperties| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE`|
| enabled| java.lang.Boolean| Whether to use the jdbcconfig catalog back-end to host the GeoServer catalog and services configuration. Only one catalog back-end can be enabled, and it&#x27;s usually done by enabling the corresponding Spring profile (e.g. runnning the applications with -Dspring.profiles.active&#x3D;jdbcconfig,...). Note this catalog back-end is deprecated, use the pgconfig back-end instead.| false|  `GEOSERVER_BACKEND_JDBCCONFIG_ENABLED`|
| initdb| java.lang.Boolean| | false|  `GEOSERVER_BACKEND_JDBCCONFIG_INITDB`|
### geoserver.backend.jdbcconfig
**Class:** `org.geoserver.cloud.config.catalog.backend.jdbcconfig.CloudJdbcConfigProperties`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| datasource-id| java.lang.String| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCEID`|
| debug-mode| java.lang.Boolean| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DEBUGMODE`|
| import| java.lang.Boolean| | |  `GEOSERVER_BACKEND_JDBCCONFIG_IMPORT`|
| init-db| java.lang.Boolean| | |  `GEOSERVER_BACKEND_JDBCCONFIG_INITDB`|
| repopulate| java.lang.Boolean| | |  `GEOSERVER_BACKEND_JDBCCONFIG_REPOPULATE`|
### geoserver.backend.jdbcconfig
**Class:** `org.geoserver.cloud.config.catalog.backend.jdbcconfig.CloudJdbcStoreProperties`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| datasource-id| java.lang.String| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCEID`|
| debug-mode| java.lang.Boolean| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DEBUGMODE`|
| import| java.lang.Boolean| | |  `GEOSERVER_BACKEND_JDBCCONFIG_IMPORT`|
| init-db| java.lang.Boolean| | |  `GEOSERVER_BACKEND_JDBCCONFIG_INITDB`|
### geoserver.backend.jdbcconfig.datasource
**Class:** `org.geoserver.cloud.config.catalog.backend.jdbcconfig.JDBCConfigBackendConfigurer$ExtendedDataSourceProperties`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| connection-timeout| java.lang.Long| | 250|  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_CONNECTIONTIMEOUT`|
| driver-class-name| java.lang.String| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_DRIVERCLASSNAME`|
| embedded-database-connection| org.springframework.boot.jdbc.EmbeddedDatabaseConnection| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_EMBEDDEDDATABASECONNECTION`|
| generate-unique-name| java.lang.Boolean| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_GENERATEUNIQUENAME`|
| idle-timeout| java.lang.Long| | 60000|  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_IDLETIMEOUT`|
| jndi-name| java.lang.String| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_JNDINAME`|
| maximum-pool-size| java.lang.Integer| | 10|  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_MAXIMUMPOOLSIZE`|
| minimum-idle| java.lang.Integer| | 2|  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_MINIMUMIDLE`|
| name| java.lang.String| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_NAME`|
| password| java.lang.String| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_PASSWORD`|
| type| java.lang.Class&lt;? extends javax.sql.DataSource&gt;| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_TYPE`|
| unique-name| java.lang.String| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_UNIQUENAME`|
| url| java.lang.String| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_URL`|
| username| java.lang.String| | |  `GEOSERVER_BACKEND_JDBCCONFIG_DATASOURCE_USERNAME`|

## jndi
simple JNDI, allows to declare JNDI JDBC data sources as externalized
					  configuration properties in a an application container (tomcat, jetty, etc.) independent way
### jndi
**Class:** `org.geoserver.cloud.config.jndidatasource.JNDIDataSourcesConfigurationProperties`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| datasources| java.util.Map&lt;java.lang.String,org.geoserver.cloud.config.jndidatasource.JNDIDatasourceConfig&gt;| | |  `JNDI_DATASOURCES`|

## security
GeServer Cloud custom security settings
### geoserver.security.gateway-shared-auth
**Class:** `org.geoserver.cloud.autoconfigure.authzn.GatewaySharedAuthConfigProperties`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| auto| java.lang.Boolean| Whether to automatically create the gateway-shared-auth authentication filter and append it to the filter chains when enabled| true|  `GEOSERVER_SECURITY_GATEWAYSHAREDAUTH_AUTO`|
| enabled| java.lang.Boolean| Whether the gateway-shared-auth webui authentication conveyor protocol is enabled| true|  `GEOSERVER_SECURITY_GATEWAYSHAREDAUTH_ENABLED`|
| server| java.lang.Boolean| true to act as server (i.e. to be set in the webui service) or client (default)| false|  `GEOSERVER_SECURITY_GATEWAYSHAREDAUTH_SERVER`|

## gwc
GeoWebCache configuration properties
### gwc
**Class:** `org.geoserver.cloud.gwc.config.core.GeoWebCacheConfigurationProperties`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| cache-directory| java.nio.file.Path| Location of the default cache directory. This is the directory where tile images will be stored, unless a separate &quot;Blob Store&quot; is configured for a given Tile Layer.| |  `GWC_CACHEDIRECTORY`|
| config-directory| java.nio.file.Path| Alternate parent directory for the global geowebcache.xml configuration file. If specified, must be an absolute path to a writable directory. An attempt to create it will be made if it doesn&#x27;t exist. Defaults to null, meaning the GeoServer resource store&#x27;s gwc/ directory will be used.| |  `GWC_CONFIGDIRECTORY`|
| enabled| java.lang.Boolean| Enables the core GeoWebCache functionality and integration with GeoServer tile layers. All other config properties depend on this one to be enabled.| true|  `GWC_ENABLED`|
| rest-config| java.lang.Boolean| Enables or disables the GWC REST API to configure layers, blob stores, etc.| false|  `GWC_RESTCONFIG`|
| web-ui| java.lang.Boolean| Enables or disables the GWC user interface| false|  `GWC_WEBUI`|
| wms-integration| java.lang.Boolean| Enables or disables the extension to integrate GWC with GeoServer&#x27;s WMS. This is a component-level configuration property that, if enabled, the option to activate the integration needs to be configured in GeoServer&#x27;s WEB-UI, and if disabled, the Web-UI don&#x27;t even show the option| false|  `GWC_WMSINTEGRATION`|
### gwc.blobstores
**Class:** `org.geoserver.cloud.gwc.config.core.GeoWebCacheConfigurationProperties$BlobstoresConfig`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| azure| java.lang.Boolean| Enables or disables support for Microsoft Azure BLOB Store. This is not a dynamic runtime setting, but an application container level one. Disabled BLOB stores won&#x27;t even be loaded to the runtime context.| false|  `GWC_BLOBSTORES_AZURE`|
| s3| java.lang.Boolean| Enables or disables support for Amazon-S3 BLOB Store. This is not a dynamic runtime setting, but an application container level one. Disabled BLOB stores won&#x27;t even be loaded to the runtime context.| false|  `GWC_BLOBSTORES_S3`|
### gwc.disk-quota
**Class:** `org.geoserver.cloud.gwc.config.core.GeoWebCacheConfigurationProperties$DiskQuotaConfig`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| data-source| org.springframework.boot.autoconfigure.jdbc.DataSourceProperties| | |  `GWC_DISKQUOTA_DATASOURCE`|
| enabeld| java.lang.Boolean| | false|  `GWC_DISKQUOTA_ENABELD`|
### gwc.services
**Class:** `org.geoserver.cloud.gwc.config.core.GeoWebCacheConfigurationProperties$ServicesConfig`

|Key|Type|Description|Default value|Environment variable |
|---|----|-----------|-------------|----------------------|
| gmaps| java.lang.Boolean| Enables or disables the GoogleMaps service. This is not a dynamic runtime setting, but an application container level one. Disabled services won&#x27;t even be loaded to the runtime context.| false|  `GWC_SERVICES_GMAPS`|
| kml| java.lang.Boolean| Enables or disables the KML service. This is not a dynamic runtime setting, but an application container level one. Disabled services won&#x27;t even be loaded to the runtime context.| false|  `GWC_SERVICES_KML`|
| mgmaps| java.lang.Boolean| Enables or disables the MGMaps service. This is not a dynamic runtime setting, but an application container level one. Disabled services won&#x27;t even be loaded to the runtime context.| false|  `GWC_SERVICES_MGMAPS`|
| tms| java.lang.Boolean| Enables or disables the TMS service. This is not a dynamic runtime setting, but an application container level one. Disabled services won&#x27;t even be loaded to the runtime context.| false|  `GWC_SERVICES_TMS`|
| wms| java.lang.Boolean| Enables or disables the WMS service. This is not a dynamic runtime setting, but an application container level one. Disabled services won&#x27;t even be loaded to the runtime context.| false|  `GWC_SERVICES_WMS`|
| wmts| java.lang.Boolean| Enables or disables the WMTS service. This is not a dynamic runtime setting, but an application container level one. Disabled services won&#x27;t even be loaded to the runtime context.| false|  `GWC_SERVICES_WMTS`|

