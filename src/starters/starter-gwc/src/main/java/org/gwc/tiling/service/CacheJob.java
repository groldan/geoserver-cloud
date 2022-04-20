/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.tiling.service;

import lombok.Getter;

import org.gwc.tiling.model.CacheJobInfo;

class CacheJob {
    private @Getter CacheJobInfo info;
    private @Getter CacheJobStatus localStatus;
    private @Getter CacheJobStatus clusterStatus;

    CacheJob(CacheJobInfo jobInfo) {
        this.info = jobInfo;
        this.localStatus = new CacheJobStatus(jobInfo.getId());
        this.clusterStatus = new CacheJobStatus(jobInfo.getId());
    }
}
