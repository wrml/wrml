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
package org.wrml.runtime;

import org.wrml.model.Model;
import org.wrml.model.format.Format;
import org.wrml.model.rest.Method;
import org.wrml.model.rest.ResourceOptions;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.format.FormatLoader;
import org.wrml.runtime.format.ModelReadingException;
import org.wrml.runtime.format.ModelWriteOptions;
import org.wrml.runtime.format.ModelWritingException;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.runtime.service.ServiceLoader;
import org.wrml.runtime.service.cache.ModelCache;
import org.wrml.runtime.syntax.SyntaxLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

/**
 * The primary interface for the WRML runtime; exposes Model-related interactions.
 */
public interface Context
{

    /**
     * Delete the {@link Model} identified by the {@link Keys}.
     * 
     * @param keys
     *            the Keys that identify the Model to delete.
     * @param dimensions
     *            the Dimension that may help inform/scope the delete request.
     * @throws ContextException
     */
    void deleteModel(final Keys keys, final Dimensions dimensions) throws ContextException;

    /**
     * The {@link ApiLoader} component owned by this {@link Context}.
     */
    ApiLoader getApiLoader();

    /**
     * The configuration.
     */
    ContextConfiguration getConfig();

    /**
     * Get the value mapped to the key associated with the specified schema interface.
     * 
     * @param keys
     *            the Keys containing the value to extract.
     * @param schemaInterface
     *            the identity of the schema that declared the desired key.
     * @param <V>
     *            the generic return type of the desired key's value.
     * @return the value associated with the desired key.
     */
    <V> V getKeyValue(final Keys keys, Class<?> schemaInterface);

    /**
     * Get the value mapped to the key associated with the specified slot name.
     * 
     * @param keys
     *            the Keys containing the value to extract.
     * @param slotName
     *            the name of the slot associated with the desired key's value.
     * @param <V>
     *            the generic return type of the desired key's value.
     * @return the value associated with the desired key.
     */
    <V> V getKeyValue(final Keys keys, String slotName);

    /**
     * The {@link FormatLoader} component owned by this {@link Context}.
     */
    FormatLoader getFormatLoader();

    /**
     * Get the {@link Model} with the specified {@link Keys} and requested {@link Dimensions}
     * 
     * @param keys
     *            the Keys that identify the Model to retrieve.
     * @param dimensions
     *            the Dimension that may help inform/scope the retrieval request.
     * @param <M>
     *            the generic return type of the desired Model.
     * @return the requested Model (or null if the model was not found).
     */
    <M extends Model> M getModel(final Keys keys, final Dimensions dimensions) throws ContextException;

    /**
     * The optional {@link ModelCache} component owned by this {@link Context}.
     */
    ModelCache getModelCache();

    /**
     * The {@link ModelBuilder} component owned by this {@link Context}.
     */
    ModelBuilder getModelBuilder();

    <M extends Model> List<M> getMultipleModels(final List<Keys> multipleKeys, final Dimensions sameDimensions) throws ContextException;

    /**
     * The {@link SchemaLoader} component owned by this {@link Context}.
     */
    SchemaLoader getSchemaLoader();

    /**
     * The {@link ServiceLoader} component owned by this {@link Context}.
     */
    ServiceLoader getServiceLoader();

    /**
     * The {@link SyntaxLoader} component owned by this {@link Context}.
     */
    SyntaxLoader getSyntaxLoader();

    /**
     * Initialize the Context from configuration.
     * 
     * @param config
     *            The Context's configuration
     */
    void init(ContextConfiguration config) throws ContextException;

    /**
     * Creates a new Model conforming to the specified interface.
     * 
     * @see ModelBuilder#newModel(Class)
     * @see ModelBuilder#newDimensions(Class)
     */
    <M extends Model> M newModel(final Class<?> schemaInterface) throws ModelBuilderException;

    /**
     * Creates a new Model conforming to the specified {@link Dimensions}.
     * 
     * @see ModelBuilder#newModel(Dimensions)
     */
    <M extends Model> M newModel(final Dimensions dimensions);

