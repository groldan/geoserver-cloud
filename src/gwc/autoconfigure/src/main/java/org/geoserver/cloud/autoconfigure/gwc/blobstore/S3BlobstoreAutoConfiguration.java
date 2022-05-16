/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.gwc.blobstore;

import lombok.extern.slf4j.Slf4j;

import org.geoserver.cloud.autoconfigure.gwc.ConditionalOnGeoServerWebUIEnabled;
import org.geoserver.cloud.autoconfigure.gwc.ConditionalOnS3BlobstoreEnabled;
import org.geoserver.cloud.autoconfigure.gwc.blobstore.S3BlobstoreAutoConfiguration.GsWebUIAutoConfiguration;
import org.geoserver.cloud.gwc.config.blobstore.S3BlobstoreConfiguration;
import org.geoserver.cloud.gwc.config.blobstore.S3BlobstoreGsWebUIConfiguration;
import org.geoserver.gwc.web.blob.BlobStorePage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

/**
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnS3BlobstoreEnabled
@Import({S3BlobstoreConfiguration.class, GsWebUIAutoConfiguration.class})
@Slf4j(topic = "org.geoserver.cloud.autoconfigure.gwc.blobstore")
public class S3BlobstoreAutoConfiguration {

    public @PostConstruct void log() {
        log.info("GeoWebCache S3BlobStore integration enabled");
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnGeoServerWebUIEnabled
    @ConditionalOnClass(BlobStorePage.class)
    @Import(S3BlobstoreGsWebUIConfiguration.class)
    static class GsWebUIAutoConfiguration {}
}
