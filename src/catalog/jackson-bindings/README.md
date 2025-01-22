# GeoServer Cloud Jackson bindings

These modules provide Jackson-databind serialization and deserialization support for GeoTools and GeoServer objects.

## Modules:

* `gt-jackson-bindings`: provides bindings for `org.opengis.filter.Filter` and `org.locationtech.jts.geom.Geometry`
* `gs-jackson-bindings`: provides bindings for GeoServer Catalog and configuration objects
* `gs-jackson-bindings-cloud`: provides bindings for GeoServer Cloud-specific extensions to Catalog and Configuration objects
* `gs-cloud-starter-jackson`: Spring Boot starter project to auto-configure the default `ObjectMapper` with support for JSON encoding of GeoTools and GeoServer objects
