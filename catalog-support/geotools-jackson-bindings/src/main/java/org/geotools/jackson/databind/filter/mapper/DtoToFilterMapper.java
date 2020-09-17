/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geotools.jackson.databind.filter.mapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.IsEqualsToImpl;
import org.geotools.filter.IsGreaterThanImpl;
import org.geotools.filter.IsGreaterThanOrEqualToImpl;
import org.geotools.filter.IsLessThenImpl;
import org.geotools.filter.IsLessThenOrEqualToImpl;
import org.geotools.filter.IsNotEqualToImpl;
import org.geotools.filter.spatial.BBOXImpl;
import org.geotools.filter.spatial.BeyondImpl;
import org.geotools.filter.spatial.ContainsImpl;
import org.geotools.filter.spatial.CrossesImpl;
import org.geotools.filter.spatial.DWithinImpl;
import org.geotools.filter.spatial.DisjointImpl;
import org.geotools.filter.spatial.EqualsImpl;
import org.geotools.filter.spatial.IntersectsImpl;
import org.geotools.filter.spatial.OverlapsImpl;
import org.geotools.filter.spatial.TouchesImpl;
import org.geotools.filter.spatial.WithinImpl;
import org.geotools.filter.temporal.AfterImpl;
import org.geotools.filter.temporal.AnyInteractsImpl;
import org.geotools.filter.temporal.BeforeImpl;
import org.geotools.filter.temporal.BeginsImpl;
import org.geotools.filter.temporal.BegunByImpl;
import org.geotools.filter.temporal.DuringImpl;
import org.geotools.filter.temporal.EndedByImpl;
import org.geotools.filter.temporal.EndsImpl;
import org.geotools.filter.temporal.MeetsImpl;
import org.geotools.filter.temporal.MetByImpl;
import org.geotools.filter.temporal.OverlappedByImpl;
import org.geotools.filter.temporal.TContainsImpl;
import org.geotools.filter.temporal.TEqualsImpl;
import org.geotools.filter.temporal.TOverlapsImpl;
import org.geotools.jackson.databind.filter.dto.Filter;
import org.geotools.jackson.databind.filter.dto.Filter.Id.ResourceId;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.opengis.filter.And;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.MultiValuedFilter.MatchAction;
import org.opengis.filter.NativeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNil;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.AnyInteracts;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.Begins;
import org.opengis.filter.temporal.BegunBy;
import org.opengis.filter.temporal.During;
import org.opengis.filter.temporal.EndedBy;
import org.opengis.filter.temporal.Ends;
import org.opengis.filter.temporal.Meets;
import org.opengis.filter.temporal.MetBy;
import org.opengis.filter.temporal.OverlappedBy;
import org.opengis.filter.temporal.TContains;
import org.opengis.filter.temporal.TEquals;
import org.opengis.filter.temporal.TOverlaps;

@Mapper(config = FilterMapperConfig.class)
abstract class DtoToFilterMapper {

    private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    private ExpressionMapper expm = Mappers.getMapper(ExpressionMapper.class);
    private final ValueMappers valueMappers = Mappers.getMapper(ValueMappers.class);

    private Expression exp(org.geotools.jackson.databind.filter.dto.Expression e) {
        return expm.map(e);
    }

