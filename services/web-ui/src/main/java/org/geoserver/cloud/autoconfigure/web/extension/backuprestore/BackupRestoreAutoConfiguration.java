/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.web.extension.backuprestore;

import lombok.Getter;
import org.geoserver.cloud.autoconfigure.web.core.AbstractWebUIAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(name = "org.geoserver.backuprestore.web.BackupRestorePage")
@ConditionalOnProperty( // enabled by default
    prefix = BackupRestoreAutoConfiguration.CONFIG_PREFIX,
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@Import(BackupRestoreConfiguration.class)
public class BackupRestoreAutoConfiguration extends AbstractWebUIAutoConfiguration {

    static final String CONFIG_PREFIX = "geoserver.web-ui.extensions.backuprestore";

    private final @Getter String configPrefix = CONFIG_PREFIX;
}
