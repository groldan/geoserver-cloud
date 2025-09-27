/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.cloud.backend.pgconfig.config;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

import java.util.Optional;
import java.util.stream.Stream;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.plugin.Patch;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.LoggingInfo;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.SettingsInfo;
import org.geoserver.config.plugin.ConfigRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @since 2.8
 */
@Transactional(transactionManager = "pgconfigTransactionManager", propagation = SUPPORTS)
public interface PgconfigConfigRepository extends ConfigRepository {

    @Override
    Optional<GeoServerInfo> getGlobal();

    @Override
    @Transactional(transactionManager = "pgconfigTransactionManager", propagation = REQUIRED)
    void setGlobal(GeoServerInfo global);

    @Override
    Optional<SettingsInfo> getSettingsByWorkspace(WorkspaceInfo workspace);

    @Override
    Optional<SettingsInfo> getSettingsById(String id);

    @Override
    @Transactional(transactionManager = "pgconfigTransactionManager", propagation = REQUIRED)
    void add(SettingsInfo settings);

    @Override
    @Transactional(transactionManager = "pgconfigTransactionManager", propagation = REQUIRED)
    SettingsInfo update(SettingsInfo settings, Patch patch);

    @Override
    @Transactional(transactionManager = "pgconfigTransactionManager", propagation = REQUIRED)
    void remove(SettingsInfo settings);

    @Override
    Optional<LoggingInfo> getLogging();

    @Override
    @Transactional(transactionManager = "pgconfigTransactionManager", propagation = REQUIRED)
    void setLogging(LoggingInfo logging);

    @Override
    @Transactional(transactionManager = "pgconfigTransactionManager", propagation = REQUIRED)
    void add(ServiceInfo service);

    @Override
    @Transactional(transactionManager = "pgconfigTransactionManager", propagation = REQUIRED)
    void remove(ServiceInfo service);

    @Override
    @Transactional(transactionManager = "pgconfigTransactionManager", propagation = REQUIRED)
    <S extends ServiceInfo> S update(S service, Patch patch);

    @Override
    Stream<ServiceInfo> getGlobalServices();

    @Override
    Stream<ServiceInfo> getServicesByWorkspace(WorkspaceInfo workspace);

    @Override
    <T extends ServiceInfo> Optional<T> getGlobalService(Class<T> clazz);

    @Override
    <T extends ServiceInfo> Optional<T> getServiceByWorkspace(WorkspaceInfo workspace, Class<T> clazz);

    @Override
    <T extends ServiceInfo> Optional<T> getServiceById(String id, Class<T> clazz);

    @Override
    <T extends ServiceInfo> Optional<T> getServiceByName(String name, Class<T> clazz);

    @Override
    <T extends ServiceInfo> Optional<T> getServiceByNameAndWorkspace(
            String name, WorkspaceInfo workspace, Class<T> clazz);
}
