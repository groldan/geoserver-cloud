package org.geoserver.cloud.rest.backuprestore;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.geoserver.platform.resource.NullLockProvider;
import org.geoserver.platform.resource.Paths;
import org.geoserver.platform.resource.Resource;
import org.geoserver.platform.resource.ResourceListener;

@Data
@Accessors(chain = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ResourceDto.DirectoryResource.class),
    @JsonSubTypes.Type(value = ResourceDto.FileResource.class)
})
public class ResourceDto {

    private String path;
    private Instant lastModified;

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true, exclude = "content")
    @JsonTypeName("Resource")
    public static class FileResource extends ResourceDto {
        private byte[] content;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @JsonTypeName("Directory")
    public static class DirectoryResource extends ResourceDto {}

    public static ResourceDto valueOf(@NonNull Resource resource) {

        return switch (resource.getType()) {
            case RESOURCE -> new FileResource()
                    .setContent(contentsOf(resource))
                    .setPath(resource.path())
                    .setLastModified(Instant.ofEpochMilli(resource.lastmodified()));
            case DIRECTORY -> new DirectoryResource()
                    .setPath(resource.path())
                    .setLastModified(Instant.ofEpochMilli(resource.lastmodified()));
            default -> throw new IllegalArgumentException("Invalid resource " + resource);
        };
    }

    private static byte[] contentsOf(@NonNull Resource resource) {
        try {
            return resource.getContents();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Resource parse(@NonNull ResourceDto dto) {
        return new ResourceDtoAdapter(dto);
    }

    @RequiredArgsConstructor
    private static class ResourceDtoAdapter implements Resource {

        @NonNull
        private final ResourceDto dto;

        @Override
        public String toString() {
            return "Resource(%s)".formatted(dto.path);
        }

        @Override
        public String path() {
            return dto.getPath();
        }

        @Override
        public String name() {
            return Paths.name(path());
        }

        @Override
        public Type getType() {
            return dto instanceof FileResource ? Type.RESOURCE : Type.DIRECTORY;
        }

        @Override
        public long lastmodified() {
            return dto.getLastModified().toEpochMilli();
        }

        @Override
        public Lock lock() {
            return NullLockProvider.instance().acquire(path());
        }

        @Override
        public InputStream in() {
            if (dto instanceof FileResource res) return new ByteArrayInputStream(res.getContent());
            throw new UnsupportedOperationException();
        }

        @Override
        public void addListener(ResourceListener listener) {
            // no-op
        }

        @Override
        public void removeListener(ResourceListener listener) {
            // no-op
        }

        @Override
        public OutputStream out() {
            throw new UnsupportedOperationException();
        }

        @Override
        public File file() {
            throw new UnsupportedOperationException();
        }

        @Override
        public File dir() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Resource parent() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Resource get(String resourcePath) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Resource> list() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean delete() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean renameTo(Resource dest) {
            throw new UnsupportedOperationException();
        }
    }
}
