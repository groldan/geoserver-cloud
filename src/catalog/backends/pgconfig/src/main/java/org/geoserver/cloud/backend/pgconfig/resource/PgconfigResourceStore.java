package org.geoserver.cloud.backend.pgconfig.resource;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import org.geoserver.platform.resource.Resource;
import org.geoserver.platform.resource.ResourceListener;
import org.geoserver.platform.resource.ResourceStore;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(transactionManager = "pgconfigTransactionManager", propagation = SUPPORTS)
public interface PgconfigResourceStore extends ResourceStore {

    long ROOT_ID = 0L;
    long UNDEFINED_ID = -1L;

    @Override
    @Transactional(transactionManager = "pgconfigTransactionManager", propagation = REQUIRED)
    boolean remove(String path);

    @Override
    @Transactional(transactionManager = "pgconfigTransactionManager", propagation = REQUIRED)
    boolean move(String path, String target);

    /**
     * Creates the resource if it doesn't exist, updates it if it does
     *
     * @throws IllegalArgumentException if {@link PgconfigResource#isUndefined()}
     */
    @Transactional(transactionManager = "pgconfigTransactionManager", propagation = REQUIRED)
    void save(PgconfigResource resource);

    /**
     * Saves the contents of the given resource
     *
     * @return the new resource lastupdated timestamp
     * @throws IllegalArgumentException if <code>
     *      {@link PgconfigResource#isDirectory() resource.isDirectory()} || !{@link
     *     PgconfigResource#exists() resource.exists()}</code>
     */
    @Transactional(transactionManager = "pgconfigTransactionManager", propagation = REQUIRED)
    long save(PgconfigResource resource, byte[] contents);

    /**
     * Support method for {@link PgconfigResource#renameTo(Resource)}
     */
    @Transactional(transactionManager = "pgconfigTransactionManager", propagation = REQUIRED)
    boolean move(PgconfigResource source, PgconfigResource target);

    /**
     * Support method for {@link PgconfigResource#mkdirs()}
     */
    @Transactional(transactionManager = "pgconfigTransactionManager", propagation = REQUIRED)
    PgconfigResource mkdirs(PgconfigResource resource);

    /**
     * Support method for {@link PgconfigResource#delete()}
     */
    @Transactional(
            transactionManager = "pgconfigTransactionManager",
            propagation = REQUIRED,
            isolation = Isolation.SERIALIZABLE)
    boolean delete(PgconfigResource resource);

    Optional<PgconfigResource> findByPath(String path);

    /**
     * Support method for {@link PgconfigResource#addListener(ResourceListener)}
     */
    void addListener(String path, ResourceListener listener);

    /**
     * Support method for {@link PgconfigResource#removeListener(ResourceListener)}
     */
    void removeListener(String path, ResourceListener listener);

    /**
     * Support method for {@link PgconfigResource#get(String)}
     */
    Resource getChild(PgconfigResource parent, String childPath);

    /**
     * Support method for {@link PgconfigResource#parent()}
     */
    PgconfigResource getParent(PgconfigResource resource);

    /**
     * Support method for {@link PgconfigResource#out()}
     */
    OutputStream out(PgconfigResource resource);

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
    void updateState(PgconfigResource resource);

    /**
     * Support method for {@link PgconfigResource#in()}
     */
    byte[] contents(PgconfigResource resource);

    /**
     * Support method for {@link PgconfigResource#list()}
     *
     * @return direct children of resource if resource is a directory, empty list
     *         otherwise
     */
    List<Resource> list(PgconfigResource resource);

    /**
     * Support method for {@link PgconfigResource#file()}
     */
    File asFile(PgconfigResource resource);

    /**
     * Support method for {@link PgconfigResource#dir()}
     */
    File asDir(PgconfigResource resource);
}
