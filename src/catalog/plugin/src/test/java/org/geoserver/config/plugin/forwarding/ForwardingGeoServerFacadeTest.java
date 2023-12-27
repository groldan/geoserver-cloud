/*
 * (c) 2023 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.config.plugin.forwarding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerFacade;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.LoggingInfo;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.SettingsInfo;
import org.geoserver.config.plugin.ConfigRepository;
import org.geoserver.config.plugin.RepositoryGeoServerFacade;
import org.geoserver.wms.WMSInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ForwardingGeoServerFacadeTest {

    private RepositoryGeoServerFacade subject;
    private ForwardingGeoServerFacade forwarding;

    private SettingsInfo settings;
    private LoggingInfo logging;
    private ServiceInfo service;

    @BeforeEach
    void beforeEach() {
        subject = mock(RepositoryGeoServerFacade.class);
        forwarding = new ForwardingGeoServerFacade(subject);
        settings = mock(SettingsInfo.class);
        logging = mock(LoggingInfo.class);
        service = mock(ServiceInfo.class);
    }

    @Test
    void testGetSubject() {
        assertThat(forwarding.getSubject()).isSameAs(subject);
    }

    @Test
    void testSetRepository() {
        ConfigRepository repo = mock(ConfigRepository.class);
        forwarding.setRepository(repo);
        verify(subject, times(1)).setRepository(repo);
    }

    @Test
    void testSetRepositoryFailsIfSubjectIsNotARepositoryGeoServerFacade() {
        forwarding = new ForwardingGeoServerFacade(mock(GeoServerFacade.class));
        ConfigRepository repo = mock(ConfigRepository.class);
        assertThrows(IllegalStateException.class, () -> forwarding.setRepository(repo));
    }

    @Test
    void testGetGeoServer() {
        GeoServer gs = mock(GeoServer.class);
        when(subject.getGeoServer()).thenReturn(gs);
        assertThat(forwarding.getGeoServer()).isSameAs(gs);
        verify(subject, times(1)).getGeoServer();
    }

    @Test
    void testSetGeoServer() {
        GeoServer gs = mock(GeoServer.class);
        forwarding.setGeoServer(gs);
        verify(subject, times(1)).setGeoServer(gs);
        forwarding.setGeoServer(null);
        verify(subject, times(1)).setGeoServer(null);
    }

    @Test
    void testGetGlobal() {
        GeoServerInfo global = mock(GeoServerInfo.class);
        when(subject.getGlobal()).thenReturn(global);
        assertThat(forwarding.getGlobal()).isSameAs(global);
        verify(subject, times(1)).getGlobal();
    }

    @Test
    void testSetGlobal() {
        GeoServerInfo global = mock(GeoServerInfo.class);
        forwarding.setGlobal(global);
        verify(subject, times(1)).setGlobal(global);
    }

    @Test
    void testSaveGeoServerInfo() {
        GeoServerInfo info = mock(GeoServerInfo.class);
        forwarding.save(info);
        verify(subject, times(1)).save(info);
    }

    @Test
    void testGetSettingsString() {
        String id = "some-id";
        when(subject.getSettings(id)).thenReturn(settings);

        assertThat(forwarding.getSettings(id)).isSameAs(settings);
        verify(subject, times(1)).getSettings(id);
    }

    @Test
    void testGetSettingsStringFailsIfNotARepositoryGeoServerFacade() {
        forwarding = new ForwardingGeoServerFacade(mock(GeoServerFacade.class));
        assertThrows(UnsupportedOperationException.class, () -> forwarding.getSettings("some-id"));
    }

    @Test
    void testGetSettingsWorkspaceInfo() {
        WorkspaceInfo ws = mock(WorkspaceInfo.class);
        when(subject.getSettings(ws)).thenReturn(settings);

        assertThat(forwarding.getSettings(ws)).isSameAs(settings);
        verify(subject, times(1)).getSettings(ws);
    }

    @Test
    void testAddSettingsInfo() {
        forwarding.add(settings);
        verify(subject, times(1)).add(settings);
    }

    @Test
    void testSaveSettingsInfo() {
        forwarding.save(settings);
        verify(subject, times(1)).save(settings);
    }

    @Test
    void testRemoveSettingsInfo() {
        forwarding.remove(settings);
        verify(subject, times(1)).remove(settings);
    }

    @Test
    void testGetLogging() {
        when(subject.getLogging()).thenReturn(logging);
        assertThat(forwarding.getLogging()).isSameAs(logging);
        verify(subject, times(1)).getLogging();
    }

    @Test
    void testSetLogging() {
        forwarding.setLogging(logging);
        verify(subject, times(1)).setLogging(logging);
    }

    @Test
    void testSaveLoggingInfo() {
        forwarding.save(logging);
        verify(subject, times(1)).save(logging);
    }

    @Test
    void testAddServiceInfo() {
        forwarding.add(service);
        verify(subject, times(1)).add(service);
    }

    @Test
    void testRemoveServiceInfo() {
        forwarding.remove(service);
        verify(subject, times(1)).remove(service);
    }

    @Test
    void testSaveServiceInfo() {
        forwarding.save(service);
        verify(subject, times(1)).save(service);
    }

    @Test
    void testGetServices() {
        forwarding.getServices();
        verify(subject, times(1)).getServices();
    }

    @Test
    void testGetServicesWorkspaceInfo() {
        WorkspaceInfo ws = mock(WorkspaceInfo.class);
        forwarding.getServices(ws);
        verify(subject, times(1)).getServices(ws);
    }

    @Test
    void testGetServiceClassOfT() {
        WMSInfo wms = mock(WMSInfo.class);
        when(subject.getService(WMSInfo.class)).thenReturn(wms);
        assertThat(forwarding.getService(WMSInfo.class)).isSameAs(wms);
        verify(subject, times(1)).getService(WMSInfo.class);
    }

    @Test
    void testGetServiceWorkspaceInfoClassOfT() {
        WorkspaceInfo ws = mock(WorkspaceInfo.class);
        WMSInfo wms = mock(WMSInfo.class);
        when(subject.getService(ws, WMSInfo.class)).thenReturn(wms);
        assertThat(forwarding.getService(ws, WMSInfo.class)).isSameAs(wms);
        verify(subject, times(1)).getService(ws, WMSInfo.class);
    }

    @Test
    void testGetServiceStringClassOfT() {
        String id = "some-service-id";
        WMSInfo wms = mock(WMSInfo.class);
        when(subject.getService(id, WMSInfo.class)).thenReturn(wms);
        assertThat(forwarding.getService(id, WMSInfo.class)).isSameAs(wms);
        verify(subject, times(1)).getService(id, WMSInfo.class);
    }

    @Test
    void testGetServiceByNameStringClassOfT() {
        String name = "WMS";
        WMSInfo wms = mock(WMSInfo.class);
        when(subject.getServiceByName(name, WMSInfo.class)).thenReturn(wms);
        assertThat(forwarding.getServiceByName(name, WMSInfo.class)).isSameAs(wms);
        verify(subject, times(1)).getServiceByName(name, WMSInfo.class);
    }

    @Test
    void testGetServiceByNameStringWorkspaceInfoClassOfT() {
        String name = "WMS";
        WorkspaceInfo ws = mock(WorkspaceInfo.class);
        WMSInfo wms = mock(WMSInfo.class);
        when(subject.getServiceByName(name, ws, WMSInfo.class)).thenReturn(wms);
        assertThat(forwarding.getServiceByName(name, ws, WMSInfo.class)).isSameAs(wms);
        verify(subject, times(1)).getServiceByName(name, ws, WMSInfo.class);
    }

    @Test
    void testDispose() {
        forwarding.dispose();
        verify(subject, times(1)).dispose();

        clearInvocations(subject);

        forwarding.dispose();
        verify(subject, times(1)).dispose();
    }
}
