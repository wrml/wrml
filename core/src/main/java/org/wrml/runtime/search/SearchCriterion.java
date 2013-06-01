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
package org.wrml.runtime.search;

import org.wrml.model.schema.ComparisonOperator;

/**
 * TODO: Javadoc
 */
public interface SearchCriterion
{


    /**
     * <p>
     * The name of a slot within the "collected" (<i>contained</i>) {@link org.wrml.model.schema.Schema} that this {@link SearchCriterion} is relating to.
     * </p>
     * <p>
     * In binary comparison operations (e.g. {@link org.wrml.model.schema.ComparisonOperator#lessThan}, the {@link #getReferenceSlot()} is the "left hand side".
     * </p>
     * <p>
     * Note that this value may optionally contain one or more "." characters separating subordinate slot names in order to base this {@link SearchCriterion} on a <i>nested</i> value.
     * </p>
     *
     * @return The name of a slot within the "collected" (<i>contained</i>) {@link org.wrml.model.schema.Schema} that this {@link SearchCriterion} is relating to.
     */
    String getReferenceSlot();

    /**
     * <p>
     * The value that will be compared when evaluating this {@link SearchCriterion} to search for matching models to populate the {@link org.wrml.runtime.schema.CollectionSlot}.
     * </p>
     * <p>
     * In binary comparison operations (e.g. {@link org.wrml.model.schema.ComparisonOperator#greaterThanOrEqualTo}, the {@link #getReferenceSlot()} is the "right hand side".
     * </p>
     *
     * @return The source of the value that will be used to "fill in" the reference slot when searching.
     */
    Object getComparisonValue();

    /**
     * <p>
     * The {@link org.wrml.model.schema.ComparisonOperator} to be used when comparing the {@link #getReferenceSlot()} value to the {@link #getComparisonValue()}.
     * </p>
     * <p>
     * The default value is {@link org.wrml.model.schema.ComparisonOperator#equalTo} (value equality comparison).
     * </p>
     *
     * @return The {@link org.wrml.model.schema.ComparisonOperator} to be used when comparing the {@link #getReferenceSlot()} value to the {@link #getComparisonValue()}.
     */
    ComparisonOperator getComparisonOperator();

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
}
