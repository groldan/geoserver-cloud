package org.geoserver.cloud.autoconfigure.rest.backuprestore;

import org.geoserver.cloud.backuprestore.BackupRestoreConfiguration;
import org.geoserver.cloud.rest.backuprestore.NdJsonBackupRestoreConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @see NdJsonBackupRestoreConfiguration
 */
@AutoConfiguration
@Import({BackupRestoreConfiguration.class, NdJsonBackupRestoreConfiguration.class})
public class NdJsonBackupRestoreAutoConfiguration {}
