/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.tiling.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.gwc.tiling.event.jobs.CacheJobAbortComand;
import org.gwc.tiling.event.jobs.CacheJobEvent;
import org.gwc.tiling.event.jobs.CacheJobListCommand;
import org.gwc.tiling.event.jobs.CacheJobStartCommand;
import org.gwc.tiling.model.CacheJobInfo;
import org.gwc.tiling.model.CacheJobRequest;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * CacheJobManager.joinCluster() -> {@link CacheJobListCommand} CacheJobRegistry.onCacheJobn
 *
 * @since 1.0
 */
@RequiredArgsConstructor
public class CacheJobManager {

    private final @NonNull CacheJobRegistry registry;
    private final @NonNull Supplier<CacheJobRequestBuilder> requestBuilderFactory;
    private final @NonNull Consumer<? super CacheJobEvent> eventPublisher;

    public void joinCluster() {
        eventPublisher.accept(new CacheJobListCommand());
        throw new UnsupportedOperationException("implement");
    }

    public void leaveCluster() {
        throw new UnsupportedOperationException("implement");
    }

    public CacheJobRequestBuilder newRequestBuilder() {
        return requestBuilderFactory.get();
    }

    public List<CacheJobInfo> getJobs() {
        return registry.getJobs();
    }

    public @NonNull CacheJobInfo launchJob(@NonNull CacheJobRequest request) {
        CacheJobInfo jobInfo = new CacheJobInfo(UUID.randomUUID().toString(), request);
        eventPublisher.accept(new CacheJobStartCommand(jobInfo));
        return jobInfo;
    }

    public void cancelJob(@NonNull String jobId) {
        eventPublisher.accept(new CacheJobAbortComand(jobId));
    }
}
