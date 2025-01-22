package org.geoserver.cloud.rest.backuprestore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Function;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = true)
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(asyncTaskExecutor());
    }

    @Bean
    AsyncTaskExecutor asyncTaskExecutor() {
        return new ConcurrentTaskExecutor(taskExecutor());
    }

    @Bean
    ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setQueueCapacity(25);
        return taskExecutor;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.findModulesViaServiceLoader(true);
        final ObjectMapper objectMapper = builder.build();

        //        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        //        converters.add(0, converter);

        AbstractJackson2HttpMessageConverter ndjsonConverter =
                new AbstractJackson2HttpMessageConverter(objectMapper, MediaType.APPLICATION_NDJSON) {
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

                        MediaType contentType = inputMessage.getHeaders().getContentType();
                        Charset charset = getCharset(contentType);

                        InputStream inputStream = inputMessage.getBody();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));

                        Function<? super String, ? extends Object> jsonMapper = json -> {
                            try {
                                ObjectMapper mapper = super.getObjectMapper();
                                Object value = mapper.readValue(json, org.geoserver.catalog.Info.class);
                                return value;
                            } catch (JsonProcessingException e) {
                                throw new UncheckedIOException(e);
                            }
                        };
                        return reader.lines().map(jsonMapper);
                    }

                    @Override
                    protected void writeInternal(Object t, @Nullable Type type, HttpOutputMessage outputMessage)
                            throws IOException, HttpMessageNotWritableException {

                        super.writeInternal(t, outputMessage);
                    }
                };

        converters.add(0, ndjsonConverter);
    }
}
