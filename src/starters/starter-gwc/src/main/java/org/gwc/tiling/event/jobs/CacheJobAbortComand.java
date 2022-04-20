/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.tiling.event.jobs;

import lombok.Value;

/**
 * @since 1.0
 */
@Value
public class CacheJobAbortComand implements CacheJobEvent {

    private String jobId;
}
