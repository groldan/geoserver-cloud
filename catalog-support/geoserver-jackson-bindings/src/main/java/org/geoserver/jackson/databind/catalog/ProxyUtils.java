/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.catalog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.Info;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.PublishedInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.LayerGroupInfoImpl;
import org.geoserver.catalog.impl.LayerInfoImpl;
import org.geoserver.catalog.impl.ModificationProxy;
import org.geoserver.catalog.impl.ResolvingProxy;
import org.geoserver.catalog.impl.ResourceInfoImpl;
import org.geoserver.catalog.impl.StoreInfoImpl;
import org.geoserver.catalog.plugin.Patch;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.LoggingInfo;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.SettingsInfo;

/** */
@Slf4j
@RequiredArgsConstructor
public class ProxyUtils {

    private final @NonNull @Getter Catalog catalog;
    private final @NonNull @Getter GeoServer config;

    private boolean failOnNotFound = false;

    /**
     * @param fail if {@code true}, a proxied {@link Info} reference that's not found in the catalog
     *     will result in an {@link IllegalStateException}
     * @return {@code this}
     */
    public ProxyUtils failOnMissingReference(boolean fail) {
        this.failOnNotFound = fail;
        return this;
    }

    private <T> T unwrap(T obj) {
        return ModificationProxy.unwrap(obj);
    }

    public Patch resolve(Patch patch) {
        Patch resolved = new Patch();
        for (Patch.Property p : patch.getPatches()) {
            resolved.add(p.getName(), resolvePatchPropertyValue(p.getValue()));
        }
        return resolved;
    }

