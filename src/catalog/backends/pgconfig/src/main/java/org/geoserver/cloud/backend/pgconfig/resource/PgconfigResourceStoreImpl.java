/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.cloud.backend.pgconfig.resource;

import static org.geoserver.platform.resource.ResourceNotification.Kind.ENTRY_DELETE;
import static org.geoserver.platform.resource.SimpleResourceNotificationDispatcher.createEvents;
import static org.geoserver.platform.resource.SimpleResourceNotificationDispatcher.createRenameEvents;

import com.google.common.base.Preconditions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.platform.resource.LockProvider;
import org.geoserver.platform.resource.Paths;
import org.geoserver.platform.resource.Resource;
import org.geoserver.platform.resource.Resource.Type;
import org.geoserver.platform.resource.ResourceListener;
import org.geoserver.platform.resource.ResourceNotification;
import org.geoserver.platform.resource.ResourceNotification.Kind;
import org.geoserver.platform.resource.ResourceNotificationDispatcher;
import org.geoserver.platform.resource.SimpleResourceNotificationDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @since 1.4
 */
@Slf4j
public class PgconfigResourceStoreImpl implements PgconfigResourceStore {

    private final ResourceNotificationDispatcher dispatcher = new SimpleResourceNotificationDispatcher();

    private final JdbcTemplate template;
    private final FileSystemResourceStoreCache cache;

    private @NonNull @Getter @Setter LockProvider lockProvider;

    private final Predicate<String> fileSystemOnlyPathMatcher;
    private final PgconfigResourceRowMapper queryMapper;

    private PgconfigResourceStore transactionalReference;

    public PgconfigResourceStoreImpl(
            @NonNull FileSystemResourceStoreCache cache,
            @NonNull JdbcTemplate template,
            @NonNull PgconfigLockProvider lockProvider,
            @NonNull Predicate<String> fileSystemOnlyPathMatcher) {
        this.template = template;
        this.lockProvider = lockProvider;
        this.cache = cache;
        final String root = "";
        Predicate<String> notRoot = path -> !root.equals(path);
        this.fileSystemOnlyPathMatcher = notRoot.and(fileSystemOnlyPathMatcher);
        this.transactionalReference = this;
        this.queryMapper = new PgconfigResourceRowMapper(this);
    }

    @Lazy
    @Autowired
    void setTransactionalReference(@NonNull PgconfigResourceStore transactionalReference) {
        this.transactionalReference = transactionalReference;
        this.queryMapper.setStore(transactionalReference);
    }

    /*
     * ///////////////////////////////////////
     * /// ResourceStore methods ///
     * ///////////////////////////////////////
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceNotificationDispatcher getResourceNotificationDispatcher() {
        return dispatcher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource get(@NonNull String path) {
        final String validPath = normalize(path);
        if (fileSystemOnlyPathMatcher.test(validPath)) {
            Resource fsResource = cache.getLocalOnlyStore().get(validPath);
            return new FileSystemResourceAdaptor(fsResource, transactionalReference);
        }
        return findByPath(validPath).orElseGet(() -> queryMapper.undefined(validPath));
    }

    @Override
    public boolean remove(@NonNull String path) {

        String validPath = normalize(path);
        if (fileSystemOnlyPathMatcher.test(validPath)) {
            return cache.getLocalOnlyStore().remove(validPath);
        }
        return findByPath(validPath).map(PgconfigResource::delete).orElse(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean move(@NonNull String path, @NonNull String target) {
        Resource from = get(path);
        Resource to = get(target);
        if (from instanceof PgconfigResource pgFrom && to instanceof PgconfigResource pgTo) {
            return transactionalReference.move(pgFrom, pgTo);
        }
        if (from instanceof PgconfigResource) {
            throw new UnsupportedOperationException(
                    "source resource targets database but target resource matches the ignored resources predicate. Source: %s, target: %s"
                            .formatted(path, target));
        }
        if (to instanceof PgconfigResource) {
            throw new UnsupportedOperationException(
                    "target resource targets database but source resource matches the ignored resources predicate. Source: %s, target: %s"
                            .formatted(path, target));
        }
        return cache.getLocalOnlyStore().move(path, target);
    }

    /*
     * ///////////////////////////////////////
     * /// Utility methods ///
     * ///////////////////////////////////////
     */

