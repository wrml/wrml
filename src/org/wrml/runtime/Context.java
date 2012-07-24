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

package org.wrml.runtime;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang3.reflect.TypeUtils;

import org.wrml.model.Model;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.EngineConfiguration.ServiceMapping;
import org.wrml.runtime.EngineConfiguration.SyntaxConstraintHandler;
import org.wrml.service.Service;
import org.wrml.util.UniversallyUniqueObject;
import org.wrml.util.UriTemplate;
import org.wrml.util.observable.ObservableMap;
import org.wrml.util.observable.Observables;
import org.wrml.util.transformer.ToStringTransformer;

/**
 * The runtime context supporting some scope within a WRML program's execution.
 * 
 * A context forms a boundary for {@link Model}s and their associated types
 * (both WRML {@link Schema} and Java {@link Class}).
 */
public class Context extends UniversallyUniqueObject {

    private final Engine _Engine;
    private final Context _ParentContext;
    private final SchemaLoader _SchemaLoader;
    private final ModelHeap _ModelHeap;
    private final ObservableMap<URI, Service<?>> _SingleSchemaServices;
    private final ObservableMap<UriTemplate, Service<?>> _MultiSchemaServices;

    private final ObservableMap<URI, Class<?>> _SyntaxJavaClasses;
    private final ObservableMap<Class<?>, ToStringTransformer<?>> _JavaClassToStringTransformers;

    public Context(final Engine engine) throws ContextException {
        this(engine, null);
    }

    public Context(final Engine engine, final Context parentContext) throws ContextException {

        _Engine = engine;
        _ParentContext = parentContext;

        final EngineConfiguration config = engine.getConfig();

        // TODO: Add a flag to allow for use some/all of the parent context's state (schema loader, heap, services, transformers).

        _SchemaLoader = new SchemaLoader(this);
        _ModelHeap = new ModelHeap(this, config.isStrictlyStatic());

        // TODO: Should the transformers and service state be moved into the engine itself or does it need to vary by context?

        _SyntaxJavaClasses = Observables.observableMap(new TreeMap<URI, Class<?>>());
        _JavaClassToStringTransformers = Observables.observableMap(new HashMap<Class<?>, ToStringTransformer<?>>());
        initSyntaxConstraintHandlers();

        _SingleSchemaServices = Observables.observableMap(new TreeMap<URI, Service<?>>());
        _MultiSchemaServices = Observables.observableMap(new TreeMap<UriTemplate, Service<?>>());
        startServices();
    }

    public <M extends Model> M create(Dimensions dimensions) throws ContextException {

        if (dimensions == null) {
            throw new ContextException("The dimensions cannot be null", null, this);
        }

        final ModelHeap heap = getModelHeap();
        final Model model = heap.createModelReference();
        return model.getAlternate(dimensions);
    }