    private Object resolvePatchPropertyValue(Object orig) {
        if (orig instanceof Info) {
            return resolve((Info) orig);
        }
        if (orig instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) orig;
            resolve(list);
            return list;
        }
        if (orig instanceof Set) {
            @SuppressWarnings("unchecked")
            Set<Object> set = (Set<Object>) orig;
            return resolve(set);
        }
        return orig;
    }

    private void resolve(List<Object> mutableList) {
        for (int i = 0; i < mutableList.size(); i++) {
            Object v = mutableList.get(i);
            Object resolved = resolvePatchPropertyValue(v);
            mutableList.set(i, resolved);
        }
    }

    private Set<Object> resolve(Set<Object> set) {
        Set<Object> target = newSet(set.getClass());
        for (Object value : set) {
            target.add(resolvePatchPropertyValue(value));
        }
        return target;
    }

    @SuppressWarnings("unchecked")
    private Set<Object> newSet(Class<? extends Set> class1) {
        try {
            return class1.getConstructor().newInstance();
        } catch (Exception e) {
            return new HashSet<Object>();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Info> T resolve(final T unresolved) {
        if (unresolved == null) {
            return null;
        }
        boolean isResolvingProxy = isResolvingProxy(unresolved);
        T info = unwrap(unresolved);
        if (isResolvingProxy) {
            if (info instanceof CatalogInfo) info = ResolvingProxy.resolve(catalog, info);
            else if (info instanceof GeoServerInfo) info = (T) this.config.getGlobal();
            else if (info instanceof LoggingInfo) info = (T) this.config.getLogging();
            else if (info instanceof ServiceInfo)
                info = (T) this.config.getService(info.getId(), ServiceInfo.class);
        }

        if (info == null) {
            if (failOnNotFound)
                throw new IllegalArgumentException("Reference to " + unresolved.getId());
            return null;
        } else if (!Proxy.isProxyClass(info.getClass())) {
            if (info instanceof StyleInfo) resolveInternal((StyleInfo) info);
            if (info instanceof LayerInfo) resolveInternal((LayerInfo) info);
            if (info instanceof LayerGroupInfo) resolveInternal((LayerGroupInfo) info);
            if (info instanceof ResourceInfo) resolveInternal((ResourceInfo) info);
            if (info instanceof StoreInfo) resolveInternal((StoreInfo) info);
            if (info instanceof SettingsInfo) resolveInternal((SettingsInfo) info);
            if (info instanceof ServiceInfo) resolveInternal((ServiceInfo) info);
            if (info instanceof GeoServerInfo) resolveInternal((GeoServerInfo) info);
            if (info instanceof LoggingInfo) resolveInternal((LoggingInfo) info);
        }
        return info;
    }

    protected <T extends Info> boolean isResolvingProxy(final T unresolved) {
        boolean isProxy = Proxy.isProxyClass(unresolved.getClass());
        boolean isResolvingProxy = false;
        if (isProxy) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(unresolved);
            isResolvingProxy = invocationHandler instanceof ResolvingProxy;
        }
        return isResolvingProxy;
    }

    private void resolveInternal(LoggingInfo info) {}

    private void resolveInternal(GeoServerInfo info) {}

    protected void resolveInternal(SettingsInfo settings) {
        if (settings.getWorkspace() != null) {
            settings.setWorkspace(resolve(settings.getWorkspace()));
        }
    }

    protected void resolveInternal(ServiceInfo service) {
        if (service.getWorkspace() != null) {
            service.setWorkspace(resolve(service.getWorkspace()));
        }
    }

    protected void resolveInternal(LayerInfo layer) {
        layer.setResource(resolve(layer.getResource()));
        layer.setDefaultStyle(resolve(layer.getDefaultStyle()));
        LinkedHashSet<StyleInfo> styles = new LinkedHashSet<StyleInfo>();
        for (StyleInfo s : layer.getStyles()) {
            styles.add(resolve(s));
        }
        ((LayerInfoImpl) layer).setStyles(styles);
    }

    protected <T extends PublishedInfo> T resolveInternal(T published) {
        if (published instanceof LayerInfo) resolve((LayerInfo) published);
        else if (published instanceof LayerGroupInfo) resolve((LayerGroupInfo) published);
        return published;
    }

    protected void resolveInternal(LayerGroupInfo layerGroup) {
        LayerGroupInfoImpl lg = (LayerGroupInfoImpl) layerGroup;

        for (int i = 0; i < lg.getLayers().size(); i++) {
            PublishedInfo l = lg.getLayers().get(i);
            if (l != null) {
                lg.getLayers().set(i, resolve(l));
            }
        }

        for (int i = 0; i < lg.getStyles().size(); i++) {
            StyleInfo s = lg.getStyles().get(i);
            if (s != null) {
                lg.getStyles().set(i, resolve(s));
            }
        }
        lg.setWorkspace(resolve(lg.getWorkspace()));
    }

    protected StyleInfo resolveInternal(StyleInfo style) {
        // resolve the workspace
        WorkspaceInfo ws = style.getWorkspace();
        if (ws != null) {
            style.setWorkspace(resolve(ws));
            if (style.getWorkspace() == null) {
                log.info(
                        "Failed to resolve workspace for style \"{}\". This means the workspace has not yet been added to the catalog, keep the proxy around",
                        style.getName());
            }
        }
        return style;
    }

    protected StoreInfo resolveInternal(StoreInfo store) {
        StoreInfoImpl s = (StoreInfoImpl) store;

        // resolve the workspace
        WorkspaceInfo ws = store.getWorkspace();
        if (ws != null) {
            s.setWorkspace(resolve(ws));
            if (store.getWorkspace() == null) {
                log.info(
                        "Failed to resolve workspace for store \"{}\". This means the workspace has not yet been added to the catalog, keep the proxy around",
                        store.getName());
            }
        }
        return s;
    }

    protected ResourceInfo resolveInternal(ResourceInfo resource) {
        ResourceInfoImpl r = (ResourceInfoImpl) resource;

        // resolve the store
        StoreInfo store = resource.getStore();
        if (store != null) {
            r.setStore(resolve(store));
        }

        // resolve the namespace
        NamespaceInfo namespace = resource.getNamespace();
        if (namespace != null) {
            r.setNamespace(resolve(namespace));
        }
        return r;
    }
}