    public org.opengis.filter.Filter map(org.geotools.jackson.databind.filter.dto.Filter dto) {
        if (dto == null) return null;
        final Class<? extends Filter> dtoFilterType = dto.getClass();
        Method mapperMethod;
        try {
            mapperMethod = getClass().getMethod("toFilter", dtoFilterType);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        org.opengis.filter.Filter filter;
        try {
            filter = (org.opengis.filter.Filter) mapperMethod.invoke(this, dto);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return filter;
    }

    public abstract IncludeFilter toFilter(Filter.IncludeFilter dto);

    public abstract ExcludeFilter toFilter(Filter.ExcludeFilter dto);

    public PropertyIsNil toFilter(Filter.PropertyIsNil dto) {
        return ff.isNil(exp(dto.getExpression()), dto.getNilReason());
    }

    public PropertyIsNull toFilter(Filter.PropertyIsNull dto) {
        return ff.isNull(exp(dto.getExpression()));
    }

    public NativeFilter toFilter(Filter.NativeFilter dto) {
        return ff.nativeFilter(dto.getNative());
    }

    public PropertyIsLike toFilter(Filter.PropertyIsLike dto) {
        MatchAction matchAction = valueMappers.matchAction(dto.getMatchAction());
        return ff.like(
                exp(dto.getExpression()),
                dto.getLiteral(),
                dto.getWildCard(),
                dto.getSingleChar(),
                dto.getEscape(),
                dto.isMatchingCase(),
                matchAction);
    }

    public PropertyIsBetween toFilter(Filter.PropertyIsBetween dto) {
        Expression expression = exp(dto.getExpression());
        Expression lower = exp(dto.getLowerBoundary());
        Expression upper = exp(dto.getUpperBoundary());
        MatchAction matchAction = valueMappers.matchAction(dto.getMatchAction());
        return ff.between(expression, lower, upper, matchAction);
    }

    @IterableMapping(elementTargetType = org.opengis.filter.Filter.class)
    protected abstract List<org.opengis.filter.Filter> list(List<Filter> dtos);

    public And toFilter(Filter.BinaryLogicOperator.And dto) {
        return ff.and(list(dto.getChildren()));
    }

    public Or toFilter(Filter.BinaryLogicOperator.Or dto) {
        return ff.or(list(dto.getChildren()));
    }

    public Not toFilter(Filter.Not dto) {
        return ff.not(map(dto.getFilter()));
    }

    public Id toFilter(Filter.Id dto) {
        Set<Filter.Id.FeatureId> identifiers =
                dto.getIdentifiers() == null ? Collections.emptySet() : dto.getIdentifiers();

        Set<? extends Identifier> ids =
                identifiers.stream().map(this::toIdentifier).collect(Collectors.toSet());

        return ff.id(ids);
    }

    Identifier toIdentifier(Filter.Id.FeatureId dto) {
        if (dto instanceof Filter.Id.ResourceId) {
            Filter.Id.ResourceId rid = (ResourceId) dto;
            if (rid.getStartTime() != null || rid.getEndTime() != null)
                return ff.resourceId(rid.getId(), rid.getStartTime(), rid.getEndTime());

            throw new UnsupportedOperationException();
        }
        if (dto instanceof Filter.Id.FeatureId) {
            String id = dto.getId();
            String featureVersion = ((Filter.Id.FeatureId) dto).getFeatureVersion();
            if (featureVersion == null) {
                return ff.featureId(id);
            }
            return ff.featureId(id, featureVersion);
        }
        throw new IllegalArgumentException(
                "Unsupported identifier type: " + dto.getClass().getCanonicalName());
    }

    private static @FunctionalInterface interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    private static @FunctionalInterface interface QuadFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }

    private <T extends org.opengis.filter.Filter> T toBinaryOperator(
            Filter.BinaryOperator dto,
            TriFunction<Expression, Expression, MatchAction, T> factory) {

        Expression e1 = exp(dto.getExpression1());
        Expression e2 = exp(dto.getExpression2());
        MatchAction matchAction = valueMappers.matchAction(dto.getMatchAction());
        return factory.apply(e1, e2, matchAction);
    }

    private <T extends org.opengis.filter.BinaryComparisonOperator> T toBinaryComparisonOperator(
            Filter.BinaryComparisonOperator dto,
            QuadFunction<Expression, Expression, Boolean, MatchAction, T> factory) {

        Expression e1 = exp(dto.getExpression1());
        Expression e2 = exp(dto.getExpression2());
        MatchAction matchAction = valueMappers.matchAction(dto.getMatchAction());
        Boolean matchCase = Boolean.valueOf(dto.isMatchingCase());

        return factory.apply(e1, e2, matchCase, matchAction);
    }

    public Within toFilter(Filter.BinarySpatialOperator.Within dto) {
        return toBinaryOperator(dto, WithinImpl::new);
    }

    public Touches toFilter(Filter.BinarySpatialOperator.Touches dto) {
        return toBinaryOperator(dto, TouchesImpl::new);
    }

    public Overlaps toFilter(Filter.BinarySpatialOperator.Overlaps dto) {
        return toBinaryOperator(dto, OverlapsImpl::new);
    }

    public Intersects toFilter(Filter.BinarySpatialOperator.Intersects dto) {
        return toBinaryOperator(dto, IntersectsImpl::new);
    }

    public Equals toFilter(Filter.BinarySpatialOperator.Equals dto) {
        return toBinaryOperator(dto, EqualsImpl::new);
    }

    public Disjoint toFilter(Filter.BinarySpatialOperator.Disjoint dto) {
        return toBinaryOperator(dto, DisjointImpl::new);
    }

    public Crosses toFilter(Filter.BinarySpatialOperator.Crosses dto) {
        return toBinaryOperator(dto, CrossesImpl::new);
    }

    public Contains toFilter(Filter.BinarySpatialOperator.Contains dto) {
        return toBinaryOperator(dto, ContainsImpl::new);
    }

    public BBOX toFilter(Filter.BinarySpatialOperator.BBOX dto) {
        return toBinaryOperator(dto, BBOXImpl::new);
    }

    public Beyond toFilter(Filter.BinarySpatialOperator.Beyond dto) {
        BeyondImpl impl = toBinaryOperator(dto, BeyondImpl::new);
        impl.setDistance(dto.getDistance());
        impl.setUnits(dto.getDistanceUnits());
        return impl;
    }

    public DWithin toFilter(Filter.BinarySpatialOperator.DWithin dto) {
        DWithinImpl impl = toBinaryOperator(dto, DWithinImpl::new);
        impl.setDistance(dto.getDistance());
        impl.setUnits(dto.getDistanceUnits());
        return impl;
    }

    private static class PropertyEquals extends IsEqualsToImpl {
        public PropertyEquals(
                Expression expression1,
                Expression expression2,
                boolean matchCase,
                MatchAction matchAction) {
            super(expression1, expression2, matchCase, matchAction);
        }
    }

    private static class PropertyNotEquals extends IsNotEqualToImpl {
        public PropertyNotEquals(
                Expression expression1,
                Expression expression2,
                boolean matchCase,
                MatchAction matchAction) {
            super(expression1, expression2, matchCase, matchAction);
        }
    }

    private static class PropertyLessThan extends IsLessThenImpl {
        public PropertyLessThan(
                Expression expression1,
                Expression expression2,
                boolean matchCase,
                MatchAction matchAction) {
            super(expression1, expression2, matchCase, matchAction);
        }
    }

    private static class PropertyLessThanOrEquals extends IsLessThenOrEqualToImpl {
        public PropertyLessThanOrEquals(
                Expression expression1,
                Expression expression2,
                boolean matchCase,
                MatchAction matchAction) {
            super(expression1, expression2, matchCase, matchAction);
        }
    }

    private static class PropertyGreaterThan extends IsGreaterThanImpl {
        public PropertyGreaterThan(
                Expression expression1,
                Expression expression2,
                boolean matchCase,
                MatchAction matchAction) {
            super(expression1, expression2, matchAction);
        }
    }

    private static class PropertyGreaterThanOrEqual extends IsGreaterThanOrEqualToImpl {
        public PropertyGreaterThanOrEqual(
                Expression expression1,
                Expression expression2,
                boolean matchCase,
                MatchAction matchAction) {
            super(expression1, expression2, matchCase, matchAction);
        }
    }

    public PropertyIsEqualTo toFilter(Filter.BinaryComparisonOperator.PropertyIsEqualTo dto) {
        return toBinaryComparisonOperator(dto, PropertyEquals::new);
    }

    public PropertyIsNotEqualTo toFilter(Filter.BinaryComparisonOperator.PropertyIsNotEqualTo dto) {
        return toBinaryComparisonOperator(dto, PropertyNotEquals::new);
    }

    public PropertyIsLessThanOrEqualTo toFilter(
            Filter.BinaryComparisonOperator.PropertyIsLessThanOrEqualTo dto) {
        return toBinaryComparisonOperator(dto, PropertyLessThanOrEquals::new);
    }

    public PropertyIsLessThan toFilter(Filter.BinaryComparisonOperator.PropertyIsLessThan dto) {
        return toBinaryComparisonOperator(dto, PropertyLessThan::new);
    }

    public PropertyIsGreaterThanOrEqualTo toFilter(
            Filter.BinaryComparisonOperator.PropertyIsGreaterThanOrEqualTo dto) {
        return toBinaryComparisonOperator(dto, PropertyGreaterThanOrEqual::new);
    }

    public PropertyIsGreaterThan toFilter(
            Filter.BinaryComparisonOperator.PropertyIsGreaterThan dto) {
        return toBinaryComparisonOperator(dto, PropertyGreaterThan::new);
    }

    public After toFilter(Filter.BinaryTemporalOperator.After dto) {
        return toBinaryOperator(dto, AfterImpl::new);
    }

    public TEquals toFilter(Filter.BinaryTemporalOperator.TEquals dto) {
        return toBinaryOperator(dto, TEqualsImpl::new);
    }

    public TContains toFilter(Filter.BinaryTemporalOperator.TContains dto) {
        return toBinaryOperator(dto, TContainsImpl::new);
    }

    public OverlappedBy toFilter(Filter.BinaryTemporalOperator.OverlappedBy dto) {
        return toBinaryOperator(dto, OverlappedByImpl::new);
    }

    public MetBy toFilter(Filter.BinaryTemporalOperator.MetBy dto) {
        return toBinaryOperator(dto, MetByImpl::new);
    }

    public Meets toFilter(Filter.BinaryTemporalOperator.Meets dto) {
        return toBinaryOperator(dto, MeetsImpl::new);
    }

    public Ends toFilter(Filter.BinaryTemporalOperator.Ends dto) {
        return toBinaryOperator(dto, EndsImpl::new);
    }

    public EndedBy toFilter(Filter.BinaryTemporalOperator.EndedBy dto) {
        return toBinaryOperator(dto, EndedByImpl::new);
    }

    public During toFilter(Filter.BinaryTemporalOperator.During dto) {
        return toBinaryOperator(dto, DuringImpl::new);
    }

    public BegunBy toFilter(Filter.BinaryTemporalOperator.BegunBy dto) {
        return toBinaryOperator(dto, BegunByImpl::new);
    }

    public Begins toFilter(Filter.BinaryTemporalOperator.Begins dto) {
        return toBinaryOperator(dto, BeginsImpl::new);
    }

    public Before toFilter(Filter.BinaryTemporalOperator.Before dto) {
        return toBinaryOperator(dto, BeforeImpl::new);
    }

    public AnyInteracts toFilter(Filter.BinaryTemporalOperator.AnyInteracts dto) {
        return toBinaryOperator(dto, AnyInteractsImpl::new);
    }

    public TOverlaps toFilter(Filter.BinaryTemporalOperator.TOverlaps dto) {
        return toBinaryOperator(dto, TOverlapsImpl::new);
    }
}
