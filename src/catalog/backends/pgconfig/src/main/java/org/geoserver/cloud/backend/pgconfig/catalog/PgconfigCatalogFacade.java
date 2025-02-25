/*
 * (c) 2023 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.backend.pgconfig.catalog;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import lombok.NonNull;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.PublishedInfo;
import org.geoserver.catalog.plugin.CatalogInfoRepository;
import org.geoserver.catalog.plugin.CatalogInfoRepository.LayerGroupRepository;
import org.geoserver.catalog.plugin.CatalogInfoRepository.LayerRepository;
import org.geoserver.catalog.plugin.Query;
import org.geoserver.catalog.plugin.RepositoryCatalogFacadeImpl;
import org.geoserver.catalog.plugin.resolving.CatalogPropertyResolver;
import org.geoserver.catalog.plugin.resolving.CollectionPropertiesInitializer;
import org.geoserver.catalog.plugin.resolving.ResolvingProxyResolver;
import org.geoserver.cloud.backend.pgconfig.catalog.repository.PgconfigCatalogInfoRepository;
import org.geoserver.cloud.backend.pgconfig.catalog.repository.PgconfigLayerGroupRepository;
import org.geoserver.cloud.backend.pgconfig.catalog.repository.PgconfigLayerRepository;
import org.geoserver.cloud.backend.pgconfig.catalog.repository.PgconfigNamespaceRepository;
import org.geoserver.cloud.backend.pgconfig.catalog.repository.PgconfigPublishedInfoReadOnlyRepository;
import org.geoserver.cloud.backend.pgconfig.catalog.repository.PgconfigPublishedInfoRepository;
import org.geoserver.cloud.backend.pgconfig.catalog.repository.PgconfigResourceRepository;
import org.geoserver.cloud.backend.pgconfig.catalog.repository.PgconfigStoreRepository;
import org.geoserver.cloud.backend.pgconfig.catalog.repository.PgconfigStyleRepository;
import org.geoserver.cloud.backend.pgconfig.catalog.repository.PgconfigWorkspaceRepository;
import org.geotools.api.filter.Filter;
import org.geotools.filter.visitor.SimplifyingFilterVisitor;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @see #queryPublishedInfo(Query)
 * @since 1.4
 */
public class PgconfigCatalogFacade extends RepositoryCatalogFacadeImpl {

    /**
     * Read-only {@link PublishedInfo} repository, used to query published without having to perform
     * two queries (one for layers and one for layer groups) and them merge-sort the results.
     * @see #queryPublishedInfo(Query)
     */
    private PgconfigPublishedInfoReadOnlyRepository publishedInfoReadOnlyRepo;

    public PgconfigCatalogFacade(@NonNull JdbcTemplate template) {
        var namespaces = new PgconfigNamespaceRepository(template);
        var workspaces = new PgconfigWorkspaceRepository(template);
        var styles = new PgconfigStyleRepository(template);

        var stores = new PgconfigStoreRepository(template);
        var layers = new PgconfigLayerRepository(template, styles);
        var resources = new PgconfigResourceRepository(template, layers);
        var layerGroups = new PgconfigLayerGroupRepository(template, styles);

        super.setNamespaceRepository(namespaces);
        super.setWorkspaceRepository(workspaces);
        super.setStoreRepository(stores);
        super.setResourceRepository(resources);
        super.setLayerRepository(layers);
        super.setLayerGroupRepository(layerGroups);
        super.setStyleRepository(styles);
        /*
         * We have a PublishedInfo repository, despite not being common practice (since
         * PublishedInfo was introduced later as a superclass for LayerInfo and
         * LayerGroupInfo, it never had its own "repository" in the default
         * Catalog/Facade implementation)
         */
        this.publishedInfoReadOnlyRepo = new PgconfigPublishedInfoReadOnlyRepository(template, styles);
    }

    @Override
    public void setCatalog(Catalog catalog) {
        super.setCatalog(catalog);
        setOutboundResolver();
    }

    @Override
    protected int countPublishedInfo(Filter filter) {
        return countInternal(PublishedInfo.class, filter);
    }

    @SuppressWarnings("unchecked")
    private void setOutboundResolver() {
        UnaryOperator<CatalogInfo> resolvingFunction = resolvingFunction(this::getCatalog);

        super.repositories.all().stream()
                .map(PgconfigCatalogInfoRepository.class::cast)
                .forEach(repo -> repo.setOutboundResolver(resolvingFunction));
    }

    public static <T extends CatalogInfo> UnaryOperator<T> resolvingFunction(Supplier<Catalog> catalog) {
        return CatalogPropertyResolver.<T>of(catalog)
                .andThen(ResolvingProxyResolver.<T>of(catalog))
                .andThen(CollectionPropertiesInitializer.instance())::apply;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CatalogInfo, R extends CatalogInfoRepository<T>> R repository(Class<T> of) {
        if (PublishedInfo.class.equals(of)) {
            return (R) publishedInfoReadOnlyRepo;
        }
        return repositories.repository(of);
    }

    @Override
    public <T extends CatalogInfo, R extends CatalogInfoRepository<T>> R repositoryFor(T info) {
        return repositories.repositoryFor(info);
    }

    /**
     * Queries {@link PgconfigPublishedInfoRepository} directly. The default implementation will
     * query {@link LayerRepository} and {@link LayerGroupRepository}, returning
     * a merge-sorted stream.
     */
    @Override
    protected Stream<PublishedInfo> queryPublishedInfo(Query<PublishedInfo> query) {
        final Filter filter = SimplifyingFilterVisitor.simplify(query.getFilter());

        return this.publishedInfoReadOnlyRepo.findAll(query.withFilter(filter));
    }
}
