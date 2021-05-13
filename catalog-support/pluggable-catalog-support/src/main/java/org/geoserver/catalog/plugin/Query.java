/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin;

import com.google.common.collect.Ordering;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.Info;
import org.geoserver.ows.util.OwsUtils;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

/** */
@NoArgsConstructor
@RequiredArgsConstructor
@Accessors(chain = true)
public @Data class Query<T extends Info> {

    /** constant no-op Comparator for {@link #providedOrder()} */
    private static final Ordering<?> PROVIDED_ORDER = Ordering.allEqual();

    private @NonNull Class<T> type;
    private @NonNull Filter filter = Filter.INCLUDE;
    private @NonNull List<SortBy> sortBy = new ArrayList<>();
    private Integer offset;
    private Integer count;

    /** retype constructor */
    public Query(@NonNull Class<T> type, @NonNull Query<?> query) {
        this.type = type;
        this.filter = query.getFilter();
        this.sortBy = new ArrayList<>(query.getSortBy());
        this.offset = query.getOffset();
        this.count = query.getCount();
    }

    /** Copy constructor */
    public Query(@NonNull Query<T> query) {
        this.type = query.getType();
        this.filter = query.getFilter();
        this.sortBy = new ArrayList<>(query.getSortBy());
        this.offset = query.getOffset();
        this.count = query.getCount();
    }

    public boolean isSorting() {
        return !sortBy.isEmpty();
    }

    public OptionalInt count() {
        return count == null ? OptionalInt.empty() : OptionalInt.of(count.intValue());
    }

    public OptionalInt offset() {
        return offset == null ? OptionalInt.empty() : OptionalInt.of(offset.intValue());
    }

    public static <C extends Info> Query<C> all(Class<? extends Info> type) {
        return valueOf(type, Filter.INCLUDE, null, null);
    }

    public static <T extends CatalogInfo> Query<T> valueOf(Class<T> type, Filter filter) {
        return valueOf(type, filter, null, null);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Info> Query<T> valueOf(
            Class<? extends Info> type,
            Filter filter,
            Integer offset,
            Integer count,
            SortBy... sortOrder) {

        List<SortBy> sortBy =
                sortOrder == null
                        ? Collections.emptyList()
                        : Arrays.asList(sortOrder)
                                .stream()
                                .filter(s -> s != null)
                                .collect(Collectors.toList());

        filter = filter == null ? Filter.INCLUDE : filter;
        return new Query<>((Class<T>) type)
                .setFilter(filter)
                .setOffset(offset)
                .setCount(count)
                .setSortBy(sortBy);
    }

    /**
     * @return {@code this} if {@code filter} equals {@link #getFilter()}, a copy of this query with
     *     the provided filter otherwise
     */
    public Query<T> withFilter(Filter filter) {
        return filter.equals(this.filter) ? this : new Query<>(this).setFilter(filter);
    }

    public <U extends CatalogInfo> Comparator<U> toComparator() {
        Comparator<U> comparator = providedOrder();
        for (SortBy sortBy : this.getSortBy()) {
            comparator =
                    (comparator == PROVIDED_ORDER)
                            ? comparator(sortBy)
                            : comparator.thenComparing(comparator(sortBy));
        }
        return comparator;
    }

    public static <U extends CatalogInfo> Comparator<U> toComparator(Query<U> query) {
        Comparator<U> comparator = providedOrder();
        for (SortBy sortBy : query.getSortBy()) {
            comparator =
                    (comparator == PROVIDED_ORDER)
                            ? comparator(sortBy)
                            : comparator.thenComparing(comparator(sortBy));
        }
        return comparator;
    }

    public static <U extends CatalogInfo> Comparator<U> comparator(final SortBy sortOrder) {
        Comparator<U> comparator =
                new Comparator<>() {
                    public @Override int compare(U o1, U o2) {
                        Object v1 = OwsUtils.get(o1, sortOrder.getPropertyName().getPropertyName());
                        Object v2 = OwsUtils.get(o2, sortOrder.getPropertyName().getPropertyName());
                        if (v1 == null) {
                            if (v2 == null) {
                                return 0;
                            } else {
                                return -1;
                            }
                        } else if (v2 == null) {
                            return 1;
                        }
                        @SuppressWarnings({"rawtypes", "unchecked"})
                        Comparable<Object> c1 = (Comparable) v1;
                        @SuppressWarnings({"rawtypes", "unchecked"})
                        Comparable<Object> c2 = (Comparable) v2;
                        return c1.compareTo(c2);
                    }
                };
        if (SortOrder.DESCENDING.equals(sortOrder.getSortOrder())) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    @SuppressWarnings("unchecked")
    public static <U extends CatalogInfo> Comparator<U> providedOrder() {
        return (Comparator<U>) PROVIDED_ORDER;
    }
}
