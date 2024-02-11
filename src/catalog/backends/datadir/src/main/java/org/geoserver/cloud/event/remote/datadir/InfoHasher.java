/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.event.remote.datadir;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.geoserver.catalog.AttributeTypeInfo;
import org.geoserver.catalog.AuthorityURLInfo;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.Info;
import org.geoserver.catalog.KeywordInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.cloud.event.info.ConfigInfoType;
import org.geoserver.ows.util.ClassProperties;
import org.geoserver.ows.util.OwsUtils;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.util.CodeList;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.Converters;
import org.geotools.util.GrowableInternationalString;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
class InfoHasher {

    private static final Set<Class<?>> knownValueTypes =
            Set.of(
                    AttributeTypeInfo.class,
                    AuthorityURLInfo.class,
                    // org.geoserver.catalog.impl.CoverageDimensionImpl
                    KeywordInfo.class
                    // org.geotools.coverage.grid.GridGeometry2D
                    // org.geotools.gce.arcgrid.ArcGridFormat
                    // org.geotools.gce.geotiff.GeoTiffFormat
                    // org.geotools.gce.imagemosaic.ImageMosaicFormat
                    // org.geotools.gce.image.WorldImageFormat
                    );
    private static final Map<Class<?>, Set<String>> ignore =
            Map.of( //
                    CoverageStoreInfo.class,
                    Set.of("Format"),
                    ResourceInfo.class,
                    Set.of("QualifiedName", "QualifiedNativeName", "prefixedName"),
                    FeatureTypeInfo.class,
                    Set.of("FeatureType"),
                    AttributeTypeInfo.class,
                    Set.of("Attribute", "FeatureType"),
                    CoverageInfo.class,
                    Set.of("Grid", "Dimensions", "Parameters"),
                    StyleInfo.class,
                    Set.of("SLD", "Style", "prefixedName"),
                    WMSLayerInfo.class,
                    Set.of(
                            "RemoteStyleInfos",
                            "remoteStyles",
                            "availableFormats",
                            "Styles",
                            "DefaultStyle"));

    private final Map<Class<?>, SortedSet<String>> propNames = new ConcurrentHashMap<>();

    public org.geoserver.cloud.event.remote.datadir.HashCode hash(@NonNull Info info) {
        if (Proxy.isProxyClass(info.getClass())) {
            throw new IllegalArgumentException("Got proxy %s: %s".formatted(info.getClass(), info));
        }
        Hasher hasher = org.geoserver.cloud.event.remote.datadir.HashCode.newHasher();
        hashBean(info, hasher);
        return org.geoserver.cloud.event.remote.datadir.HashCode.valueOf(hasher.hash());
    }

    private void hashBean(Object bean, Hasher hasher) {
        SortedSet<String> propertyNames = getPropertyNames(bean);
        if (bean instanceof Info info) hashValue(info.getId(), hasher);
        propertyNames.forEach(property -> hashBean(bean, property, hasher));
    }

    private void hashBean(@NonNull Object bean, String property, Hasher hasher) {
        try {
            Object value = OwsUtils.get(bean, property);
            hashValue(value, hasher);
        } catch (RuntimeException e) {
            log.error("Error encoding {}.{}", typeOf(bean).getSimpleName(), property, e);
            throw e;
        }
    }