    /**
     * Returns a filter that matches the directories defined in the {@link #defaultIgnoredDirs()} filter, plus the following resources:
     * <ul>
     * <li>{@literal security/role/default/roles.xml.lock}:
     * {@code org.geoserver.security.xml.XMLRoleStore}'s lock file, uses a shutdown
     * hook through an {@code org.geoserver.security.file.LockFile} which doesn't follow standard locking mechanisms and causes exceptions since the jdbc datasource is alredy closed
     * <li>{@literal security/usergroup/default/users.xml.lock}:
     * {@code org.geoserver.security.xml.XMLUserGroupStore}'s lock file, uses a
     * shutdown hook through an {@code org.geoserver.security.file.LockFile} which doesn't follow standard locking mechanisms and causes exceptions since the jdbc datasource is alredy closed
     * </ul>
     *
     * @return
     */
    public static Predicate<String> defaultIgnoredResources() {
        final Set<String> ignoredResources =
                Set.of("security/role/default/roles.xml.lock", "security/usergroup/default/users.xml.lock");
        return defaultIgnoredDirs().or(ignoredResources::contains);
    }

    public static Predicate<String> defaultIgnoredDirs() {
        return PgconfigResourceStoreImpl.simplePathMatcher("temp", "tmp", "legendsamples", "logs", "data");
    }

    public static Predicate<String> simplePathMatcher(String... paths) {
        Predicate<String> matcher = path -> false;
        for (String path : paths) {
            path = normalize(path);
            matcher = matcher.or(path::equals);
            @SuppressWarnings("java:S1075")
            final String dirpath = path + "/";
            matcher = matcher.or(r -> r.startsWith(dirpath));
        }
        return matcher;
    }

    @RequiredArgsConstructor
    static class FileSystemResourceAdaptor implements Resource {
        @Delegate
        @NonNull
        private final Resource delegate;

        private final @NonNull PgconfigResourceStore store;

        @Override
        public Resource parent() {
            String parentPath = Paths.parent(this.path());
            return store.get(parentPath);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FileSystemResourceAdaptor fra
                    && Objects.equals(path(), fra.path())
                    && Objects.equals(getType(), fra.getType());
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }
    }

