package org.geoserver.cloud.rest.backuprestore;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.plugin.CatalogSupport;
import org.geoserver.catalog.util.CloseableIterator;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.LoggingInfo;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.SettingsInfo;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.platform.resource.Resource;
import org.geoserver.platform.resource.Resource.Type;
import org.geotools.api.filter.Filter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * <p>
 * <ul>
 * <li>{@code GET /rest/catalog}: {@link #backup back up} the catalog and
 * configuration
 * <li>{@code POST /rest/catalog}: {@link #restore restore} a backup into an
 * empty catalog
 * <li>{@code PUT /rest/catalog}: {@link #ingest ingest} (import) a backup,
 * overriding existing objects
 * <li>{@code DELETE /rest/catalog}:{@link #prune prune} the whole Catalog and
 * Configuration</li>
 */
@RestController
@RequestMapping(path = "/rest/catalog")
@RequiredArgsConstructor
@Slf4j
public class JsonBackupRestoreController {

    @NonNull
    private final GeoServer geoServer;

    @NonNull
    private final Catalog rawCatalog;

    /**
     *
     * @return
     */
    @GetMapping(produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Object> backup() {

        Supplier<Stream<? extends Object>> concatStreams = concatStreams();
        Summary summary = new Summary();
        Flux<Object> flux = Flux.fromStream(concatStreams).doOnNext(summary::add);

        return flux.concatWith(Flux.just(summary));
    }

    /**
     * Restores a {@link #backup()} stream. The catalog and configuration objects
     * should be empty, or at least non conflicting with any object to be consumed.
     *
     * @param data
     */
    @PostMapping(consumes = MediaType.APPLICATION_NDJSON_VALUE)
    public void restore(@RequestBody Stream<Object> data) {
        log.info("processing request body");

        CatalogSupport catalogSupport = new CatalogSupport(rawCatalog);

        data.forEach(d -> processIncoming(d, catalogSupport));
        log.info("finished processing request body");
    }

    /**
     * Imports a {@link #backup()} stream, overriding existing objects
     * <p>
     *
     * @apiNote the {@link GeoServerInfo global configuration} will be replaced by
     *          the one from the data stream, if any, but the
     *          {@link GeoServerInfo#getUpdateSequence() update sequence} will be
     *          preserved if it's value is greater than the value from the incoming
     *          {@link GeoServerInfo} object.
     * @param data
     */
    @PutMapping(consumes = MediaType.APPLICATION_NDJSON_VALUE)
    public void ingest(@RequestBody Stream<Object> data) {
        log.info("processing request body");

        CatalogSupport catalogSupport = new CatalogSupport(rawCatalog);

        data.forEach(d -> processIncoming(d, catalogSupport));
        log.info("finished processing request body");
    }

    @DeleteMapping
    public Summary prune() {
        throw new UnsupportedOperationException("unimplemented");
    }

    private void processIncoming(Object d, CatalogSupport catalogSupport) {
        log.info("processing {}: {}", d.getClass().getCanonicalName(), d);
        if (d instanceof Resource res) {

        } else if (d instanceof CatalogInfo info) {
            info = catalogSupport.resolve(info);
            catalogSupport.add(info);
        } else if (d instanceof GeoServerInfo global) {
            geoServer.setGlobal(global);
        } else if (d instanceof LoggingInfo logging) {
            geoServer.setLogging(logging);
        } else if (d instanceof ServiceInfo service) {
            geoServer.add(service);
        } else if (d instanceof SettingsInfo settings) {
            geoServer.add(settings);
        }
    }

    private Supplier<Stream<? extends Object>> concatStreams() {
        Supplier<Stream<Resource>> resources = () -> Stream.empty(); // resources();
        Supplier<Stream<CatalogInfo>> catalog = catalog();
        Supplier<Stream<Object>> geoserver = config();

        return () -> Stream.of(() -> resources.get().map(ResourceDto::valueOf), catalog, geoserver)
                .flatMap(Supplier::get);
    }

    private Supplier<Stream<Object>> config() {
        return () -> {
            GeoServerInfo global = geoServer.getGlobal();
            LoggingInfo logging = geoServer.getLogging();
            Collection<? extends ServiceInfo> globalServices = geoServer.getServices();

            Stream<WorkspaceInfo> workspaces =
                    list(geoServer.getCatalog(), WorkspaceInfo.class).get();
            Stream<?> workspaceConfigs = workspaces.flatMap(this::workspaceConfigs);
            Stream<Object> globalConfig = Stream.concat(Stream.of(global, logging), globalServices.stream());

            return Stream.concat(globalConfig, workspaceConfigs).filter(Objects::nonNull);
        };
    }

    private Supplier<Stream<CatalogInfo>> catalog() {
        Catalog catalog = geoServer.getCatalog();
        Supplier<Stream<NamespaceInfo>> namespaces = list(catalog, NamespaceInfo.class);
        Supplier<Stream<WorkspaceInfo>> workspaces = list(catalog, WorkspaceInfo.class);
        Supplier<Stream<StoreInfo>> stores = list(catalog, StoreInfo.class);
        Supplier<Stream<ResourceInfo>> resources = list(catalog, ResourceInfo.class);
        // TODO: filter out default styles
        Supplier<Stream<StyleInfo>> styles = list(catalog, StyleInfo.class);
        Supplier<Stream<LayerInfo>> layers = list(catalog, LayerInfo.class);
        Supplier<Stream<LayerGroupInfo>> layerGroups = list(catalog, LayerGroupInfo.class);

        Stream<Supplier<? extends Stream<? extends CatalogInfo>>> all;
        all = Stream.of(namespaces, workspaces, stores, resources, styles, layers, layerGroups);
        return () -> all.flatMap(Supplier::get);
    }

    private <T extends CatalogInfo> Supplier<Stream<T>> list(Catalog catalog, Class<T> type) {
        return () -> {
            CloseableIterator<T> it = catalog.list(type, Filter.INCLUDE);
            Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED);
            boolean parallel = true;
            Stream<T> stream = StreamSupport.stream(spliterator, parallel);
            return stream.onClose(() -> {
                log.info("Closing iterator of {}", type.getSimpleName());
                it.close();
            });
        };
    }

    private Supplier<Stream<Resource>> resources() {
        return () -> {
            GeoServerResourceLoader resourceLoader = geoServer.getCatalog().getResourceLoader();
            Resource root = resourceLoader.get("");
            return iterate(root);
        };
    }

    private Stream<Resource> iterate(Resource root) {
        log.info("listing resource '{}'", root.path());
        List<Resource> children = root.list();

        Stream<Resource> files =
                children.stream().filter(r -> r.getType() == Type.RESOURCE).filter(this::filter);

        Stream<Resource> directories =
                children.stream().filter(r -> r.getType() == Type.DIRECTORY).filter(this::filter);

        Stream<Resource> recurseChildren = directories.flatMap(this::iterate);

        return Stream.concat(files, recurseChildren).peek(r -> log.info("appending resource {}", r.path()));
    }

    private Set<String> nameExcludes = Set.of(
            "global.xml",
            "settings.xml",
            "styles",
            "workspace.xml",
            "namespace.xml",
            "coveragestore.xml",
            "datastore.xml",
            "wmsstore.xml",
            "wmtsstore.xml",
            "coverage.xml",
            "featuretype.xml",
            "wmslayer.xml",
            "wmtslayer.xml",
            "layer.xml",
            "data",
            "tmp",
            "temp",
            "updateSequence.properties",
            "filelocks");

    private boolean filter(Resource resource) {

        boolean exclude = nameExcludes.contains(resource.name());
        if (exclude) {
            log.debug("excluded resource {}", resource.path());
        }
        return !exclude;
    }

    private Stream<?> workspaceConfigs(WorkspaceInfo ws) {
        SettingsInfo settings = geoServer.getSettings(ws);
        Collection<? extends ServiceInfo> services = geoServer.getServices(ws);
        return Stream.concat(Stream.of(settings), services.stream()).filter(Objects::nonNull);
    }
}
