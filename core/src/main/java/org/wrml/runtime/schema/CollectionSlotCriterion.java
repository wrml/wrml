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

import java.lang.annotation.*;

/**
 * <p>
 * A {@link CollectionSlotCriterion} may be used to filter/select the elements of a {@link CollectionSlot}.
 * This annotation provides the metadata needed to model part of a <i>query</i> that is automatically executed by the runtime whenever
 * an instance of the {@link CollectionSlot}'s owning model, known as the <i>referrer</i>, is returned from one
 * of the runtime's core methods (e.g. {@link org.wrml.runtime.Context#getModel(org.wrml.runtime.Keys, org.wrml.runtime.Dimensions)}.
 * </p>
 *
 * @see CollectionSlot
 * @see CollectionPropertyProtoSlot
 * @see ValueSourceType
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CollectionSlotCriterion {
    /**
     * <p>
     * The name of a slot within the "collected" (<i>contained</i>) {@link org.wrml.model.schema.Schema} that this {@link CollectionSlotCriterion} is relating to.
     * </p>
     * <p>
     * In binary comparison operations (e.g. {@link org.wrml.model.schema.ComparisonOperator#lessThan}, the {@link #referenceSlot()} is the "left hand side".  For example:
     * </p>
     * <p>
     * <pre>
     *          {referenceSlot} < {valueSource}
     * </pre>
     * </p>
     * <p>
     * Note that this value may optionally contain one or more "." characters separating subordinate slot names in order to base this {@link CollectionSlotCriterion} on a <i>nested</i> value.
     * </p>
     *
     * @return The name of a slot within the "collected" (<i>contained</i>) {@link org.wrml.model.schema.Schema} that this {@link CollectionSlotCriterion} is relating to.
     */
    String referenceSlot();

    /**
     * <p>
     * The source of the value that will be used to "fill in" the reference slot when evaluating this {@link CollectionSlotCriterion}
     * to search for matching models to populate the {@link CollectionSlot}.
     * </p>
     * <p>
     * In binary comparison operations (e.g. {@link org.wrml.model.schema.ComparisonOperator#greaterThanOrEqualTo}, the {@link #referenceSlot()} is the "right hand side".
     * </p>
     * <p>
     * <pre>
     *          {referenceSlot} >= {valueSource}
     * </pre>
     * </p>
     *
     * @return The source of the value that will be used to "fill in" the reference slot when searching.
     */
    String valueSource();

    /**
     * The source type for the binding value.
     *
     * @return The source type for the binding value.
     */
    ValueSourceType valueSourceType() default ValueSourceType.ReferrerSlot;

    /**
     * <p>
     * The {@link org.wrml.model.schema.ComparisonOperator} to be used when comparing the {@link #referenceSlot()} value to the value retrieved from the {@link #valueSource()}.
     * </p>
     * <p>
     * The default value is {@link org.wrml.model.schema.ComparisonOperator#equalTo} (value equality comparison).
     * </p>
     *
     * @return The {@link org.wrml.model.schema.ComparisonOperator} to be used when comparing the {@link #referenceSlot()} value to the value retrieved from the {@link #valueSource()}.
     */
    ComparisonOperator operator() default ComparisonOperator.equalTo;

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
    String regex() default "";


}
