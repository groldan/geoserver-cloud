package org.geoserver.jackson.databind.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.SneakyThrows;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogTestData;
import org.geoserver.catalog.impl.ResolvingProxy;
import org.geoserver.catalog.plugin.CatalogPlugin;
import org.geoserver.catalog.plugin.resolving.ProxyUtils;
import org.geoserver.config.GeoServer;
import org.geoserver.config.impl.GeoServerImpl;
import org.geoserver.platform.GeoServerExtensionsHelper;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.PropertyIsEqualTo;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.jackson.databind.util.ObjectMapperUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

@SuppressWarnings("java:1854")
public class BackwardsCompatibilityTestSupport {
    protected FilterFactory ff = CommonFactoryFinder.getFilterFactory();

    protected Catalog catalog;
    protected CatalogTestData data;
    protected GeoServer geoserver;
    protected ProxyUtils proxyResolver;

    protected String testMethod;

    protected ObjectMapper objectMapper;

    public static @BeforeAll void oneTimeSetup() {
        // avoid the chatty warning logs due to catalog looking up a bean of type
        // GeoServerConfigurationLock
        GeoServerExtensionsHelper.setIsSpringContext(false);
    }

    @BeforeEach
    public void before(TestInfo testInfo) {
        this.testMethod = testInfo.getTestMethod().orElseThrow().getName();
        objectMapper = newObjectMapper();
        catalog = new CatalogPlugin();
        geoserver = new GeoServerImpl();
        geoserver.setCatalog(catalog);
        data = CatalogTestData.initialized(() -> catalog, () -> geoserver).initialize();
        proxyResolver = new ProxyUtils(() -> catalog, Optional.of(geoserver));
    }

    protected ObjectMapper newObjectMapper() {
        return ObjectMapperUtil.newObjectMapper();
    }

    protected <T> T proxy(String id, Class<T> type) {
        return ResolvingProxy.create(id, type);
    }

    @SneakyThrows
    protected <T> T decode(String json, Class<T> type) {
        assertNotNull(type, "implement!");

        T decoded = objectMapper.readValue(json, type);
        assertThat(decoded).isNotNull().isInstanceOf(type);

        // String encoded = objectMapper.writeValueAsString(decoded);
        // org.skyscreamer.jsonassert.JSONAssert.assertEquals(json, encoded, false);

        return decoded;
    }

    protected void testFilterLiteral(Object value, String json) {
        PropertyIsEqualTo expected = equalsTo("prop", value);
        Filter actual = decode(json, Filter.class);

        assertEqualsFilter(expected, actual);
    }

    protected void assertEqualsFilter(PropertyIsEqualTo expected, Filter actual) {
        assertThat(actual).isInstanceOf(PropertyIsEqualTo.class);
        PropertyIsEqualTo eq = (PropertyIsEqualTo) actual;
        assertThat(eq.getExpression1()).isEqualTo(expected.getExpression1());
        assertThat(eq.getExpression2()).isEqualTo(expected.getExpression2());
    }

    protected PropertyIsEqualTo equalsTo(String property, Object literal) {
        return ff.equals(ff.property(property), ff.literal(literal));
    }
}
