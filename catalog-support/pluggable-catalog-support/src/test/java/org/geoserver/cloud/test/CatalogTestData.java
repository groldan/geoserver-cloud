/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.test;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.geoserver.catalog.CascadeDeleteVisitor;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogFactory;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.Info;
import org.geoserver.catalog.Keyword;
import org.geoserver.catalog.KeywordInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.MetadataMap;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.PublishedInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.catalog.WMSStoreInfo;
import org.geoserver.catalog.WMTSLayerInfo;
import org.geoserver.catalog.WMTSStoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.geoserver.catalog.impl.LayerGroupInfoImpl;
import org.geoserver.catalog.impl.MetadataLinkInfoImpl;
import org.geoserver.config.ContactInfo;
import org.geoserver.config.CoverageAccessInfo;
import org.geoserver.config.CoverageAccessInfo.QueueType;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.GeoServerInfo.WebUIMode;
import org.geoserver.config.JAIInfo;
import org.geoserver.config.JAIInfo.PngEncoderType;
import org.geoserver.config.LoggingInfo;
import org.geoserver.config.ResourceErrorHandling;
import org.geoserver.config.SettingsInfo;
import org.geoserver.config.impl.ContactInfoImpl;
import org.geoserver.config.impl.CoverageAccessInfoImpl;
import org.geoserver.config.impl.GeoServerImpl;
import org.geoserver.config.impl.GeoServerInfoImpl;
import org.geoserver.config.impl.JAIEXTInfoImpl;
import org.geoserver.config.impl.JAIInfoImpl;
import org.geoserver.config.impl.LoggingInfoImpl;
import org.geoserver.config.impl.ServiceInfoImpl;
import org.geoserver.config.impl.SettingsInfoImpl;
import org.geoserver.ows.util.OwsUtils;
import org.geoserver.security.CatalogMode;
import org.geoserver.wcs.WCSInfo;
import org.geoserver.wcs.WCSInfoImpl;
import org.geoserver.wfs.WFSInfo;
import org.geoserver.wfs.WFSInfo.ServiceLevel;
import org.geoserver.wfs.WFSInfoImpl;
import org.geoserver.wms.CacheConfiguration;
import org.geoserver.wms.WMSInfo;
import org.geoserver.wms.WMSInfoImpl;
import org.geoserver.wps.ProcessGroupInfo;
import org.geoserver.wps.ProcessGroupInfoImpl;
import org.geoserver.wps.WPSInfo;
import org.geoserver.wps.WPSInfoImpl;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.process.factory.AnnotationDrivenProcessFactory;
import org.geotools.util.Converters;
import org.geotools.util.Version;
import org.junit.rules.ExternalResource;
import org.springframework.util.Assert;

/**
 * Junit {@code @Rule} to provide or populate a catalog; use {@link CatalogTestData#empty
 * CatalogTestData.empty(Supplier<Catalog>)} to start up with an empty catalog but having the test
 * data {@link #createCatalogObjects() ready to be used}, or {@link CatalogTestData#initialized
 * CatalogTestData.initialized(Supplier<Catalog>)} to pre-populate the catalog with the {@link
 * #createCatalogObjects() test objects} before running the tests.
 */
public class CatalogTestData extends ExternalResource {

    private Supplier<Catalog> catalog;
    private @Setter @Getter GeoServer configCatalog;

    private boolean initialize;

    private CatalogTestData(Supplier<Catalog> catalog, boolean initialize) {
        this.catalog = catalog;
        this.initialize = initialize;
    }

    public static CatalogTestData empty(Supplier<Catalog> catalog) {
        return new CatalogTestData(catalog, false);
    }

    public static CatalogTestData initialized(Supplier<Catalog> catalog) {
        return new CatalogTestData(catalog, true);
    }

    protected @Override void before() {
        initCatalog();
    }

    public CatalogTestData initCatalog() {
        createCatalogObjects();
        if (initialize) {
            deleteAll();
            addObjects();
        }
        return this;
    }

    protected @Override void after() {
        if (initialize) {
            deleteAll();
        }
    }

    public void deleteAll() {
        deleteAll(catalog.get());
    }

