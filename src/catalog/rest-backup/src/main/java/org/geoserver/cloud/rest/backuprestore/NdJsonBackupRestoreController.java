package org.geoserver.cloud.rest.backuprestore;

import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.cloud.backuprestore.BackupRestoreService;
import org.geoserver.cloud.backuprestore.BackupSummary;
import org.geoserver.config.GeoServerInfo;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * <p>
 * <ul>
 * <li>{@code GET /rest/catalog}: {@link #backup back up} the catalog and
 * configuration
 * <li>{@code POST /rest/catalog}: {@link #restore restore} a backup into an
 * empty catalog
 * <li>{@code PUT /rest/catalog}: {@link #ingest ingest} (import) a backup,
 * overriding existing objects
 * <li>{@code DELETE /rest/catalog}:{@link #prune prune} the whole Catalog and
 * Configuration</li>
 */
@RestController
@RequestMapping(path = "/rest/catalog")
@RequiredArgsConstructor
@Slf4j
public class NdJsonBackupRestoreController {

    @NonNull
    private final BackupRestoreService service;

    /**
     *
     * @return
     */
    @GetMapping(produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Object> backup() {

        return Flux.fromStream(service::backup);
        //
        //        Supplier<Stream<? extends Object>> concatStreams = concatStreams();
        //        BackupSummary summary = new BackupSummary();
        //        Flux<Object> data = Flux.fromStream(concatStreams).doOnNext(summary::add);
        //
        //        BackupHeader header = new BackupHeader().withUser(currentUsername());
        //
        //        return Flux.<Object>just(header).concatWith(data.concatWith(Flux.just(summary)));
    }

    /**
     * Restores a {@link #backup()} stream. The catalog and configuration objects
     * should be empty, or at least non conflicting with any object to be consumed.
     *
     * @param data
     */
    @PostMapping(consumes = MediaType.APPLICATION_NDJSON_VALUE)
    public void restore(@RequestBody Stream<Object> data) {
        log.info("processing request body");
        service.restore(data);
        log.info("finished processing request body");
    }

    /**
     * Imports a {@link #backup()} stream, overriding existing objects
     * <p>
     *
     * @apiNote the {@link GeoServerInfo global configuration} will be replaced by
     *          the one from the data stream, if any, but the
     *          {@link GeoServerInfo#getUpdateSequence() update sequence} will be
     *          preserved if it's value is greater than the value from the incoming
     *          {@link GeoServerInfo} object.
     * @param data
     */
    @PutMapping(consumes = MediaType.APPLICATION_NDJSON_VALUE)
    public void ingest(@RequestBody Stream<Object> data) {
        log.info("processing request body");
        service.ingest(data);
        log.info("finished processing request body");
    }

    @DeleteMapping
    public BackupSummary prune() {
        return service.prune();
    }
}
