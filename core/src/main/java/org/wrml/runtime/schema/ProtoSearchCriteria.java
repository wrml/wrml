/**
 * WRML - Web Resource Modeling Language
 *  __     __   ______   __    __   __
 * /\ \  _ \ \ /\  == \ /\ "-./  \ /\ \
 * \ \ \/ ".\ \\ \  __< \ \ \-./\ \\ \ \____
 *  \ \__/".~\_\\ \_\ \_\\ \_\ \ \_\\ \_____\
 *   \/_/   \/_/ \/_/ /_/ \/_/  \/_/ \/_____/
 *
 * http://www.wrml.org
 *
 * Copyright (C) 2011 - 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wrml.runtime.schema;

import org.wrml.model.Model;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.Embedded;
import org.wrml.model.schema.ComparisonOperator;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.DimensionsBuilder;
import org.wrml.runtime.search.SearchCriteria;
import org.wrml.runtime.search.SearchCriterion;

import java.net.URI;
import java.util.*;

/**
 * <p>
 * A {@link ProtoSearchCriteria} is used by the runtime to filter/select the elements of a {@link CollectionPropertyProtoSlot}.
 * </p>
 *
 * @see CollectionPropertyProtoSlot
 * @see ProtoSearchCriterion
 * @see ProtoValueSource
 * @see SearchCriteria
 * @see SearchCriterion
 * @see org.wrml.runtime.service.Service#search(org.wrml.runtime.search.SearchCriteria)
 * @see CollectionSlot
 * @see CollectionSlotCriterion
 * @see org.wrml.model.schema.CollectionValue
 * @see org.wrml.model.schema.CollectionValueSearchCriterion
 */
public final class ProtoSearchCriteria
{

    private final CollectionPropertyProtoSlot _CollectionPropertyProtoSlot;

    private final Prototype _ReferencePrototype;

    private final Prototype _ReferrerPrototype;

    private final List<ProtoSearchCriterion> _And;

    private final List<ProtoSearchCriterion> _Or;

    ProtoSearchCriteria(CollectionPropertyProtoSlot collectionPropertyProtoSlot, final Prototype referencePrototype, final Prototype referrerPrototype)
    {

        _CollectionPropertyProtoSlot = collectionPropertyProtoSlot;
        _ReferencePrototype = referencePrototype;
        _ReferrerPrototype = referrerPrototype;

        final CollectionSlot collectionSlot = collectionPropertyProtoSlot.getCollectionSlot();
        final CollectionSlotCriterion[] andArray = collectionSlot.and();
        if (andArray.length > 0)
        {

            _And = new ArrayList<>(andArray.length);
            for (final CollectionSlotCriterion criterion : andArray)
            {
                final ProtoSearchCriterion protoSearchCriterion = new ProtoSearchCriterion(this, criterion);
                _And.add(protoSearchCriterion);
            }
        }
        else
        {
            _And = null;
        }

        final CollectionSlotCriterion[] orArray = collectionSlot.or();
        if (orArray.length > 0)
        {

            _Or = new ArrayList<>(orArray.length);
            for (final CollectionSlotCriterion criterion : orArray)
            {
                final ProtoSearchCriterion protoSearchCriterion = new ProtoSearchCriterion(this, criterion);
                _Or.add(protoSearchCriterion);
            }
        }
        else
        {
            _Or = null;
        }
    }

    public CollectionPropertyProtoSlot getCollectionPropertyProtoSlot()
    {

        return _CollectionPropertyProtoSlot;
    }

    public Prototype getReferencePrototype()
    {

        return _ReferencePrototype;
    }

    public Prototype getReferrerPrototype()
    {

        return _ReferrerPrototype;
    }

    public List<ProtoSearchCriterion> getAnd()
    {

        return _And;
    }

    public List<ProtoSearchCriterion> getOr()
    {

        return _Or;
    }

