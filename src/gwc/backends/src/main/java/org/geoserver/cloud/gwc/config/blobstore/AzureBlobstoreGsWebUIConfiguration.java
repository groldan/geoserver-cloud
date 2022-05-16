/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.gwc.config.blobstore;

import org.geoserver.gwc.web.blob.AzureBlobStoreType;
import org.geoserver.platform.ModuleStatusImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AzureBlobstoreGsWebUIConfiguration {

    @Bean(name = "AzureBlobStoreType")
    public AzureBlobStoreType azureBlobStoreType() {
        return new AzureBlobStoreType();
    }

    @Bean(name = "GWC-AzureExtension")
    public ModuleStatusImpl gwcAzureExtension() {
        ModuleStatusImpl module = new ModuleStatusImpl();
        module.setModule("gs-gwc-azure");
        module.setName("GeoWebCache Azure Extension");
        module.setComponent("GeoWebCache Azure BlobStore plugin");
        module.setEnabled(true);
        module.setAvailable(true);
        return module;
    }
}
