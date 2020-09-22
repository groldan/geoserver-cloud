/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.catalog.api.v1;

import static org.geoserver.catalog.impl.ClassMappings.RESOURCE;
import static org.geoserver.catalog.impl.ClassMappings.STORE;
import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON_VALUE;

import lombok.NonNull;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.ClassMappings;
import org.geoserver.catalog.plugin.Patch;
import org.geoserver.cloud.catalog.service.ProxyResolver;
import org.geoserver.cloud.catalog.service.ReactiveCatalog;
import org.opengis.filter.Filter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** */
@RestController
@RequestMapping(path = ReactiveCatalogController.BASE_URI)
public class ReactiveCatalogController {

    public static final String BASE_URI = "/api/v1/catalog";

    private ReactiveCatalog catalog;

    private ProxyResolver proxyResolver;

    public ReactiveCatalogController(ReactiveCatalog catalog, ProxyResolver proxyResolver) {
        this.catalog = catalog;
        this.proxyResolver = proxyResolver;
    }

    @PostMapping(path = "/{endpoint}")
    @ResponseStatus(HttpStatus.CREATED)
    public <C extends CatalogInfo> Mono<C> create(
            @PathVariable("endpoint") String endpoint, @RequestBody C info) {

        return catalog.create(Mono.just(info).flatMap(proxyResolver::resolve));
    }

    @PatchMapping(path = "/{endpoint}/{id}")
    public Mono<CatalogInfo> update(
            @PathVariable("endpoint") String endpoint,
            @PathVariable("id") String id,
            @RequestBody Patch patch) {

        Mono<Patch> resolvedPatch = Mono.just(patch).flatMap(proxyResolver::resolve);

        ClassMappings type = endpointToType(endpoint);

        Mono<CatalogInfo> object =
                catalog.getById(id, type.getInterface())
                        .switchIfEmpty(
                                noContent(
                                        "%s with id '%s' does not exist",
                                        type.getInterface().getSimpleName(), id));

        return object.flatMap(c -> catalog.update(c, resolvedPatch));
    }

    @DeleteMapping(path = "/{endpoint}")
    public Mono<CatalogInfo> delete(
            @PathVariable("endpoint") String endpoint, @NonNull CatalogInfo value) {

        ClassMappings type = endpointToType(endpoint);
        return catalog.delete(value)
                .switchIfEmpty(
                        noContent(
                                "%s with id '%s' does not exist",
                                type.getInterface().getSimpleName(), value.getId()));
    }

    @DeleteMapping(path = "/{endpoint}/{id}")
    public Mono<CatalogInfo> deleteById(
            @PathVariable("endpoint") String endpoint, @PathVariable("id") String id) {
        ClassMappings type = endpointToType(endpoint, null);
        return catalog.getById(id, type.getInterface())
                .switchIfEmpty(
                        noContent(
                                "%s with id '%s' does not exist",
                                type.getInterface().getSimpleName(), id))
                .flatMap(i -> catalog.delete(i));
    }

    @GetMapping(path = "/{endpoint}", produces = APPLICATION_STREAM_JSON_VALUE)
    public Flux<CatalogInfo> findAll(
            @PathVariable("endpoint") String endpoint,
            @RequestParam(name = "type", required = false) ClassMappings subType) {

        ClassMappings type = endpointToType(endpoint, subType);
        return catalog.getAll(type.getInterface());
    }

    @GetMapping(path = {"/{endpoint}/{id}"})
    public Mono<CatalogInfo> findById( //
            @PathVariable("endpoint") String endpoint,
            @PathVariable("id") String id,
            @RequestParam(name = "type", required = false) ClassMappings subType) {

        final @NonNull ClassMappings type = endpointToType(endpoint, subType);
        return catalog.getById(id, type.getInterface())
                .switchIfEmpty(
                        noContent(
                                "%s with id '%s' does not exist",
                                type.getInterface().getSimpleName(), id));
    }

