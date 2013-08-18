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

import org.wrml.model.schema.ComparisonOperator;
import org.wrml.model.schema.ValueSourceType;

import java.util.regex.Pattern;

/**
 * <p>
 * A {@link ProtoSearchCriterion} may be used to filter/select the elements of a {@link org.wrml.model.Model}'s collection slot.
 * This runtime class provides the metadata needed to describe part of a search <i>query</i>.
 * </p>
 *
 * @see CollectionPropertyProtoSlot
 * @see ProtoSearchCriteria
 * @see org.wrml.runtime.search.SearchCriterion
 */
public final class ProtoSearchCriterion
{

    private final ProtoSearchCriteria _ProtoSearchCriteria;

    private final ProtoValueSource _ProtoValueSource;

    private final ComparisonOperator _ComparisonOperator;

    private final String _Regex;

    private final Pattern _RegexPattern;

    ProtoSearchCriterion(final ProtoSearchCriteria protoSearchCriteria, final CollectionSlotCriterion criterion)
    {

        _ProtoSearchCriteria = protoSearchCriteria;

        final String referenceSlot = criterion.referenceSlot();
        final ValueSourceType valueSourceType = criterion.valueSourceType();
        final String valueSource = criterion.valueSource();

        final Prototype referencePrototype = protoSearchCriteria.getReferencePrototype();
        final Prototype referrerPrototype = protoSearchCriteria.getReferrerPrototype();

        _ProtoValueSource =
                new ProtoValueSource(referencePrototype, referenceSlot, referrerPrototype, valueSource, valueSourceType);


        _ComparisonOperator = criterion.operator();
        _Regex = criterion.regex();

        if (_Regex == null || _Regex.isEmpty())
        {
            _RegexPattern = null;
        }
        else
        {
            _RegexPattern = Pattern.compile(_Regex);
        }
    }

    public ProtoSearchCriteria getProtoSearchCriteria()
    {

        return _ProtoSearchCriteria;
    }

    public ProtoValueSource getProtoValueSource()
    {

        return _ProtoValueSource;
    }

    public ComparisonOperator getComparisonOperator()
    {

        return _ComparisonOperator;
    }

    public String getRegex()
    {

        return _Regex;
    }


    public Pattern getRegexPattern()
    {

        return _RegexPattern;
    }

}

