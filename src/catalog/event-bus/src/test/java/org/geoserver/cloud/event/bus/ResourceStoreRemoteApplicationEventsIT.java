/* (c) 2025 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.cloud.event.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.geoserver.platform.resource.SimpleResourceNotificationDispatcher.*;

import java.util.List;
import org.geoserver.cloud.event.resource.ResourceStoreEvent;
import org.geoserver.platform.resource.Resource;
import org.geoserver.platform.resource.ResourceNotification;
import org.geoserver.platform.resource.ResourceNotification.Event;
import org.geoserver.platform.resource.ResourceNotification.Kind;
import org.geoserver.platform.resource.ResourceStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the propagation of {@link ResourceStoreEvent}s through the event bus
 */
class ResourceStoreRemoteApplicationEventsIT extends BusAmqpIntegrationTests {

    ResourceStore resourceStore;

    @Override
    @BeforeEach
    public void before() {
        super.before();
        eventsCaptor.captureEventsOf(ResourceStoreEvent.class).start();
        resourceStore = catalog.getResourceLoader().getResourceStore();
    }

    @Test
    void directoryCreated() {
        Resource data = resourceStore.get("data");
        data.dir();
        ResourceStoreEvent event = created(data);
        testResourceStoreEvent(event);
    }

    @Test
    void subDirectoryCreated() {
        Resource dir = resourceStore.get("data/workspaces/test");
        dir.dir();
        ResourceStoreEvent event = created(dir);
        testResourceStoreEvent(event);
    }

    @Test
    void directoryModified() {
        Resource dir = resourceStore.get("data/workspaces/test");
        dir.dir();
        dir.get("file1").file();
        dir.get("file2").file();

        ResourceStoreEvent event = modified(dir);
        testResourceStoreEvent(event);
    }

    @Test
    void directoryDeleted() {
        Resource dir = resourceStore.get("data/workspaces/test");
        dir.dir();
        dir.get("file1").file();
        dir.get("file2").file();

        ResourceStoreEvent event = deleted(dir);
        testResourceStoreEvent(event);
    }

    @Test
    void directoryRenamed() {
        Resource data = resourceStore.get("data");
        Resource dir = data.get("workspaces/test");
        dir.dir();
        dir.get("file1").file();
        dir.get("file2").file();

        Resource target = resourceStore.get("dirRenamed");
        List<Event> renameEvents = createRenameEvents(data, target);
        ResourceStoreEvent event = localEvent(dir, Kind.ENTRY_MODIFY, renameEvents);
        testResourceStoreEvent(event);
    }

    private void testResourceStoreEvent(ResourceStoreEvent event) {
        localAppContext.publishEvent(event);

        RemoteGeoServerEvent local = eventsCaptor.local().expectOne(ResourceStoreEvent.class);
        assertThat(local.getEvent()).isSameAs(event);

        RemoteGeoServerEvent remote = eventsCaptor.remote().expectOne(ResourceStoreEvent.class);
        ResourceStoreEvent remoteEvent = (ResourceStoreEvent) remote.getEvent();
        assertThat(remoteEvent.getPath()).isEqualTo(event.getPath());
        assertThat(remoteEvent.getEventKind()).isEqualTo(event.getEventKind());
        assertThat(remoteEvent.getChildPathsCreated()).isEqualTo(event.getChildPathsCreated());
        assertThat(remoteEvent.getChildPathsModified()).isEqualTo(event.getChildPathsModified());
        assertThat(remoteEvent.getChildPathsDeleted()).isEqualTo(event.getChildPathsDeleted());
    }

    private ResourceStoreEvent created(Resource resource) {
        return localEvent(resource, Kind.ENTRY_CREATE);
    }

    private ResourceStoreEvent modified(Resource resource) {
        return localEvent(resource, Kind.ENTRY_MODIFY);
    }

    private ResourceStoreEvent deleted(Resource resource) {
        return localEvent(resource, Kind.ENTRY_DELETE);
    }

    private ResourceStoreEvent localEvent(Resource resource, Kind kind) {
        List<Event> events = createEvents(resource, kind);
        return localEvent(resource, kind, events);
    }

    private ResourceStoreEvent localEvent(Resource resource, Kind kind, List<Event> events) {
        ResourceNotification notification = notification(resource, kind, events);
        return ResourceStoreEvent.of(notification);
    }

    private ResourceNotification notification(
            Resource resource, ResourceNotification.Kind kind, List<ResourceNotification.Event> events) {
        String path = resource.path();
        long timestamp = System.currentTimeMillis();
        ResourceNotification notification = new ResourceNotification(path, kind, timestamp, events);
        return notification;
    }
}