    @GetMapping(path = "/{endpoint}/name/{name}/first")
    public Mono<CatalogInfo> findFirstByName( //
            @PathVariable("endpoint") String endpoint,
            @PathVariable(name = "name") String name,
            @RequestParam(name = "type", required = false) ClassMappings subType) {

        ClassMappings type = endpointToType(endpoint, subType);
        return catalog.getFirstByName(name, type.getInterface())
                .switchIfEmpty(
                        noContent(
                                "%s with name '%s' does not exist",
                                type.getInterface().getSimpleName(), name));
    }

    @PostMapping(path = "/{endpoint}/query", produces = APPLICATION_STREAM_JSON_VALUE)
    public Flux<CatalogInfo> query( //
            @PathVariable("endpoint") String endpoint,
            @RequestParam(name = "type", required = false) ClassMappings subType,
            @RequestBody Filter filter) {

        ClassMappings type = endpointToType(endpoint, subType);
        return catalog.query(type.getInterface(), filter);
    }

    @PutMapping(path = "/workspaces/default/{workspaceId}")
    public Mono<WorkspaceInfo> setDefaultWorkspace(
            @PathVariable("workspaceId") String workspaceId) {

        return catalog.getById(workspaceId, WorkspaceInfo.class)
                .switchIfEmpty(noContent("WorkspaceInfo with id '%s' does not exist", workspaceId))
                .flatMap(catalog::setDefaultWorkspace);
    }

    @GetMapping(path = "/workspaces/default")
    public Mono<WorkspaceInfo> getDefaultWorkspace() {
        return catalog.getDefaultWorkspace().switchIfEmpty(noContent("No default workspace"));
    }

    @PutMapping(path = "namespaces/default/{namespaceId}")
    public Mono<NamespaceInfo> setDefaultNamespace(
            @PathVariable("namespaceId") String namespaceId) {

        return catalog.getById(namespaceId, NamespaceInfo.class)
                .switchIfEmpty(noContent("Namespace %s does not exist", namespaceId))
                .flatMap(catalog::setDefaultNamespace);
    }

    @GetMapping(path = "namespaces/default")
    public Mono<NamespaceInfo> getDefaultNamespace() {
        return catalog.getDefaultNamespace().switchIfEmpty(noContent("No default namespace"));
    }

    @GetMapping(path = "namespaces/uri")
    public Mono<NamespaceInfo> findOneNamespaceByURI(@RequestParam("uri") String uri) {
        return catalog.getOneNamespaceByURI(uri)
                .switchIfEmpty(noContent("No NamespaceInfo found for uri %s", uri));
    }

    @GetMapping(path = "namespaces/uri/all", produces = APPLICATION_STREAM_JSON_VALUE)
    public Flux<NamespaceInfo> findAllNamespacesByURI(@RequestParam("uri") String uri) {
        return catalog.getAllNamespacesByURI(uri);
    }

    @GetMapping(path = "/stores/defaults", produces = APPLICATION_STREAM_JSON_VALUE)
    public Flux<DataStoreInfo> getDefaultDataStores() {
        return catalog.getDefaultDataStores();
    }

    @PutMapping(path = "/workspaces/{workspaceId}/stores/defaults/{dataStoreId}")
    public Mono<DataStoreInfo> setDefaultDataStoreByWorkspaceId( //
            @PathVariable("workspaceId") String workspaceId,
            @RequestParam(name = "dataStoreId") String dataStoreId) {

        // return catalog.setDefaultDataStore(workspace, dataStore);
        throw new UnsupportedOperationException();
    }

    @GetMapping(path = "/workspaces/{workspaceId}/stores/defaults")
    public Mono<DataStoreInfo> findDefaultDataStoreByWorkspaceId( //
            @PathVariable("workspaceId") String workspaceId) {

        return catalog.getById(workspaceId, WorkspaceInfo.class)
                .flatMap(catalog::getDefaultDataStore)
                .switchIfEmpty(noContent("Workspace not found: %s", workspaceId));
    }

