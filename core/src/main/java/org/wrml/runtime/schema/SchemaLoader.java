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

import org.wrml.model.Abstract;
import org.wrml.model.format.Format;
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.schema.Choices;
import org.wrml.model.schema.Schema;
import org.wrml.model.schema.ValueType;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.Loader;
import org.wrml.runtime.format.application.schema.json.JsonSchema;
import org.wrml.runtime.format.application.schema.json.JsonSchemaLoader;
import org.wrml.util.UniqueName;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.SortedSet;

/**
 * <p>
 * The SchemaLoader is a ClassLoader that is specialized to load WRML Schema-based Java classes. It acts as both a
 * schema class loader and a dynamic (just-in-time) schema class "compiler".
 * </p>
 * <p/>
 * <p>
 * The SchemaLoader is also responsible for loading org.wrml.model.schema.Choices as java.lang.Enum classes.
 * </p>
 */
public interface SchemaLoader extends Loader
{
    /**
     * Get the {@link URI} of {@link org.wrml.model.rest.AggregateDocument}.
     */
    URI getAggregateDocumentSchemaUri();

    /**
     * Get the standard {@link Dimensions} associated with an {@link org.wrml.model.rest.Api} model.
     */
    Dimensions getApiDimensions();

    /**
     * Get the {@link URI} of {@link org.wrml.model.rest.Api}.
     */
    URI getApiSchemaUri();

    /**
     * Get the {@link URI} of {@link org.wrml.model.schema.Choices}.
     */
    URI getChoicesSchemaUri();

    /**
     * Get the standard {@link Dimensions} associated with an {@link org.wrml.model.schema.Choices} model.
     */
    Dimensions getChoicesDimensions();

    /**
     * <p>
     * Get a set of {@link UniqueName}s to identify the known (<i>loadable</i>) {@link Choices} documents within the specified namespace.
     * </p>
     */
    SortedSet<UniqueName> getChoicesNames(final UniqueName namespace);

    /**
     * Get the {@link SchemaLoaderConfiguration} associated with this SchemaLoader.
     */
    SchemaLoaderConfiguration getConfig();

    /**
     * Get the runtime {@link Context} which owns this {@link SchemaLoader}.
     */
    Context getContext();

    /**
     * Get the {@link URI} of {@link org.wrml.model.rest.Document}.
     */
    URI getDocumentSchemaUri();

    /**
     * Get the {@link URI} of {@link org.wrml.model.rest.Embedded}.
     */
    URI getEmbeddedSchemaUri();

    /**
     * Get the standard {@link Dimensions} associated with an {@link Format} model.
     */
    Dimensions getFormatDimensions();

    /**
     * Get the {@link URI} of {@link org.wrml.model.format.Format}.
     */
    URI getFormatSchemaUri();

    /**
     * <p>
     * Get the (optional) {@link JsonSchemaLoader} used to load schemas in the JSON Schema format.
     * Will return <code>null</code> if JSON Schema loading is not available within the current runtime.
     * </p>
     */
    JsonSchemaLoader getJsonSchemaLoader();

    /**
     * Get the standard {@link Dimensions} associated with an {@link LinkRelation} model.
     */
    Dimensions getLinkRelationDimensions();

    /**
     * Get the {@link URI} of {@link org.wrml.model.rest.LinkRelation}.
     */
    URI getLinkRelationSchemaUri();

    /**
     * Get the {@link URI} of {@link org.wrml.model.rest.Link}.
     */
    URI getLinkSchemaUri();

    /**
     * Get a previously loaded {@link Schema} by its keys.
     */
    Schema getLoadedSchema(final Keys keys);

    /**
     * Get a set of {@link URI}s to identify the currently loaded {@link Schema}s.
     */
    SortedSet<URI> getLoadedSchemaUris();

    /**
     * Get a <i>native</i> {@link Schema} by its keys. A native {@link Schema} is loaded as a Java {@link Class} (<i><b>interface</b></i>) found in the classpath.
     */
    Schema getNativeSchema(final Keys keys);