    /**
     * Creates a new Model conforming to the specified Schema.
     * 
     * @see ModelBuilder#newModel(URI)
     * @see ModelBuilder#newDimensions(URI)
     */
    <M extends Model> M newModel(final URI schemaUri) throws ModelBuilderException;

    /**
     * Returns a the resource {@link Method#Options options} associated with this {@link Model}.
     * 
     * @see {@link ResourceOptions}
     */
    <M extends Model> M optionsModel(final Model model) throws ContextException;

    /**
     * Reads a Model, conforming to the specified {@link Dimensions}, associated with the specified {@link Keys} from the specified {@link InputStream}, which is assumed to be
     * formatted with the current default {@link Format}.
     */
    <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys, final Dimensions rootModelDimensions) throws ModelReadingException;

    /**
     * Reads a Model, conforming to the specified {@link Dimensions}, associated with the specified {@link Keys} from the specified {@link InputStream}, which is assumed to be
     * formatted with the identified {@link Format}.
     */
    <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys, final Dimensions rootModelDimensions, final URI formatUri) throws ModelReadingException;

    /**
     * Reads a Model, conforming to the specified {@link Schema}, associated with the specified {@link URI} (key) from the specified {@link InputStream}, which is assumed to be
     * formatted with the identified {@link Format}.
     */
    <M extends Model> M readModel(final InputStream in, final URI uri, final URI schemaUri, final URI formatUri) throws ModelReadingException;

    // TODO: The WrmlServlet needs to create & init Dimensions
    // TODO: Remove this once we have resolved: https://wrmlorg.jira.com/browse/WRML-360
    @Deprecated
    <M extends Model> M request(final Method requestMethod, final URI uri, URI schemaUri, final Model parameter);

    /**
     * Requests a Model, conforming to the specified Dimensions, associated with the specified {@link Keys} using the specified request {@link Method} (which may optionally accept
     * a parameter).
     * 
     * @see #visitLink(Model, String, DimensionsBuilder, Model)
     */
    <M extends Model> M request(Method requestMethod, final Keys keys, final Dimensions dimensions, final Model parameter);

    /**
     * Saves the {@link Model} and returns the saved version of the {@link Model}.
     */
    <M extends Model> M saveModel(final M model) throws ContextException;

    /**
     * Visits the named link, requesting the {@link Model} from the end point.
     */
    <M extends Model> M visitLink(Model model, String linkSlotName);

    /**
     * Visits the named link, requesting the {@link Model} from the end point.
     */
    <M extends Model> M visitLink(final Model model, final String linkSlotName, DimensionsBuilder dimensionsBuilder, final Model parameter) throws ContextException;

    /**
     * Writes a model's representation to the OutputStream provided.
     * 
     * @param out
     *            The output stream receiving the model bits.
     * @param model
     *            The model to write out.
     * @throws ModelWritingException
     */
    void writeModel(final OutputStream out, final Model model) throws ModelWritingException;

    /**
     * Writes a model's representation to the OutputStream provided.
     * 
     * @param out
     *            The output stream receiving the model bits.
     * @param model
     *            The model to write out.
     * @param writeOptions
     *            The options for writing.
     * @throws ModelWritingException
     */
    void writeModel(final OutputStream out, final Model model, final ModelWriteOptions writeOptions) throws ModelWritingException;

    /**
     * Writes a model's representation to the OutputStream provided.
     * 
     * @param out
     *            The output stream receiving the model bits.
     * @param model
     *            The model to write out.
     * @param writeOptions
     *            The options for writing.
     * @param formatUri
     *            The format of the output.
     * @throws ModelWritingException
     */
    void writeModel(final OutputStream out, final Model model, final ModelWriteOptions writeOptions, final URI formatUri) throws ModelWritingException;

    /**
     * Writes a model's representation to the OutputStream provided.
     * 
     * @param out
     *            The output stream receiving the model bits.
     * @param model
     *            The model to write out.
     * @param formatUri
     *            The format of the output.
     * @throws ModelWritingException
     */
    void writeModel(final OutputStream out, final Model model, final URI formatUri) throws ModelWritingException;

}
