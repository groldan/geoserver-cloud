/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.tiling.service;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.gwc.tiling.model.CacheJobInfo;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Mutable state tracker for a {@link CacheJobInfo job}
 *
 * @since 1.0
 */
@RequiredArgsConstructor
public class CacheJobStatus {

    public static enum Status {
        SCHEDULED,
        RUNNING,
        COMPLETE,
        ABORTED
    }

    private final @NonNull @Getter String jobId;

    private Status status = Status.SCHEDULED;

    private AtomicLong tilesCreated = new AtomicLong();
    private AtomicLong tilesSkipped = new AtomicLong();
    private AtomicLong tilesFailed = new AtomicLong();

    public void merge(@NonNull CacheJobStatus instanceStatus) {
        checkSameJobId(instanceStatus.getJobId());
        tilesCreated.addAndGet(instanceStatus.getTilesCreated());
        tilesSkipped.addAndGet(instanceStatus.getTilesSkipped());
        tilesFailed.addAndGet(instanceStatus.getTilesFailed());
    }

    public boolean isRunning() {
        return status == Status.RUNNING;
    }

    /**
     * @return {@code true} if {@link #getStatus()} is either {@link Status#COMPLETE complete} or
     *     {@link Status#ABORTED aborted}
     */
    public boolean isFinished() {
        return status == Status.COMPLETE || status == Status.ABORTED;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getTilesCreated() {
        return tilesCreated.get();
    }

    public long getTilesSkipped() {
        return tilesSkipped.get();
    }

    public long getTilesFailed() {
        return tilesFailed.get();
    }

    protected void checkSameJobId(String instanceJobId) {
        if (!jobId.equals(instanceJobId))
            throw new IllegalArgumentException(
                    "job ids differ. this: " + jobId + ", instance: " + instanceJobId);
    }
}
