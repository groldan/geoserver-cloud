package org.geoserver.cloud.rest.backuprestore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.Info;
import org.geoserver.cloud.backuprestore.BackupHeader;
import org.geoserver.cloud.backuprestore.BackupSummary;
import org.geoserver.platform.resource.Resource;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

class NdJsonJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {

    public NdJsonJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper, MediaType.APPLICATION_NDJSON);
    }

    @Override
    public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        return super.canRead(type, contextClass, mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
        return super.canWrite(clazz, mediaType);
    }

    @Override
    public Object read(Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        ObjectMapper mapper = super.getObjectMapper();
        BufferedReader reader = createReader(inputMessage);

        return reader.lines().map(new Parser(mapper));
    }

    private BufferedReader createReader(HttpInputMessage inputMessage) throws IOException {
        InputStream inputStream = inputMessage.getBody();
        MediaType contentType = inputMessage.getHeaders().getContentType();
        Charset charset = getCharset(contentType);
        return new BufferedReader(new InputStreamReader(inputStream, charset));
    }

    @RequiredArgsConstructor
    private static class Parser implements Function<String, Object> {

        private final @NonNull ObjectMapper mapper;

        private Iterator<Class<?>> expected = List.of(
                        BackupHeader.class, Resource.class, CatalogInfo.class, Info.class, BackupSummary.class)
                .iterator();

        private Class<?> attempted = expected.next();

        @Override
        public Object apply(String json) {
            try {
                return mapper.readValue(json, attempted);
            } catch (InvalidTypeIdException invalidType) {
                if (expected.hasNext()) {
                    attempted = expected.next();
                    return apply(json);
                }
                throw new UncheckedIOException(invalidType);
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    protected void writeInternal(Object t, @Nullable Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {

        super.writeInternal(t, outputMessage);
    }
}
