/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.logging.accesslog;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Configuration to set white/black list over the request URL to determine if the access log filter
 * will log an entry for it.
 */
@Data
@ConfigurationProperties(prefix = "logging.accesslog")
@Slf4j(topic = "org.geoserver.cloud.accesslog")
public class AccessLogFilterConfig {

    /** Enable/disable the access log filter */
    private boolean enabled = true;

    /**
     * A list of java regular expressions applied to the request URL to include them from logging.
     */
    List<Pattern> include = new ArrayList<>();

    /**
     * A list of java regular expressions applied to the request URL to exclude them from logging. A
     * request URL must pass all the include filters before being tested for exclusion. Useful to
     * avoid flooding the logs with frequent non-important requests such as static resources (i.e.
     * static images, etc).
     */
    List<Pattern> exclude = new ArrayList<>();

    /**
     * @param uri the origin URL (e.g. https://my.domain.com/geoserver/web/)
     * @return {@code true} if disabled or an access log entry shall be logged for this request
     */
    public boolean shouldLog(URI uri) {
        return shouldLog(uri.toString());
    }

    public boolean shouldLog(String uri) {
        if (isEnabled() && log.isDebugEnabled()) {
            if (include.isEmpty() && exclude.isEmpty()) return true;
            return matches(uri, include, true) && !matches(uri, exclude, false);
        }
        return false;
    }

    private boolean matches(String url, List<Pattern> patterns, boolean fallbackIfEmpty) {
        return (patterns == null || patterns.isEmpty())
                ? fallbackIfEmpty
                : patterns.stream().anyMatch(pattern -> pattern.matcher(url).matches());
    }

    public void log(String message, Object... args) {
        log.debug(message, args);
    }
}
