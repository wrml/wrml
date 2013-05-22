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

import org.wrml.model.*;
import org.wrml.model.rest.Document;
import org.wrml.runtime.schema.Description;
import org.wrml.runtime.schema.WRML;

import java.net.URI;
import java.util.List;

/**
 * <p>
 * Schema models describe the structure for a "class" of {@link Model}s. In this way, {@link Schema} are like Java's
 * {@link Class} object instances. {@link Schema} models are also a bit like: SQL tables, XML DTDS, JSON or XML schemas,
 * AVRO schemas, XLS templates, CMS content types, etc.
 * </p>
 *
 * @see <a
 *      href="http://blog.programmableweb.com/2011/11/18/rest-api-design-putting-the-type-in-content-type/">ProgrammableWeb.com
 *      blog post</a>
 */
@WRML(keySlotNames = {"uniqueName"})
@Description("This is the Meta Schema. Schema models describe the structure for a \"class\" of models.  Schema models are like Java Class object instances. WRML Schema models are also a bit like: SQL tables, XML DTDS, JSON or XML schemas, AVRO schemas, XLS templates, CMS content types, etc.")
public interface Schema extends Described, MaybeReadOnly, Versioned, Tagged, Titled, Thumbnailed, UniquelyNamed, Document
{

    /**
     * The WRML constant name for a Schema's <i>keySlotNames</i> slot.
     */
    public static final String SLOT_NAME_KEY_SLOT_NAMES = "keySlotNames";

    /**
     * <p>
     * A list of this schema's base {@link Schema} ids (List of {@link URI}s).
     * </p>
     * <p/>
     * <p>
     * WRML supports interface style inheritance via {@link URI} reference, thus this schema indicates the schemas that
     * it "inherits" from or "mixes in" to define its own models.
     * </p>
     * <p/>
     * <p>
     * WRML's schematic inheritance is recursive, meaning that base schema's are linked so that a schema virally
     * inherits the base schemas of its base schemas, and their base schemas, and so on...
     * </p>
     *
     * @return An {@link List} containing the base schema documents'
     *         URIs.
     */
    @Description("A list of this schema's base Schema ids (List of URIs). WRML supports interface style inheritance via URI reference, thus this schema indicates the schemas that it \"inherits\" from or \"mixes in\" to define its own models. WRML's schematic inheritance is recursive, meaning that base schema's are linked so that a schema inherits the base schemas of its base schemas, and their base schemas, and so on...")
    List<URI> getBaseSchemaUris();

    /**
     * <p>
     * A list of {@link Slot} names that determine the default/natural sort order for {@link Model}s of this
     * {@link Schema}.
     * </p>
     *
     * @return the {@link List} of {@link String} slot names.
     */
    @Description("A list of Slot names that determine the default/natural sort order for models of this Schema.")
    List<String> getComparableSlotNames();

    /**
     * <p>
     * A list of {@link Slot} names that make up the key/identity. The value(s) of the named {@link Slot}s are used to
     * compute a unique key for {@link Model} instances.
     * </p>
     *
     * @return the {@link List} of {@link String} slot names.
     */
    @Description("A list of Slot names that make up the key/identity. The value(s) of the named Slots are used to compute a unique key for model instances.")
    List<String> getKeySlotNames();

    /**
     * <p>
     * A list of {@link Slot} models. Each {@link Slot} model describes a slot that may be accessed in {@link Model}s
     * representing this {@link Schema}.
     * </p>
     * <p/>
     * <p>
     * The names of the slots must be unique within this {@link Schema}, meaning that the list may not contain more than
     * one {@link Slot} with the same exact value in the {@link Slot}'s "name" slot.
     * </p>
     * <p/>
     * <p>
     * If the name of a {@link Slot} in this list "overrides" a slot of the same name defined in one (or more) of this
     * {@link Schema}'s base schema(s), then models of this schema should consider this shema's slot's constraints to
     * "take precedence" over any similar constraints defined by the ancestral schema(s).
     * </p>
     *
     * @return A List containing this {@link Schema}'s {@link Slot}s.
     */
    @Description("A list of Slot models. Each Slot model describes a slot that may be accessed in models representing this Schema. The names of the slots must be unique within this Schema, meaning that the list may not contain more than one Slot with the same exact value in the Slot's \"name\" slot. If the name of a Slot in this list \"overrides\" a slot of the same name defined in one (or more) of this Schema's base Schema(s), then models of this Schema should consider this Schema's slot's constraints to \"take precedence\" over any similar constraints defined by the ancestral Schema(s).")
    List<Slot> getSlots();

}