    @GetMapping(path = "/workspaces/{workspaceId}/stores", produces = APPLICATION_STREAM_JSON_VALUE)
    public Flux<StoreInfo> findStoresByWorkspaceId( //
            @PathVariable("workspaceId") String workspaceId,
            @RequestParam(name = "type", required = false) ClassMappings subType) {

        final Class<? extends StoreInfo> type = (subType == null ? STORE : subType).getInterface();

        return catalog.getById(workspaceId, WorkspaceInfo.class)
                .flatMapMany(w -> catalog.getStoresByWorkspace(w, type));
    }

    @GetMapping(path = "/workspaces/{workspaceId}/stores/name/{name}")
    public Mono<StoreInfo> findStoreByWorkspaceIdAndName( //
            @PathVariable("workspaceId") String workspaceId,
            @PathVariable("name") String name,
            @RequestParam(name = "type", required = false) ClassMappings subType) {

        final Class<? extends StoreInfo> type = (subType == null ? STORE : subType).getInterface();
        return catalog.getById(workspaceId, WorkspaceInfo.class)
                .switchIfEmpty(noContent("Workspace does not exist: %s", workspaceId))
                .flatMap(w -> catalog.getStoreByName(w, name, type));
    }

    @GetMapping(path = "/namespaces/{namespaceId}/resources/name/{name}")
    public Mono<ResourceInfo> findResourceByNamespaceIdAndName(
            @PathVariable("namespaceId") String namespaceId,
            @PathVariable("name") String name,
            @RequestParam(name = "type", required = false) ClassMappings subType) {

        final Class<? extends ResourceInfo> type =
                (subType == null ? RESOURCE : subType).getInterface();

        return catalog.getById(namespaceId, NamespaceInfo.class)
                .switchIfEmpty(noContent("Namesapce does not exist: %s", namespaceId))
                .flatMap(n -> catalog.getResourceByName(n, name, type));
    }

    @GetMapping(path = "/layers/style/{styleId}", produces = APPLICATION_STREAM_JSON_VALUE)
    public Flux<LayerInfo> findLayersWithStyle(@PathVariable("styleId") String styleId) {
        return catalog.getById(styleId, StyleInfo.class)
                .switchIfEmpty(noContent("Style does not exist: %s", styleId))
                .flatMapMany(s -> catalog.getLayersWithStyle(s));
    }

    @GetMapping(path = "/layers/resource/{resourceId}", produces = APPLICATION_STREAM_JSON_VALUE)
    public Flux<LayerInfo> findLayersByResourceId(@PathVariable("resourceId") String resourceId) {

        return catalog.getById(resourceId, ResourceInfo.class)
                .switchIfEmpty(noContent("ResourceInfo does not exist: %s", resourceId))
                .flatMapMany(r -> catalog.getLayersByResource(r));
    }

    @GetMapping(path = "/layergroups/noworkspace", produces = APPLICATION_STREAM_JSON_VALUE)
    public Flux<LayerGroupInfo> findLayerGroupsByNullWoskspace() {
        return catalog.getLayerGroupsWithNoWoskspace();
    }

    @GetMapping(
        path = "/workspaces/{workspaceId}/layergroups",
        produces = APPLICATION_STREAM_JSON_VALUE
    )
    public Flux<LayerGroupInfo> findLayerGroupsByWoskspaceId(
            @PathVariable("workspaceId") String workspaceId) {

        return catalog.getById(workspaceId, WorkspaceInfo.class)
                .switchIfEmpty(noContent("Workspace does not exist: %s", workspaceId))
                .flatMapMany(catalog::getLayerGroupsByWoskspace);
    }

    @GetMapping(path = "/layergroups/noworkspace/{name}")
    public Mono<LayerGroupInfo> findLayerGroupByNameAndNullWorkspace(
            @PathVariable("name") String name) {

        return catalog.getLayerGroupByName(name)
                .switchIfEmpty(noContent("LayerGroup named '%s' does not exist", name));
    }