    public void deleteAll(Catalog catalog) {
        CascadeDeleteVisitor deleteVisitor = new CascadeDeleteVisitor(catalog);
        catalog.getLayerGroups().forEach(lg -> lg.accept(deleteVisitor));
        catalog.getWorkspaces().forEach(ws -> ws.accept(deleteVisitor));
        catalog.getNamespaces().forEach(ws -> ws.accept(deleteVisitor));
        // bypass catalog's check for default style
        catalog.getStyles().forEach(catalog.getFacade()::remove);
    }

    public GeoServerInfo global;
    public LoggingInfo logging;
    public SettingsInfo workspaceASettings;
    public WMSInfo wmsService;
    public WFSInfo wfsService;
    public WCSInfo wcsService;
    public WPSInfo wpsService;

    public WorkspaceInfo workspaceA;
    public WorkspaceInfo workspaceB;
    public WorkspaceInfo workspaceC;

    public NamespaceInfo namespaceA;
    public NamespaceInfo namespaceB;
    public NamespaceInfo namespaceC;

    public DataStoreInfo dataStoreA;
    public DataStoreInfo dataStoreB;
    public DataStoreInfo dataStoreC;

    public CoverageStoreInfo coverageStoreA;
    public WMSStoreInfo wmsStoreA;
    public WMTSStoreInfo wmtsStoreA;
    public FeatureTypeInfo featureTypeA;
    public CoverageInfo coverageA;
    public WMSLayerInfo wmsLayerA;
    public WMTSLayerInfo wmtsLayerA;
    public LayerInfo layerFeatureTypeA;
    public StyleInfo style1;
    public StyleInfo style2;
    public LayerGroupInfo layerGroup1;

    public CatalogTestData addObjects() {
        Catalog catalog = this.catalog.get();
        catalog.add(workspaceA);
        catalog.add(workspaceB);
        catalog.add(workspaceC);

        catalog.add(namespaceA);
        catalog.add(namespaceB);
        catalog.add(namespaceC);

        catalog.add(dataStoreA);
        catalog.add(dataStoreB);
        catalog.add(dataStoreC);

        catalog.add(coverageStoreA);
        catalog.add(wmsStoreA);
        catalog.add(wmtsStoreA);
        catalog.add(featureTypeA);
        catalog.add(coverageA);
        catalog.add(wmsLayerA);
        catalog.add(wmtsLayerA);
        catalog.add(style1);
        catalog.add(style2);
        catalog.add(layerFeatureTypeA);
        catalog.add(layerGroup1);
        return this;
    }

    public CatalogTestData createCatalogObjects() {
        namespaceA = createNamespace("ns1", "wsName", "nsURI");
        namespaceB = createNamespace("ns2", "aaa", "nsURIaaa");
        namespaceC = createNamespace("ns3", "bbb", "nsURIbbb");
        workspaceA = createWorkspace("ws1", "wsName");
        workspaceB = createWorkspace("ws2", "aaa");
        workspaceC = createWorkspace("ws3", "bbb");

        dataStoreA = createDataStore("ds1", workspaceA, "dsName", "dsDescription", true);
        dataStoreB = createDataStore("ds2", workspaceB, "dsNameA", "dsDescription", true);
        dataStoreC = createDataStore("ds3", workspaceC, "dsNameB", "dsDescription", true);

        featureTypeA =
                createFeatureType(
                        "ft1",
                        dataStoreA,
                        namespaceA,
                        "ftName",
                        "ftAbstract",
                        "ftDescription",
                        true);

        coverageStoreA =
                createCoverageStore("cs1", workspaceA, "csName", "fakeCoverageType", "file://fake");
        coverageA = createCoverage("cov1", coverageStoreA, "cvName");

        wmsStoreA = createWebMapServer("wms1", workspaceA, "wmsName", "http://fake.url", true);

        wmsLayerA = createWMSLayer("wmsl-1", wmsStoreA, namespaceA, "wmsLayer1", true);

        wmtsStoreA =
                createWebMapTileServer(
                        "wmts1", workspaceA, "wmtsName", "http://fake.wmts.url", true);

        wmtsLayerA = createWMTSLayer("wmtsl1", wmtsStoreA, namespaceA, "wmtsLayer", true);

        style1 = createStyle("style1", null, "style1", "styleFilename");

        style2 = createStyle("style2", null, "style2", "style2.sld");

        layerFeatureTypeA = createLayer("layer1", featureTypeA, "Layer1", true, style1);

        layerGroup1 = createLayerGroup("lg1", null, "layerGroup", layerFeatureTypeA, style1);

        return this;
    }

