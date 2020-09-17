/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geotools.jackson.databind.geojson.geometry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class GeometrySerializer extends StdSerializer<Geometry> {
    private static final long serialVersionUID = 1L;

    public GeometrySerializer() {
        super(Geometry.class);
    }

    public @Override void serializeWithType(
            Geometry value,
            JsonGenerator gen,
            SerializerProvider serializers,
            TypeSerializer typeSer)
            throws IOException {

        WritableTypeId typeIdDef =
                typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.START_OBJECT));

        serializeContent(value, gen, null);

        typeSer.writeTypeSuffix(gen, typeIdDef);
    }

    public @Override void serialize(
            Geometry value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        serialize(value, gen);
    }

    public void serialize(Geometry geometry, JsonGenerator generator) throws IOException {
        serialize(geometry, generator, (String) null);
    }

    public void serialize(Geometry geometry, JsonGenerator generator, String customNameProperty)
            throws IOException {
        if (geometry == null) {
            generator.writeNull();
            return;
        }
        generator.writeStartObject();
        serializeContent(geometry, generator, customNameProperty);
        generator.writeEndObject();
    }

    private void serializeContent(
            Geometry geometry, JsonGenerator generator, String customNameProperty)
            throws IOException {
        generator.writeStringField("type", geometry.getGeometryType());
        writeDimensions(geometry, generator);
        if (customNameProperty != null) {
            generator.writeStringField("name", customNameProperty);
        }
        if (geometry instanceof GeometryCollection
                && !(geometry instanceof MultiPoint
                        || geometry instanceof MultiLineString
                        || geometry instanceof MultiPolygon)) {

            generator.writeFieldName("geometries");
            generator.writeStartArray();
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                serialize(geometry.getGeometryN(i), generator, customNameProperty);
            }
            generator.writeEndArray();
            return;
        }
        generator.writeFieldName("coordinates");
        writeGeometry(geometry, generator);
    }

    /**
     * Custom extension adding {@code dimensions (int)} and {@code measures (boolean)} properties if
     * the geometry has Z and/or M ordinates.
     */
    private void writeDimensions(Geometry geometry, JsonGenerator generator) throws IOException {
        CoordinateSequence sampleSequence = findSampleSequence(geometry);
        if (sampleSequence != null && sampleSequence.getDimension() > 2) {
            int outputDimension = sampleSequence.getDimension();
            boolean hasM = sampleSequence.hasM();
            generator.writeNumberField("dimensions", outputDimension);
            generator.writeBooleanField("hasM", hasM);
        }
    }

    private CoordinateSequence findSampleSequence(Geometry g) {
        if (g == null || g.isEmpty()) return null;
        if (g instanceof GeometryCollection) {
            return findSampleSequence(g.getGeometryN(0));
        }
        if (g instanceof Point) return ((Point) g).getCoordinateSequence();
        if (g instanceof LineString) return ((LineString) g).getCoordinateSequence();
        if (g instanceof Polygon) return findSampleSequence(((Polygon) g).getExteriorRing());
        return null;
    }

    private void writeGeometry(Geometry geometry, JsonGenerator generator) throws IOException {
        if (geometry instanceof GeometryCollection) {
            writeMultiGeom((GeometryCollection) geometry, generator);
        } else {
            writeSimpleGeom(geometry, generator);
        }
    }

    private void writeMultiGeom(GeometryCollection multi, JsonGenerator generator)
            throws IOException {
        generator.writeStartArray();
        for (int i = 0; i < multi.getNumGeometries(); i++) {
            writeGeometry(multi.getGeometryN(i), generator);
        }
        generator.writeEndArray();
    }

    private void writeSimpleGeom(Geometry geometry, JsonGenerator generator) throws IOException {
        if (geometry.isEmpty()) {
            generator.writeStartArray();
            generator.writeEndArray();
            return;
        }
        if (geometry instanceof Point) {
            Point p = (Point) geometry;
            writeCoordinate(p.getCoordinateSequence(), 0, generator);
        } else if (geometry instanceof Polygon) {
            Polygon poly = (Polygon) geometry;
            generator.writeStartArray();
            writeCoordinateSequence(poly.getExteriorRing(), generator);
            for (int r = 0; r < poly.getNumInteriorRing(); r++) {
                writeCoordinateSequence(poly.getInteriorRingN(r), generator);
            }
            generator.writeEndArray();
        } else {
            writeCoordinateSequence(geometry, generator);
        }
    }

    private void writeCoordinateSequence(Geometry simpleGeom, JsonGenerator generator)
            throws IOException {
        final AtomicReference<CoordinateSequence> seqRef = new AtomicReference<>();
        simpleGeom.apply(
                new CoordinateSequenceFilter() {
                    public @Override void filter(CoordinateSequence seq, int i) {
                        seqRef.set(seq);
                    }

                    public @Override boolean isGeometryChanged() {
                        return false;
                    }

                    public @Override boolean isDone() {
                        return true;
                    }
                });

        CoordinateSequence seq = seqRef.get();
        int size = seq.size();
        generator.writeStartArray();
        for (int i = 0; i < size; i++) {
            writeCoordinate(seq, i, generator);
        }
        generator.writeEndArray();
    }

    private void writeCoordinate(CoordinateSequence seq, int index, JsonGenerator generator)
            throws IOException {
        int dimension = seq.getDimension();
        generator.writeStartArray();
        for (int i = 0; i < dimension; i++) generator.writeNumber(seq.getOrdinate(index, i));
        generator.writeEndArray();
    }
}
