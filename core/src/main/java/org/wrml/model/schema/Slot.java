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

import org.wrml.model.Described;
import org.wrml.model.Model;
import org.wrml.model.Named;
import org.wrml.model.Titled;
import org.wrml.runtime.schema.Aliases;
import org.wrml.runtime.schema.Description;
import org.wrml.runtime.schema.ProtoSlot;

import java.util.List;

/**
 * <p>
 * {@link Slot} models are embedded within {@link Schema} documents. {@link Slot}s are named and described by
 * {@link Schema} designers (data modelers).
 * </p>
 * <p/>
 * <p>
 * A slot is like a property, attribute, element, field, row, mapping, name/value pair, etc.
 * </p>
 * <p/>
 * <p>
 * <p/>
 * <pre>
 *
 *     Slot
 *     -----------|------------
 *     |  {Name} ---> Value   |
 *     |----------|---- | ----|
 *                      |
 *                      +-- BooleanValue
 *                      +-- DateValue
 *                      +-- DoubleValue
 *                      +-- IntegerValue
 *                      +-- ...
 *                      +-- TextValue
 *
 * </pre>
 * <p/>
 * </p>
 *
 * @see Schema
 * @see Named
 * @see Value
 * @see Model#getSlotValue(String)
 * @see ProtoSlot
 */
@Description("Slot models are embedded within Schemas. Slots are named and described by Schema designers (data modelers).")
public interface Slot extends Named, Titled, Described, Model
{

    /**
     * <p>
     * The {@link Slot}s associated {@link Value}.
     * </p>
     *
     * @see Model#getSlotValue(String)
     * @see ProtoSlot#getHeapValueType()
     */
    @Description("The Value associated with this Slot.")
    Value getValue();

    /**
     * @see #getValue()
     */
    Value setValue(Value value);

    /**
     * (Optional) list of the other names that this slot goes by (useful for deprecation and overlay-based composition).
     *
     * @see Aliases
     */
    @Description("The (optional) list of the other names that this slot goes by (useful for deprecation and overlay-based composition).")
    List<String> getAliases();

    /**
     * A "searchable" slot is one that should be "indexed" (if possible), or otherwise optimized, so that models may be searched for based upon this Slot's value.
     *
     * @return <code>true</code> if this slot is searchable.
     * @see org.wrml.runtime.schema.Searchable
     */
    @Description("A \"searchable\" slot is one that should be \"indexed\" (if possible), or otherwise optimized, so that models may be searched for based upon this Slot's value.")
    Boolean isSearchable();

    Boolean setSearchable(Boolean isSearchable);

}
