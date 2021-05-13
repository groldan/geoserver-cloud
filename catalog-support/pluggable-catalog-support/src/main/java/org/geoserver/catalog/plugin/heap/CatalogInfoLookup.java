/*
 * (c) 2017 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.heap;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.impl.ClassMappings;
import org.geoserver.catalog.plugin.Patch;
import org.geoserver.catalog.plugin.Query;
import org.geoserver.catalog.plugin.repository.CatalogInfoRepository;
import org.geoserver.ows.util.OwsUtils;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.springframework.lang.Nullable;

/**
 * A support index for {@link DefaultMemoryCatalogFacade}, can perform fast lookups of {@link
 * CatalogInfo} objects by id or by "name", where the name is defined by a a user provided mapping
 * function.
 *
 * <p>The lookups by predicate have been tested and optimized for performance, in particular the
 * current for loops turned out to be significantly faster than building and returning streams
 *
 * @param <T>
 */
public abstract class CatalogInfoLookup<T extends CatalogInfo> implements CatalogInfoRepository<T> {
    static final Logger LOGGER = Logging.getLogger(CatalogInfoLookup.class);

    protected final ConcurrentMap<Class<? extends T>, ConcurrentNavigableMap<String, T>> idMultiMap;
    protected final ConcurrentMap<Class<? extends T>, ConcurrentNavigableMap<Name, T>> nameMultiMap;
    protected final ConcurrentMap<Class<? extends T>, ConcurrentNavigableMap<String, Name>>
            idToMameMultiMap;
    protected final Function<T, Name> nameMapper;
    protected final Class<T> infoType;

    static final <T> Predicate<T> alwaysTrue() {
        return x -> true;
    }

    protected CatalogInfoLookup(Class<T> type, Function<T, Name> nameMapper) {
        super();
        this.nameMapper = nameMapper;
        this.infoType = type;
        this.idMultiMap = new ConcurrentHashMap<>();
        this.nameMultiMap = new ConcurrentHashMap<>();
        this.idToMameMultiMap = new ConcurrentHashMap<>();
    }

    public @Override Class<T> getContentType() {
        return infoType;
    }

    <K, V> ConcurrentMap<K, V> getMapForValue(
            ConcurrentMap<Class<? extends T>, ConcurrentNavigableMap<K, V>> maps, T value) {
        @SuppressWarnings("unchecked")
        Class<T> vc = (Class<T>) value.getClass();
        return getMapForType(maps, vc);
    }

    protected <K, V> ConcurrentMap<K, V> getMapForType(
            ConcurrentMap<Class<? extends T>, ConcurrentNavigableMap<K, V>> maps,
            Class<? extends T> vc) {
        return maps.computeIfAbsent(vc, k -> new ConcurrentSkipListMap<K, V>());
    }

    static void checkNotAProxy(CatalogInfo value) {
        if (Proxy.isProxyClass(value.getClass())) {
            throw new IllegalArgumentException(
                    "Proxy values shall not be passed to CatalogInfoLookup");
        }
    }

    public @Override void add(T value) {
        requireNonNull(value);
        checkNotAProxy(value);
        Map<String, T> idMap = getMapForValue(idMultiMap, value);
        Map<Name, T> nameMap = getMapForValue(nameMultiMap, value);
        Map<String, Name> idToName = getMapForValue(idToMameMultiMap, value);
        // TODO: improve concurrency with lock sharding instead of blocking the whole ConcurrentMaps
        synchronized (idMap) {
            if (null != idMap.putIfAbsent(value.getId(), value)) {
                String msg =
                        String.format(
                                "%s:%s(%s) already exists",
                                ClassMappings.fromImpl(value.getClass()),
                                value.getId(),
                                nameMapper.apply(value).getLocalPart());
                LOGGER.warning(msg);
                // throw new IllegalArgumentException(msg);
            }
            Name name = nameMapper.apply(value);
            nameMap.put(name, value);
            idToName.put(value.getId(), name);
        }
    }

