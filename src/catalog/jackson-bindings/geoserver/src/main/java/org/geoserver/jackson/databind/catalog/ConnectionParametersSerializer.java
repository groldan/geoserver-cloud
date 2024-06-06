package org.geoserver.jackson.databind.catalog;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.geoserver.jackson.databind.catalog.mapper.ConnectionParameters;

import java.io.IOException;

@SuppressWarnings("serial")
public class ConnectionParametersSerializer extends StdSerializer<ConnectionParameters> {

    @SuppressWarnings("unchecked")
    public ConnectionParametersSerializer() {
        super(ConnectionParameters.class);
    }

    @Override
    public void serialize(
            ConnectionParameters params, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        //
        //    	MapSerializer delegate = MapSerializer.construct(null, null,
        // isUnwrappingSerializer(), null, null, null, provider)

        gen.writeStartObject();
        //
        //        for (Entry<String, Object> entry : params.entrySet()) {
        //            String key = entry.getKey();
        //            Object v = entry.getValue();
        //            if (v == null) {
        //                gen.writeNullField(key);
        //            } else if (v instanceof CharSequence c) {
        //                gen.writeStringField(key, c.toString());
        //            } else if (v instanceof Number n) {
        //
        //                gen.writeNumberField(key, n);
        //            }
        //            gen.writeStringField(entry.getKey(), sanitize(entry.getKey(), v));
        //        }

        gen.writeEndObject();
    }

    //    @Nullable
    //    private String sanitize(String key, @Nullable Object value) {
    //        if (value == null) {
    //            return null;
    //        }
    //
    //        boolean matchesAnyPattern =
    //                Arrays.stream(this.keysToSanitize)
    //                        .anyMatch((pattern) -> pattern.matcher(key).matches());
    //        return matchesAnyPattern ? "******" : value;
    //    }
}
