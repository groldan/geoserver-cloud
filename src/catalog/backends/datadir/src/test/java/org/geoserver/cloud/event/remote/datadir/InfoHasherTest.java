/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.event.remote.datadir;

import static org.assertj.core.api.Assertions.assertThat;

import org.geoserver.catalog.CatalogTestData;
import org.geoserver.catalog.Info;
import org.geotools.api.util.InternationalString;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.GrowableInternationalString;
import org.geotools.util.SimpleInternationalString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

class InfoHasherTest {

    private InfoHasher hasher;
    private CatalogTestData data;

    @BeforeEach
    void setUp() throws Exception {
        hasher = new InfoHasher();
        data = CatalogTestData.empty();
        data.createCatalogObjects();

        data.style2.setWorkspace(data.workspaceB);
        data.layerFeatureTypeA.setInternationalTitle(growableIsSample());
        data.layerFeatureTypeA.setLegend(data.faker().legendInfo());

        data.coverageA.setInternationalTitle(new SimpleInternationalString("simple title"));
        data.coverageA.getDataLinks().add(data.faker().dataLinkInfo());
        data.coverageA.getDataLinks().add(data.faker().dataLinkInfo());

        data.layerGroup1.getStyles().add(data.style1);
        data.layerGroup1.getStyles().add(data.style2);
        data.layerGroup1.getAuthorityURLs().add(data.faker().authorityURLInfo());
        data.layerGroup1.setAttribution(data.faker().attributionInfo());
        data.layerGroup1.setBounds(
                new ReferencedEnvelope(-180, 180, -90, 90, CRS.decode("EPSG:4326")));
    }

    private InternationalString growableIsSample() {
        GrowableInternationalString is =
                data.faker()
                        .internationalString(
                                Locale.ENGLISH, "eng string", Locale.ITALIAN, "it string");
        is.add(null, "default value");
        return is;
    }

    @Test
    void testHash() {
        testConsistency(data.workspaceA);
        testConsistency(data.namespaceA);

        testConsistency(data.dataStoreA);
        testConsistency(data.coverageStoreA);
        testConsistency(data.wmsStoreA);
        testConsistency(data.wmtsStoreA);

        testConsistency(data.featureTypeA);
        testConsistency(data.coverageA);
        testConsistency(data.wmsLayerA);
        testConsistency(data.wmtsLayerA);

        testConsistency(data.layerFeatureTypeA);
        testConsistency(data.layerGroup1);
        testConsistency(data.style1);
        testConsistency(data.style2);
    }

    private void testConsistency(Info info) {
        HashCode h1 = hasher.hash(info);
        HashCode h2 = hasher.hash(info);
        assertThat(h2).isNotNull().isEqualTo(h1);
    }

    @Test
    void testXor() {
        testXor((Info[]) null);
        testXor(new Info[0]);
        data.namespaceA.getMetadata().put("k1", getClass());
        data.namespaceB.getMetadata().put("k2", getClass());
        HashCode expected =
                testXor(
                        data.workspaceA,
                        data.namespaceA,
                        data.dataStoreA,
                        data.coverageStoreA,
                        data.featureTypeA,
                        data.coverageA,
                        data.layerFeatureTypeA,
                        data.layerGroup1,
                        data.style1,
                        data.style2);

        data.namespaceA.getMetadata().clear();
        data.namespaceA.getMetadata().put("k1", getClass());
        data.namespaceB.getMetadata().put("k2", getClass());

        HashCode actual =
                testXor(
                        data.style2,
                        data.style1,
                        data.layerGroup1,
                        data.layerFeatureTypeA,
                        data.coverageA,
                        data.featureTypeA,
                        data.coverageStoreA,
                        data.dataStoreA,
                        data.namespaceA,
                        data.workspaceA);
        assertThat(actual).isEqualTo(expected);
    }

    HashCode testXor(Info... infos) {
        HashCode xor1 = hasher.xor(infos);

        Info[] shuffled;
        if (null == infos) {
            shuffled = infos;
        } else {
            List<Info> list = new ArrayList<>(Arrays.asList(infos));
            Collections.shuffle(list);
            shuffled = list.toArray(Info[]::new);
        }
        HashCode xor2 = hasher.xor(shuffled);

        assertThat(xor2).isNotNull().isEqualTo(xor1);

        if (null != infos && infos.length > 0) {
            HashCode xor = hasher.hash(infos[0]);
            for (int i = 1; i < infos.length; i++) {
                xor = xor.xor(hasher.hash(infos[i]));
            }
            assertThat(xor).isEqualTo(xor1);
            assertThat(xor).hasToString(xor1.toString());
        }
        return xor1;
    }
}