    /**
     * <p>
     * The WRML runtime assumes that {@link SchemaLoader} implementations also subclass {@link java.lang.ClassLoader}.
     * </p>
     * <p>
     * In Java, {@link java.lang.ClassLoader} is not an interface, but if it were, this interface would like to extend it.
     * </p>
     *
     * @see ClassLoader#getParent()
     */
    ClassLoader getParent();

    /**
     * <p>
     * Get the {@link Prototype} associated with the {@link URI}-identified {@link Schema}.
     * </p>
     * <p>
     * The {@link SchemaLoader} will "prototype" each identified {@link Schema} only once and it will retain the "pre-compiled" {@link Prototype} in the heap.
     * Calling this method with a <i>valid</i> {@link Schema} {@link URI}, that has <i>not</i> yet been <i>prototyped</i>, will create a new {@link Prototype} instance to retain and return.
     * </p>
     */
    Prototype getPrototype(final URI schemaUri);

    /**
     * Get the {@link URI} of {@link org.wrml.model.rest.ResourceTemplate}.
     */
    URI getResourceTemplateSchemaUri();

    /**
     * Get the standard {@link Dimensions} associated with an {@link Schema} model.
     */
    Dimensions getSchemaDimensions();

    /**
     * <p>
     * Get the Java {@link Class} (<i><b>interface</b></i>) representation of the {@link URI}-identified {@link Schema}.
     * </p>
     * <p>
     * The {@link SchemaLoader} will "load" each identified {@link Class} only once and it will retain them in the heap.
     * Calling this method with a <i>valid</i> {@link Schema} {@link URI}, that has <i>not</i> yet been <i>loaded</i>, will load a possibly auto-generated {@link Class} to retain and return.
     * </p>
     */
    Class<?> getSchemaInterface(final URI schemaUri) throws ClassNotFoundException;

    byte[] getSchemaInterfaceBytecode(final Schema schema) throws ClassNotFoundException;

    /**
     * <p>
     * Get the native type name associated with the {@link URI}-identified <i>type</i> (either a {@link Schema} or {@link Choices}).
     * </p>
     */
    String getNativeTypeName(final URI typeUri);

    /**
     * <p>
     * Get a set of {@link UniqueName}s to identify the known (<i>loadable</i>) {@link Schema} documents within the specified namespace.
     * </p>
     */
    SortedSet<UniqueName> getSchemaNames(final UniqueName namespace);

    /**
     * <p>
     * Get a set of {@link UniqueName}s to identify the known (<i>browseable</i>) {@link org.wrml.model.schema.SchemaNamespace} documents within the specified namespace.
     * </p>
     */
    SortedSet<UniqueName> getSchemaSubnamespaces(final UniqueName namespace);

    /**
     * Get the standard {@link Dimensions} associated with an {@link org.wrml.model.schema.SchemaNamespace} model.
     */
    Dimensions getSchemaNamespaceDimensions();

    /**
     * Get the {@link URI} of {@link org.wrml.model.schema.SchemaNamespace}.
     */
    URI getSchemaNamespaceSchemaUri();

    /**
     * Get the {@link URI} of {@link org.wrml.model.schema.Schema}.
     */
    URI getSchemaSchemaUri();

    /**
     * Get the {@link URI} of {@link org.wrml.model.schema.Syntax}.
     */
    URI getSyntaxSchemaUri();

    /**
     * <p>
     * Get the {@link URI} associated with the native type name-identified <i>type</i> (either a {@link Class} or {@link java.lang.Enum}).
     * </p>
     */
    URI getTypeUri(final String typeName);

    /**
     * <p>
     * Get the {@link URI} associated with the native type-identified <i>type</i> (either a {@link Class} or {@link java.lang.Enum}).
     * </p>
     */
    URI getTypeUri(final Type type);

