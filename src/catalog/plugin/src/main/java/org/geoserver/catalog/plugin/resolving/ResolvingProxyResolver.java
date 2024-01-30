/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.resolving;

import static java.util.Objects.requireNonNull;

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
import org.geoserver.catalog.impl.LayerGroupStyle;
import org.geoserver.catalog.impl.ResolvingProxy;
import org.geoserver.catalog.impl.StyleInfoImpl;
import org.geoserver.catalog.plugin.forwarding.ResolvingCatalogFacadeDecorator;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.SettingsInfo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * {@link ResolvingCatalogFacadeDecorator#setObjectResolver resolving function} that resolves {@link
 * CatalogInfo} properties that are proxied through {@link ResolvingProxy} before returning the
 * object from the facade.
 *
 * <p>When resolving object references from a stream of objects, it's convenient to use the {@link
 * #memoizing() memoizing} supplier, which will keep a local cache during the lifetime of the stream
 * to avoid querying the catalog over repeated occurrences. Note though, this may not be necessary
 * at if the catalog can do very fast id lookups. For example, if it has its own caching mechanism
 * or is a purely in-memory catalog.
 *
 * @see ResolvingProxy
 */
@Slf4j
public class ResolvingProxyResolver<T extends Info> implements UnaryOperator<T> {

    private final Catalog catalog;
    private final BiConsumer<CatalogInfo, ResolvingProxy> onNotFound;
    private final ProxyUtils proxyUtils;

    public ResolvingProxyResolver(Catalog catalog) {
        this(
                catalog,
                (info, proxy) ->
                        log.warn(
                                "ResolvingProxy object not found in catalog, keeping proxy around: %s"
                                        .formatted(info.getId())));
    }

    public ResolvingProxyResolver(
            Catalog catalog, BiConsumer<CatalogInfo, ResolvingProxy> onNotFound) {
        requireNonNull(catalog);
        requireNonNull(onNotFound);
        this.catalog = catalog;
        this.onNotFound = onNotFound;
        this.proxyUtils = new ProxyUtils(catalog, Optional.empty());
    }

    public static <I extends Info> ResolvingProxyResolver<I> of(
            Catalog catalog, BiConsumer<CatalogInfo, ResolvingProxy> onNotFound) {
        return new ResolvingProxyResolver<>(catalog, onNotFound);
    }

    public static <I extends Info> ResolvingProxyResolver<I> of(
            Catalog catalog, boolean errorOnNotFound) {
        if (errorOnNotFound)
            return ResolvingProxyResolver.of(
                    catalog,
                    (proxiedInfo, proxy) -> {
                        throw new NoSuchElementException(
                                "Object not found: %s".formatted(proxiedInfo.getId()));
                    });
        return ResolvingProxyResolver.of(catalog);
    }

    public static <I extends Info> ResolvingProxyResolver<I> of(Catalog catalog) {
        return new ResolvingProxyResolver<>(catalog);
    }

    @SuppressWarnings("unchecked")
    public <I extends Info> ResolvingProxyResolver<I> memoizing() {
        return (ResolvingProxyResolver<I>) new MemoizingProxyResolver(catalog, onNotFound);
    }

    @Override
    public T apply(T info) {
        return resolve(info);
    }

    @SuppressWarnings("unchecked")
    public <I extends Info> I resolve(final I orig) {
        if (orig == null) {
            return null;
        }

        final ResolvingProxy resolvingProxy = getResolvingProxy(orig);
        final boolean isResolvingProxy = null != resolvingProxy;
        if (isResolvingProxy) {
            // may the object itself be a resolving proxy
            I resolved = doResolveProxy(orig);
            if (resolved == null && orig instanceof CatalogInfo cinfo) {
                log.info("Proxy object {} not found, calling on-not-found consumer", orig.getId());
                onNotFound.accept(cinfo, resolvingProxy);
                // return the proxied value if the consumer didn't throw an exception
                return orig;
            }
            return resolved;
        }

        if (orig instanceof StyleInfo style) return (I) resolveInternal(style);

        if (orig instanceof PublishedInfo published) return (I) resolveInternal(published);

        if (orig instanceof ResourceInfo resource) return (I) resolveInternal(resource);

        if (orig instanceof StoreInfo store) return (I) resolveInternal(store);

        if (orig instanceof SettingsInfo settings) return (I) resolveInternal(settings);

        if (orig instanceof ServiceInfo service) return (I) resolveInternal(service);

        return orig;
    }

    protected <I extends Info> I doResolveProxy(final I orig) {
        return proxyUtils.resolve(orig);
    }

    protected boolean isResolvingProxy(final CatalogInfo unresolved) {
        return getResolvingProxy(unresolved) != null;
    }

