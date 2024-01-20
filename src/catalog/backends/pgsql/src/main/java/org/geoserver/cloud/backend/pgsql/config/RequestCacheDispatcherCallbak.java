/*
 * (c) 2023 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.backend.pgsql.config;

import org.geoserver.ows.AbstractDispatcherCallback;
import org.geoserver.ows.DispatcherCallback;
import org.geoserver.ows.Request;

public class RequestCacheDispatcherCallbak extends AbstractDispatcherCallback
        implements DispatcherCallback {

    @Override
    public Request init(Request request) {
        RequestCache.init();
        return request;
    }

    @Override
    public void finished(Request request) {
        RequestCache.clear();
    }
}