    public @Override void remove(T value) {
        requireNonNull(value);
        checkNotAProxy(value);
        Map<String, T> idMap = getMapForValue(idMultiMap, value);
        // TODO: improve concurrency with lock sharding instead of blocking the whole ConcurrentMaps
        synchronized (idMap) {
            T removed = idMap.remove(value.getId());
            if (removed != null) {
                Name name = getMapForValue(idToMameMultiMap, value).remove(value.getId());
                getMapForValue(nameMultiMap, value).remove(name);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public @Override <I extends T> I update(final I value, Patch patch) {
        requireNonNull(value);
        requireNonNull(patch);
        checkNotAProxy(value);
        Map<String, T> idMap = getMapForValue(idMultiMap, value);
        // for the sake of correctness, get the stored value, contract does not force the supplied
        // value to be attached
        T storedValue = idMap.get(value.getId());
        if (storedValue == null) {
            throw new NoSuchElementException(
                    value.getClass().getSimpleName()
                            + " with id "
                            + value.getId()
                            + " does not exist");
        }
        // TODO: improve concurrency with lock sharding instead of blocking the whole ConcurrentMaps
        synchronized (idMap) {
            patch.applyTo(storedValue);
            ConcurrentMap<String, Name> idToName = getMapForValue(idToMameMultiMap, value);
            Name oldName = idToName.get(value.getId());
            Name newName = nameMapper.apply(storedValue);
            if (!Objects.equals(oldName, newName)) {
                Map<Name, T> nameMap = getMapForValue(nameMultiMap, value);
                nameMap.remove(oldName);
                nameMap.put(newName, value);
                idToName.put(value.getId(), newName);
            }
        }
        return (I) storedValue;
    }

    public @Override void dispose() {
        clear();
    }

    protected void clear() {
        idMultiMap.clear();
        nameMultiMap.clear();
        idToMameMultiMap.clear();
    }

    /**
     * This default implementation supports sorting against properties (could be nested) that are
     * either of a primitive type or implement {@link Comparable}.
     *
     * @param propertyName the property name of the objects of type {@code type} to sort by
     * @see org.geoserver.catalog.CatalogFacade#canSort(java.lang.Class, java.lang.String)
     */
    public @Override boolean canSortBy(String propertyName) {
        return CatalogInfoLookup.canSort(propertyName, getContentType());
    }

    public static boolean canSort(String propertyName, Class<? extends CatalogInfo> type) {
        final String[] path = propertyName.split("\\.");
        Class<?> clazz = type;
        for (int i = 0; i < path.length; i++) {
            String property = path[i];
            Method getter;
            try {
                getter = OwsUtils.getter(clazz, property, null);
            } catch (RuntimeException e) {
                return false;
            }
            clazz = getter.getReturnType();
            if (i == path.length - 1) {
                boolean primitive = clazz.isPrimitive();
                boolean comparable = Comparable.class.isAssignableFrom(clazz);
                boolean canSort = primitive || comparable;
                return canSort;
            }
        }
        throw new IllegalStateException("empty property name");
    }

    @Override
    public <U extends T> Stream<U> findAll(Query<U> query) {
        requireNonNull(query);

        Comparator<U> comparator = query.toComparator();
        Stream<U> stream = list(query.getType(), toPredicate(query.getFilter()), comparator);

        if (query.offset().isPresent()) {
            stream = stream.skip(query.offset().getAsInt());
        }
        if (query.count().isPresent()) {
            stream = stream.limit(query.count().getAsInt());
        }
        return stream;
    }

    public @Override <U extends T> long count(Class<U> type, Filter filter) {
        return Filter.INCLUDE.equals(filter)
                ? idMultiMap
                        .entrySet()
                        .stream()
                        .filter(k -> type.isAssignableFrom(k.getKey()))
                        .map(Map.Entry::getValue)
                        .mapToLong(Map::size)
                        .sum()
                : findAll(Query.valueOf(type, filter)).count();
    }

    protected <V> Predicate<V> toPredicate(Filter filter) {
        return o -> filter.evaluate(o);
    }

    <U extends CatalogInfo> Stream<U> list(Class<U> clazz, Predicate<U> predicate) {
        return list(clazz, predicate, Query.providedOrder());
    }

    /**
     * Looks up objects by class and matching predicate.
     *
     * <p>This method is significantly faster than creating a stream and the applying the predicate
     * on it. Just using this approach instead of the stream makes the overall startup of GeoServer
     * with 20k layers go down from 50s to 44s (which is a lot, considering there is a lot of other
     * things going on)
     */
    <U extends CatalogInfo> Stream<U> list(
            Class<U> clazz, Predicate<U> predicate, Comparator<U> comparator) {
        requireNonNull(clazz);
        requireNonNull(predicate);
        requireNonNull(comparator);
        List<U> result = new ArrayList<U>();
        for (Class<? extends T> key : nameMultiMap.keySet()) {
            if (clazz.isAssignableFrom(key)) {
                Map<Name, T> valueMap = getMapForType(nameMultiMap, key);
                for (T v : valueMap.values()) {
                    final U u = clazz.cast(v);
                    if (predicate.test(u)) {
                        result.add(u);
                    }
                }
            }
        }

        if (!Query.providedOrder().equals(comparator)) {
            Collections.sort(result, comparator);
        }
        return result.stream();
    }

    /** Looks up a CatalogInfo by class and identifier */
    public @Override <U extends T> Optional<U> findById(String id, Class<U> clazz) {
        requireNonNull(id, () -> "id is null, class: " + clazz);
        requireNonNull(clazz);
        for (Class<? extends T> key : idMultiMap.keySet()) {
            if (clazz.isAssignableFrom(key)) {
                Map<String, T> valueMap = getMapForType(idMultiMap, key);
                T t = valueMap.get(id);
                if (t != null) {
                    return Optional.of(clazz.cast(t));
                }
            }
        }

        return Optional.empty();
    }

    /** Looks up a CatalogInfo by class and name */
    public @Override <U extends T> Optional<U> findFirstByName(
            String name, @Nullable Class<U> clazz) {
        requireNonNull(name);
        requireNonNull(clazz);
        return findFirst(clazz, i -> name.equals(nameMapper.apply(i).getLocalPart()));
    }

    protected <U extends T> Optional<U> findFirstByName(Name name, @Nullable Class<U> clazz) {
        for (Class<? extends T> key : nameMultiMap.keySet()) {
            if (clazz.isAssignableFrom(key)) {
                Map<Name, T> valueMap = getMapForType(nameMultiMap, key);
                T t = valueMap.get(name);
                if (t != null) {
                    return Optional.of(clazz.cast(t));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Looks up objects by class and matching predicate.
     *
     * <p>This method is significantly faster than creating a stream and the applying the predicate
     * on it. Just using this approach instead of the stream makes the overall startup of GeoServer
     * with 20k layers go down from 50s to 44s (which is a lot, considering there is a lot of other
     * things going on)
     */
    <U extends CatalogInfo> Optional<U> findFirst(Class<U> clazz, Predicate<U> predicate) {
        for (Class<? extends T> key : nameMultiMap.keySet()) {
            if (clazz.isAssignableFrom(key)) {
                Map<Name, T> valueMap = getMapForType(nameMultiMap, key);
                for (T v : valueMap.values()) {
                    final U u = clazz.cast(v);
                    if (predicate.test(u)) {
                        return Optional.of(u);
                    }
                }
            }
        }

        return Optional.empty();
    }

    public @Override void syncTo(CatalogInfoRepository<T> target) {
        requireNonNull(target);
        if (target instanceof CatalogInfoLookup) {
            CatalogInfoLookup<T> other = (CatalogInfoLookup<T>) target;
            other.clear();
            other.idMultiMap.putAll(this.idMultiMap);
            other.nameMultiMap.putAll(this.nameMultiMap);
            other.idToMameMultiMap.putAll(this.idToMameMultiMap);
        } else {
            this.idMultiMap.values().forEach(typeMap -> typeMap.values().forEach(target::add));
        }
    }
}
