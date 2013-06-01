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

import java.util.List;

/**
 * <p>
 * A {@link ListValue} is a linear ordering of {@link Value}s.
 * </p>
 *
 * @see Model
 * @see Schema
 * @see Slot
 * @see List
 * @see <a href="http://www.json.org">JSON array</a>
 */
public interface ListValue extends Primitive, Value
{

    /**
     * The WRML constant name for a ListValue's <i>elementUniquenessConstrained</i> slot.
     */
    public static final String SLOT_NAME_ELEMENT_UNIQUENESS_CONSTRAINED = "elementUniquenessConstrained";
    /**
     * The WRML constant name for a ListValue's <i>maximumSize</i> slot.
     */
    public static final String SLOT_NAME_MAXIMUM_SIZE = "maximumSize";
    /**
     * The WRML constant name for a ListValue's <i>minimumSize</i> slot.
     */
    public static final String SLOT_NAME_MINIMUM_SIZE = "minimumSize";

    /**
     * <p>
     * The {@link Slot} that represents each of the {@link ListValue}'s elements.
     * </p>
     * <p>
     * <p>
     * <i>Design Note: Supporting "generic" Schemas</i>
     * </p>
     * Make this an <i>optional</i> {@link Slot} value that may be used to constrain the {@link ListValue}'s element {@link Value}
     * type. The <i>name</i> component of the <i>name=value</i> {@link Slot} is used to identify the parameterized
     * {@link Value} variable (e.g. "E" as in List<E>) associated with a {@link Slot} with an "unbound" value (meaning
     * that {@link Slot#getValue()} returns <code>null</code>). For {@link ListValue}s with a known/restricted element
     * {@link Value} type, the {@link Slot}'s name can be left <code>null</code>.
     * </p>
     */
    Slot getElementSlot();

    /**
     * The maximum number of elements that are allowed in the {@link ListValue}.
     *
     * @return The maximum number of elements that are allowed in the {@link ListValue}.
     */
    Integer getMaximumSize();

    /**
     * The minimum number of elements that are allowed in the {@link ListValue}.
     *
     * @return The minimum number of elements that are allowed in the {@link ListValue}.
     */
    Integer getMinimumSize();

    /**
     * The flag that determines if this {@link ListValue} allows duplicate entries.
     * <p/>
     * <i>Design Note: Supporting Set Values</i>
     * </p>
     * This flag is currently ignored. Consider supporting a Set ({@link java.util.Set} value type instead.
     * </p>
     *
     * @return The flag that determines if this {@link ListValue} allows duplicate entries.
     */
    Boolean isElementUniquenessConstrained();

    /**
     * @see #getElementSlot()
     */
    Slot setElementSlot(final Slot slot);

    /**
     * @see #isElementUniquenessConstrained()
     */
    Boolean setElementUniquenessConstrained(final boolean isElementUniquenessConstrained);

    /**
     * @see #getMaximumSize()
     */
    Integer setMaximumSize(final Integer maxSize);

    /**
     * @see #getMinimumSize()
     */
    Integer setMinimumSize(final Integer minSize);
}