    /**
     * Gets the identified model by its key.
     * 
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public <M extends Model, K> M get(K key, Dimensions dimensions) throws ContextException {

        if (key == null) {
            throw new ContextException("The key cannot be null", null, this);
        }

        if (dimensions == null) {
            throw new ContextException("The dimensions cannot be null", null, this);
        }

        final ModelHeap heap = getModelHeap();

        final URI requestedSchemaId = dimensions.getRequestedSchemaId();

        if (requestedSchemaId == null) {
            throw new ContextException("The (Dimensions) requested schema id (URI) cannot be null", null, this);
        }

        final UUID heapId = heap.getHeapId(requestedSchemaId, key);
        if (heapId != null) {

            // TODO: Handle cache expiry for existing models.

            final Model model = heap.createModelReference(heapId);

            // Return a new proxy of the reference.
            return (M) model.getAlternate(dimensions);
        }
        else {
            // We do not already have this model in our heap. Find a Service that can deliver it.            

            final Service<K> service = (Service<K>) getService(requestedSchemaId);

            if (service == null) {
                throw new ContextException("There is no service configured to handle requests for schema ("
                        + requestedSchemaId + ").", null, this);
            }

            // Ask the Service for it.       
            return service.get(key, dimensions);
        }
    }

    public URI getDefaultFormatId() {
        final EngineConfiguration config = getEngine().getConfig();
        return config.getDefaultFormatId();
    }

    public Engine getEngine() {
        return _Engine;
    }

    public ModelHeap getModelHeap() {
        return _ModelHeap;
    }

    @SuppressWarnings("unchecked")
    public <M extends Model, K> List<M> getMultiple(final List<K> keys, final Dimensions dimensions) {

        if (keys == null) {
            throw new ContextException("The keys cannot be null", null, this);
        }

        if (dimensions == null) {
            throw new ContextException("The dimensions cannot be null", null, this);
        }

        final List<Model> modelList = new ArrayList<Model>(keys.size());
        for (final K key : keys) {
            final Model model = get(key, dimensions);
            modelList.add(model);
        }

        return (List<M>) modelList;
    }

    public Context getParentContext() {
        return _ParentContext;
    }

    public SchemaLoader getSchemaLoader() {
        return _SchemaLoader;
    }

    public Service<?> getService(URI requestedSchemaId) {

        if (_SingleSchemaServices.containsKey(requestedSchemaId)) {
            return _SingleSchemaServices.get(requestedSchemaId);
        }
        else if (!_MultiSchemaServices.isEmpty()) {
            for (final UriTemplate schemaIdTemplate : _MultiSchemaServices.keySet()) {
                if (schemaIdTemplate.matches(requestedSchemaId)) {
                    return _MultiSchemaServices.get(schemaIdTemplate);
                }
            }
        }

        return null;
    }

    public Class<?> getSyntaxJavaClass(URI syntaxContstraintId) {
        if (!_SyntaxJavaClasses.containsKey(syntaxContstraintId)) {
            return null;
        }

        return _SyntaxJavaClasses.get(syntaxContstraintId);
    }

    @SuppressWarnings("unchecked")
    public <T> ToStringTransformer<T> getToStringTransformer(Class<T> syntaxJavaClass) {
        if (!_JavaClassToStringTransformers.containsKey(syntaxJavaClass)) {
            return null;
        }

        return (ToStringTransformer<T>) _JavaClassToStringTransformers.get(syntaxJavaClass);
    }

    private void initSyntaxConstraintHandlers() {

        final EngineConfiguration config = _Engine.getConfig();
        final SyntaxConstraintHandler[] syntaxConstraintHandlers = config.getSyntaxConstraintHandlers();
        if ((syntaxConstraintHandlers == null) || (syntaxConstraintHandlers.length == 0)) {
            return;
        }

        for (final SyntaxConstraintHandler syntaxConstraintHandler : syntaxConstraintHandlers) {

            final URI syntaxConstraintId = syntaxConstraintHandler.getSyntaxConstraintId();
            if (syntaxConstraintId == null) {
                throw new ContextException("The Transformer Text Syntax Constraint id (URI) cannot be null", null, this);
            }

            final String transformerClassName = syntaxConstraintHandler.getToStringTransformerClassName();
            if (transformerClassName == null) {
                throw new ContextException("The Transformer class name cannot be null", null, this);
            }

            Class<?> transformerClass;
            try {
                transformerClass = Class.forName(transformerClassName);
            }
            catch (final ClassNotFoundException e) {
                throw new ContextException("Failed to load Transformer class (" + transformerClassName + ")", e, this);
            }

            Class<?> syntaxJavaClass = null;
            final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(transformerClass,
                    ToStringTransformer.class);

            for (final TypeVariable<?> typeVar : typeArguments.keySet()) {

                final String typeVarName = typeVar.getName();
                if (ToStringTransformer.TYPE_VARIABLE_NAME.equals(typeVarName)) {
                    syntaxJavaClass = (Class<?>) typeArguments.get(typeVar);
                    break;
                }
                else {
                    throw new ContextException("Unexpected type variable name  \"" + typeVarName
                            + "\" in Transformer class (" + transformerClassName + ")", null, this);
                }

            }

            ToStringTransformer<?> transformer = null;
            try {
                transformer = (ToStringTransformer<?>) transformerClass.newInstance();
            }
            catch (final Exception e) {
                throw new ContextException("Failed to create new instance of Transformer class ("
                        + transformerClassName + ")", e, this);
            }

            _SyntaxJavaClasses.put(syntaxConstraintId, syntaxJavaClass);
            _JavaClassToStringTransformers.put(syntaxJavaClass, transformer);
        }
    }

    private void mapMultiSchemaService(UriTemplate schemaIdTemplate, Service<?> service) {
        _MultiSchemaServices.put(schemaIdTemplate, service);
    }

    private void mapSingleSchemaService(URI schemaId, Service<?> service) {
        _SingleSchemaServices.put(schemaId, service);
    }

    private void startServices() {

        final EngineConfiguration config = _Engine.getConfig();
        final ServiceMapping[] serviceMappings = config.getServiceMappings();
        if ((serviceMappings == null) || (serviceMappings.length == 0)) {
            throw new ContextException("The engine has no services configured.", null, this);
        }

        for (final ServiceMapping serviceMapping : serviceMappings) {
            final String serviceClassName = serviceMapping.getServiceClassName();
            if (serviceClassName == null) {
                throw new ContextException("The Service class name cannot be null", null, this);
            }

            Class<?> serviceClass;
            try {
                serviceClass = Class.forName(serviceClassName);
            }
            catch (final ClassNotFoundException e) {
                throw new ContextException("Failed to load Service class (" + serviceClassName + ")", e, this);
            }

            Service<?> service = null;
            try {
                service = (Service<?>) serviceClass.newInstance();
            }
            catch (final Exception e) {
                throw new ContextException("Failed to create new instance of Service class (" + serviceClassName + ")",
                        e, this);
            }

            service.start(this);

            final URI[] schemaIds = serviceMapping.getSchemaIds();
            if ((schemaIds != null) && (schemaIds.length > 0)) {
                for (final URI id : schemaIds) {
                    mapSingleSchemaService(id, service);
                }
            }

            final UriTemplate[] schemaIdTemplates = serviceMapping.getSchemaIdTemplates();
            if ((schemaIdTemplates != null) && (schemaIdTemplates.length > 0)) {

                for (final UriTemplate schemaIdTemplate : schemaIdTemplates) {
                    mapMultiSchemaService(schemaIdTemplate, service);
                }
            }
            if (((schemaIds == null) || (schemaIds.length == 0))
                    && ((schemaIdTemplates == null) || (schemaIdTemplates.length == 0))) {

                throw new ContextException(
                        "There is no schema id (neither URI nor URI Template) mapping configured for Service ("
                                + service + ")", null, this);

            }
        }
    }

}
