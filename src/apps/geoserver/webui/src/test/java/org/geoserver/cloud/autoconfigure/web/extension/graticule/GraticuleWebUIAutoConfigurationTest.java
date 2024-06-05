/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.web.extension.graticule;

import static org.assertj.core.api.Assertions.assertThat;

import org.geoserver.web.data.resource.DataStorePanelInfo;
import org.geoserver.web.data.store.graticule.GraticuleStoreEditPanel;
import org.geotools.data.graticule.GraticuleDataStoreFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Test suite for {@link GraticuleConfiguration}
 *
 * @since 1.8
 */
class GraticuleWebUIAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(GraticuleWebUIAutoConfiguration.class));

    @Test
    void testConditionalOnGraticuleWebUI_enabled() {

        contextRunner.run(
                context -> {
                    assertThat(context)
                            .hasBean("graticuleStorePanel")
                            .getBean("graticuleStorePanel")
                            .isInstanceOf(DataStorePanelInfo.class);

                    assertThat(context.getBean("graticuleStorePanel", DataStorePanelInfo.class))
                            .hasFieldOrPropertyWithValue(
                                    "factoryClass", GraticuleDataStoreFactory.class);

                    assertThat(context.getBean("graticuleStorePanel", DataStorePanelInfo.class))
                            .hasFieldOrPropertyWithValue(
                                    "componentClass", GraticuleStoreEditPanel.class);
                });
    }
}
