/* (c) 2025 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.cloud.event.resource;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.geoserver.cloud.event.GeoServerEvent;
import org.geoserver.platform.resource.ResourceNotification;
import org.springframework.core.style.ToStringCreator;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonTypeName("ResourceStoreEvent")
@SuppressWarnings("serial")
public class ResourceStoreEvent extends GeoServerEvent {

    @Getter
    private String path;

    @Getter
    private ResourceNotification.Kind eventKind;

    @Getter
    private List<String> childPathsCreated = List.of();

    @Getter
    private List<String> childPathsModified = List.of();

    @Getter
    private List<String> childPathsDeleted = List.of();

    public static ResourceStoreEvent of(ResourceNotification notification) {
        ResourceStoreEvent event = new ResourceStoreEvent();
        event.path = notification.getPath();
        event.eventKind = notification.getKind();
        event.timestamp = notification.getTimestamp();
        notification.events().forEach(e -> {
            if (e.getPath().equals(event.path)) {
                // ignore same object event
                return;
            }
            // add child events
            String childPath = e.getPath();
            switch (e.getKind()) {
                case ENTRY_CREATE:
                    mutableCreated(event).add(childPath);
                    break;
                case ENTRY_DELETE:
                    mutableDeleted(event).add(childPath);
                    break;
                case ENTRY_MODIFY:
                    mutableModified(event).add(childPath);
                    break;
                default:
                    throw new IllegalStateException("unknown enum value " + e.getKind());
            }
        });
        return event;
    }

    private static List<String> mutableCreated(ResourceStoreEvent event) {
        if (event.childPathsCreated.isEmpty()) event.childPathsCreated = new ArrayList<>();
        return event.childPathsCreated;
    }

    private static List<String> mutableDeleted(ResourceStoreEvent event) {
        if (event.childPathsDeleted.isEmpty()) event.childPathsDeleted = new ArrayList<>();
        return event.childPathsDeleted;
    }

    private static List<String> mutableModified(ResourceStoreEvent event) {
        if (event.childPathsModified.isEmpty()) event.childPathsModified = new ArrayList<>();
        return event.childPathsModified;
    }

    @Override
    public String toShortString() {
        return "%s '%s', child paths: created %,d, modified %,d, deleted %,d"
                .formatted(
                        eventKind, path, childPathsCreated.size(), childPathsModified.size(), childPathsDeleted.size());
    }

    @Override
    protected ToStringCreator toStringBuilder() {
        return super.toStringBuilder().append("kind", eventKind).append("path", path);
    }
}