    public CatalogTestData initConfig() {
        createConfigObjects();
        if (configCatalog == null) {
            configCatalog = new GeoServerImpl();
        }
        configCatalog.setGlobal(global);
        configCatalog.setLogging(logging);
        configCatalog.add(wmsService);
        configCatalog.add(wfsService);
        configCatalog.add(wpsService);
        configCatalog.add(wcsService);

        configCatalog.add(workspaceASettings);
        return this;
    }

    public CatalogTestData createConfigObjects() {
        global = createGlobal();
        logging = createLogging();
        workspaceASettings = createSettings(workspaceA);
        wmsService = createService("wms", WMSInfoImpl::new);
        wfsService = createService("wfs", WFSInfoImpl::new);
        wcsService = createService("wcs", WCSInfoImpl::new);
        wpsService = createService("wps", WPSInfoImpl::new);

        // ignore simple boolean properties

        wmsService.setCacheConfiguration(new CacheConfiguration());
        wmsService.setRemoteStyleMaxRequestTime(2000);
        wmsService.setRemoteStyleTimeout(500);

        wfsService.setMaxFeatures(50);
        wfsService.setServiceLevel(ServiceLevel.COMPLETE);
        wfsService.setMaxNumberOfFeaturesForPreview(10);

        wcsService.setOverviewPolicy(OverviewPolicy.QUALITY);

        wpsService.setConnectionTimeout(1000);
        wpsService.setResourceExpirationTimeout(2000);
        wpsService.setMaxSynchronousProcesses(4);
        wpsService.setMaxAsynchronousProcesses(16);
        ProcessGroupInfo pgi = new ProcessGroupInfoImpl();
        pgi.setEnabled(true);
        pgi.setFactoryClass(AnnotationDrivenProcessFactory.class);
        wpsService.getProcessGroups().add(pgi);
        wpsService.setCatalogMode(CatalogMode.CHALLENGE);
        wpsService.setMaxComplexInputSize(1024);
        wpsService.setMaxAsynchronousExecutionTime(1);
        wpsService.setMaxAsynchronousTotalTime(2);
        wpsService.setMaxSynchronousExecutionTime(3);
        wpsService.setMaxSynchronousTotalTime(4);
        return this;
    }

    public LayerGroupInfo createLayerGroup(
            String id, WorkspaceInfo workspace, String name, PublishedInfo layer, StyleInfo style) {
        // not using factory cause SecuredCatalog would return SecuredLayerGroupInfo which has no id
        // setter
        LayerGroupInfo lg = new LayerGroupInfoImpl();
        OwsUtils.set(lg, "id", id);
        lg.setName(name);
        lg.setWorkspace(workspace);
        lg.getLayers().add(layer);
        lg.getStyles().add(style);
        OwsUtils.resolveCollections(lg);
        return lg;
    }

    public LayerInfo createLayer(
            String id,
            ResourceInfo resource,
            String title,
            boolean enabled,
            StyleInfo defaultStyle,
            StyleInfo... additionalStyles) {
        LayerInfo lyr = catalog.get().getFactory().createLayer();
        OwsUtils.set(lyr, "id", id);
        lyr.setResource(resource);
        lyr.setEnabled(enabled);
        lyr.setDefaultStyle(defaultStyle);
        lyr.setTitle(title);
        for (int i = 0; null != additionalStyles && i < additionalStyles.length; i++) {
            lyr.getStyles().add(additionalStyles[i]);
        }
        OwsUtils.resolveCollections(lyr);
        return lyr;
    }

    public StyleInfo createStyle(@NonNull String name) {
        return createStyle(name, (WorkspaceInfo) null);
    }

    public StyleInfo createStyle(@NonNull String name, WorkspaceInfo workspace) {
        return createStyle(name + "-id", workspace, name, name + ".sld");
    }

