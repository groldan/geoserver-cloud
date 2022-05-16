/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.gwc.config.blobstore;

import static org.geoserver.cloud.gwc.event.GeoWebCacheEvent.Type.CREATED;
import static org.geoserver.cloud.gwc.event.GeoWebCacheEvent.Type.DELETED;
import static org.geoserver.cloud.gwc.event.GeoWebCacheEvent.Type.MODIFIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.geoserver.cloud.gwc.event.BlobStoreEvent;
import org.geoserver.cloud.gwc.event.GeoWebCacheEvent;
import org.geoserver.cloud.gwc.repository.CloudGwcXmlConfiguration;
import org.geowebcache.azure.AzureBlobStoreInfo;
import org.geowebcache.config.BlobStoreConfigurationListener;
import org.geowebcache.config.BlobStoreInfo;
import org.geowebcache.config.ConfigurationResourceProvider;
import org.geowebcache.config.DefaultGridsets;
import org.geowebcache.config.XMLFileResourceProvider;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.storage.DefaultStorageFinder;
import org.geowebcache.util.ApplicationContextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @since 1.0
 */
class AzureBlobStoreXmlConfigurationTest {

    private CloudGwcXmlConfiguration config;

    private AtomicReference<GeoWebCacheEvent> mockEventPublisher;

    private @TempDir Path tmpdir;
    private Path configFile;

    private final DefaultGridsets defaultGridsets = new DefaultGridsets(false, false);

    @BeforeEach
    void setUp() throws Exception {
        mockEventPublisher = new AtomicReference<>();

        configFile = tmpdir.resolve("geowebcache.xml");
        Files.copy(getClass().getResourceAsStream("/geowebcache-empty.xml"), configFile);

        config = createStubConfig();
    }

    private CloudGwcXmlConfiguration createStubConfig() throws Exception {
        ApplicationContextProvider appCtx = mock(ApplicationContextProvider.class);

        ConfigurationResourceProvider inFac =
                new XMLFileResourceProvider(
                        "geowebcache.xml",
                        appCtx,
                        tmpdir.toAbsolutePath().toString(),
                        (DefaultStorageFinder) null);

        CloudGwcXmlConfiguration config =
                new CloudGwcXmlConfiguration(appCtx, inFac, mockEventPublisher::set);
        GridSetBroker broker = new GridSetBroker(List.of(defaultGridsets));
        config.setGridSetBroker(broker);
        config.afterPropertiesSet();
        return config;
    }

    @Test
    void testOnBlobStoreEvent_Created() throws Exception {
        final CloudGwcXmlConfiguration local = this.config;
        final CloudGwcXmlConfiguration remote = createStubConfig();

        BlobStoreConfigurationListener localListener = mock(BlobStoreConfigurationListener.class);
        BlobStoreConfigurationListener remoteListener = mock(BlobStoreConfigurationListener.class);
        local.addBlobStoreListener(localListener);
        remote.addBlobStoreListener(remoteListener);

        AzureBlobStoreInfo bsi = newAzureBlobStoreInfo();

        assertFalse(remote.getBlobStore(bsi.getName()).isPresent());
        assertFalse(local.getBlobStore(bsi.getName()).isPresent());

        remote.addBlobStore(bsi);
        assertTrue(remote.getBlobStore(bsi.getName()).isPresent());
        assertFalse(local.getBlobStore(bsi.getName()).isPresent());
        verify(remoteListener).handleAddBlobStore(eq(bsi));

        final Object unknownSource = new Object();
        BlobStoreEvent event = new BlobStoreEvent(unknownSource);
        event.setBlobStoreId(bsi.getName());
        event.setEventType(CREATED);

        local.onBlobStoreEvent(event);
        BlobStoreInfo actual = local.getBlobStore(bsi.getName()).orElse(null);
        assertEquals(bsi, actual);
        verify(localListener).handleAddBlobStore(eq(bsi));
    }

