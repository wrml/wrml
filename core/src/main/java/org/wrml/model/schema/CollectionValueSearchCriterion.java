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
package org.wrml.model.schema;

import org.wrml.model.Model;
import org.wrml.runtime.schema.DefaultValue;

/**
 * <p>
 * A {@link CollectionValueSearchCriterion} may be used to filter/select the elements of a {@link CollectionValue}.
 * This model provides the metadata needed to describe part of a <i>query</i> that is automatically executed by the runtime whenever
 * an instance of the {@link CollectionValue}'s owning model, known as the <i>referrer</i>, is returned from one
 * of the runtime's core methods (e.g. {@link org.wrml.runtime.Context#getModel(org.wrml.runtime.Keys, org.wrml.runtime.Dimensions)}.
 * </p>
 *
 * @see CollectionValue
 * @see org.wrml.runtime.schema.CollectionSlot
 * @see org.wrml.runtime.schema.CollectionSlotCriterion
 * @see org.wrml.runtime.service.Service#search(org.wrml.runtime.search.SearchCriteria)
 */
public interface CollectionValueSearchCriterion extends Model {

    /**
     * <p>
     * The name of a slot within the "collected" (<i>contained</i>) {@link org.wrml.model.schema.Schema} that this {@link CollectionValueSearchCriterion} is relating to.
     * </p>
     * <p>
     * In binary comparison operations (e.g. {@link ComparisonOperator#lessThan}, the {@link #getReferenceSlot()} is the "left hand side".  For example:
     * </p>
     * <p>
     * <pre>
     *          {referenceSlot} < {valueSource}
     * </pre>
     * </p>
     * <p>
     * Note that this value may optionally contain one or more "." characters separating subordinate slot names in order to base this {@link CollectionValueSearchCriterion} on a <i>nested</i> value.
     * </p>
     *
     * @return The name of a slot within the "collected" (<i>contained</i>) {@link org.wrml.model.schema.Schema} that this {@link CollectionValueSearchCriterion} is relating to.
     */
    String getReferenceSlot();

    /**
     * <p>
     * The source of the value that will be used to "fill in" the reference slot when evaluating this {@link CollectionValueSearchCriterion}
     * to search for matching models to populate the {@link org.wrml.runtime.schema.CollectionSlot}.
     * </p>
     * <p>
     * In binary comparison operations (e.g. {@link ComparisonOperator#greaterThanOrEqualTo}, the {@link #getReferenceSlot()} is the "right hand side".
     * </p>
     * <p>
     * <pre>
     *          {referenceSlot} >= {valueSource}
     * </pre>
     * </p>
     *
     * @return The source of the value that will be used to "fill in" the reference slot when searching.
     */
    String getValueSource();

    /**
     * The source type for the binding value.
     *
     * @return The source type for the binding value.
     */
    @DefaultValue("ReferrerSlot")
    ValueSourceType getValueSourceType();

    /**
     * <p>
     * The {@link ComparisonOperator} to be used when comparing the {@link #getReferenceSlot()} value to the value retrieved from the {@link #getValueSource()}.
     * </p>
     * <p>
     * The default value is {@link ComparisonOperator#equalTo} (value equality comparison).
     * </p>
     *
     * @return The {@link ComparisonOperator} to be used when comparing the {@link #getReferenceSlot()} value to the value retrieved from the {@link #getValueSource()}.
     */
    @DefaultValue("equalTo")
    ComparisonOperator getOperator();

    /**
     * <p>
     * The regular expression pattern value to be used in conjunction with the {@link ComparisonOperator#regex} comparison operator.
     * </p>
     * <p>
     * The default value is an empty {@link String} indicating no pattern.
     * </p>
     *
     * @return The regular expression pattern value to be used in conjunction with the {@link ComparisonOperator#regex} comparison operator.
     */
    String getRegex();

    /**
     * @see #getReferenceSlot()
     */
    String setReferenceSlot(String referenceSlot);

    /**
     * @see #getValueSource()
     */
    String setValueSource(String valueSource);

    /**
     * @see #getValueSourceType()
     */
    ValueSourceType setValueSourceType(ValueSourceType valueSourceType);

    /**
     * @see #getOperator()
     */
    ComparisonOperator setOperator(ComparisonOperator operator);

    /**
     * @see #getRegex()
     */
    String setRegex(String regexPattern);


}
