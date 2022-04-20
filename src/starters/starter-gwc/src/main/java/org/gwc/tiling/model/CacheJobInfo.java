/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.tiling.model;

import lombok.NonNull;
import lombok.Value;
import lombok.With;

/**
 * @since 1.0
 */
@Value
@With
public class CacheJobInfo {

    private @NonNull String id;

    private @NonNull CacheJobRequest request;
}
