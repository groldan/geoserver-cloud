/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.cloud.config.catalog.backend.pgconfig;

import java.util.function.Predicate;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.GeoServerConfigurationLock;
import org.geoserver.catalog.plugin.ExtendedCatalogFacade;
import org.geoserver.catalog.plugin.locking.LockProviderGeoServerConfigurationLock;
import org.geoserver.cloud.backend.pgconfig.catalog.PgconfigCatalogFacade;
import org.geoserver.cloud.backend.pgconfig.config.PgconfigConfigRepository;
import org.geoserver.cloud.backend.pgconfig.config.PgconfigConfigRepositoryImpl;
import org.geoserver.cloud.backend.pgconfig.config.PgconfigGeoServerFacade;
import org.geoserver.cloud.backend.pgconfig.config.PgconfigUpdateSequence;
import org.geoserver.cloud.backend.pgconfig.resource.FileSystemResourceStoreCache;
import org.geoserver.cloud.backend.pgconfig.resource.PgconfigLockProvider;
import org.geoserver.cloud.backend.pgconfig.resource.PgconfigResourceStore;
import org.geoserver.cloud.backend.pgconfig.resource.PgconfigResourceStoreImpl;
import org.geoserver.cloud.config.catalog.backend.core.GeoServerBackendConfigurer;
import org.geoserver.cloud.config.catalog.backend.pgconfig.DatabaseMigrationConfiguration.Migrations;
import org.geoserver.cloud.event.resource.ApplicationEventResourceNotificationDispatcher;
import org.geoserver.config.GeoServerLoader;
import org.geoserver.security.GeoServerSecurityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

/**
 * @since 1.4
 */
@Configuration(proxyBeanMethods = false)
@Slf4j(topic = "org.geoserver.cloud.config.catalog.backend.pgconfig")
public class PgconfigBackendConfiguration extends GeoServerBackendConfigurer {

    private String instanceId;
    private DataSource dataSource;

    /**
     * @param instanceId used as client-id for the {@link #pgconfigLockRepository() LockRepository}
     * @param dataSource DataSource for {@link #template()}, {@link #pgconfigLockRepository()}, and
     *     {@link #updateSequence()}
     * @param catalogProperties properties for {@link #rawCatalog()}
     * @param migrations required to make sure the migrations ran before this configuration takes
     *     place
     */
    PgconfigBackendConfiguration(
            @Value("${info.instance-id:}") String instanceId,
            @Qualifier("pgconfigDataSource") DataSource dataSource,
            Migrations migrations) {
        this.instanceId = instanceId;
        this.dataSource = dataSource;
        log.info(
                "Loading geoserver config backend with {}. {}",
                PgconfigBackendConfiguration.class.getSimpleName(),
                migrations);
    }

    @Bean(name = "pcconfigJdbcTemplate")
    JdbcTemplate template() {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    protected ExtendedCatalogFacade catalogFacade(@Qualifier("pcconfigJdbcTemplate") JdbcTemplate template) {
        return new PgconfigCatalogFacade(template);
    }

    @Bean
    protected GeoServerConfigurationLock configurationLock(PgconfigLockProvider lockProvider) {
        return new LockProviderGeoServerConfigurationLock(lockProvider);
    }

    @Bean
    protected PgconfigUpdateSequence updateSequence(PgconfigGeoServerFacade geoserverFacade) {
        return new PgconfigUpdateSequence(dataSource, geoserverFacade);
    }

    @Bean
    protected GeoServerLoader geoServerLoaderImpl(
            GeoServerSecurityManager securityManager,
            PgconfigGeoServerResourceLoader resourceLoader,
            GeoServerConfigurationLock configurationLock) {
        log.debug("Creating GeoServerLoader {}", PgconfigGeoServerLoader.class.getSimpleName());
        return new PgconfigGeoServerLoader(resourceLoader, configurationLock);
    }

    @Bean
    PgconfigConfigRepository configRepository(@Qualifier("pcconfigJdbcTemplate") JdbcTemplate template) {
        return new PgconfigConfigRepositoryImpl(template);
    }

    @Bean
    protected PgconfigGeoServerFacade geoserverFacade(PgconfigConfigRepository configRepository) {
        return new PgconfigGeoServerFacade(configRepository);
    }

    @Bean
    protected PgconfigResourceStore resourceStoreImpl(
            @Qualifier("pcconfigJdbcTemplate") JdbcTemplate template,
            PgconfigLockProvider lockProvider,
            FileSystemResourceStoreCache resourceStoreCache,
            ApplicationEventResourceNotificationDispatcher eventPublisher) {

        log.debug("Creating ResourceStore {}", PgconfigResourceStore.class.getSimpleName());
        Predicate<String> localOnlyFilter = PgconfigResourceStoreImpl.defaultIgnoredResources();
        PgconfigResourceStoreImpl store =
                new PgconfigResourceStoreImpl(resourceStoreCache, template, lockProvider, localOnlyFilter);
        store.setResourceNotificationDispatcher(eventPublisher);
        return store;
    }

    @Bean
    ApplicationEventResourceNotificationDispatcher applicationEventResourceNotificationDispatcher(
            ApplicationEventPublisher eventPublisher) {
        return new ApplicationEventResourceNotificationDispatcher(eventPublisher);
    }

    @Bean
    FileSystemResourceStoreCache pgconfigFileSystemResourceStoreCache() {
        return FileSystemResourceStoreCache.newTempDirInstance();
    }

    @Bean
    protected PgconfigGeoServerResourceLoader resourceLoader(
            @Qualifier("resourceStoreImpl") PgconfigResourceStore resourceStore) {
        log.debug("Creating GeoServerResourceLoader {}", PgconfigGeoServerResourceLoader.class.getSimpleName());
        return new PgconfigGeoServerResourceLoader(resourceStore);
    }

    @Bean
    PgconfigLockProvider pgconfigLockProvider(
            @Qualifier("pgconfigLockRepository") LockRepository pgconfigLockRepository) {
        log.debug("Creating {}", PgconfigLockProvider.class.getSimpleName());
        JdbcLockRegistry lockRegistry = new JdbcLockRegistry(pgconfigLockRepository);
        return new PgconfigLockProvider(lockRegistry);
    }

    @Bean
    LockRepository pgconfigLockRepository() {
        log.debug("Creating {} for instance {}", LockRepository.class.getSimpleName(), this.instanceId);
        String id = this.instanceId;
        DefaultLockRepository lockRepository;
        if (StringUtils.hasLength(id)) {
            lockRepository = new DefaultLockRepository(dataSource, id);
        } else {
            lockRepository = new DefaultLockRepository(dataSource);
        }
        // override default table prefix "INT" by "RESOURCE_" (matching table definition
        // RESOURCE_LOCK in init.XXX.sql
        lockRepository.setPrefix("RESOURCE_");
        // time in ms to expire dead locks (10k is the default)
        lockRepository.setTimeToLive(300_000);
        return lockRepository;
    }
}