    public SearchCriteria buildSearchCriteria(final Model referrer)
    {

        final List<SearchCriterion> and = buildSearchCriterionList(referrer, _And);
        final List<SearchCriterion> or = buildSearchCriterionList(referrer, _Or);

        final Dimensions resultDimensions = buildResultDimensions(referrer);
        final Set<String> projectionSlotNames = buildProjectionFromDimensions(resultDimensions);

        final String referrerCollectionSlotName = _CollectionPropertyProtoSlot.getName();
        final Integer resultLimit = _CollectionPropertyProtoSlot.getLimit();

        final SearchCriteria searchCriteria =
                new DefaultSearchCriteria(resultDimensions, and, or, projectionSlotNames, resultLimit, referrer, referrerCollectionSlotName);

        return searchCriteria;
    }

    private List<SearchCriterion> buildSearchCriterionList(final Model referrer, final List<ProtoSearchCriterion> protoSearchCriterionList)
    {

        if (protoSearchCriterionList == null || protoSearchCriterionList.isEmpty())
        {
            return Collections.EMPTY_LIST;
        }

        final List<SearchCriterion> searchCriterionList = new LinkedList<>();

        for (final ProtoSearchCriterion protoSearchCriterion : protoSearchCriterionList)
        {

            final ProtoValueSource protoValueSource = protoSearchCriterion.getProtoValueSource();
            final String referenceSlot = protoValueSource.getReferenceSlot();
            final Object comparisonValue = protoValueSource.getValue(referrer);
            final ComparisonOperator comparisonOperator = protoSearchCriterion.getComparisonOperator();
            final String regex = protoSearchCriterion.getRegex();

            final SearchCriterion searchCriterion = new DefaultSearchCriterion(referenceSlot, comparisonValue, comparisonOperator, regex);
            searchCriterionList.add(searchCriterion);
        }


        return searchCriterionList;
    }

    private Dimensions buildResultDimensions(final Model referrer)
    {

        final Dimensions referrerDimensions = referrer.getDimensions();
        final DimensionsBuilder dimensionsBuilder = new DimensionsBuilder(_ReferencePrototype.getSchemaUri());

        URI referrerUri = null;
        if (referrer instanceof Document)
        {
            referrerUri = ((Document) referrer).getUri();
        }
        else if (referrer instanceof Embedded)
        {
            referrerUri = ((Embedded) referrer).getDocumentUri();
        }

        if (referrerUri != null)
        {
            dimensionsBuilder.setReferrerUri(referrerUri);
        }

        dimensionsBuilder.setLocale(referrerDimensions.getLocale());

        final String referrerCollectionSlotName = _CollectionPropertyProtoSlot.getName();

        final String prefix = referrerCollectionSlotName + ".";

        subscopeDimension(prefix, referrerDimensions.getExcludedSlotNames(), dimensionsBuilder.getExcludedSlotNames());
        subscopeDimension(prefix, referrerDimensions.getIncludedSlotNames(), dimensionsBuilder.getIncludedSlotNames());
        subscopeDimension(prefix, referrerDimensions.getEmbeddedLinkSlotNames(), dimensionsBuilder.getEmbeddedLinkSlotNames());
        subscopeDimension(prefix, referrerDimensions.getQueryParameters(), dimensionsBuilder.getQueryParameters());

        return dimensionsBuilder.toDimensions();
    }

    private void subscopeDimension(final String prefix, final List<String> fromList, final List<String> toList)
    {

        if (fromList == null || fromList.isEmpty())
        {
            return;
        }

        final int beginIndex = prefix.length();

        for (final String item : fromList)
        {
            if (item.startsWith(prefix))
            {
                final String subscopedItem = item.substring(beginIndex);
                toList.add(subscopedItem);
            }
        }
    }

    private void subscopeDimension(final String prefix, final Map<String, String> fromMap, final Map<String, String> toMap)
    {

        if (fromMap == null || fromMap.isEmpty())
        {
            return;
        }

        final int beginIndex = prefix.length();
        final Set<String> keys = fromMap.keySet();
        for (final String key : keys)
        {
            if (key.startsWith(prefix))
            {
                final String subscopedKey = key.substring(beginIndex);
                toMap.put(subscopedKey, fromMap.get(key));
            }
        }
    }

