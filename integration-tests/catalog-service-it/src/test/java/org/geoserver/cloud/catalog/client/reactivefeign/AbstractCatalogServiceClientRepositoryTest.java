/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.catalog.client.reactivefeign;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.impl.ClassMappings;
import org.geoserver.catalog.impl.ModificationProxy;
import org.geoserver.catalog.plugin.CatalogInfoRepository;
import org.geoserver.catalog.plugin.Patch;
import org.geoserver.catalog.plugin.PropertyDiff;
import org.geoserver.cloud.catalog.app.CatalogServiceApplication;
import org.geoserver.cloud.catalog.client.impl.CatalogClientConfiguration;
import org.geoserver.cloud.catalog.client.impl.CatalogServiceCatalogFacade;
import org.geoserver.cloud.catalog.client.impl.InnerResolvingProxy;
import org.geoserver.cloud.test.CatalogTestData;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(
    classes = { //
        CatalogServiceApplication.class, //
        CatalogClientConfiguration.class //
    },
    webEnvironment = WebEnvironment.DEFINED_PORT,
    properties = {
        "spring.main.web-application-type=reactive",
        "server.port=15556",
        "geoserver.backend.catalog-service.uri=http://localhost:${server.port}"
    }
)
@RunWith(SpringRunner.class)
@ActiveProfiles("it.catalog-service")
@EnableAutoConfiguration
public abstract class AbstractCatalogServiceClientRepositoryTest<
        C extends CatalogInfo, CL extends CatalogInfoRepository<C>> {

    /**
     * WebFlux catalog-service catalog with backend as configured by
     * bootstrap-it.catalog-service.yml
     */
    protected @Autowired @Qualifier("catalog") Catalog serverCatalog;

    private @Autowired CatalogServiceCatalogFacade rawCatalogServiceFacade;

    protected CatalogTestData testData;

    protected final @NonNull Class<C> infoType;

    protected InnerResolvingProxy proxyResolver;

    protected AbstractCatalogServiceClientRepositoryTest(@NonNull Class<C> infoType) {
        this.infoType = infoType;
    }

    public @Before void setup() {
        testData = CatalogTestData.initialized(() -> serverCatalog).initCatalog();
        this.proxyResolver = new InnerResolvingProxy(rawCatalogServiceFacade, null);
        // proxyResolver = new ProxyUtils(catalog, geoServer).failOnMissingReference(true);
        // clientCatalog = new CatalogImpl(clientFacade);
    }

    /** Prune the server catalog */
    public @After void tearDown() {
        testData.deleteAll(serverCatalog);
    }

    protected abstract CL repository();

    public abstract @Test void testFindAll();

    public abstract @Test void testFindAllByType();

    public abstract @Test void testFindById();

    public abstract @Test void testQueryFilter();

    protected void testFind(
            Supplier<Stream<? extends C>> command, @SuppressWarnings("unchecked") C... expected) {

        Set<String> expectedIds =
                Arrays.stream(expected).map(CatalogInfo::getId).collect(Collectors.toSet());
        Set<String> actual = command.get().map(CatalogInfo::getId).collect(Collectors.toSet());
        assertEquals(expectedIds, actual);
    }

    protected void testFindAll(@SuppressWarnings("unchecked") C... expected) {
        testFind(() -> repository().findAll(), expected);
    }

    protected <S extends C> void testFindAllIncludeFilter(
            Class<S> type, @SuppressWarnings("unchecked") S... expected) {

        testFind(() -> repository().findAll(Filter.INCLUDE, type), expected);
    }

    protected <S extends C> void testQueryFilter(
            String ecqlFilter, @SuppressWarnings("unchecked") S... expected) {
        testQueryFilter(this.infoType, ecqlFilter, expected);
    }

    protected <S extends C> void testQueryFilter(
            Class<S> type, String ecqlFilter, @SuppressWarnings("unchecked") S... expected) {
        Filter filter;
        try {
            filter = ECQL.toFilter(ecqlFilter);
        } catch (CQLException e) {
            throw new RuntimeException(e);
        }
        this.testQueryFilter(type, filter, expected);
    }

    protected <S extends C> void testQueryFilter(
            Class<S> type, Filter filter, @SuppressWarnings("unchecked") S... expected) {

        Stream<S> found = repository().findAll(filter, type);

        Set<String> expectedIds =
                Arrays.stream(expected).map(CatalogInfo::getId).collect(Collectors.toSet());
        Set<String> returnedIds = found.map(CatalogInfo::getId).collect(Collectors.toSet());
        assertEquals(expectedIds, returnedIds);
    }

    protected void testFindById(C expected) {
        assertNotNull(expected.getId());
        C responseBody = repository().findById(expected.getId(), infoType);
        C resolved = resolveProxies(responseBody);
        assertCatalogInfoEquals(expected, resolved);
    }

    protected <I extends CatalogInfo> I resolveProxies(I info) {
        return proxyResolver.resolve(info);
    }

    protected final void assertCatalogInfoEquals(C expected, C actual) {
        assertPropertriesEqual(expected, actual);
    }

    /**
     * Subclasses should override to provide {@link CatalogInfo} subtype specific assertions. Not
     * doing {@code assertEquals(expected, actual)} because the returned object may differ from the
     * submitted one as the catalog populated default properties
     */
    protected abstract void assertPropertriesEqual(C expected, C actual);

    public @Test void testFindByIdNotFound() throws IOException {
        assertNull(repository().findById("non-existent-ws-id", infoType));
    }

    protected void crudTest(
            final C toCreate,
            Function<String, C> catalogLookup,
            Consumer<C> modifyingConsumer,
            BiConsumer<C, C> updateVerifier) {

        C created = testCreate(toCreate, catalogLookup);

        C updated = testUpdate(created, modifyingConsumer, updateVerifier);

        testDelete(updated, catalogLookup);
    }

    protected C testUpdate(
            C created, Consumer<C> modifyingConsumer, BiConsumer<C, C> updateVerifier) {

        Patch patch = createPatch(created, modifyingConsumer);
        C updated = repository().update(created, patch);
        assertNotSame(created, updated);
        updated = resolveProxies(updated);
        updateVerifier.accept(created, updated);

        C foundAfterUpdate = repository().findById(updated.getId(), infoType);
        foundAfterUpdate = resolveProxies(foundAfterUpdate);
        updateVerifier.accept(created, foundAfterUpdate);

        return updated;
    }

    public Patch createPatch(C info, Consumer<C> modifyingConsumer) {
        C real = ModificationProxy.unwrap(info);
        Class<? extends CatalogInfo> clazz = real.getClass();
        ClassMappings classMappings = ClassMappings.fromImpl(clazz);
        C proxied = ModificationProxy.create(info, classMappings.getInterface());

        modifyingConsumer.accept(proxied);
        ModificationProxy proxy = (ModificationProxy) Proxy.getInvocationHandler(proxied);
        Patch patch = PropertyDiff.valueOf(proxy).toPatch();
        return patch;
    }

    protected C testCreate(C toCreate, Function<String, C> catalogLookup) {
        final String providedId = toCreate.getId();
        assertNotNull("id must be provided", providedId);
        assertNull(
                "Object to be created shall not already exist in catalog",
                catalogLookup.apply(toCreate.getId()));

        repository().add(toCreate);
        C created = repository().findById(providedId, infoType);
        assertNotNull("Object not found after added: " + toCreate, created);
        created = resolveProxies(created);
        assertCatalogInfoEquals(toCreate, created);
        return created;
    }

    protected C testDelete(final C toDelete, Function<String, C> catalogLookup) {

        C foundBeforeDelete = repository().findById(toDelete.getId(), infoType);

        assertNotNull(foundBeforeDelete);
        repository().remove(toDelete);
        assertNull(
                "object not deleted from backend catalog",
                repository().findById(toDelete.getId(), infoType));

        return foundBeforeDelete;
    }
}
