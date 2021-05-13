/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.Getter;
import org.geoserver.catalog.WorkspaceInfo;
import org.junit.Test;

/** */
public abstract class WorkspaceRepositoryConformanceTest
        extends CatalogInfoRepositoryConformanceTest<WorkspaceInfo, WorkspaceRepository> {

    private final @Getter Class<WorkspaceInfo> infoType = WorkspaceInfo.class;

    protected @Override WorkspaceInfo createOne(String name) {
        return testData.createWorkspace(name);
    }

    /*
     * WorkspaceRepository-specific methods tests
     */

    public @Test void unsetDefaultWorkspace() {
        assertFalse(repository.getDefaultWorkspace().isPresent());
        repository.unsetDefaultWorkspace();
        assertFalse(repository.getDefaultWorkspace().isPresent());

        WorkspaceInfo ws = testData.workspaceA;
        repository.add(ws);
        repository.setDefaultWorkspace(ws);
        assertEquals(ws, repository.getDefaultWorkspace().orElse(null));

        repository.unsetDefaultWorkspace();
        assertFalse(repository.getDefaultWorkspace().isPresent());
    }

    @Test(expected = NoSuchElementException.class)
    public void setDefaultWorkspace_nonExistentWorkspace_NoSuchElementException() {
        WorkspaceInfo ws1 = testData.workspaceA;
        repository.setDefaultWorkspace(ws1);
    }

    public @Test void setDefaultWorkspace() {
        assertFalse(repository.getDefaultWorkspace().isPresent());

        WorkspaceInfo ws1 = testData.workspaceA;
        WorkspaceInfo ws2 = testData.workspaceB;
        repository.add(ws1);
        repository.add(ws2);
        assertFalse(repository.getDefaultWorkspace().isPresent());

        repository.setDefaultWorkspace(ws1);
        assertEquals(ws1, repository.getDefaultWorkspace().orElse(null));

        repository.setDefaultWorkspace(ws2);
        assertEquals(ws2, repository.getDefaultWorkspace().orElse(null));
    }

    public @Test void getDefaultWorkspace() {
        Optional<WorkspaceInfo> defaultWorkspace = repository.getDefaultWorkspace();
        assertNotNull(defaultWorkspace);
        assertFalse(defaultWorkspace.isPresent());
        WorkspaceInfo ws1 = testData.workspaceA;
        repository.add(ws1);
        repository.setDefaultWorkspace(ws1);
        assertEquals(ws1, repository.getDefaultWorkspace().orElse(null));
    }
}
