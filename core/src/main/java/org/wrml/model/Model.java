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
package org.wrml.model;

import org.wrml.model.schema.Schema;
import org.wrml.model.schema.Slot;
import org.wrml.runtime.*;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.service.Service;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

/**
 * <h1>WRML - Web Resource Modeling Language</h1>
 * <p/>
 * <p>
 * <pre>
 *    __     __   ______   __    __   __
 *   /\ \  _ \ \ /\  == \ /\ "-./  \ /\ \
 *   \ \ \/ ".\ \\ \  __< \ \ \-./\ \\ \ \____
 *    \ \__/".~\_\\ \_\ \_\\ \_\ \ \_\\ \_____\
 *     \/_/   \/_/ \/_/ /_/ \/_/  \/_/ \/_____/
 * </pre>
 * </p>
 * <p/>
 * <h2>
 * <a href="http://www.wrml.org">http://www.wrml.org</a></h2>
 * <p/>
 * <p>
 * Copyright 2013 Mark Masse (OSS project WRML.org)
 * </p>
 * <p/>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * </p>
 * <p/>
 * <p>
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 * </p>
 * <p/>
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * </p>
 * <p/>
 * <p>
 * WRML stands for "Web Resource Modeling Language" (and is pronounced "Wormle"). As a idea, WRML wants to help
 * establish a shared, uniform programmable Web platform - to unlock it's real potential. WRML wants to be ported to
 * other programming languages to help spread this uniformity.
 * </p>
 * <p/>
 * <p>
 * WRML, like other model-driven architectures before it, says that data structures are "{@link Model}s", which don't
 * need be "coded". To promote reuse, abstraction, and separation of concerns; WRML says that data structures are
 * *modeled* and algorithms are *coded* (in {@link Service}s).
 * </p>
 * <p/>
 * <p>
 * WRML suggests that there are two unique disciplines within computer science *modelers* and *coders*. WRML modelers
 * can use intuitive design tools. WRML coders can implement services any way they want (Java not required).
 * </p>
 * <p/>
 * <p>
 * A {@link Model} is the base concept for all WRML "data structures". {@link Model} is the base interface of all Web
 * resource {@link Schema} {@link Proxy} instances. In addition to the {@link Map}-like (or JSON-like) get/set slot
 * value methods defined by the {@link Model} interface, WRML's runtime ensures that all {@link Model} instances also
 * provide a Java POJO-style API, with <i>getters</i> and <i>setters</i> for each {@link Slot} in the {@link Model}'s
 * associated {@link Schema}.
 * </p>
 *
 * @see Schema
 * @see Context
 * @see Context#newModel(Class)
 * @see Context#getModel(Keys, Dimensions)
 * @see Context#deleteModel(Keys, Dimensions)
 * @see Context#saveModel(Model)
 * @see Keys
 * @see Dimensions
 * @see Dimensions#getSchemaUri()
 * @see Service
 * @see Proxy
 */
public interface Model
{

    /**
     * The WRML constant name for a Model's <i>schemaUri</i> slot.
     */
    public static final String SLOT_NAME_SCHEMA_URI = "schemaUri";
    /**
     * The WRML constant name for a Model's <i>heapId</i> slot.
     */
    public static final String SLOT_NAME_HEAP_ID = "heapId";

    /**
     * Clears a slot value, erasing it completely. Where a call to {@link #setSlotValue(String, Object)} like,
     * <code>setSlotValue("name", null)</code>, will leave the "name" slot with a <code>null</code> value,
     * {@link #clearSlotValue(String)} a call to <code>clearSlotValue("name")</code> will clear the value; erasing
     * it from the heap.
     *
     * @return The slot's value prior to being cleared. A <code>null</code> value is returned in cases when the slot was
     *         already clear or actually contained a <code>null</code> value. Call {@link #containsSlotValue(String)}
     *         prior to calling this method when you need to know the difference.
     * @see #containsSlotValue(String)
     * @see #setSlotValue(String, Object)
     */
    Object clearSlotValue(final String slotName);

    /**
     * Checks to see if the named slot contains a value. Any stored value, even a <code>null</code> value, will result
     * in a return value of <code>true</code>.
     *
     * @see #getSlotValue(String)
     * @see #setSlotValue(String, Object)
     * @see #clearSlotValue(String)
     */
    boolean containsSlotValue(final String slotName);

    /**
     * Returns the runtime context for this model instance.
     *
     * @return The model's runtime context.
     */
    Context getContext();

    /**
     * Get the {@link Dimensions} that came with this Model's instantiation.
     *
     * @return The {@link Dimensions} associated with this model instance.
     * @see Model#getSchemaUri()
     */
    Dimensions getDimensions();

