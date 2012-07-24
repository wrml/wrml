/**
 * Copyright (C) 2012 WRML.org <mark@wrml.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wrml.model;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.UUID;

import org.wrml.event.EventSource;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.ModelException;

/**
 * A long time ago, in a galaxy far far away, computer science was discovered.
 * The concepts of data structures and algorithms being its two major pillars.
 * Computers understood code, so computer scientists coded these concepts (in
 * progressively higher-level languages). Thus, the "coder" role emerged and
 * lots of code is written.
 * 
 * WRML stands for "Web Resource Modeling Language" (and is pronounced
 * "Wormle"). As a idea, WRML wants to help establish a shared, uniform
 * programmable Web platform - to unlock it's real potential. Like any g00d
 * worm, WRML wants to be ported to all programming languages to help spread
 * this uniformity.
 * 
 * WRML, like other model-driven architectures before it, says that data
 * structures are actually just "models" and models don't actually have to be
 * "coded" at all. To promote reuse, abstraction, and separation of concerns;
 * WRML says that data structures are *modeled* and then algorithms are *coded*
 * (as services). Arguably, Java's "interface" and "class" types say this too.
 * 
 * Speaking of Java, a question to the coders out there,
 * "When was the last time you wrote a _getter_ and _setter_ field access method pair without using an IDE wizard or key macro?"
 * 
 * Personally, I *want* to forget the coding syntax needed to do this.
 * 
 * WRML suggests that there are two unique disciplines within computer science
 * *modelers* and *coders*. WRML modelers can use intuitive design tools. WRML
 * coders can implement services any way they want (Java not required).
 * 
 * A model is the base concept for all WRML "data structures". In the Java
 * implementation terms, {@link Model} is the base interface of all Web resource
 * {@link Schema} "proxy" instances.
 * 
 * This interface represents a "model" in both the design-time context of data
 * "modeling" and the runtime context of a client-server "Model" View Controller
 * (MVC) application.
 */
public interface Model extends EventSource<ModelEventListener> {

    public void delete();

    /**
     * Create a new (Java) proxy which implements the (Java) interface
     * associated with the identified (WRML) schema.
     * 
     * See HTML's <a
     * href="http://www.w3.org/TR/html5/links.html#rel-alternate">alternate link
     * relation</a> for some inspiration.
     * 
     * @param dimensions
     *            The dimensions to represent with the new {@link Proxy}.
     * 
     * @return A new {@link Proxy} instance that represents this model instance
     *         using
     *         the specified dimensions.
     * 
     * @throws ModelException
     *             Thrown if there are problems tunneling through the worm hole.
     */
    public <M extends Model> M getAlternate(Dimensions dimensions) throws ModelException;

    /**
     * Returns the runtime context for this model instance.
     * 
     * @return The model's runtime context.
     */
    public Context getContext();

    /**
     * Get the {@link Dimensions} that came with this Model's instantiation.
     * 
     * @return The {@link Dimensions} associated with this model instance.
     */
    public Dimensions getDimensions();

    /**
     * Get the specified field name.
     * 
     * @param fieldName
     *            The name of the field value to read.
     * 
     * @return Either the field's value or null if it could not be read.
     */
    public Object getFieldValue(final String fieldName);

    /**
     * Get the specified field name, which may be declared by the specified
     * schema.
     * 
     * @param schemaId
     *            The schema id associated with the reading of the field (may be
     *            null to read fields for "undefined" models).
     * 
     * @param fieldName
     *            The name of the field value to read.
     * 
     * @return Either the field's value or null if it could not be read.
     */
    public Object getFieldValue(final URI schemaId, final String fieldName);

    /**
     * Get the (internal) heap id of this model.
     * 
     * @return The model's heap id.
     */
    public UUID getHeapId();

    /**
     * Get the schema id associated with this model instance (or null if this
     * model's type is undefined).
     * 
     * @return The schema id associated with this model instance (or null if
     *         this model's type is undefined).
     */
    public URI getSchemaId();

    public void refresh();

    /**
     * Set the specified new value on specified field name.
     * 
     * @param fieldName
     *            The name of the field value to write.
     * 
     * @param newValue
     *            The value to set on the named field.
     * 
     * @return Either the field's prior value or null if it could not be read.
     */
    public Object setFieldValue(final String fieldName, final Object newValue) throws ModelException;

    /**
     * Set the specified new value on specified field name.
     * 
     * @param schemaId
     *            The schema id associated with the writing of the field (may be
     *            null to write raw fields on "undefined" models).
     * 
     * @param fieldName
     *            The name of the field value to write.
     * 
     * @param newValue
     *            The value to set on the named field.
     * 
     * @return Either the field's prior value or null if it could not be read.
     */
    public Object setFieldValue(final URI schemaId, final String fieldName, final Object newValue)
            throws ModelException;

    public <M extends Model> M update();

}
