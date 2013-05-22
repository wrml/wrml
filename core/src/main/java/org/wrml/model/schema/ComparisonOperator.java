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
 * Copyright (C) 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org) 
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

/**
 * The types of comparison operations that are supported when filtering/querying models to dynamically fill a {@link org.wrml.runtime.schema.CollectionSlot}.
 *
 * @see org.wrml.runtime.schema.CollectionSlotCriterion
 */
public enum ComparisonOperator
{
    /**
     * <p>
     * The equals operator, for equality (exact match) comparisons.
     * </p>
     * <p>
     * Match values that are equal to the value specified in the criteria.
     * </p>
     */
    equalTo,

    /**
     * <p>
     * The "not equals" operator, for non-equality (not exact match) comparisons.
     * </p>
     * <p>
     * Match values that are not equal to the value specified in the criteria.
     * </p>
     */
    notEqualTo,

    /**
     * <p>
     * The "equal to any" operator (aka "in"), for equality comparisons against a list of allowable values.
     * </p>
     * <p>
     * Match any of the values that exist in the list specified in the criteria.
     * </p>
     */
    equalToAny,

    /**
     * <p>
     * The "not equal to any" operator (aka "not in" or "nin"), for comparisons against a list of "disallowed" values.
     * </p>
     * <p>
     * Match values that do not exist in the list specified to the criteria.
     * </p>
     */
    notEqualToAny,

    /**
     * <p>
     * The "greater than" operator, for comparisons where the {@link org.wrml.runtime.schema.CollectionSlotCriterion#referenceSlot()} is
     * greater than (numerically or Date-wise) the value provided by the {@link org.wrml.runtime.schema.CollectionSlotCriterion#valueSource()}.
     * </p>
     * <p>
     * Match values that are greater than the value specified in the criteria.
     * </p>
     */
    greaterThan,

    /**
     * <p>
     * The "greater than or equal to" operator, for comparisons where the {@link org.wrml.runtime.schema.CollectionSlotCriterion#referenceSlot()} is
     * greater than or equal to (numerically or Date-wise) the value provided by the {@link org.wrml.runtime.schema.CollectionSlotCriterion#valueSource()}.
     * </p>
     * <p>
     * Match values that are greater than or equal to the value specified in the criteria.
     * </p>
     */
    greaterThanOrEqualTo,

    /**
     * <p>
     * The "less than" operator, for comparisons where the {@link org.wrml.runtime.schema.CollectionSlotCriterion#referenceSlot()} is
     * less than (numerically or Date-wise) the value provided by the {@link org.wrml.runtime.schema.CollectionSlotCriterion#valueSource()}.
     * </p>
     * <p>
     * Match values that are less than the value specified in the criteria.
     * </p>
     */
    lessThan,

    /**
     * <p>
     * The "less than or equal to" operator, for comparisons where the {@link org.wrml.runtime.schema.CollectionSlotCriterion#referenceSlot()} is
     * less than or equal to (numerically or Date-wise) the value provided by the {@link org.wrml.runtime.schema.CollectionSlotCriterion#valueSource()}.
     * </p>
     * <p>
     * Match values that are less than or equal to the value specified in the criteria.
     * </p>
     */
    lessThanOrEqualTo,

    /**
     * <p>
     * The "exists" comparison is a unary operator that checks for the existence of any (non-<code>null</code>)
     * value in the {@link org.wrml.runtime.schema.CollectionSlotCriterion#referenceSlot()} without consideration of the {@link org.wrml.runtime.schema.CollectionSlotCriterion#valueSource()}.
     * </p>
     * <p>
     * Match values that exist with a non-<code>null</code> value.
     * </p>
     * <p>
     * NOTE: When using this operator, no value source is specified in the criteria.
     * </p>
     */
    exists,

    /**
     * <p>
     * The "not exists" comparison is a unary operator that checks for the absence of a value (<code>null</code>)
     * in the {@link org.wrml.runtime.schema.CollectionSlotCriterion#referenceSlot()} without consideration of the {@link org.wrml.runtime.schema.CollectionSlotCriterion#valueSource()}.
     * </p>
     * <p>
     * Match values that do not exist or have a <code>null</code> value.
     * </p>
     * <p>
     * NOTE: When using this operator, no value source is specified in the criteria.
     * </p>
     */
    notExists,

    /**
     * <p>
     * The "contains all" operator compares the list of values in the {@link org.wrml.runtime.schema.CollectionSlotCriterion#referenceSlot()} against the list of
     * values provided by the {@link org.wrml.runtime.schema.CollectionSlotCriterion#valueSource()}.
     * </p>
     * <p>
     * Match lists that contain all elements specified in the criteria.
     * </p>
     */
    containsAll,

    /**
     * <p>
     * The "regex" operator uses the regular expression pattern specified by {@link org.wrml.runtime.schema.CollectionSlotCriterion#regex()}
     * to find matching values within the {@link org.wrml.runtime.schema.CollectionSlotCriterion#referenceSlot()}.
     * </p>
     * <p>
     * Match values that match a regular expression (specified in the criteria).
     * </p>
     * <p>
     * TODO: Document how the valueSource's value can be dynamically substituted within the regex pattern.
     * </p>
     */
    regex;

}