    public StyleInfo createStyle(String id, WorkspaceInfo workspace, String name, String fileName) {
        StyleInfo st = catalog.get().getFactory().createStyle();
        OwsUtils.set(st, "id", id);
        st.setWorkspace(workspace);
        st.setName(name);
        st.setFilename(fileName);
        OwsUtils.resolveCollections(st);
        return st;
    }

    public WMTSLayerInfo createWMTSLayer(
            String id, StoreInfo store, NamespaceInfo namespace, String name, boolean enabled) {
        WMTSLayerInfo wmtsl = catalog.get().getFactory().createWMTSLayer();
        OwsUtils.set(wmtsl, "id", id);
        wmtsl.setStore(store);
        wmtsl.setNamespace(namespace);
        wmtsl.setName(name);
        wmtsl.setEnabled(enabled);
        OwsUtils.resolveCollections(wmtsl);
        return wmtsl;
    }

    public WMTSStoreInfo createWebMapTileServer(
            String id, WorkspaceInfo workspace, String name, String url, boolean enabled) {
        WMTSStoreInfo wmtss = catalog.get().getFactory().createWebMapTileServer();
        OwsUtils.set(wmtss, "id", id);
        wmtss.setWorkspace(workspace);
        wmtss.setName(name);
        wmtss.setType("WMTS");
        wmtss.setCapabilitiesURL(url);
        wmtss.setEnabled(enabled);
        OwsUtils.resolveCollections(wmtss);
        return wmtss;
    }

    public WMSLayerInfo createWMSLayer(
            String id, StoreInfo store, NamespaceInfo namespace, String name, boolean enabled) {
        WMSLayerInfo wmsl = catalog.get().getFactory().createWMSLayer();
        OwsUtils.set(wmsl, "id", id);
        wmsl.setStore(store);
        wmsl.setNamespace(namespace);
        wmsl.setName(name);
        wmsl.setEnabled(enabled);
        OwsUtils.resolveCollections(wmsl);
        return wmsl;
    }

    public WMSStoreInfo createWebMapServer(
            String id, WorkspaceInfo wspace, String name, String url, boolean enabled) {
        WMSStoreInfo wms = catalog.get().getFactory().createWebMapServer();
        OwsUtils.set(wms, "id", id);
        wms.setName(name);
        wms.setType("WMS");
        wms.setCapabilitiesURL(url);
        wms.setWorkspace(wspace);
        wms.setEnabled(enabled);
        OwsUtils.resolveCollections(wms);
        return wms;
    }

    public CoverageInfo createCoverage(String id, CoverageStoreInfo cstore, String name) {
        CoverageInfo coverage = catalog.get().getFactory().createCoverage();
        OwsUtils.set(coverage, "id", id);
        coverage.setName(name);
        coverage.setStore(cstore);
        OwsUtils.resolveCollections(coverage);
        return coverage;
    }

    public CoverageStoreInfo createCoverageStore(
            String id, WorkspaceInfo ws, String name, String coverageType, String uri) {
        CoverageStoreInfo cstore = catalog.get().getFactory().createCoverageStore();
        OwsUtils.set(cstore, "id", id);
        cstore.setName(name);
        cstore.setType(coverageType);
        cstore.setURL(uri);
        cstore.setWorkspace(ws);
        OwsUtils.resolveCollections(cstore);
        return cstore;
    }

    public FeatureTypeInfo createFeatureType(String name) {
        return createFeatureType(
                name + "-id",
                dataStoreA,
                namespaceA,
                name,
                name + " abstract",
                name + " description",
                true);
    }

    public FeatureTypeInfo createFeatureType(
            String id,
            DataStoreInfo ds,
            NamespaceInfo ns,
            String name,
            String ftAbstract,
            String ftDescription,
            boolean enabled) {
        FeatureTypeInfo fttype = catalog.get().getFactory().createFeatureType();
        OwsUtils.set(fttype, "id", id);
        fttype.setEnabled(true);
        fttype.setName(name);
        fttype.setAbstract(ftAbstract);
        fttype.setDescription(ftDescription);
        fttype.setStore(ds);
        fttype.setNamespace(ns);
        OwsUtils.resolveCollections(fttype);
        return fttype;
    }

