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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.wrml.event.EventSource;
import org.wrml.model.Model;
import org.wrml.model.ModelEventListener;
import org.wrml.model.schema.Link;
import org.wrml.service.Service;

/**
 * A model reference is the runtime implementation for all {@link Model}
 * instances.
 */
final class ModelReference implements Model, InvocationHandler {

    private UUID _HeapId;
    private final Context _Context;
    private Dimensions _Dimensions;

    ModelReference(final Context context, final UUID heapId) {
        _Context = context;
        _HeapId = heapId;
    }

    @Override
    public boolean addEventListener(final ModelEventListener eventListener) {
        return addEventListener(null, eventListener);
    }

    @Override
    public void delete() {

        // TODO: Implement service-based delete and heap removal here?
        // TODO: Need to "free" this instance and notify our listeners?
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M extends Model> M getAlternate(final Dimensions dimensions) throws ModelException {

        final Context context = getContext();
        final ModelHeap heap = context.getModelHeap();

        // Our alternate representation still shares our identity.
        final ModelReference alternateReference = heap.createModelReference(getHeapId());
        alternateReference.setDimensions(dimensions);
        Class<?>[] schemaInterfaceArray = null;

        final URI requestedSchemaId = dimensions.getRequestedSchemaId();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        Class<?> schemaInterface = null;
        try {
            schemaInterface = schemaLoader.getSchemaInterface(requestedSchemaId);
        }
        catch (final ClassNotFoundException e) {
            throw new ModelException("Unable to load the Java class representation of: " + requestedSchemaId, e, this);
        }

        if (schemaInterface != null) {
            if (!schemaInterface.isInterface()) {
                // TODO: Technically speaking, we could still code-generate a WRML interface based on the (impl) class's reflection interface. Potentially interesting...
                throw new ModelException("The requested Schema already exists as a Java class (not an interface).",
                        null, this);
            }

            if (Model.class.isAssignableFrom(schemaInterface)) {
                // The requested schema already extends Model (as expected)
                schemaInterfaceArray = new Class<?>[] { schemaInterface };
            }
            else {
                // The requested shema interface does not (yet) extend Model, add it to the Proxy's list manually.
                schemaInterfaceArray = new Class<?>[] { schemaInterface, Model.class };
            }
        }

        final M newProxyInstance = (M) Proxy.newProxyInstance(schemaLoader, schemaInterfaceArray, alternateReference);

        return newProxyInstance;
    }

    @Override
    public Context getContext() {
        return _Context;
    }

    @Override
    public Dimensions getDimensions() {
        return _Dimensions;
    }

    @Override
    public Object getFieldValue(String fieldName) {
        return getFieldValue(null, fieldName);
    }

    @Override
    public Object getFieldValue(final URI schemaId, final String fieldName) {
        final Context context = getContext();
        final ModelHeap heap = context.getModelHeap();
        return heap.getFieldValue(this, schemaId, fieldName);
    }

    @Override
    public UUID getHeapId() {
        return _HeapId;
    }

    @Override
    public URI getSchemaId() {
        // Return the "undefined" schema ID by default
        return null;
    }

    @Override
    public final Object invoke(final Object model, final Method method, final Object[] args) throws Throwable {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final String methodName = method.getName();

        // Get the class that defined the invoked method
        final Class<?> declaringClass = method.getDeclaringClass();
        final URI declaringSchemaId = schemaLoader.getSchemaId(declaringClass.getCanonicalName());

        if (declaringClass.equals(Object.class) || declaringClass.equals(Model.class)
                || declaringClass.equals(EventSource.class)) {

            if (methodName.equals("equals") && (args != null) && (args.length == 1)) {
                final Object other = args[0];
                if (other instanceof Proxy) {
                    return equals(Proxy.getInvocationHandler(other));
                }
                return false;
            }

            final Class<?> modelClass = model.getClass();
            final Class<?>[] modelClassInterfaces = modelClass.getInterfaces();

            // Get the model class's first implemented interface; it's schema interface
            final Class<?> modelSchemaInterface = modelClassInterfaces[0];
            final URI modelSchemaId = schemaLoader.getSchemaId(modelSchemaInterface.getCanonicalName());

            if (methodName.equals("getSchemaId")) {

                if (Model.class.equals(modelSchemaInterface)) {
                    // Return null (undefined Schema) if we are only a Model
                    return null;
                }

                // Reflect back the WRML representation            
                return modelSchemaId;
            }

            if (methodName.equals("addEventListener") && (args != null) && (args.length == 1)) {
                final ModelEventListener eventListener = (ModelEventListener) args[0];
                return addEventListener(modelSchemaId, eventListener);
            }

            if (methodName.equals("removeEventListener") && (args != null) && (args.length == 1)) {
                final ModelEventListener eventListener = (ModelEventListener) args[0];
                return removeEventListener(modelSchemaId, eventListener);
            }

            final Prototype modelPrototype = schemaLoader.getPrototype(modelSchemaId);

            if (methodName.equals("getFieldValue") && (args != null) && (args.length == 1)) {

                final String fieldName = (String) args[0];
                final Prototype.Field protoField = modelPrototype.getPrototypeField(fieldName);
                return getFieldValue(protoField.getDeclaringSchemaId(), fieldName);
            }

            if (methodName.equals("setFieldValue") && (args != null) && (args.length == 2)) {
                final String fieldName = (String) args[0];
                final Object fieldValue = args[1];
                final Prototype.Field protoField = modelPrototype.getPrototypeField(fieldName);
                return setFieldValue(protoField.getDeclaringSchemaId(), fieldName, fieldValue);
            }

            // Proxy to this ModelReference itself, since it is all of the above declaring classes.
            return method.invoke(this, args);
        }

        boolean isWrite = false;
        String fieldName = null;

        if (methodName.startsWith(TypeSystem.GET)) {
            fieldName = methodName.substring(3);
        }
        else if (methodName.startsWith(TypeSystem.IS)) {
            fieldName = methodName.substring(2);
        }
        else if (methodName.startsWith(TypeSystem.SET)) {
            fieldName = methodName.substring(3);
            isWrite = true;
        }

        if (fieldName != null) {
            fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);

            if (!isWrite) {
                return getFieldValue(declaringSchemaId, fieldName);
            }
            else {
                final Object newFieldValue = args[0];
                return setFieldValue(declaringSchemaId, fieldName, newFieldValue);
            }
        }
        else if (declaringClass.equals(Link.class)) {
            // TODO: Handle link traversal "natively" here?
        }

        final Prototype declaringPrototype = schemaLoader.getPrototype(declaringSchemaId);
        final JavaBean schemaBean = declaringPrototype.getSchemaBean();

        final Map<String, Set<JavaMethod>> nonFieldJavaMethods = schemaBean.getOtherMethods();
        if (nonFieldJavaMethods.containsKey(methodName)) {
            final Service<?> service = context.getService(declaringSchemaId);
            if (service == null) {
                throw new ModelException("There are no active services mapped to this Model's schema ("
                        + declaringSchemaId + ")." + " Model method invocation was not handled for method: " + method,
                        null, (Model) model);
            }
            return service.invoke(model, method, args);
        }

        throw new ModelException("Model method invocation was not handled for method: " + method, null, (Model) model);
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean removeEventListener(final ModelEventListener eventListener) {
        return removeEventListener(null, eventListener);
    }

    @Override
    public Object setFieldValue(String fieldName, Object newValue) throws ModelException {
        return setFieldValue(null, fieldName, newValue);
    }

    @Override
    public Object setFieldValue(final URI schemaId, final String fieldName, final Object newValue) {
        final Context context = getContext();
        final ModelHeap heap = context.getModelHeap();
        return heap.setFieldValue(this, schemaId, fieldName, newValue);
    }

    @Override
    public String toString() {
        return "Model [heapId = " + _HeapId + ", dimensions = " + _Dimensions + "]";
    }

    @Override
    public <M extends Model> M update() {

        // TODO: Implement service-based delete and heap removal here?
        return null;
    }

    void setDimensions(Dimensions dimensions) {
        _Dimensions = dimensions;
    }

    void setHeapId(final UUID heapId) {
        _HeapId = heapId;
    }

    private boolean addEventListener(URI schemaId, ModelEventListener eventListener) {
        final Context context = getContext();
        final ModelHeap heap = context.getModelHeap();
        return heap.addModelEventListener(this, schemaId, eventListener);
    }

    private boolean removeEventListener(URI schemaId, ModelEventListener eventListener) {
        final Context context = getContext();
        final ModelHeap heap = context.getModelHeap();
        return heap.removeModelEventListener(this, schemaId, eventListener);
    }

}
