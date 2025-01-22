package org.geoserver.cloud.rest.backuprestore;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.IOException;
import java.io.UncheckedIOException;
import lombok.Data;
import org.geoserver.platform.resource.Resource;

@Data
@JsonTypeName("Resource")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public class ResourceDto {
    private String path;
    private byte[] content;

    public static ResourceDto valueOf(Resource resource) {
        if (Resource.Type.RESOURCE != resource.getType()) {
            throw new IllegalArgumentException(
                    "Only RESOURCE are allowed, got %s: %s".formatted(resource.path(), resource.getType()));
        }

        ResourceDto dto = new ResourceDto();
        dto.setPath(resource.path());
        try {
            dto.setContent(resource.getContents());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return dto;
    }
}
