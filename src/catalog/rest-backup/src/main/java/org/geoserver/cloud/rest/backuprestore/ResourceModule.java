package org.geoserver.cloud.rest.backuprestore;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.geoserver.cloud.backuprestore.BackupHeader;
import org.geoserver.cloud.backuprestore.BackupSummary;
import org.geoserver.platform.resource.Resource;
import org.geotools.jackson.databind.util.MapperDeserializer;
import org.geotools.jackson.databind.util.MapperSerializer;

@SuppressWarnings("serial")
class ResourceModule extends SimpleModule {

    ResourceModule() {
        super("geoserver-resource-module");

        addSerializer(resourceSerializer());
        addDeserializer(Resource.class, resourceDeserializer());

        super.registerSubtypes(BackupHeader.class, BackupSummary.class);
    }

    private JsonSerializer<Resource> resourceSerializer() {
        return new MapperSerializer<>(Resource.class, ResourceDto::valueOf);
    }

    private JsonDeserializer<Resource> resourceDeserializer() {
        return new MapperDeserializer<>(ResourceDto.class, ResourceDto::parse);
    }
}