    /**
     * Get the {@link UniqueName} for the {@link Schema} or {@link Choices} document identified by the specified {@link URI}.
     *
     * @param uri The identity of the {@link Schema} or {@link Choices} document to be named.
     * @return The {@link UniqueName} associated with the identified type document.
     */
    UniqueName getTypeUniqueName(final URI uri);

    /**
     * Get a previously loaded {@link Choices} by its keys.
     */
    Choices getLoadedChoices(final Keys keys);

    /**
     * Get a <i>native</i> {@link Choices} by its keys. A native {@link Choices} is loaded as a Java {@link Enum} found in the classpath.
     */
    Choices getNativeChoices(final Keys keys);

    /**
     * <p>
     * Get a set of {@link URI}s to identify the currently loaded {@link Prototype}s.
     * </p>
     */
    SortedSet<URI> getPrototypedSchemaUris();

    /**
     * Get the {@link URI} of {@link org.wrml.model.Virtual}.
     */
    URI getVirtualSchemaUri();

    /**
     * Get the WRML value type associated with the native type.
     */
    ValueType getValueType(final Type type);

    /**
     * A {@link Class} is an <i>abstract {@link Schema}</i> if it claims {@link Abstract} as one of it's immediate base {@link Schema}s.
     */
    boolean isAbstractSchema(final Class<?> schemaInterface);

    /**
     * A {@link Schema} ({@link Class}) is <i>prototyped</i> if this {@link SchemaLoader} has already created and "cached" an associated {@link Prototype}.
     */
    boolean isPrototyped(final URI schemaUri);

    /**
     * <p>
     * A {@link Schema} {@link Type} is a <i>subschema</i> of a <i>base</i> {@link Schema} {@link Type} following the interface inheritance rules of Java.
     * </p>
     * <p>
     * If {@link Schema} <b>X</b> lists {@link Schema} <b>Y</b> as one of it's immediate base schemas or, recursively if any of <b>X</b>'s base schemas list <b>Y</b> as a basis, then <b>X</b> is a <i>subschema</i> of <b>Y</b>.
     * </p>
     */
    boolean isSubschema(final Type base, final Type sub);

    /**
     * <p>
     * A {@link Schema} {@link Type} is a <i>system</i> schema if it represents one of the WRML system's built-in concepts (e.g. {@link Schema}, {@link Choices}, {@link org.wrml.model.rest.Api}, etc).
     * </p>
     */
    boolean isSystemSchema(final URI schemaUri);

    /**
     * <p>
     * Load and get a {@link Schema} associated with the specified {@link JsonSchema}, "appending" the optional list of {@link URI}-identified base {@link Schema}s.
     * </p>
     */
    Schema load(final JsonSchema jsonSchema, final URI... baseSchemaUris) throws UnsupportedOperationException;

    /**
     * <p>
     * Load and get a {@link Schema} associated with the specified {@link Schema}.
     * </p>
     */
    Schema load(final Schema schema);

    /**
     * <p/>
     * Load and get a {@link Schema} (by {@link URI} id) that conforms to the specified {@link Dimensions}.
     * <p/>
     * <p/>
     * Note that calling this may involve communication with a local or remote
     * service (e.g. the Web) that is registered to handle interactions with {@link Schema} (the "meta-schema").
     * <p/>
     */
    Schema load(final URI schemaUri);

    /**
     * <p>
     * Load and get a {@link Choices} associated with the specified {@link Choices}.
     * </p>
     */
    Choices loadChoices(final Choices model);

    /**
     * <p>
     * The WRML runtime assumes that {@link SchemaLoader} implementations also subclass {@link java.lang.ClassLoader}.
     * </p>
     * <p>
     * In Java, {@link java.lang.ClassLoader} is not an interface, but if it were, this interface would like to extend it.
     * </p>
     *
     * @see ClassLoader#loadClass(String)
     */
    Class<?> loadClass(final String typeName) throws ClassNotFoundException;

}