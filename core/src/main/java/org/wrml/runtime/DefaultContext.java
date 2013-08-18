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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.Embedded;
import org.wrml.model.rest.Link;
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.rest.LinkTemplate;
import org.wrml.model.rest.Method;
import org.wrml.model.rest.ResourceOptions;
import org.wrml.model.schema.Choices;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.format.DefaultFormatLoaderFactory;
import org.wrml.runtime.format.FormatLoader;
import org.wrml.runtime.format.Formatter;
import org.wrml.runtime.format.ModelReadingException;
import org.wrml.runtime.format.ModelWriteOptions;
import org.wrml.runtime.format.ModelWritingException;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.rest.ApiNavigator;
import org.wrml.runtime.rest.DefaultApiLoaderFactory;
import org.wrml.runtime.rest.Resource;
import org.wrml.runtime.schema.CollectionPropertyProtoSlot;
import org.wrml.runtime.schema.DefaultSchemaLoaderFactory;
import org.wrml.runtime.schema.LinkProtoSlot;
import org.wrml.runtime.schema.ProtoSearchCriteria;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.runtime.search.SearchCriteria;
import org.wrml.runtime.service.DefaultServiceLoaderFactory;
import org.wrml.runtime.service.Service;
import org.wrml.runtime.service.ServiceLoader;
import org.wrml.runtime.service.cache.ModelCache;
import org.wrml.runtime.service.cache.ModelCacheConfiguration;
import org.wrml.runtime.syntax.DefaultSyntaxLoaderFactory;
import org.wrml.runtime.syntax.SyntaxLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class DefaultContext implements Context
{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultContext.class);

    private ContextConfiguration _Config;

    ApiLoader _ApiLoader;

    private FormatLoader _FormatLoader;

    private ModelBuilder _ModelBuilder;

    private SchemaLoader _SchemaLoader;

    private SyntaxLoader _SyntaxLoader;

    private ServiceLoader _ServiceLoader;

    private ModelCache _ModelCache;

    private Factory<ApiLoader> _ApiLoaderFactory;

    private Factory<FormatLoader> _FormatLoaderFactory;

    private Factory<ModelBuilder> _ModelBuilderFactory;

    private Factory<SchemaLoader> _SchemaLoaderFactory;

    private Factory<SyntaxLoader> _SyntaxLoaderFactory;

    private Factory<ServiceLoader> _ServiceLoaderFactory;

    private Prototype _ApiPrototype;

    private Prototype _SchemaPrototype;

    private Prototype _SyntaxPrototype;

    private Prototype _LinkRelationPrototype;

    private Prototype _FormatPrototype;

    private Prototype _ChoicesPrototype;

    private Prototype _VirtualPrototype;

    public DefaultContext()
    {

        LOGGER.info("Creating new instance of: " + getClass().getCanonicalName());
    }

    @Override
    public void init(final ContextConfiguration config) throws ContextException
    {

        if (config == null)
        {
            throw new ContextException("The WRML context configuration cannot be null.", null, this);
        }

        _Config = config;

        _SchemaLoaderFactory = createSchemaLoaderFactory();
        _SchemaLoader = _SchemaLoaderFactory.create();
        _SchemaLoader.init(this);

        _ModelBuilderFactory = createModelBuilderFactory();
        _ModelBuilder = _ModelBuilderFactory.create();
        _ModelBuilder.init(this);

        _SyntaxLoaderFactory = createSyntaxLoaderFactory();
        _SyntaxLoader = _SyntaxLoaderFactory.create();
        _SyntaxLoader.init(this);

        _ApiPrototype = _SchemaLoader.getPrototype(_SchemaLoader.getApiSchemaUri());
        _SchemaPrototype = _SchemaLoader.getPrototype(_SchemaLoader.getSchemaSchemaUri());
        _SyntaxPrototype = _SchemaLoader.getPrototype(_SchemaLoader.getSyntaxSchemaUri());
        _LinkRelationPrototype = _SchemaLoader.getPrototype(_SchemaLoader.getLinkRelationSchemaUri());
        _FormatPrototype = _SchemaLoader.getPrototype(_SchemaLoader.getFormatSchemaUri());
        _ChoicesPrototype = _SchemaLoader.getPrototype(_SchemaLoader.getChoicesSchemaUri());
        _VirtualPrototype = _SchemaLoader.getPrototype(_SchemaLoader.getVirtualSchemaUri());

        _ApiLoaderFactory = createApiLoaderFactory();
        _ApiLoader = _ApiLoaderFactory.create();
        _ApiLoader.init(this);

        _FormatLoaderFactory = createFormatLoaderFactory();
        _FormatLoader = _FormatLoaderFactory.create();
        _FormatLoader.init(this);

        _ServiceLoaderFactory = createServiceLoaderFactory();
        _ServiceLoader = _ServiceLoaderFactory.create();
        _ServiceLoader.init(this);

        _ServiceLoader.loadInitialState();

        _SyntaxLoader.loadInitialState();
        _FormatLoader.loadInitialState();
        _SchemaLoader.loadInitialState();
        _ApiLoader.loadInitialState();

        // TODO: init cache
        _ModelCache = createModelCache();
    }

    @Override
    public final void deleteModel(final Keys keys, final Dimensions dimensions)
    {

        if (keys == null)
        {
            throw new ContextException("The keys cannot be null", null, this);
        }

        final ModelCache cache = getModelCache();
        if (cache != null)
        {
            cache.delete(keys, dimensions);
        }

        final SchemaLoader schemaLoader = getSchemaLoader();
        final URI uri = keys.getValue(schemaLoader.getDocumentSchemaUri());
        if (uri != null)
        {
            final URI schemaUri = _ApiLoader.getDefaultResponseSchemaUri(Method.Get, uri);

            if (schemaUri != null)
            {

                final ServiceLoader serviceLoader = getServiceLoader();
                final Service service = serviceLoader.getServiceForSchema(schemaUri);
                if (service != null)
                {
                    service.delete(keys, dimensions);
                }
            }
        }
    }

    @Override
    public ApiLoader getApiLoader()
    {

        return _ApiLoader;
    }

    @Override
    public ContextConfiguration getConfig()
    {

        return _Config;
    }

    @Override
    public <V> V getKeyValue(final Keys keys, final Class<?> schemaInterface)
    {

        return keys.getValue(getSchemaLoader().getTypeUri(schemaInterface));
    }

    @Override
    public <V> V getKeyValue(final Keys keys, final String slotName)
    {

        final Set<URI> keyedSchemaUris = keys.getKeyedSchemaUris();
        final SchemaLoader schemaLoader = getSchemaLoader();
        for (final URI schemaUri : keyedSchemaUris)
        {
            final Prototype prototype = schemaLoader.getPrototype(schemaUri);
            if (prototype.isKeySlot(slotName))
            {
                final Object value = keys.getValue(schemaUri);

                if (value != null)
                {
                    return (V) value;
                }
            }
        }

        return null;
    }

    @Override
    public FormatLoader getFormatLoader()
    {

        return _FormatLoader;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <M extends Model> M getModel(final Keys keys, final Dimensions dimensions) throws ContextException
    {

        if (keys == null)
        {
            throw new IllegalArgumentException("The keys cannot be null.");
        }

        if (keys.getCount() == 0)
        {
            throw new IllegalArgumentException("The keys cannot be empty.");
        }

        if (dimensions == null)
        {
            throw new IllegalArgumentException("The dimensions cannot be null.");
        }

        final URI schemaUri = dimensions.getSchemaUri();

        if (schemaUri == null)
        {
            throw new IllegalArgumentException("The schema URI cannot be null.");
        }

        LOGGER.trace("Getting Model\n - Keys:\n{}\n - Dimensions:\n{}", keys, dimensions);

        final ModelCache cache = getModelCache();
        final SchemaLoader schemaLoader = getSchemaLoader();

        M model = null;

        if (_VirtualPrototype.isAssignableFrom(schemaUri))
        {
            // Virtual models are auto-initialized by the runtime
            model = newModel(dimensions);
            model.initKeySlots(keys);
        }
        else if (schemaLoader.isSystemSchema(schemaUri))
        {
            model = getSystemModel(keys, dimensions);
        }
        else if (cache != null && cache.contains(keys, dimensions))
        {
            model = (M) cache.get(keys, dimensions);
            model.initKeySlots(keys);
            model = (M) cache.save(model);
        }

        if (null == model)
        {
            model = getModelFromService(keys, dimensions);
        }

        if (null == model)
        {
            LOGGER.debug("Model *NOT FOUND*\n - Keys:\n{}\n - Dimensions:\n{}", keys, dimensions);
            return null;
        }

        initManagedSlots(model);
        return model;
    }

    @Override
    public ModelCache getModelCache()
    {

        return _ModelCache;
    }

    @Override
    public ModelBuilder getModelBuilder()
    {

        return _ModelBuilder;
    }

    @Override
    public final <M extends Model> List<M> getMultipleModels(final List<Keys> multipleKeys, final Dimensions sameDimensions)
    {

        if (multipleKeys == null)
        {
            throw new ContextException("The keys cannot be null", null, this);
        }

        if (sameDimensions == null)
        {
            throw new ContextException("The dimensions cannot be null", null, this);
        }

        final List<M> models = new ArrayList<>(multipleKeys.size());
        for (final Keys keys : multipleKeys)
        {
            final M model = getModel(keys, sameDimensions);
            if (model != null)
            {
                models.add(model);
            }
        }

        return models;
    }

    @Override
    public SchemaLoader getSchemaLoader()
    {

        return _SchemaLoader;
    }

    @Override
    public ServiceLoader getServiceLoader()
    {

        return _ServiceLoader;
    }

    @Override
    public SyntaxLoader getSyntaxLoader()
    {

        return _SyntaxLoader;
    }

    @Override
    public <M extends Model> M newModel(final Class<?> schemaInterface) throws ModelBuilderException
    {

        return getModelBuilder().newModel(schemaInterface);
    }

    @Override
    public <M extends Model> M newModel(final Dimensions dimensions)
    {

        return getModelBuilder().newModel(dimensions);
    }

    @Override
    public <M extends Model> M newModel(final URI schemaUri) throws ModelBuilderException
    {

        return getModelBuilder().newModel(schemaUri);
    }

    @Override
    public <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys, final Dimensions rootModelDimensions) throws ModelReadingException
    {

        return readModel(in, rootModelKeys, rootModelDimensions, getFormatLoader().getDefaultFormatUri());
    }

    @Override
    public final <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys, final Dimensions rootModelDimensions, final URI formatUri)
            throws ModelReadingException
    {

        final FormatLoader formatLoader = getFormatLoader();

        final URI formatId = (formatUri != null) ? formatUri : formatLoader.getDefaultFormatUri();

        final Formatter modelFormatter = formatLoader.getFormatter(formatId);

        if (modelFormatter == null)
        {
            throw new ModelReadingException("Unable to locate a formatter for the format: " + formatUri, null, this);
        }

        return modelFormatter.readModel(in, rootModelKeys, rootModelDimensions);
    }

    @Override
    public <M extends Model> M readModel(final InputStream in, final URI uri, final URI schemaUri, final URI formatUri) throws ModelReadingException
    {
        final Keys keys = _ApiLoader.buildDocumentKeys(uri, schemaUri);
        final Dimensions dimensions = new DimensionsBuilder(schemaUri).toDimensions();
        return readModel(in, keys, dimensions, formatUri);
    }

    @Override
    public final <M extends Model> M request(final Method requestMethod, final Keys keys, final Dimensions dimensions, final Model parameter)
    {

        switch (requestMethod)
        {
            case Get:
            {
                final M model = getModel(keys, dimensions);
                return model;
            }
            case Save:
            {
                if (parameter == null)
                {
                    throw new ContextException("The " + Method.Save + " method requires a model parameter.", this);
                }
                return saveModel((M) parameter);
            }
            case Invoke:
            {
                return invoke(keys, dimensions, parameter);
            }
            case Delete:
            {
                deleteModel(keys, dimensions);
                return null;
            }
            case Options:
            {
                return optionsModel(parameter);
            }
            default:
            {
                throw new ContextException("Failed to resolve the " + requestMethod + " request from: \"" + dimensions.getReferrerUri() + "\" with keys: \"" + keys
                        + "\" and dimensions (" + dimensions + ").", null, this);
            }

        }
    }

    @Override
    public final <M extends Model> M saveModel(final M model)
    {

        if (model == null)
        {
            throw new ContextException("Cannot save; the model is null.", this);
        }

        LOGGER.trace("Attempting to save model \n{}\n with schemaUri:\n {}", new Object[] {model, model.getDimensions().getSchemaUri()});

        M savedModel = null;

        final URI schemaUri = model.getSchemaUri();
        // final Method requestMethod = Method.Save;

        String originServiceName = model.getOriginServiceName();
        final ServiceLoader serviceLoader = getServiceLoader();

        if (originServiceName != null)
        {
            final Service originService = serviceLoader.getService(originServiceName);
            if (originService == null)
            {
                throw new ContextException("Cannot save; the origin service does not exist: " + originServiceName, this);
            }

            savedModel = (M) originService.save(model);
        }
        else
        {

            final Service service = serviceLoader.getServiceForSchema(schemaUri);
            if (service == null)
            {
                throw new ContextException("Cannot save; no service applies to model: " + model, this);
            }

            originServiceName = service.getConfiguration().getName();

            savedModel = (M) service.save(model);
        }

        if (savedModel != null)
        {
            LOGGER.debug("Saved Model:\n{}", savedModel);
        }
        else
        {
            throw new ContextException("Error saving model; no service return a saved model successfully.", this);
        }

        final ModelCache cache = getModelCache();
        if (cache != null)
        {
            savedModel = (M) cache.save(savedModel);
        }

        savedModel.setOriginServiceName(originServiceName);

        initManagedSlots(savedModel);

        return savedModel;
    }

    @Override
    public String toString()
    {

        return getClass().getSimpleName() + " { config : " + _Config + ", syntax registry : " + _SyntaxLoader + ", schemaLoader : " + _SchemaLoader + ", modelFactory : "
                + _ModelBuilder + ", apiLoader : " + _ApiLoader + "}";
    }

    @Override
    public final <M extends Model> M visitLink(final Model model, final String linkSlotName)
    {

        return visitLink(model, linkSlotName, null, null);
    }

    @Override
    public <M extends Model> M visitLink(final Model model, final String linkSlotName, final DimensionsBuilder dimensionsBuilder, final Model parameter) throws ContextException
    {

        final Link link = (Link) model.getSlotValue(linkSlotName);
        if (link == null)
        {
            throw new ModelException("Link \"" + linkSlotName + "\" not found in model, " + model + ".", null, model);
        }

        URI uri = null;

        if (model instanceof Document)
        {
            uri = ((Document) model).getUri();
        }
        else if (model instanceof Embedded)
        {
            uri = ((Embedded) model).getDocumentUri();
        }
        else
        {
            throw new ModelException("The model is not (REST) Document-related, thus it may not link to Documents.", null, model);
        }

        if (uri == null)
        {
            throw new ModelException("The model is missing a (REST) Document id.", null, model);
        }

        dimensionsBuilder.setReferrerUri(uri);

        final ApiNavigator apiNavigator = _ApiLoader.getParentApiNavigator(uri);
        if (apiNavigator == null)
        {
            throw new ModelException("The URI is not parented by any known REST API.", null, model);
        }

        return apiNavigator.visitLink(link, model, uri, dimensionsBuilder, parameter);
    }

    @Override
    public final void writeModel(final OutputStream out, final Model model) throws ModelWritingException
    {

        writeModel(out, model, getFormatLoader().getDefaultFormatUri());
    }

    @Override
    public final void writeModel(final OutputStream out, final Model model, final ModelWriteOptions writeOptions) throws ModelWritingException
    {

        writeModel(out, model, writeOptions, getFormatLoader().getDefaultFormatUri());
    }

    @Override
    public void writeModel(final OutputStream out, final Model model, final ModelWriteOptions writeOptions, final URI formatUri) throws ModelWritingException
    {

        final FormatLoader formatLoader = getFormatLoader();

        final URI formatId = (formatUri != null) ? formatUri : formatLoader.getDefaultFormatUri();

        final Formatter modelFormatter = formatLoader.getFormatter(formatId);

        if (modelFormatter == null)
        {
            throw new ModelWritingException("Unable to locate a formatter for the format: " + formatUri, null, this);
        }

        modelFormatter.writeModel(out, model, writeOptions);
    }

    @Override
    public final void writeModel(final OutputStream out, final Model model, final URI formatUri) throws ModelWritingException
    {

        writeModel(out, model, null, formatUri);
    }

    protected Factory<ApiLoader> createApiLoaderFactory()
    {

        return DefaultFactoryConfiguration.createFactory(getConfig().getApiLoader(), DefaultApiLoaderFactory.class);
    }

    protected Factory<FormatLoader> createFormatLoaderFactory()
    {

        return DefaultFactoryConfiguration.createFactory(getConfig().getFormatLoader(), DefaultFormatLoaderFactory.class);
    }

    protected Factory<ModelBuilder> createModelBuilderFactory()
    {

        return DefaultFactoryConfiguration.createFactory(getConfig().getModelBuilder(), DefaultModelBuilderFactory.class);
    }

    protected Factory<SchemaLoader> createSchemaLoaderFactory()
    {

        return DefaultFactoryConfiguration.createFactory(getConfig().getSchemaLoader(), DefaultSchemaLoaderFactory.class);
    }

    protected Factory<ServiceLoader> createServiceLoaderFactory()
    {

        return DefaultFactoryConfiguration.createFactory(getConfig().getServiceLoader(), DefaultServiceLoaderFactory.class);
    }

    protected Factory<SyntaxLoader> createSyntaxLoaderFactory()
    {

        return DefaultFactoryConfiguration.createFactory(getConfig().getSyntaxLoader(), DefaultSyntaxLoaderFactory.class);
    }

    protected ModelCache createModelCache()
    {

        final ContextConfiguration config = getConfig();
        final ModelCacheConfiguration cacheConfig = config.getModelCache();
        if (cacheConfig == null)
        {
            return null;
        }

        final String cacheClassName = cacheConfig.getImplementation();
        final ModelCache cache = DefaultConfiguration.newInstance(cacheClassName);
        cache.init(this, cacheConfig);
        return cache;
    }

    /**
     * Gets and invokes a functional Model, passing it the (optional) Model parameter and (optionally) returns a Model value that conforms to the specified Dimensions.
     * 
     * @param keys
     *            the keys of the function model
     * @param responseDimensions
     *            the dimensions of the function's Model return value.
     * @param parameter
     *            the (optional) parameter Model to pass to the function.
     * @param <M>
     *            the function's Model return value's type (schema).
     * @return the Model return value (or null if the function returns <code>void</code>).
     */
    protected <M extends Model> M invoke(final Keys keys, final Dimensions responseDimensions, final Model parameter)
    {

        final URI uri = getKeyValue(keys, Document.class);

        final DimensionsBuilder dimensionsBuilder = new DimensionsBuilder();
        dimensionsBuilder.setReferrerUri(responseDimensions.getReferrerUri()).setLocale(responseDimensions.getLocale());
        dimensionsBuilder.getMetadata().putAll(responseDimensions.getMetadata());
        dimensionsBuilder.getQueryParameters().putAll(responseDimensions.getQueryParameters());
        Dimensions functionDimensions = _ApiLoader.buildDocumentDimensions(Method.Get, uri, dimensionsBuilder);

        Model function = getModel(keys, functionDimensions);

        if (function == null)
        {
            throw new ContextException("Cannot invoke; the function model is null.", null, this);
        }

        Service service = null;
        String originServiceName = function.getOriginServiceName();
        if (originServiceName != null)
        {
            service = _ServiceLoader.getService(originServiceName);
        }

        if (service == null)
        {

            final URI functionSchemaUri = function.getSchemaUri();

            service = _ServiceLoader.getServiceForSchema(functionSchemaUri);
            if (service == null && parameter != null)
            {
                final URI parameterSchemaUri = parameter.getSchemaUri();
                service = _ServiceLoader.getServiceForSchema(parameterSchemaUri);
            }
        }

        if (service != null)
        {
            final M responseModel = (M) service.invoke(function, responseDimensions, parameter);

            originServiceName = service.getConfiguration().getName();
            responseModel.setOriginServiceName(originServiceName);

            initManagedSlots(responseModel);
            return responseModel;
        }
        else
        {
            LOGGER.debug("Service *NOT FOUND* for function invocation:\n - Function:\n{}\n - Parameter:\n{}", function, parameter);
        }

        return null;
    }

    private <M extends Model> M getModelFromService(final Keys keys, final Dimensions dimensions)
    {

        final ModelCache cache = getModelCache();
        final SchemaLoader schemaLoader = getSchemaLoader();
        final ServiceLoader serviceLoader = getServiceLoader();

        final URI schemaUri = dimensions.getSchemaUri();
        final Service service = serviceLoader.getServiceForSchema(schemaUri);
        if (service == null)
        {
            throw new ContextException("Cannot get model; no service applies to schema: " + schemaUri, null, this);
        }

        LOGGER.debug("Service for schemaUri {} is {}", new Object[] {schemaUri, service});

        M model = (M) service.get(keys, dimensions);

        if (model != null)
        {
            model.initKeySlots(keys);

            final String originServiceName = service.getConfiguration().getName();
            model.setOriginServiceName(originServiceName);

            // TODO: Make this "auto-loading" of these system types configurable?
            if (schemaLoader.isSystemSchema(schemaUri))
            {
                if (model instanceof Schema)
                {
                    schemaLoader.load((Schema) model);
                }
                else if (model instanceof LinkRelation)
                {
                    _ApiLoader.loadLinkRelation((LinkRelation) model);
                }
                else if (model instanceof Choices)
                {
                    schemaLoader.loadChoices((Choices) model);
                }
            }
            else if (cache != null)
            {
                model = (M) cache.save(model);
            }

            LOGGER.debug("Got Model:\n{}", model);

        }

        return model;
    }

    private <M extends Model> M getSystemModel(final Keys keys, final Dimensions dimensions)
    {

        final SchemaLoader schemaLoader = getSchemaLoader();
        URI schemaUri = dimensions.getSchemaUri();

        M model = null;

        if (_SchemaPrototype.isAssignableFrom(schemaUri))
        {
            model = (M) schemaLoader.getLoadedSchema(keys);

            if (model == null)
            {
                model = (M) schemaLoader.getNativeSchema(keys);
            }

        }
        else if (_LinkRelationPrototype.isAssignableFrom(schemaUri))
        {
            model = (M) _ApiLoader.getLoadedLinkRelation(keys);
        }
        else if (_ApiPrototype.isAssignableFrom(schemaUri))
        {
            model = (M) _ApiLoader.getLoadedApi(keys);
        }
        else if (_FormatPrototype.isAssignableFrom(schemaUri))
        {
            final FormatLoader formatLoader = getFormatLoader();
            model = (M) formatLoader.getLoadedFormat(keys);
        }
        else if (_ChoicesPrototype.isAssignableFrom(schemaUri))
        {
            model = (M) schemaLoader.getLoadedChoices(keys);

            if (model == null)
            {
                model = (M) schemaLoader.getNativeChoices(keys);
            }

        }
        else if (_SyntaxPrototype.isAssignableFrom(schemaUri))
        {
            final SyntaxLoader syntaxLoader = getSyntaxLoader();
            model = (M) syntaxLoader.getLoadedSyntax(keys);
        }

        return model;
    }

    /**
     * Manage the model's link and collection slots.
     * 
     * @param model
     *            The model to manage.
     * @see <a href="http://en.wikipedia.org/wiki/HATEOAS">Wikipedia on HATEOAS</a>
     */
    private void initManagedSlots(final Model model)
    {

        if (model instanceof Document)
        {
            final Document document = (Document) model;
            updateLinkSlots(document);

            // Fetch and aggregate any link-embedded documents
            embedLinkedDocuments(document);
        }

        // If the model has one ore more lists of links then they need to fill it with Link models
        searchForCollectionElements(model);
    }

    /**
     * Part of the HATEOAS automation. Uses the runtime's available REST API metadata to update the Links and Link href values in response to a change/initialization of the URI
     * slot value.
     */
    private void updateLinkSlots(final Document document)
    {

        final URI uri = document.getUri();
        if (uri == null)
        {
            return;
        }

        final ApiNavigator apiNavigator = _ApiLoader.getParentApiNavigator(uri);

        if (apiNavigator == null)
        {
            return;
        }

        final Resource resource = apiNavigator.getResource(uri);
        final Map<URI, LinkTemplate> linkTemplates = resource.getLinkTemplates();
        if (linkTemplates == null || linkTemplates.isEmpty())
        {
            return;
        }

        final SortedMap<String, URI> prototypeLinkRelUris = document.getPrototype().getLinkRelationUris();
        final Set<String> linkSlotNames = prototypeLinkRelUris.keySet();
        for (final String linkSlotName : linkSlotNames)
        {

            Link link = (Link) document.getSlotValue(linkSlotName);

            final URI linkRelationUri = prototypeLinkRelUris.get(linkSlotName);
            final Resource endpointResource = apiNavigator.getEndpointResource(linkRelationUri, uri);
            if (endpointResource == null)
            {
                continue;
            }

            final URI href = endpointResource.getHrefUri(document, linkRelationUri);
            if (href == null)
            {
                // Exclude Links that have null href values.

                if (link != null)
                {
                    document.setSlotValue(linkSlotName, null);
                }

                continue;
            }

            if (link == null)
            {

                link = newModel(_SchemaLoader.getLinkSchemaUri());
                link.setRel(linkRelationUri);
                document.setSlotValue(linkSlotName, link);
            }

            link.setHref(href);
        }
    }

    /**
     * Part of the HATEOAS automation. For Documents that have a one or more embedded Link slots, this method embeds each of the referenced Documents within the Links.
     */
    private void embedLinkedDocuments(final Document document)
    {

        final Prototype prototype = document.getPrototype();
        final Set<String> embeddedLinkSlotNameSet = new LinkedHashSet<>(document.getDimensions().getEmbeddedLinkSlotNames());

        if (embeddedLinkSlotNameSet.isEmpty() && !prototype.containsEmbeddedLink())
        {
            return;
        }

        final Collection<LinkProtoSlot> linkProtoSlots = prototype.getLinkProtoSlots().values();

        // TODO: Asynchronous Document aggregation
        // https://wrmlorg.jira.com/browse/WRML-289
        for (final LinkProtoSlot linkProtoSlot : linkProtoSlots)
        {
            final String linkSlotName = linkProtoSlot.getName();

            if (linkProtoSlot.isEmbedded() || (embeddedLinkSlotNameSet != null && embeddedLinkSlotNameSet.contains(linkSlotName)))
            {

                final Object slotValue = document.getSlotValue(linkSlotName);

                final Link link;
                if (slotValue == null)
                {
                    link = newModel(getSchemaLoader().getLinkSchemaUri());
                    link.setRel(linkProtoSlot.getLinkRelationUri());
                    document.setSlotValue(linkSlotName, link);
                }
                else
                {
                    link = (Link) slotValue;
                }

                if (link.getDoc() == null)
                {
                    final Model embedded = document.reference(linkSlotName);
                    link.setDoc(embedded);
                    URI href = link.getHref();
                    if (embedded instanceof Document)
                    {
                        href = ((Document) embedded).getUri();
                    }

                    link.setHref(href);
                }
            }

        }
    }

    /**
     * For Models that have a one or more collection slots, this method performs a search for the elements.
     */
    protected final void searchForCollectionElements(final Model referrer)
    {

        final Prototype prototype = referrer.getPrototype();

        final Map<String, CollectionPropertyProtoSlot> collectionPropertyProtoSlots = prototype.getCollectionPropertyProtoSlots();
        if (collectionPropertyProtoSlots.isEmpty())
        {
            return;
        }

        URI referrerUri = null;
        if (referrer instanceof Document)
        {
            referrerUri = ((Document) referrer).getUri();
        }
        else if (referrer instanceof Embedded)
        {
            referrerUri = ((Embedded) referrer).getDocumentUri();
        }

        final ApiNavigator apiNavigator = _ApiLoader.getParentApiNavigator(referrerUri);

        final Set<String> collectionSlotNames = collectionPropertyProtoSlots.keySet();

        // TODO: Asynchronous
        // https://wrmlorg.jira.com/browse/WRML-289
        for (final String collectionSlotName : collectionSlotNames)
        {

            final CollectionPropertyProtoSlot collectionPropertyProtoSlot = collectionPropertyProtoSlots.get(collectionSlotName);
            final ProtoSearchCriteria protoSearchCriteria = collectionPropertyProtoSlot.getProtoSearchCriteria();
            final Prototype referencePrototype = protoSearchCriteria.getReferencePrototype();
            final URI referenceSchemaUri = referencePrototype.getSchemaUri();

            final Service service = _ServiceLoader.getServiceForSchema(referenceSchemaUri);
            if (service == null)
            {
                throw new ContextException("Cannot search; no service applies to schema: " + referenceSchemaUri, null, this);
            }

            final SearchCriteria searchCriteria = protoSearchCriteria.buildSearchCriteria(referrer);

            Set<Model> resultSet = null;

            try
            {
                resultSet = service.search(searchCriteria);
            }
            catch (UnsupportedOperationException uoe)
            {
                // Swallow this.
            }

            if (resultSet != null && resultSet.size() > 0)
            {

                final URI linkRelationUri = collectionPropertyProtoSlot.getLinkRelationUri();
                final Resource endpointResource = apiNavigator.getEndpointResource(linkRelationUri, referrerUri);

                for (final Model model : resultSet)
                {
                    final String originServiceName = service.getConfiguration().getName();
                    model.setOriginServiceName(originServiceName);

                    if (model instanceof Document)
                    {
                        final Document document = (Document) model;
                        final URI uri = endpointResource.getDocumentUri(document);
                        document.setUri(uri);
                    }

                    initManagedSlots(model);
                }

                final List<Model> collection = (List<Model>) referrer.getSlotValue(collectionSlotName);
                collection.clear();
                collection.addAll(resultSet);

            }
        }
    }


    @Override
    public final <M extends Model> M optionsModel(final Model model)
    {
        if (model == null)
        {
            throw new ContextException("Cannot retrieve options for a null model.", this);
        }

        final URI uri = model.getSchemaUri();

        // TODO: WRML-72 - Implement HTTP Options
        final ApiNavigator apiNavigator = this._ApiLoader.getParentApiNavigator(uri);
        LOGGER.debug("apiNavigator={}", apiNavigator);
        // TODO: WRML-481 - Implement OPTIONS call at Resource Node
        Resource resource = apiNavigator.getResource(uri);

        M result = newModel(ResourceOptions.class);

        // TODO: WRML-487 - Modelize OPTIONS response
        // result.setSlotValue(slotName, resource.newValue);

        // TODO: WRML-478 - Implement OPTIONS call at root (/*) level

        return result;
    }

}
