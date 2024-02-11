/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.event.remote.datadir;

import com.google.common.base.Stopwatch;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.geoserver.catalog.Info;
import org.geoserver.catalog.plugin.ExtendedCatalogFacade;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "org.geoserver.cloud.event.remote.datadir")
public class EventualConsistencyHashConvergence {

    private HashCode convergedHash = new HashCode();
    private ReentrantLock hashLock = new ReentrantLock();
    private long convergedObjectCount;

    public void converge(@NonNull ExtendedCatalogFacade facade) {
        log.info("Converging eventually consistent catalog state...");
        convergedObjectCount = 0L;
        Stopwatch sw = Stopwatch.createStarted();
        hashLock.lock();
        try {
            convergedHash = new HashCode();
            facade.forEach(this::converge);
            report(sw.stop());
        } finally {
            hashLock.unlock();
        }
    }

    private void converge(@NonNull Info info) {
        convergedHash = convergedHash.xor(info);
        ++convergedObjectCount;
    }

    private void report(Stopwatch sw) {
        log.info(
                "converged eventually consistent catatlog. Objects: %,2d, hash: %s, time: %s."
                        .formatted(convergedObjectCount, convergedHash, sw));
    }
}
