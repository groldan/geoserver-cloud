/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.tiling.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.gwc.tiling.model.CacheJobInfo;
import org.gwc.tiling.service.CacheJobStatus.Status;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @since 1.0
 */
@Slf4j
public class CacheJobRegistry {

    private ConcurrentMap<String, CacheJob> jobs = new ConcurrentHashMap<>();

    public void jobCreated(@NonNull CacheJobInfo job) {
        jobs.put(job.getId(), new CacheJob(job));
    }

    public Optional<CacheJobInfo> jobStarted(@NonNull String jobId) {
        return setStatus(jobId, Status.RUNNING);
    }

    public Optional<CacheJobInfo> jobCompleted(@NonNull String jobId) {
        return setStatus(jobId, Status.COMPLETE);
    }

    public Optional<CacheJobInfo> jobAborted(@NonNull String jobId) {
        return setStatus(jobId, Status.ABORTED);
    }

    private Optional<CacheJobInfo> setStatus(String jobId, Status status) {
        CacheJob job = jobs.get(jobId);
        if (job == null) {
            return Optional.empty();
        }
        job.getLocalStatus().setStatus(status);
        return Optional.of(job.getInfo());
    }

    public List<CacheJobInfo> getJobs() {
        return jobs.values().stream().map(CacheJob::getInfo).toList();
    }
}