    @Test
    void testOnBlobStoreEvent_Modified() throws Exception {
        final CloudGwcXmlConfiguration local = this.config;
        final CloudGwcXmlConfiguration remote = createStubConfig();
        BlobStoreConfigurationListener localListener = mock(BlobStoreConfigurationListener.class);
        BlobStoreConfigurationListener remoteListener = mock(BlobStoreConfigurationListener.class);
        local.addBlobStoreListener(localListener);
        remote.addBlobStoreListener(remoteListener);

        AzureBlobStoreInfo bsi = newAzureBlobStoreInfo();

        remote.addBlobStore(bsi);

        local.deinitialize();
        local.afterPropertiesSet();

        assertTrue(remote.getBlobStore(bsi.getName()).isPresent());
        assertTrue(local.getBlobStore(bsi.getName()).isPresent());

        bsi.setDefault(!bsi.isDefault());
        bsi.setEnabled(!bsi.isEnabled());
        bsi.setAccountKey("fake-key-modified");
        bsi.setAccountName("fake-name-modified");
        bsi.setContainer("fake-container-modified");
        bsi.setName("fake-azure-store-modified");
        bsi.setPrefix("/gwc/fake/modified");
        bsi.setServiceURL("http://fake/modified");

        remote.modifyBlobStore(bsi);
        verify(remoteListener).handleModifyBlobStore(eq(bsi));

        final Object unknownSource = new Object();
        BlobStoreEvent event = new BlobStoreEvent(unknownSource);
        event.setBlobStoreId(bsi.getName());
        event.setEventType(MODIFIED);

        local.onBlobStoreEvent(event);
        BlobStoreInfo actual = local.getBlobStore(bsi.getName()).orElse(null);
        assertEquals(bsi, actual);
        verify(localListener).handleModifyBlobStore(eq(bsi));
    }

    protected AzureBlobStoreInfo newAzureBlobStoreInfo() {
        AzureBlobStoreInfo bsi = new AzureBlobStoreInfo();
        bsi.setAccountKey("fake-key");
        bsi.setAccountName("fake-name");
        bsi.setContainer("fake-container");
        bsi.setName("fake-azure-store");
        bsi.setPrefix("/gwc/fake");
        bsi.setServiceURL("http://fake");
        return bsi;
    }

    @Test
    void testOnBlobStoreEvent_Renamed() throws Exception {
        final CloudGwcXmlConfiguration local = this.config;
        final CloudGwcXmlConfiguration remote = createStubConfig();

        AzureBlobStoreInfo bsi = newAzureBlobStoreInfo();
        remote.addBlobStore(bsi);
        local.deinitialize();
        local.afterPropertiesSet();

        assertTrue(remote.getBlobStore(bsi.getName()).isPresent());
        assertTrue(local.getBlobStore(bsi.getName()).isPresent());

        final String oldName = bsi.getName();
        final String newName = "newname";
        AzureBlobStoreInfo expected = (AzureBlobStoreInfo) bsi.clone();
        expected.setName(newName);

        remote.renameBlobStore(oldName, newName);
        assertEquals(expected, remote.getBlobStore(newName).orElse(null));
        assertFalse(local.getBlobStore(newName).isPresent());

        final Object unknownSource = new Object();
        BlobStoreEvent event = new BlobStoreEvent(unknownSource);
        event.setBlobStoreId(newName);
        event.setEventType(MODIFIED);

        local.onBlobStoreEvent(event);
        assertEquals(expected, local.getBlobStore(newName).orElse(null));
    }

    @Test
    void testOnBlobStoreEvent_Deleted() throws Exception {
        final CloudGwcXmlConfiguration local = this.config;
        final CloudGwcXmlConfiguration remote = createStubConfig();

        BlobStoreConfigurationListener localListener = mock(BlobStoreConfigurationListener.class);
        BlobStoreConfigurationListener remoteListener = mock(BlobStoreConfigurationListener.class);
        local.addBlobStoreListener(localListener);
        remote.addBlobStoreListener(remoteListener);

        AzureBlobStoreInfo bsi = newAzureBlobStoreInfo();
        remote.addBlobStore(bsi);
        local.deinitialize();
        local.afterPropertiesSet();

        assertTrue(remote.getBlobStore(bsi.getName()).isPresent());
        assertTrue(local.getBlobStore(bsi.getName()).isPresent());

        remote.removeBlobStore(bsi.getName());
        assertFalse(remote.getBlobStore(bsi.getName()).isPresent());
        assertTrue(local.getBlobStore(bsi.getName()).isPresent());
        verify(remoteListener).handleRemoveBlobStore(eq(bsi));

        final Object unknownSource = new Object();
        BlobStoreEvent event = new BlobStoreEvent(unknownSource);
        event.setBlobStoreId(bsi.getName());
        event.setEventType(DELETED);

        local.onBlobStoreEvent(event);
        assertFalse(local.getBlobStore(bsi.getName()).isPresent());
        verify(localListener).handleRemoveBlobStore(eq(bsi));
    }
}