    private Set<String> buildProjectionFromDimensions(final Dimensions resultDimensions)
    {

        final Set<String> projectionSlotNames = new TreeSet<>();

        final List<String> includedSlotNames = resultDimensions.getIncludedSlotNames();
        if (!includedSlotNames.isEmpty())
        {
            projectionSlotNames.addAll(includedSlotNames);
        }
        else
        {
            final List<String> excludedSlotNames = resultDimensions.getExcludedSlotNames();
            if (!excludedSlotNames.isEmpty())
            {
                final SortedSet<String> allSlotNames = _ReferencePrototype.getAllSlotNames();
                for (final String slotName : allSlotNames)
                {

                    final ProtoSlot protoSlot = _ReferencePrototype.getProtoSlot(slotName);

                    if (protoSlot instanceof PropertyProtoSlot && !(protoSlot instanceof CollectionPropertyProtoSlot))
                    {
                        // The query's projection should include "property" slots only (not link or collection slots since they aren't persisted).
                        projectionSlotNames.add(slotName);
                    }
                }

                projectionSlotNames.removeAll(excludedSlotNames);
            }
        }

        return projectionSlotNames;
    }

    /**
     * Default <i>POJO</i> implementation of the {@link SearchCriteria} interface.
     */
    private static final class DefaultSearchCriteria implements SearchCriteria
    {

        private final Dimensions _ResultDimensions;

        private final List<SearchCriterion> _And;

        private final List<SearchCriterion> _Or;

        private final Set<String> _ProjectionSlotNames;

        private final Model _Referrer;

        private final String _ReferrerCollectionSlotName;

        private final Integer _ResultLimit;

        DefaultSearchCriteria(final Dimensions resultDimensions,
                              final List<SearchCriterion> and,
                              final List<SearchCriterion> or,
                              final Set<String> projectionSlotNames,
                              final Integer resultLimit,
                              final Model referrer,
                              final String referrerCollectionSlotName)
        {

            _ResultDimensions = resultDimensions;
            _And = and;
            _Or = or;
            _ProjectionSlotNames = projectionSlotNames;
            _ResultLimit = resultLimit;

            _Referrer = referrer;
            _ReferrerCollectionSlotName = referrerCollectionSlotName;

        }

        @Override
        public Dimensions getResultDimensions()
        {

            return _ResultDimensions;
        }

        @Override
        public List<SearchCriterion> getAnd()
        {

            return _And;
        }

        @Override
        public List<SearchCriterion> getOr()
        {

            return _Or;
        }

        @Override
        public Set<String> getProjectionSlotNames()
        {

            return _ProjectionSlotNames;
        }

        @Override
        public Integer getResultLimit()
        {

            return _ResultLimit;
        }

        @Override
        public Model getReferrer()
        {

            return _Referrer;
        }

        @Override
        public String getReferrerCollectionSlotName()
        {

            return _ReferrerCollectionSlotName;
        }
    }

    /**
     * Default <i>POJO</i> implementation of the {@link SearchCriterion} interface.
     */
    private static final class DefaultSearchCriterion implements SearchCriterion
    {


        private final String _ReferenceSlot;

        private final Object _ComparisonValue;

        private final ComparisonOperator _ComparisonOperator;

        private final String _Regex;

        DefaultSearchCriterion(final String referenceSlot,
                               final Object comparisonValue,
                               final ComparisonOperator comparisonOperator,
                               final String regex)
        {

            _ReferenceSlot = referenceSlot;
            _ComparisonValue = comparisonValue;
            _ComparisonOperator = comparisonOperator;
            _Regex = regex;
        }

        @Override
        public String getReferenceSlot()
        {

            return _ReferenceSlot;
        }

        @Override
        public Object getComparisonValue()
        {

            return _ComparisonValue;
        }

        @Override
        public ComparisonOperator getComparisonOperator()
        {

            return _ComparisonOperator;
        }

        @Override
        public String getRegex()
        {

            return _Regex;
        }
    }

}