    protected ResolvingProxy getResolvingProxy(final Info unresolved) {
        if (unresolved != null) {
            boolean isProxy = Proxy.isProxyClass(unresolved.getClass());
            if (isProxy) {
                InvocationHandler invocationHandler = Proxy.getInvocationHandler(unresolved);
                if (invocationHandler instanceof ResolvingProxy resolvingProxy) {
                    return resolvingProxy;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <P extends PublishedInfo> P resolveInternal(P published) {
        if (published instanceof LayerInfo layer) return (P) resolveInternal(layer);

        if (published instanceof LayerGroupInfo lg) return (P) resolveInternal(lg);

        return published;
    }

    protected LayerInfo resolveInternal(LayerInfo layer) {
        if (isResolvingProxy(layer.getResource())) layer.setResource(resolve(layer.getResource()));

        if (isResolvingProxy(layer.getDefaultStyle()))
            layer.setDefaultStyle(resolve(layer.getDefaultStyle()));

        final boolean hasProxiedStyles =
                layer.getStyles().stream().anyMatch(this::isResolvingProxy);
        if (hasProxiedStyles) {

            LinkedHashSet<StyleInfo> resolvedStyles;
            resolvedStyles =
                    layer.getStyles().stream()
                            .map(this::resolve)
                            .collect(Collectors.toCollection(LinkedHashSet::new));

            layer.getStyles().clear();
            layer.getStyles().addAll(resolvedStyles);
        }
        return layer;
    }

    protected LayerGroupInfo resolveInternal(LayerGroupInfo lg) {
        resolveLayerGroupLayers(lg.getLayers());
        resolveLayerGroupStyles(lg.getLayers(), lg.getStyles());
        // now resolves layers and styles defined in layer group styles
        for (LayerGroupStyle groupStyle : lg.getLayerGroupStyles()) {
            resolveLayerGroupLayers(groupStyle.getLayers());
            resolveLayerGroupStyles(groupStyle.getLayers(), groupStyle.getStyles());
        }

        lg.setWorkspace(resolve(lg.getWorkspace()));
        lg.setRootLayer(resolve(lg.getRootLayer()));
        lg.setRootLayerStyle(resolve(lg.getRootLayerStyle()));
        return lg;
    }

    private void resolveLayerGroupLayers(List<PublishedInfo> layers) {
        for (int i = 0; i < layers.size(); i++) {
            PublishedInfo l = layers.get(i);

            if (l != null) {
                PublishedInfo resolved;
                if (l instanceof LayerGroupInfo || l instanceof LayerInfo) {
                    resolved = resolve(l);
                    // special case to handle catalog loading, when nested publishibles might not be
                    // loaded.
                    if (resolved == null) {
                        resolved = l;
                    }
                } else {
                    // Special case for null layer (style group)
                    resolved = ResolvingProxy.resolve(catalog, l);
                }
                layers.set(i, resolved);
            }
        }
    }

    private void resolveLayerGroupStyles(
            List<PublishedInfo> assignedLayers, List<StyleInfo> styles) {
        for (int i = 0; i < styles.size(); i++) {
            StyleInfo style = styles.get(i);
            if (null != style) {
                PublishedInfo assignedLayer = assignedLayers.get(i);
                StyleInfo resolved = resolveLayerGroupStyle(style, assignedLayer);
                styles.set(i, resolved);
            }
        }
    }

    private StyleInfo resolveLayerGroupStyle(StyleInfo style, PublishedInfo assignedLayer) {
        StyleInfo resolved = null;
        if (assignedLayer instanceof LayerGroupInfo) {
            // special case we might have a StyleInfo representing
            // only the name of a LayerGroupStyle thus not present in Catalog.
            // We take the ref and create a new object
            // without searching in catalog.
            String ref = ResolvingProxy.getRef(style);
            if (ref != null) {
                StyleInfo styleInfo = new StyleInfoImpl(catalog);
                styleInfo.setName(ref);
                resolved = styleInfo;
            }
        }
        return (resolved == null) ? resolve(style) : resolved;
    }

    protected StyleInfo resolveInternal(StyleInfo style) {
        // resolve the workspace
        WorkspaceInfo ws = style.getWorkspace();
        if (isResolvingProxy(ws)) {
            style.setWorkspace(resolve(ws));
        }
        return style;
    }

    protected StoreInfo resolveInternal(StoreInfo store) {
        // resolve the workspace
        WorkspaceInfo ws = store.getWorkspace();
        if (isResolvingProxy(ws)) {
            store.setWorkspace(resolve(ws));
        }
        return store;
    }

    protected SettingsInfo resolveInternal(SettingsInfo settings) {
        settings.setWorkspace(resolve(settings.getWorkspace()));
        return settings;
    }

    protected ServiceInfo resolveInternal(ServiceInfo service) {
        service.setWorkspace(resolve(service.getWorkspace()));
        return service;
    }

    protected ResourceInfo resolveInternal(ResourceInfo resource) {
        // resolve the store
        StoreInfo store = resource.getStore();
        if (isResolvingProxy(store)) {
            resource.setStore(resolve(store));
        }

        // resolve the namespace
        NamespaceInfo namespace = resource.getNamespace();
        if (isResolvingProxy(namespace)) {
            resource.setNamespace(resolve(namespace));
        }
        return resource;
    }

    private static class MemoizingProxyResolver extends ResolvingProxyResolver<Info> {

        private Map<String, Info> resolvedById = new ConcurrentHashMap<>();

        public MemoizingProxyResolver(
                Catalog catalog, BiConsumer<CatalogInfo, ResolvingProxy> onNotFound) {
            super(catalog, onNotFound);
        }

        @SuppressWarnings("unchecked")
        protected @Override <I extends Info> I doResolveProxy(final I orig) {
            String id = orig.getId();
            I resolved = (I) this.resolvedById.get(id);
            if (null == resolved) {
                log.trace("Memoized cache miss, resolving proxy reference {}", id);
                resolved = computeIfAbsent(orig);
            } else {
                log.trace("Memoized cache hit for {}", resolved.getId());
            }
            return resolved;
        }

        @SuppressWarnings("unchecked")
        private <I extends Info> I computeIfAbsent(final I orig) {
            return (I)
                    resolvedById.computeIfAbsent(orig.getId(), key -> super.doResolveProxy(orig));
        }
    }
}
