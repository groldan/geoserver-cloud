/*
 * (c) 2023 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.backend.pgsql.config;

import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.ServiceInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class RequestCache {

    private static ThreadLocal<Optional<RequestCache>> CACHE =
            ThreadLocal.withInitial(Optional::empty);

    static void init() {
        CACHE.set(Optional.of(new RequestCache()));
    }

    static void clear() {
        CACHE.remove();
    }

    public static Optional<RequestCache> get() {
        return CACHE.get();
    }

    private Optional<GeoServerInfo> global = Optional.empty();

    private Map<Class, ServiceInfo> services;

    public Optional<GeoServerInfo> getGlobal(Supplier<Optional<GeoServerInfo>> loader) {
        if (global.isEmpty()) {
            global = loader.get();
        }
        return global;
    }

    public <T extends ServiceInfo> T getService(
            Class<T> clazz, @SuppressWarnings("rawtypes") Function<Class, T> loader) {
        if (null == services) services = new HashMap<>();
        ServiceInfo service = services.computeIfAbsent(clazz, loader);
        return clazz.cast(service);
    }
}
