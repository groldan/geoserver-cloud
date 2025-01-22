package org.geoserver.cloud.rest.backuprestore;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.platform.resource.Resource;

@Data
@JsonTypeName("CatalogSummary")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public class Summary {

    private ResourceStore resourceStore = new ResourceStore();
    private Catalog catalog = new Catalog();

    @Data
    public static class ResourceStore {
        private AtomicLong resources = new AtomicLong();
    }

    @Data
    public static class Catalog {
        private AtomicLong namespaces = new AtomicLong();
        private AtomicLong workspaces = new AtomicLong();
        private AtomicLong stores = new AtomicLong();
        private AtomicLong resources = new AtomicLong();
        private AtomicLong styles = new AtomicLong();
        private AtomicLong layers = new AtomicLong();
        private AtomicLong layerGroups = new AtomicLong();
    }

    public void add(Object o) {
        if (o instanceof Resource) resourceStore.resources.incrementAndGet();
        else if (o instanceof NamespaceInfo) catalog.namespaces.incrementAndGet();
        else if (o instanceof WorkspaceInfo) catalog.workspaces.incrementAndGet();
        else if (o instanceof StoreInfo) catalog.stores.incrementAndGet();
        else if (o instanceof ResourceInfo) catalog.resources.incrementAndGet();
        else if (o instanceof StyleInfo) catalog.styles.incrementAndGet();
        else if (o instanceof LayerInfo) catalog.layers.incrementAndGet();
        else if (o instanceof LayerGroupInfo) catalog.layerGroups.incrementAndGet();
    }
}
