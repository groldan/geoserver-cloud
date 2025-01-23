package org.geoserver.cloud.rest.backuprestore;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.List;
import org.geoserver.cloud.backuprestore.BackupRestoreService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = true)
@EnableAsync
public class NdJsonBackupRestoreConfiguration implements WebMvcConfigurer {

    @Bean
    NdJsonBackupRestoreController jsonBackupRestoreController(BackupRestoreService service) {
        return new NdJsonBackupRestoreController(service);
    }

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

    @Bean
    ObjectMapper ndjsonGeoServerObjectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.findModulesViaServiceLoader(true);
        builder.modulesToInstall(new ResourceModule());
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        builder.serializationInclusion(JsonInclude.Include.NON_EMPTY);

        return builder.build();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        final ObjectMapper objectMapper = ndjsonGeoServerObjectMapper();

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        converters.add(0, converter);

        AbstractJackson2HttpMessageConverter ndjsonConverter = new NdJsonJackson2HttpMessageConverter(objectMapper);
        converters.add(1, ndjsonConverter);
    }
}
