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

import org.wrml.model.Abstract;
import org.wrml.model.MaybeReadOnly;
import org.wrml.model.MaybeRequired;

/**
 * The WRML representation of the abstract <code>number</code> (or {@link Number}) primitive base type.
 */
public interface NumericValue extends MaybeReadOnly, MaybeRequired, Primitive, Abstract, Value {

    /**
     * The WRML constant name for a NumericValue's <i>divisibleBy</i> slot.
     */
    public static final String SLOT_NAME_DIVISIBLE_BY = "divisibleBy";

    /**
     * The WRML constant name for a NumericValue's <i>exclusiveMaximum</i> slot.
     */
    public static final String SLOT_NAME_EXCLUSIVE_MAXIMUM = "exclusiveMaximum";

    /**
     * The WRML constant name for a NumericValue's <i>exclusiveMinimum</i> slot.
     */
    public static final String SLOT_NAME_EXCLUSIVE_MINIMUM = "exclusiveMinimum";

    /**
     * The WRML constant name for a NumericValue's <i>maximum</i> slot.
     */
    public static final String SLOT_NAME_MAXIMUM = "maximum";

    /**
     * The WRML constant name for a NumericValue's <i>minimum</i> slot.
     */
    public static final String SLOT_NAME_MINIMUM = "minimum";

    boolean isExclusiveMaximum();

    boolean isExclusiveMinimum();

    boolean setExclusiveMaximum(boolean isExclusiveMaximum);

    boolean setExclusiveMinimum(boolean isExclusiveMinimum);

}