    @GetMapping(path = "/workspaces/{workspaceId}/layergroups/{name}")
    public Mono<LayerGroupInfo> findLayerGroupByNameAndWorkspaceId(
            @PathVariable(required = false, name = "workspaceId") String workspaceId,
            @PathVariable("name") String name) {

        return catalog.getById(workspaceId, WorkspaceInfo.class)
                .flatMap(w -> catalog.getLayerGroupByName(w, name))
                .switchIfEmpty(noContent("Workspace does not exist: %s", workspaceId));
    }

    @GetMapping(path = "/styles/noworkspace", produces = APPLICATION_STREAM_JSON_VALUE)
    public Flux<StyleInfo> findStylesByNullWorkspace() {
        return catalog.getStylesWithNoWorkspace();
    }

    @GetMapping(path = "/workspaces/{workspaceId}/styles", produces = APPLICATION_STREAM_JSON_VALUE)
    public Flux<StyleInfo> findStylesByWorkspaceId(
            @PathVariable(name = "workspaceId") String workspaceId) {

        return catalog.getById(workspaceId, WorkspaceInfo.class)
                .switchIfEmpty(noContent("Workspace does not exist: %s", workspaceId))
                .flatMapMany(catalog::getStylesByWorkspace);
    }

    @GetMapping(path = "/workspaces/{workspaceId}/styles/{name}")
    public Mono<StyleInfo> findStyleByWorkspaceIdAndName(
            @PathVariable(name = "workspaceId") String workspaceId,
            @PathVariable("name") String name) {

        return catalog.getById(workspaceId, WorkspaceInfo.class)
                .flatMap(w -> catalog.getStyleByName(w, name))
                .switchIfEmpty(noContent("Workspace does not exist: %s", workspaceId));
    }

    @GetMapping(path = "/styles/noworkspace/{name}")
    public Mono<StyleInfo> findStyleByNameAndNullWorkspace(@PathVariable("name") String name) {

        return catalog.getStyleByName(name)
                .switchIfEmpty(noContent("Style named '%s' does not exist", name));
    }

    private @NonNull ClassMappings endpointToType(@NonNull String endpoint) {
        // e.g. "workspaces" -> "WORKSPACE"
        String enumKey = endpoint.toUpperCase().substring(0, endpoint.length() - 1);
        ClassMappings type = ClassMappings.valueOf(enumKey);
        if (type == null) {
            throw new IllegalArgumentException("Invalid end point: " + endpoint);
        }
        return type;
    }

    private @NonNull ClassMappings endpointToType(@NonNull String endpoint, ClassMappings subType) {
        ClassMappings type = endpointToType(endpoint);
        if (subType != null) {
            if (!type.getInterface().isAssignableFrom(subType.getInterface())) {
                throw new IllegalArgumentException(
                        String.format("%s is not a subtype of %s", subType, type));
            }
            return subType;
        }
        return type;
    }

    protected <T> Mono<T> error(HttpStatus status, String messageFormat, Object... messageArgs) {
        return Mono.error(
                () ->
                        new ResponseStatusException(
                                status, String.format(messageFormat, messageArgs)));
    }

    /**
     * We use response code 204 (No Content) to mean something was not found, to differentiate from
     * the actual meaning of 404 - Not found, that the url itself is not found.
     */
    protected <T> Mono<T> noContent(String messageFormat, Object... messageArgs) {
        // revisit whether and now to return a reason message as header for debugging purposes
        // ex.getResponseHeaders().add("x-debug-reason", reason);
        return Mono.error(() -> new ResponseStatusException(HttpStatus.NO_CONTENT));
    }

    protected <T> Mono<T> internalError(String messageFormat, Object... messageArgs) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, messageFormat, messageArgs);
    }
}
