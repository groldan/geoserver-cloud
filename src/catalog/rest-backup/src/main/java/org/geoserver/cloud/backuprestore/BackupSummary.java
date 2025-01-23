package org.geoserver.cloud.backuprestore;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;
import lombok.NonNull;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.Info;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.LoggingInfo;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.SettingsInfo;
import org.geoserver.platform.resource.Resource;
import org.geoserver.platform.resource.Resource.Type;

@Data
@JsonTypeName("GeoServerBackupSummary")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public class BackupSummary {

    private ResourceStoreSummary resourceStore = new ResourceStoreSummary();
    private CatalogSummary catalog = new CatalogSummary();
    private ConfigSummary config = new ConfigSummary();

    @Data
    public static class ResourceStoreSummary {
        private AtomicLong files = new AtomicLong();
    }

    @Data
    public static class CatalogSummary {
        private AtomicLong namespaces = new AtomicLong();
        private AtomicLong workspaces = new AtomicLong();
        private AtomicLong stores = new AtomicLong();
        private AtomicLong resources = new AtomicLong();
        private AtomicLong styles = new AtomicLong();
        private AtomicLong layers = new AtomicLong();
        private AtomicLong layerGroups = new AtomicLong();
    }

    @Data
    public static class ConfigSummary {
        private AtomicBoolean global = new AtomicBoolean();
        private AtomicBoolean logging = new AtomicBoolean();
        private AtomicLong services = new AtomicLong();
        private AtomicLong settings = new AtomicLong();
    }

    @Data
    public static class TilingSummary {
        private AtomicLong layers = new AtomicLong();
    }

    public void add(@NonNull Object o) {
        if (o instanceof Resource r) {
            addResourceStoreItem(r);
        } else if (o instanceof CatalogInfo c) {
            addCatalogInfo(c);
        } else if (o instanceof Info c) {
            addConfigInfo(c);
        } else
            throw new IllegalArgumentException(
                    "Unexpected object type " + o.getClass().getCanonicalName() + ": " + o);
    }

    private void addResourceStoreItem(@NonNull Resource r) {
        Resource.Type type = r.getType();
        if (type != Type.RESOURCE) {
            throw new IllegalArgumentException("Invalid resource type " + type + ": " + r.path());
        }
        resourceStore.files.incrementAndGet();
    }

    private void addConfigInfo(@NonNull Info o) {
        if (o instanceof GeoServerInfo) {
            config.global.set(true);
        } else if (o instanceof LoggingInfo) {
            config.logging.set(true);
        } else if (o instanceof ServiceInfo) {
            config.services.incrementAndGet();
        } else if (o instanceof SettingsInfo) {
            config.settings.incrementAndGet();
        } else {
            throw new IllegalArgumentException(
                    "Unexpected config Info type " + o.getClass().getCanonicalName() + ": " + o);
        }
    }

    private void addCatalogInfo(@NonNull CatalogInfo o) {
        if (o instanceof NamespaceInfo) catalog.namespaces.incrementAndGet();
        else if (o instanceof WorkspaceInfo) catalog.workspaces.incrementAndGet();
        else if (o instanceof StoreInfo) catalog.stores.incrementAndGet();
        else if (o instanceof ResourceInfo) catalog.resources.incrementAndGet();
        else if (o instanceof StyleInfo) catalog.styles.incrementAndGet();
        else if (o instanceof LayerInfo) catalog.layers.incrementAndGet();
        else if (o instanceof LayerGroupInfo) catalog.layerGroups.incrementAndGet();
        else
            throw new IllegalArgumentException(
                    "Unexpected CatalogInfo type " + o.getClass().getCanonicalName() + ": " + o);
    }
}
