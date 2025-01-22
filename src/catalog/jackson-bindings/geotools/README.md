# GeoTools Jackson bindings


The `gt-jackson-bindings` module provides Jackson-databind serialization and deserialization support for GeoTools objects
commonly used in GeoServer Cloud projects. This allows GeoTools data types to be easily converted to and from JSON.

Specifically, it provides bindings for:

- `org.opengis.filter.Filter`, so `Filter` objects can be serialized/deserialized directly with Jackson
- `org.locationtech.jts.geom.Geometry`, to serialize/deserialize JTS geometries as GeoJSON with Jackson

  
## Maven dependencies:

```shell
[INFO] org.geoserver.cloud.catalog.jackson:gt-jackson-bindings:jar:1.10-SNAPSHOT
[INFO] +- com.fasterxml.jackson.core:jackson-databind:jar:2.17.2:compile
[INFO] |  +- com.fasterxml.jackson.core:jackson-annotations:jar:2.17.2:compile
[INFO] |  \- com.fasterxml.jackson.core:jackson-core:jar:2.17.2:compile
[INFO] +- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:jar:2.17.2:compile
[INFO] +- com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:jar:2.17.2:compile
[INFO] |  \- org.yaml:snakeyaml:jar:2.2:compile
[INFO] +- org.geotools:gt-main:jar:32.1:provided
```

## Usage


### Maven Dependencies Setup

To use this module effectively, you need to be aware of its dependencies, particularly `org.geotools:gt-main`, which is marked as provided.
This means you must explicitly include it in your project if you plan to work with GeoTools data types at runtime.

#### 1. Add gt-jackson-bindings to Your pom.xml

Include the gt-jackson-bindings dependency in your project:

```xml
<dependency>
    <groupId>org.geoserver.cloud.catalog.jackson</groupId>
    <artifactId>gt-jackson-bindings</artifactId>
    <version>1.10.0</version> <!-- Use the appropriate version -->
</dependency>
```

#### 2. Add org.geotools:gt-main Explicitly

Since `gt-main` is declared as a provided dependency in `gt-jackson-bindings`, you must include it explicitly in your project to use GeoTools types at runtime:

```xml
<dependency>
    <groupId>org.geotools</groupId>
    <artifactId>gt-main</artifactId>
    <version>25.0</version> <!-- Use the version compatible with your setup -->
</dependency>
```

### Jackson Modules

The `gt-jackson-bindings` module adheres to Jackson’s SPI (Service Provider Interface) Module System, which allows modules to be automatically discovered and loaded at runtime. This simplifies the integration of custom Jackson modules, such as those provided for GeoTools, into an application. Here’s how it works and how to properly include the necessary modules:

#### What is the Jackson SPI Module System?

The Jackson SPI system enables automatic discovery and registration of modules on the classpath.
Modules implement the Module interface and include a `META-INF/services/com.fasterxml.jackson.databind.Module` file in their JAR, which lists the fully qualified class name of the module.

When `ObjectMapper.findAndRegisterModules()` is called, Jackson scans the classpath, detects all available modules via the SPI mechanism, and registers them automatically.

#### Modules Provided by gt-jackson-bindings

1. **`org.geotools.jackson.databind.filter.GeoToolsFilterModule`**:
    - Provides serialization and deserialization support for GeoTools filtering objects, such as `Filter` and `Expression`.
    - These types are essential for representing complex geospatial queries.
2. **`org.geotools.jackson.databind.geojson.GeoToolsGeoJsonModule`**:
    - Adds support for handling GeoJSON, including features and geometries, through GeoTools.
    - This module enables seamless integration with GeoJSON data formats.

#### Module dependencies

Jackson does not natively support Java 8 date-time types (`java.time.Instant`, `java.time.LocalDate`, etc.) without the `jackson-datatype-jsr310` module. However:

* If you use `ObjectMapper.findAndRegisterModules()`, Jackson will automatically load `jackson-datatype-jsr310` since it should be in on the classpath, for `gt-jackson-bindings` depends on `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` already.
* When not using `ObjectMapper.findAndRegisterModules()`, you must register it manually:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

ObjectMapper objectMapper = new ObjectMapper();
objectMapper.registerModule(new JavaTimeModule());
objectMapper.registerModule(new GeoToolsFilterModule());
objectMapper.registerModule(new GeoToolsGeoJsonModule());
```
  