    private void hashValue(Object value, Hasher hasher) {
        if (isEmpty(value)) {
            hasher.putByte((byte) 0);
        } else if (value instanceof Info nested) {
            if (ConfigInfoType.isPersistable(nested)) {
                // it's a reference-able value
                if (nested instanceof StyleInfo style && nested.getId() == null) {
                    // special case for remote style
                    // hashBean(nested, hasher);
                    hasher.putString(style.getName(), UTF_8);
                } else {
                    hasher.putString(nested.getId(), UTF_8);
                }
            } else {
                // it's a value-object that implements Info (e.g. AttributeTypeInfo, ConcatactInfo,
                // etc)
                hashBean(nested, hasher);
            }
        } else if (isKnownValueType(value)) {
            hashBean(value, hasher);
        } else if (value instanceof Class<?> c) {
            hashValue(c.getCanonicalName(), hasher);
        } else if (value instanceof ReferencedEnvelope r) {
            hashValue(r.getCoordinateReferenceSystem(), hasher);
            hashValue(r.getMinX(), hasher);
            hashValue(r.getMaxX(), hasher);
            hashValue(r.getMinY(), hasher);
            hashValue(r.getMaxY(), hasher);
        } else if (value instanceof CoordinateReferenceSystem crs) {
            hashValue(crs.toWKT(), hasher);
        } else if (value instanceof GrowableInternationalString gis) {
            gis.getLocales()
                    .forEach(
                            l -> {
                                hashValue(l == null ? null : l.toString(), hasher);
                                hashValue(gis.toString(l), hasher);
                            });
        } else if (value instanceof Enum<?>) {
            hasher.putInt(((Enum<?>) value).ordinal());
        } else if (value instanceof CodeList<?> c) {
            hashValue(c.name(), hasher);
        } else if (value instanceof CharSequence cs) {
            hasher.putString(cs, UTF_8);
        } else if (value instanceof Boolean b) {
            hasher.putBoolean(b);
        } else if (value instanceof Float n) {
            float v = (n.floatValue() / 1000f) * 1000f;
            hasher.putFloat(v);
        } else if (value instanceof Double n) {
            double v = (n.doubleValue() / 1000d) * 1000d;
            hasher.putDouble(v);
        } else if (value instanceof Number n) {
            hasher.putLong(n.longValue());
        } else if (value instanceof java.util.Date d) {
            hasher.putLong(d.getTime());
        } else if (value instanceof Set<?> set) {
            hash(set, hasher);
        } else if (value instanceof Collection<?> col) {
            col.forEach(v -> hashValue(v, hasher));
        } else if (value instanceof Map<?, ?> map) {
            Set<?> keys = new TreeSet<>(map.keySet());
            for (Object k : keys) {
                hashValue(k, hasher);
                hashValue(map.get(k), hasher);
            }
        } else {
            // fall back to toString()
            log.warn(
                    "falling back to string representation for value type {}: {}",
                    value.getClass().getCanonicalName(),
                    value);
            String converted = Converters.convert(value, String.class);
            hashValue(converted, hasher);
        }
    }

    private boolean isEmpty(Object value) {
        if (null == value) return true;
        if (value instanceof CharSequence s && s.length() == 0) return true;
        if (value instanceof Collection<?> c && c.isEmpty()) return true;
        if (value instanceof Map<?, ?> m && m.isEmpty()) return true;
        return false;
    }

    private boolean isKnownValueType(Object value) {
        for (Class<?> c : knownValueTypes) {
            if (c.isInstance(value)) {
                return true;
            }
        }
        return false;
    }

    private void hash(@NonNull Set<?> set, Hasher hasher) {
        HashCode[] list = set.stream().map(this::hashObject).toArray(HashCode[]::new);
        hasher.putBytes(xor(list).asBytes());
    }

    private HashCode hashObject(Object object) {
        Hasher hasher = org.geoserver.cloud.event.remote.datadir.HashCode.newHasher();
        hashValue(object, hasher);
        return hasher.hash();
    }

    private SortedSet<String> getPropertyNames(@NonNull Object bean) {
        Class<?> type = typeOf(bean);
        return propNames.computeIfAbsent(type, this::loadPropertyNames);
    }

    private Class<?> typeOf(Object bean) {
        if (bean instanceof Info info) {
            if (ConfigInfoType.isPersistable(info)) return ConfigInfoType.valueOf(info).getType();

            return ClassUtils.getAllInterfacesAsSet(info).stream()
                    .filter(Info.class::isAssignableFrom)
                    .findFirst()
                    .orElseThrow();
        }
        return bean.getClass();
    }

    private SortedSet<String> loadPropertyNames(Class<?> type) {
        ClassProperties classProperties = OwsUtils.getClassProperties(type);
        var allProps = new TreeSet<>(classProperties.properties());
        Set<String> ignoredProperties = ignoredProperties(type);
        allProps.removeAll(ignoredProperties);
        return allProps;
    }

    private Set<String> ignoredProperties(Class<?> type) {
        return ignore.keySet().stream()
                .filter(c -> c.isAssignableFrom(type))
                .map(ignore::get)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    public org.geoserver.cloud.event.remote.datadir.HashCode xor(Info... infos) {
        if (null == infos) return new org.geoserver.cloud.event.remote.datadir.HashCode();
        var hashes =
                Stream.of(infos)
                        .map(this::hash)
                        .map(org.geoserver.cloud.event.remote.datadir.HashCode::hash)
                        .toArray(HashCode[]::new);

        return org.geoserver.cloud.event.remote.datadir.HashCode.valueOf(xor(hashes));
    }

    private HashCode xor(HashCode... codes) {
        if (null == codes || 0 == codes.length)
            return org.geoserver.cloud.event.remote.datadir.HashCode.newHasher().hash();
        if (1 == codes.length) return codes[0];
        byte[] xor = codes[0].asBytes();
        final int length = xor.length;
        byte[] buff = new byte[length];
        for (int i = 1; i < codes.length; i++) {
            int size = codes[i].writeBytesTo(buff, 0, length);
            for (int x = 0; x < size; x++) {
                xor[x] = (byte) (xor[x] ^ buff[x]);
            }
        }
        return HashCode.fromBytes(xor);
    }
}
