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

import java.lang.annotation.*;

/**
 * <p>
 * This {@link Annotation} documents the WRML attributes of a {@link org.wrml.model.schema.Schema} when it is
 * represented as a Java interface.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface WRML
{


    /**
     * <p>
     * A <i>universally</i> unique name for the {@link org.wrml.model.schema.Schema} expressed as a {@link org.wrml.util.UniqueName}.
     * </p>
     * <p>
     * NOTE: This value is OPTIONAL and is added automatically by the WRML framework's {@link org.wrml.runtime.schema.generator.SchemaGenerator} and
     * is not intended to be added in .java files. Unlike the other parameters of this {@link Annotation}, the WRML runtime ignores
     * this value and it is used to document the WRML name of the Java interface representing the {@link org.wrml.model.schema.Schema}.
     * The runtime does not need this value since it can be easily derived via Java reflection.
     * </p>
     *
     * @return The {@link org.wrml.model.schema.Schema}'s {@link org.wrml.util.UniqueName}.
     */
    String uniqueName() default "";

    /**
     * <p>
     * An optional {@link String}[] containing the {@link org.wrml.model.schema.Schema}'s "key" {@link org.wrml.model.schema.Slot} names.
     * </p>
     * <p>
     * In Java, a WRML {@link org.wrml.model.schema.Schema} is expressed as a Java interface extending {@link org.wrml.model.Model}.
     * </p>
     * <p>
     * A key names one (or more) of a {@link org.wrml.model.schema.Schema}'s slots, which (in combination) may be used to determine
     * identity and ensure uniqueness between {@link org.wrml.model.Model}s of the same {@link org.wrml.model.schema.Schema}.
     * </p>
     * <p>
     * This annotation is applied to a Schema (not a method) with {@link ElementType#TYPE} so that it may name
     * one or more slots that are declared locally and/or name one or more slots that are declared by a base interface.
     * </p>
     * <p>
     * Keys are crucial to implementing {@link org.wrml.model.Model} singularity, which is the collapsing/merging of two distinct
     * {@link org.wrml.model.Model}s that share the same key value(s). Where, after the merge, there are still two separate {@link org.wrml.model.Model}
     * references, but they will both share the same internal heap id, which results in them sharing the overlapping/common
     * slot value storage.
     * </p>
     *
     * @see org.wrml.model.schema.Schema#getKeySlotNames()
     * @see org.wrml.runtime.Keys
     * @see org.wrml.runtime.CompositeKey
     * @see org.wrml.model.Model#getKeys()
     * @see org.wrml.runtime.Context#getModel(org.wrml.runtime.Keys, org.wrml.runtime.Dimensions)
     * @see Prototype#getAllKeySlotNames()
     * @see org.wrml.runtime.rest.UriTemplate
     * @see org.wrml.runtime.Keys
     * @see org.wrml.model.Model
     * @see org.wrml.model.schema.Schema
     * @see org.wrml.model.schema.Slot
     */
    String[] keySlotNames() default {};

    /**
     * <p>
     * An optional {@link String}[] of {@link org.wrml.model.schema.Slot} names that determine the default/natural sort order for {@link org.wrml.model.Model}s of this
     * {@link org.wrml.model.schema.Schema}.
     * </p>
     *
     */
    String[] comparableSlotNames() default {};

}