    /**
     * Get the (internal) heap id of this model.
     * <p/>
     * <p>
     * Within the same model heap, other models may share this same id in order to achieve instance folding (model
     * singularity) and support other use cases where cross-model shared/overlapping slots are handy. For example,
     * "lightweight" and "heavier weight" models of the same data may overlap on the lightweight model's complete slot
     * set.
     * </p>
     *
     * @return The model's heap id.
     */
    UUID getHeapId();

    /**
     * The (optional) {@link Keys} associated with this model. A model's (optional) key slot values are what makes it
     * unique within
     * the domain of its {@link Schema} (and base schemas).
     *
     * @return the {@link Keys} associated with this model. The keys will be <code>null</code> if this model isn't
     *         "special"; meaning that there is nothing <i>unique</i> about this model.
     */
    Keys getKeys();

    /**
     * The (optional) name of the {@link Service} that originally "got" (originated) this model. A model's origin {@link Service} is
     * name is <code>null</code> if the model did not come from a Service.
     *
     * @return the name of the {@link Service} that brought this model into the {@link Context}.
     */
    String getOriginServiceName();

    /**
     * The {@link Prototype} associated with this model's {@link Schema}.
     *
     * @return the model's {@link Prototype}.
     */
    Prototype getPrototype();

    /**
     * Get the schema URI associated with this model instance (or null if this
     * model's type is undefined).
     *
     * @return The schema URI associated with this model instance (or null if
     *         this model's type is undefined).
     * @see Model#getDimensions()
     * @see Dimensions#getSchemaUri()
     * @see Model#getKeys()
     */
    URI getSchemaUri();

    /**
     * Get a {@link Map} containing the slot name/value pairs, where the values have been explicitly set (by client or
     * framework code).
     */
    Map<String, Object> getSlotMap();

    /**
     * Get the specified slot name.
     *
     * @param slotName The name of the slot value to read.
     * @return Either the slot's value or null if it could not be read.
     */
    Object getSlotValue(final String slotName);

    /**
     * Initialize the key slots in this {@link Model} using the specified {@link Keys}.
     *
     * @param keys The {@link Keys}, whose values should be applied to any matching named slot with either a currently
     *             <code>null</code> value or a non-null, equal value to the Key value applied. Once a {@link Model}'s
     *             key slot has been set (non-null), it is considered immutable by the runtime.
     */
    void initKeySlots(Keys keys);

    /**
     * Create a new (Java) local model instance which implements the (Java) interface associated with the identified
     * (WRML) schema.
     * <p/>
     * See HTML's <a
     * href="http://www.w3.org/TR/html5/links.html#rel-alternate">alternate link
     * relation</a> for some inspiration.
     *
     * @param dimensions The dimensions to represent with the new {@link Proxy}.
     * @return A new {@link Proxy} instance that represents this model instance
     *         using the specified dimensions.
     * @throws ModelException Thrown if there are problems tunneling through the worm hole.
     */
    <M extends Model> M newAlternate(Dimensions dimensions) throws ModelException;

    /**
     * Makes a new, local copy of this model, copying all of the "valued slots" (including <code>null</code> values).
     *
     * @return The model's "clone" (different heap id and slot storage), which is not the same as an "alternate" (same
     *         shared heap id and slot storage).
     */
    <M extends Model> M newCopy() throws ModelException;

    /**
     * Visit the named link slot, returning the response entity as a model.
     *
     * @param linkSlotName the link slot to reference.
     * @param <E>          the response model type.
     * @return the response model.
     */
    <E extends Model> E reference(final String linkSlotName) throws ModelException;

    /**
     * Visit the named link slot, returning the response entity as a model.
     *
     * @param linkSlotName      the link slot to reference.
     * @param dimensionsBuilder the DimensionsBuilder for the requested model.
     * @param <E>               the response model type.
     * @return the response model.
     */
    <E extends Model> E reference(final String linkSlotName, DimensionsBuilder dimensionsBuilder) throws ModelException;

    /**
     * Visit the named link slot, returning the response entity as a model.
     *
     * @param linkSlotName      the link slot to reference.
     * @param dimensionsBuilder the DimensionsBuilder for the requested model.
     * @param parameter         the model to pass along with the reference request.
     * @param <E>               the response model type.
     * @return the response model.
     */
    <E extends Model> E reference(final String linkSlotName, DimensionsBuilder dimensionsBuilder, Model parameter) throws ModelException;

    /**
     * Sets the {@link Model}'s origin {@link Service} name, which may be used for future interactions involving the model's
     * state (e.g. saving, reloading, etc).
     */
    String setOriginServiceName(final String originServiceName);

    /**
     * Set the specified new value on specified slot name.
     *
     * @param slotName The name of the slot value to write.
     * @param newValue The value to set on the named slot.
     * @return Either the slot's prior value or null if it could not be read.
     */
    Object setSlotValue(final String slotName, final Object newValue) throws ModelException;

}
