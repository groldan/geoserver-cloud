package org.geoserver.cloud.backend.pgconfig.resource;

import org.geoserver.cloud.event.resource.ResourceStoreEvent;
import org.springframework.context.event.EventListener;

public class PgconfigResourceStoreEventSynchronizer {

    @EventListener(ResourceStoreEvent.class)
    public void onRemoteResourceEvent(ResourceStoreEvent event) {
        if (event.isLocal()) {
            return;
        }
    }
}