    public void assertEqualsLenientConnectionParameters(Info info1, Info info2) {
        if (info1 != null && info2 != null) {
            if (info1 instanceof DataStoreInfo) {
                DataStoreInfo ds1 = (DataStoreInfo) info1;
                DataStoreInfo ds2 = (DataStoreInfo) info2;
                Map<String, Serializable> p1 = new HashMap<>(ds1.getConnectionParameters());
                Map<String, Serializable> p2 = new HashMap<>(ds2.getConnectionParameters());
                p1.forEach(
                        (k, v) ->
                                ds1.getConnectionParameters()
                                        .put(k, Converters.convert(v, String.class)));
                p2.forEach(
                        (k, v) ->
                                ds2.getConnectionParameters()
                                        .put(k, Converters.convert(v, String.class)));
                assertEquals(ds1.getConnectionParameters(), ds2.getConnectionParameters());
            }
        }
        assertEquals(info1, info2);
    }

    public DataStoreInfo createDataStore(
            String id, WorkspaceInfo ws, String name, String description, boolean enabled) {
        DataStoreInfoImpl dstore = (DataStoreInfoImpl) catalog.get().getFactory().createDataStore();
        OwsUtils.set(dstore, "id", id);
        dstore.setEnabled(enabled);
        dstore.setName(name);
        dstore.setDescription(description);
        dstore.setWorkspace(ws);
        dstore.setConnectionParameters(new HashMap<>());
        dstore.getConnectionParameters().put("param1", "test value");
        dstore.getConnectionParameters().put("param2", Integer.valueOf(1000));
        OwsUtils.resolveCollections(dstore);
        return dstore;
    }

    public WorkspaceInfo createWorkspace(String name) {
        return createWorkspace(name + "-id", name);
    }

    public WorkspaceInfo createWorkspace(String id, String name) {
        WorkspaceInfo workspace = catalog.get().getFactory().createWorkspace();
        OwsUtils.set(workspace, "id", id);
        workspace.setName(name);
        OwsUtils.resolveCollections(workspace);
        return workspace;
    }

    public NamespaceInfo createNamespace(String name, String uri) {
        return createNamespace(name + "-id", name, uri);
    }

    public NamespaceInfo createNamespace(String id, String name, String uri) {
        Catalog cat = catalog.get();
        CatalogFactory factory = cat.getFactory();
        NamespaceInfo namesapce = factory.createNamespace();
        OwsUtils.set(namesapce, "id", id);
        namesapce.setPrefix(name);
        namesapce.setURI(uri);
        OwsUtils.resolveCollections(namesapce);
        return namesapce;
    }

    public GeoServerInfo createGlobal() {
        GeoServerInfoImpl g = new GeoServerInfoImpl();

        g.setId("GeoServer.global");
        g.setAdminPassword("geoserver");
        g.setAdminUsername("admin");
        g.setAllowStoredQueriesPerWorkspace(true);
        g.setCoverageAccess(createCoverageAccessInfo());
        g.setFeatureTypeCacheSize(1000);
        g.setGlobalServices(true);
        g.setId("GeoServer.global");
        g.setJAI(creteJAI());
        g.setLockProviderName("testLockProvider");
        g.setMetadata(createMetadata("k1", Integer.valueOf(1), "k2", "2", "k3", Boolean.FALSE));
        g.setResourceErrorHandling(ResourceErrorHandling.OGC_EXCEPTION_REPORT);
        g.setSettings(createSettings(null));
        g.setUpdateSequence(999);
        g.setUseHeadersProxyURL(true);
        g.setWebUIMode(WebUIMode.DO_NOT_REDIRECT);
        g.setXmlExternalEntitiesEnabled(Boolean.TRUE);
        g.setXmlPostRequestLogBufferSize(1024);

        return g;
    }

    public MetadataMap createMetadata(Serializable... kvps) {
        Assert.isTrue(kvps == null || kvps.length % 2 == 0, "expected even number");
        MetadataMap m = new MetadataMap();
        if (kvps != null) {
            for (int i = 0; i < kvps.length; i += 2) {
                m.put((String) kvps[i], kvps[i + 1]);
            }
        }
        return m;
    }