    /**
     * Removes leading and trailing backslashes from {@code path}
     */
    static String normalize(String path) {
        path = Paths.valid(path);
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @Override
    public Optional<PgconfigResource> findByPath(@NonNull String path) {
        path = Paths.valid(path);
        Preconditions.checkArgument(!path.startsWith("/"), "Absolute paths not supported: %s", path);
        try {
            return Optional.of(template.queryForObject(
                    """
                    SELECT id, parentid, "type", path, mtime FROM resourcestore WHERE path = ?
                    """,
                    queryMapper,
                    path));
        } catch (EmptyResultDataAccessException empty) {
            return Optional.empty();
        }
    }

    /*
     * ///////////////////////////////////////////////////////////////////////////
     * /// PgconfigResource support methods to centralize logic in the store ///
     * ///////////////////////////////////////////////////////////////////////////
     */

    /**
     * Support method for {@link PgconfigResource#addListener(ResourceListener)}
     */
    @Override
    public void addListener(String path, ResourceListener listener) {
        dispatcher.addListener(path, listener);
    }

    /**
     * Support method for {@link PgconfigResource#removeListener(ResourceListener)}
     */
    @Override
    public void removeListener(String path, ResourceListener listener) {
        dispatcher.removeListener(path, listener);
    }

    /**
     * Support method for {@link PgconfigResource#get(String)}
     */
    @Override
    public Resource getChild(PgconfigResource parent, @NonNull String childPath) {
        if ("".equals(childPath)) {
            return parent;
        }
        String resourcePath = Paths.path(parent.path(), childPath);
        return get(resourcePath);
    }

    /**
     * Support method for {@link PgconfigResource#parent()}
     */
    @Override
    public PgconfigResource getParent(PgconfigResource resource) {
        if (PgconfigResourceStore.ROOT_ID == resource.getId()) {
            return null;
        }
        String parentPath = resource.parentPath();
        return (PgconfigResource) get(parentPath);
    }

    /**
     * Support method for {@link PgconfigResource#out()}
     */
    @Override
    public OutputStream out(PgconfigResource resource) {
        if (resource.isDirectory()) {
            throw new IllegalStateException("%s is a directory".formatted(resource.path()));
        }
        if (resource.isUndefined()) {
            resource.type = Type.RESOURCE;
        }
        return new ByteArrayOutputStream() {
            @Override
            public void close() {
                ResourceNotification.Kind eventKind;
                if (resource.exists()) {
                    eventKind = Kind.ENTRY_MODIFY;
                } else {
                    eventKind = Kind.ENTRY_CREATE;
                    String path = resource.path();
                    transactionalReference.save(resource);
                    PgconfigResource saved = findByPath(path).orElseThrow();
                    resource.copy(saved);
                }
                byte[] contents = this.toByteArray();
                long mtime = transactionalReference.save(resource, contents);
                resource.lastmodified = mtime;
                cache.dump(resource, new ByteArrayInputStream(contents));

                ResourceNotification.Event event = new ResourceNotification.Event(resource.path(), eventKind);
                publish(resource, eventKind, List.of(event));
            }
        };
    }

    /**
     * Creates the resource if it doesn't exist, updates it if it does
     *
     * @throws IllegalArgumentException if {@link PgconfigResource#isUndefined()}
     */
    @Override
    public void save(@NonNull PgconfigResource resource) {
        if (resource.exists()) {
            String sql =
                    """
                    UPDATE resourcestore SET parentid = ?, "type" = ?, path = ?
                    WHERE id = ?;
                    """;
            long id = resource.getId();
            long parentId = resource.getParentId();
            String type = resource.getType().toString();
            String path = resource.path();
            template.update(sql, parentId, type, path, id);
        } else {
            // not calling resource.isUndefined() to avoid querying the getType() -> updateState()
            final Type type = resource.type;
            if (type == Type.UNDEFINED) {
                throw new IllegalArgumentException(
                        "Attempting to save a resource of undefined type: %s".formatted(resource));
            }
            PgconfigResource parent = resource.parent().mkdirs();
            String sql =
                    """
                    INSERT INTO resourcestore (parentid, "type", path, content)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT (parentid, path)
                    DO UPDATE SET
                        "type" = EXCLUDED."type",
                        content = EXCLUDED.content;
                    """;

            long parentId = parent.getId();
            String path = resource.path();
            byte[] contents = type == Type.DIRECTORY ? null : new byte[0];

            template.update(sql, parentId, type.toString(), path, contents);
        }
        PgconfigResource updated = (PgconfigResource) get(resource.path);
        resource.id = updated.getId();
        resource.lastmodified = updated.lastmodified();
        resource.parentId = updated.getParentId();
    }

    /**
     * Saves the contents of the given resource
     *
     * @return the new resource lastupdated timestamp
     * @throws IllegalArgumentException if <code>
     *      {@link PgconfigResource#isDirectory() resource.isDirectory()} || !{@link
     *     PgconfigResource#exists() resource.exists()}</code>
     */
    @Override
    public long save(@NonNull PgconfigResource resource, byte[] contents) {
        if (!resource.exists()) {
            throw new IllegalArgumentException("Resource does not exist: %s".formatted(resource.path()));
        }
        if (!resource.isFile()) {
            throw new IllegalArgumentException(
                    "Resource is a directory, can't have contents: %s".formatted(resource.path()));
        }
        if (null == contents) {
            contents = new byte[0];
        }
        template.update(
                """
                UPDATE resourcestore SET content = ? WHERE id = ?
                """,
                contents,
                resource.getId());
        return getLastmodified(resource.getId());
    }

    private long getLastmodified(long resourceId) {
        Timestamp ts =
                template.queryForObject("SELECT mtime FROM resourcestore WHERE id = ?", Timestamp.class, resourceId);
        return null == ts ? 0L : ts.getTime();
    }

    /**
     * Updates the state of a resource from the database.
     *
     * <p>
     * This method is crucial for maintaining consistency of long-lived resource
     * references. It queries the database for the current state of a resource and
     * updates the provided resource instance with the latest information.
     * </p>
     *
     * <p>
     * It's particularly important for components like AbstractAccessRuleDAO and
     * RESTAccessRuleDAO that hold resource references as instance variables. These
     * references can become stale when the underlying database record is modified
     * by another process or service instance.
     * </p>
     *
     * <p>
     * If the resource no longer exists in the database, both its type is set to
     * UNDEFINED and its id is set to UNDEFINED_ID to ensure consistent state.
     * </p>
     *
     * @param resource the resource to update
     * @see PgconfigResource#updateState()
     */
    @Override
    public void updateState(PgconfigResource resource) {
        Optional<PgconfigResource> indb = findByPath(resource.path());
        indb.ifPresentOrElse(
                // Resource found in database - copy all properties
                resource::copy,
                // Resource not found in database - mark as undefined
                () -> {
                    resource.type = Type.UNDEFINED;
                    resource.id = PgconfigResourceStore.UNDEFINED_ID;
                    resource.parentId = PgconfigResourceStore.UNDEFINED_ID;
                    // lastmodified intentionally not updated to avoid inconsistency with
                    // ResourceNotificationDispatcher
                });
    }

    @Override
    public boolean move(@NonNull final PgconfigResource source, @NonNull final PgconfigResource target) {
        if (source.isUndefined()) {
            return true;
        }
        if (!source.exists()) {
            return false;
        }
        if (source.path().equals(target.path())) {
            return true;
        }
        final String parentPath = target.parentPath();
        if (null != parentPath && parentPath.contains(source.path())) {
            log.warn("Cannot rename a resource to a descendant of itself ({} to {})", source.path(), target.path());
            return false;
        }

        final List<ResourceNotification.Event> eventsDelete;
        final List<ResourceNotification.Event> eventsRename;
        eventsDelete = createEvents(source, ENTRY_DELETE);
        eventsRename = createRenameEvents(source, target);

        ResourceNotification.Kind targetEvent = doMove(source, target);

        publish(source, ENTRY_DELETE, eventsDelete);
        publish(source, targetEvent, eventsRename);

        return true;
    }

    private Kind doMove(final PgconfigResource source, final PgconfigResource target) {
        final List<PgconfigResource> allChildren = findAllChildren(source);
        PgconfigResource parent = target.parent().mkdirs();
        PgconfigResource renamed = new PgconfigResource(
                transactionalReference,
                source.getId(),
                parent.getId(),
                source.getType(),
                target.path(),
                source.lastmodified());

        ResourceNotification.Kind targetEvent = Kind.ENTRY_CREATE;
        if (target.exists()) {
            deleteQuietly(target);
            targetEvent = Kind.ENTRY_MODIFY;
        }

        transactionalReference.save(renamed);
        target.copy(renamed);
        source.type = Type.UNDEFINED;

        final String oldParentPath = source.path();
        final String newParehtPath = target.path();
        for (var child : allChildren) {
            String oldPath = child.path().substring(oldParentPath.length());
            String newPath = newParehtPath + oldPath;
            String sql = "UPDATE resourcestore SET path = ? WHERE id = ?;";
            long id = child.getId();
            template.update(sql, newPath, id);
        }

        cache.moved(source, target);
        return targetEvent;
    }

    List<PgconfigResource> findAllChildren(PgconfigResource resource) {
        if (!resource.exists() || !resource.isDirectory()) {
            return List.of();
        }
        String sql =
                """
                SELECT id, parentid, "type", path, mtime FROM resourcestore WHERE path LIKE ?
                """;

        String likeQuery = resource.path() + "/%";
        try (Stream<PgconfigResource> s = template.queryForStream(sql, queryMapper, likeQuery)) {
            return s.toList();
        }
    }

    /**
     * Support method for {@link PgconfigResource#in()}
     */
    @Override
    public byte[] contents(PgconfigResource resource) {
        if (!resource.exists() || resource.isUndefined()) {
            throw new IllegalStateException("File not found %s".formatted(resource.path()));
        }
        if (resource.isDirectory()) {
            throw new IllegalStateException("%s is a directory".formatted(resource.path()));
        }

        long id = resource.getId();
        return template.queryForObject(
                """
                SELECT content FROM resourcestore WHERE id = ?
                """,
                byte[].class,
                id);
    }

    /**
     * Support method for {@link PgconfigResource#delete()}
     */
    @Override
    public boolean delete(PgconfigResource resource) {

        List<ResourceNotification.Event> events = createEvents(resource, ENTRY_DELETE);
        final boolean deleted = deleteQuietly(resource);
        if (deleted) {
            resource.type = Type.UNDEFINED;
            publish(resource, ENTRY_DELETE, events);
        }
        return deleted;
    }

    /**
     * Deletes the resource and all its children without issuing events
     */
    private boolean deleteQuietly(PgconfigResource resource) {
        final String sql = "DELETE FROM resourcestore WHERE id = ?";
        final int deleteCount = template.update(sql, resource.getId());
        final boolean deleted = deleteCount > 0;
        return deleted;
    }

    /**
     * Support method for {@link PgconfigResource#list()}
     *
     * @return direct children of resource if resource is a directory, empty list
     *         otherwise
     */
    @Override
    public List<Resource> list(PgconfigResource resource) {
        if (!resource.exists() || !resource.isDirectory()) {
            return List.of();
        }

        final String sql =
                """
                SELECT id, parentid, "type", path, mtime FROM resourcestore WHERE parentid = ?
                """;

        List<Resource> list;
        try (Stream<PgconfigResource> s = template.queryForStream(sql, queryMapper, resource.getId())) {
            // for pre 1.8.1 backwards compatibility, ignore resources that are only to be
            // stored in the filesystem (e.g. tmp/, temp/, etc)
            Stream<PgconfigResource> resources = s.filter(r -> !fileSystemOnlyPathMatcher.test(r.path()));
            list = resources.map(Resource.class::cast).toList();
        }
        cache.updateAll(list);
        return list;
    }

    /**
     * @return
     */
    @Override
    public File asFile(PgconfigResource resource) {
        if (!resource.exists()) {
            resource.type = Type.RESOURCE;
            transactionalReference.save(resource);
            publish(resource, Kind.ENTRY_CREATE, createEvents(resource, Kind.ENTRY_CREATE));
        }
        return cache.getFile(resource);
    }

    /**
     * @param resource
     * @return
     */
    @Override
    public File asDir(PgconfigResource resource) {
        if (!resource.exists()) {
            resource.type = Type.DIRECTORY;
            transactionalReference.save(resource);
            publish(resource, Kind.ENTRY_CREATE, createEvents(resource, Kind.ENTRY_CREATE));
        }
        return cache.getDirectory(resource);
    }

    @Override
    public PgconfigResource mkdirs(PgconfigResource resource) {
        if (resource.exists() && resource.isDirectory()) {
            return resource;
        }
        if (resource.isFile()) {
            throw new IllegalStateException("mkdirs() can only be called on DIRECTORY or UNDEFINED resources");
        }
        PgconfigResource parent = getParent(resource);
        if (null == parent) {
            return resource;
        }
        if (!parent.exists()) {
            parent = parent.mkdirs();
        }
        resource.parentId = parent.getId();
        resource.type = Type.DIRECTORY;
        transactionalReference.save(resource);
        PgconfigResource saved = (PgconfigResource) get(resource.path());
        resource.copy(saved);
        return resource;
    }

    private void publish(
            PgconfigResource resource, ResourceNotification.Kind kind, List<ResourceNotification.Event> events) {
        if (!events.isEmpty()) {
            String path = resource.path();
            long timestamp = System.currentTimeMillis();
            ResourceNotification notification = new ResourceNotification(path, kind, timestamp, events);
            System.err.println(notification);
            dispatcher.changed(notification);
        }
    }
}
