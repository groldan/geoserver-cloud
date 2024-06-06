/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.catalog.mapper;

import org.geotools.jackson.databind.filter.dto.Literal;

import java.util.LinkedHashMap;

@SuppressWarnings("serial")
public class ConnectionParameters extends LinkedHashMap<String, Literal> {}