    private JAIInfo creteJAI() {
        JAIInfoImpl jai = new JAIInfoImpl();
        jai.setAllowInterpolation(true);
        jai.setAllowNativeMosaic(true);
        jai.setAllowNativeWarp(true);
        jai.setImageIOCache(true);
        JAIEXTInfoImpl jaiext = new JAIEXTInfoImpl();
        jaiext.setJAIEXTOperations(Collections.singleton("categorize"));
        jaiext.setJAIOperations(Collections.singleton("band"));
        jai.setJAIEXTInfo(jaiext);
        jai.setJpegAcceleration(true);
        jai.setMemoryCapacity(4096);
        jai.setMemoryThreshold(0.75);
        jai.setPngAcceleration(true);
        jai.setPngEncoderType(PngEncoderType.PNGJ);
        jai.setRecycling(true);
        jai.setTilePriority(1);
        jai.setTileThreads(7);
        return jai;
    }

    private CoverageAccessInfo createCoverageAccessInfo() {
        CoverageAccessInfoImpl c = new CoverageAccessInfoImpl();
        c.setCorePoolSize(9);
        c.setImageIOCacheThreshold(11);
        c.setKeepAliveTime(1000);
        c.setMaxPoolSize(18);
        c.setQueueType(QueueType.UNBOUNDED);
        return c;
    }

    public ContactInfo createContact() {
        ContactInfoImpl c = new ContactInfoImpl();
        c.setId("cinfo-id");
        c.setAddress("right here");
        c.setAddressCity("Sin City");
        c.setAddressCountry("USA");
        c.setContactPerson("myself");
        c.setContactVoice("yes please");
        return c;
    }

    public LoggingInfo createLogging() {
        LoggingInfoImpl l = new LoggingInfoImpl();
        l.setId("weird-this-has-id");
        l.setLevel("super");
        l.setLocation("there");
        l.setStdOutLogging(true);
        return l;
    }

    public SettingsInfo createSettings(WorkspaceInfo workspace) {
        SettingsInfoImpl s = new SettingsInfoImpl();
        s.setWorkspace(workspace);
        s.setId(workspace == null ? "global-settings-id" : workspace.getName() + "-settings-id");
        s.setTitle(workspace == null ? "Global Settings" : workspace.getName() + " Settings");
        s.setCharset("UTF-8");
        s.setContact(createContact());
        s.setMetadata(createMetadata("k1", Integer.valueOf(1), "k2", "2", "k3", Boolean.FALSE));
        s.setNumDecimals(9);
        s.setOnlineResource("http://geoserver.org");
        s.setProxyBaseUrl("http://test.geoserver.org");
        s.setSchemaBaseUrl("file:data/schemas");
        s.setVerbose(true);
        s.setVerboseExceptions(true);
        return s;
    }

    public <S extends ServiceInfoImpl> S createService(String name, Supplier<S> factory) {
        S s = factory.get();
        s.setId(name + "-id");
        s.setName(name);
        s.setTitle(name + " Title");
        s.setAbstract(name + " Abstract");
        s.setAccessConstraints("NONE");
        s.setCiteCompliant(true);
        s.setEnabled(true);
        s.setExceptionFormats(Collections.singletonList("fake-" + name + "-exception-format"));
        s.setFees("NONE");
        s.setKeywords(createKeywords(name));
        s.setMaintainer("Claudious whatever");
        s.setMetadata(createMetadata(name, "something"));
        MetadataLinkInfoImpl metadataLink = new MetadataLinkInfoImpl();
        metadataLink.setAbout("about");
        metadataLink.setContent("content");
        metadataLink.setId("medatata-link-" + name);
        metadataLink.setMetadataType("fake");
        metadataLink.setType("void");
        s.setMetadataLink(metadataLink);
        s.setOnlineResource("http://geoserver.org/" + name);
        s.setOutputStrategy("SPEED");
        s.setSchemaBaseURL("file:data/" + name);
        s.setVerbose(true);
        s.setVersions(Arrays.asList(new Version("1.0.0"), new Version("2.0.0")));
        return s;
    }

    private List<KeywordInfo> createKeywords(String name) {
        Keyword k1 = new Keyword("GeoServer");
        Keyword k2 = new Keyword(name);
        k2.setLanguage("eng");
        k2.setVocabulary("watchit");
        return Arrays.asList(k1, k2);
    }
}
