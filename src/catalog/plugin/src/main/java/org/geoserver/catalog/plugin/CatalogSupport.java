package org.geoserver.catalog.plugin;

import java.util.function.UnaryOperator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.Info;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.MapInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.ModificationProxy;
import org.geoserver.catalog.plugin.resolving.CatalogPropertyResolver;
import org.geoserver.catalog.plugin.resolving.CollectionPropertiesInitializer;
import org.geoserver.catalog.plugin.resolving.ResolvingProxyResolver;

@RequiredArgsConstructor
public class CatalogSupport {

    @NonNull
    private final Catalog catalog;

    public void add(@NonNull CatalogInfo info) {
        switch (info) {
            case WorkspaceInfo ws -> catalog.add(ws);
            case NamespaceInfo ns -> catalog.add(ns);
            case StoreInfo st -> catalog.add(st);
            case ResourceInfo r -> catalog.add(r);
            case LayerInfo l -> catalog.add(l);
            case LayerGroupInfo lg -> catalog.add(lg);
            case StyleInfo s -> catalog.add(s);
            case MapInfo m -> catalog.add(m);
            default -> throw new IllegalArgumentException("Unexpected value: %s"
                    .formatted(ModificationProxy.unwrap(info).getClass()));
        }
    }

    public void remove(@NonNull CatalogInfo info) {
        switch (info) {
            case WorkspaceInfo ws -> catalog.remove(ws);
            case NamespaceInfo ns -> catalog.remove(ns);
            case StoreInfo st -> catalog.remove(st);
            case ResourceInfo r -> catalog.remove(r);
            case LayerInfo l -> catalog.remove(l);
            case LayerGroupInfo lg -> catalog.remove(lg);
            case StyleInfo s -> catalog.remove(s);
            case MapInfo m -> catalog.remove(m);
            default -> throw new IllegalArgumentException("Unexpected value: %s"
                    .formatted(ModificationProxy.unwrap(info).getClass()));
        }
    }

    public <T extends Info> T resolve(T info) {
        UnaryOperator<T> function = resolvingFunction(catalog);
        return function.apply(info);
    }

    public static <T extends Info> UnaryOperator<T> resolvingFunction(Catalog catalog) {
        CatalogPropertyResolver<T> catalogPropResolver = CatalogPropertyResolver.<T>of(catalog);
        ResolvingProxyResolver<T> proxyRefsResolver = ResolvingProxyResolver.<T>failOnNotFound(catalog);
        CollectionPropertiesInitializer<T> collectionsInitializer = CollectionPropertiesInitializer.instance();
        return catalogPropResolver.andThen(proxyRefsResolver).andThen(collectionsInitializer)::apply;
    }
}
