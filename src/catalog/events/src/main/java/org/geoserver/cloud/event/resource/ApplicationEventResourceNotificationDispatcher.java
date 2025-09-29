/* (c) 2025 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.cloud.event.resource;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.geoserver.platform.resource.ResourceListener;
import org.geoserver.platform.resource.ResourceNotification;
import org.geoserver.platform.resource.SimpleResourceNotificationDispatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

/**
 * {@link SimpleResourceNotificationDispatcher} that also publishes {@link ResourceNotification}s as
 * {@link ApplicationContext} events.
 * <p>
 * This allows to consume {@link ResourceNotification} events in {@link EventListener @EventListener} methods,
 * since adding a {@link ResourceListener} that captures all {@code ResourceNotification}s is not possible.
 * <p>
 * A listener event that captures all notifications can be used, for example, to transmit all notifications through
 * the event bus.
 */
@RequiredArgsConstructor
public class ApplicationEventResourceNotificationDispatcher extends SimpleResourceNotificationDispatcher {

    private final @NonNull ApplicationEventPublisher eventPublisher;

    @Override
    public void changed(ResourceNotification notification) {
        super.changed(notification);
        eventPublisher.publishEvent(ResourceStoreEvent.of(notification));
    }
}
