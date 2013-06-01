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

import com.google.common.base.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.model.rest.Document;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.schema.*;
import org.wrml.runtime.service.Service;
import org.wrml.util.AsciiArt;
import org.wrml.util.JavaBean;
import org.wrml.util.JavaMethod;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.*;

/**
 * The runtime implementation for all {@link Model} instances.
 * <p/>
 * The {@link DefaultModel} is a {@link Model}, which gives it a "base class"-like role for all other models (WRML
 * schema instances).
 * <p/>
 * The {@link DefaultModel} is also an {@link InvocationHandler}, which enables it to handle the implementation of every
 * model method; including methods generated from user-defined schema slots. See
 * {@link #invoke(Object, Method, Object[])} for more details.
 * <p/>
 * One to <i>N</i> {@link DefaultModel}s may share the same heap id, which means that they share the exact same slot
 * value storage. This aspect of WRML's design is intended to support MVC application use cases, with
 * {@link DefaultModel} abstracting the "actual" model state much like an MVC view might. For example, an app may have a
 * few different views displaying or editing the same model data by having a {@link DefaultModel} associated with each
 * view. WRML's {@link DefaultModel} may be thought of both "model" and (headless) view for MVC purposes.
 * <p/>
 * The WRML runtime uses schema-defined keys {@link Schema#getKeySlotNames()} to map a {@link DefaultModel}'s heap id to
 * its unique (possibly composite key value). This technique ensures that the "slot state" of a unique model instance is
 * stored only once within the heap. If a given model is unique, determined by it key slot value(s), then the WRML
 * runtime will manage its data once but allow multiple {@link DefaultModel}s to reference it. This concept is known as
 * "instance folding" and it ensures model singularity.
 */
final class DefaultModel implements Model, InvocationHandler
{

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(DefaultModel.class);

    /**
     * The runtime context within which this model exists.
     */
    private final transient Context _Context;

    /**
     * The {@link ModelState} which holds the slot values.
     */
    private final ModelState _ModelState;

    /**
     * The {@link Dimensions} associated with this model.
     */
    private Dimensions _Dimensions;

    /**
     * Only the WRML runtime can create {@link DefaultModel}s directly.
     *
     * @param context    The {@link Context} for this model.
     * @param modelState The state for the model.
     */
    DefaultModel(final Context context, final ModelState modelState)
    {

        _Context = context;
        _ModelState = modelState;
    }

    @Override
    public Object clearSlotValue(final String slotName)
    {

        // NOTE: Will be handled by invoke as a special method for all "defined" models
        return clearSlotValue(this, slotName, null);
    }

    @Override
    public boolean containsSlotValue(final String slotName)
    {
        // NOTE: Will be handled by invoke as a special method for all "defined" models
        return containsSlotValue(this, slotName, null);
    }

    @Override
    public Context getContext()
    {

        return _Context;
    }

    @Override
    public Dimensions getDimensions()
    {

        return _Dimensions;
    }

    void setDimensions(final Dimensions dimensions)
    {

        _Dimensions = dimensions;
    }

    @Override
    public UUID getHeapId()
    {

        return getModelState().getHeapId(this);
    }

    @Override
    public Keys getKeys()
    {
        // See NOTE in getSchemaUri.
        return null;
    }

    @Override
    public String getOriginServiceName()
    {

        return getModelState().getOriginServiceName(this);
    }

    @Override
    public Prototype getPrototype()
    {
        // See NOTE in getSchemaUri.

        // Return the "undefined" Prototype.
        return null;
    }

    @Override
    public URI getSchemaUri()
    {
        // NOTE: If this model is already a Proxy model's InvocationHandler, then calls to this method would be routed
        // to invoke instead, where an appropriate schema id value will be returned.

        // Return the "undefined" schema URI by default.
        return null;
    }

    @Override
    public Map<String, Object> getSlotMap()
    {

        final ModelState modelState = getModelState();
        return modelState.getValuedSlots(this);
    }

    @Override
    public Object getSlotValue(final String slotName)
    {
        // NOTE: If this model is already a Proxy model's InvocationHandler, then calls to this method would be routed
        // to invoke instead, where an appropriate schema id value will be used for this call.

        return getSlotValue(slotName, null);
    }

    @Override
    public void initKeySlots(final Keys keys)
    {

        initKeySlots(this, keys);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
    {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final Model model = (Model) proxy;
        final String methodName = method.getName();

        final Class<?> modelClass = model.getClass();
        final Class<?>[] modelClassInterfaces = modelClass.getInterfaces();

        // Get the model class's first implemented interface; it's schema interface
        final Class<?> modelSchemaInterface = modelClassInterfaces[0];
        final URI modelSchemaUri = schemaLoader.getTypeUri(modelSchemaInterface);
        final Prototype prototype = schemaLoader.getPrototype(modelSchemaUri);

        // Get the class that defined the invoked method
        final Class<?> declaringClass = method.getDeclaringClass();

        //
        // If the invoked method comes from one of the base model types, then it may need to be handled in a
        // special way.
        //

        if (declaringClass.equals(ValueType.JAVA_TYPE_MODEL) || declaringClass.equals(Object.class)
                || declaringClass.equals(Comparable.class))
        {

            // The invoked method was declared by one of the base model types, it may be a special case method.
            final SpecialMethod specialMethod = SpecialMethod.fromString(methodName);

            if (specialMethod != null)
            {

                // LOG.debug("Model method: " + method + " is considered a special method.");

                // The name of the invoked method matches the name of one of our special cases.

                final int argCount = (args != null) ? args.length : 0;
                final boolean hasArgs = argCount > 0;
                final Object firstArg = (hasArgs) ? args[0] : null;

                switch (specialMethod)
                {

                    // Handle the special case methods:

                    case clearSlotValue:
                    {

                        if (argCount == 1 && firstArg instanceof String)
                        {
                            final String slotName = (String) firstArg;
                            return clearSlotValue(model, slotName, modelSchemaUri);
                        }

                        break;
                    }

                    case containsSlotValue:
                    {
                        if (argCount == 1 && firstArg instanceof String)
                        {
                            final String slotName = (String) firstArg;
                            return containsSlotValue(model, slotName, modelSchemaUri);
                        }

                        break;
                    }

                    case equals:
                    {

                        if (argCount == 1)
                        {
                            final Object other = firstArg;
                            if (other instanceof Proxy)
                            {
                                return equals(Proxy.getInvocationHandler(other));
                            }
                            return false;
                        }

                        break;
                    }

                    case getKeys:
                    {
                        if (argCount == 0)
                        {

                            return buildKeys(model);
                        }

                        break;
                    }

                    case getPrototype:
                    {
                        if (argCount == 0)
                        {

                            return prototype;
                        }

                        break;
                    }

                    case getSchemaUri:
                    {
                        if (argCount == 0)
                        {

                            if (ValueType.JAVA_TYPE_MODEL.equals(modelSchemaInterface))
                            {
                                // Return null (undefined Schema) if we are only a Model
                                return null;
                            }

                            // Reflect back the WRML representation
                            return modelSchemaUri;
                        }

                        break;
                    }

                    case getSlotValue:
                    {

                        if (argCount >= 1)
                        {
                            final String slotName = (String) firstArg;
                            final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);
                            return getSlotValue(model, slotName, protoSlot.getDeclaringSchemaUri());
                        }

                        break;
                    }
                    case initKeySlots:
                    {

                        if (argCount == 1)
                        {
                            final Keys keys = (Keys) firstArg;
                            initKeySlots(model, keys);
                            return null;
                        }

                        break;
                    }
                    case reference:
                    {

                        if (argCount >= 1)
                        {

                            final String linkSlotName = (String) firstArg;
                            final LinkProtoSlot linkProtoSlot = prototype.getProtoSlot(linkSlotName);

                            Object[] referenceMethodArgs = null;
                            if (argCount > 1)
                            {
                                referenceMethodArgs = ArrayUtils.subarray(args, 1, args.length);
                            }
                            return invokeReference(model, linkProtoSlot, referenceMethodArgs);

                        }

                        break;
                    }

                    case setSlotValue:
                    {

                        if (argCount >= 2)
                        {
                            final String slotName = (String) firstArg;
                            final Object slotValue = args[1];
                            final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);
                            return setSlotValue(model, slotName, slotValue, protoSlot.getDeclaringSchemaUri());
                        }

                        break;
                    }
                    default:
                        break;

                } // End of switch

            }

            // LOG.debug("Model method: " + method + " was not handled as a special case.");

            // Proxy to this RuntimeModel itself, since it is all of the above
            // declaring classes (independent of Proxy's magic trick).
            return method.invoke(this, args);
        }

        final JavaBean schemaBean = prototype.getSchemaBean();
        final SortedMap<String, SortedSet<JavaMethod>> linkMethods = schemaBean.getOtherMethods();

        if (linkMethods.containsKey(methodName))
        {
            String linkSlotName = methodName;

            if (linkSlotName.startsWith(JavaBean.GET))
            {
                linkSlotName = methodName.substring(3);
                linkSlotName = Character.toLowerCase(linkSlotName.charAt(0)) + linkSlotName.substring(1);
            }

            final LinkProtoSlot linkProtoSlot = prototype.getProtoSlot(linkSlotName);

            DefaultModel.LOG.debug("Model method: " + method + " is considered a link/reference assicuated with slot: "
                    + linkProtoSlot);

            // The java method's invocation is interpreted as a link reference (i.e. "click").
            return invokeReference(model, linkProtoSlot, args);
        }

        //
        // Determine if the method name looks like a slot accessor (setter/getter) meaning that it starts with "get",
        // "set", or "is".
        //

        boolean isWrite = false;
        String slotName = null;

        if (methodName.startsWith(JavaBean.GET))
        {
            slotName = methodName.substring(3);
        }
        else if (methodName.startsWith(JavaBean.IS))
        {
            slotName = methodName.substring(2);
        }
        else if (methodName.startsWith(JavaBean.SET))
        {
            slotName = methodName.substring(3);
            isWrite = true;
        }

        if (slotName != null)
        {
            slotName = Character.toLowerCase(slotName.charAt(0)) + slotName.substring(1);

            final URI declaringSchemaUri = schemaLoader.getTypeUri(declaringClass);

            if (isWrite)
            {
                final Object newSlotValue = args[0];
                final Object oldSlotValue = setSlotValue(model, slotName, newSlotValue, declaringSchemaUri);
                return oldSlotValue;
            }
            else
            {
                final Object slotValue = getSlotValue(model, slotName, declaringSchemaUri);

                return slotValue;
            }
        }

        throw new ModelException("Model method invocation was not handled for method: " + method, null, model);
    }

    @Override
    public <M extends Model> M newAlternate(final Dimensions alternateDimensions) throws ModelException
    {

        final Context context = getContext();
        final DefaultModelBuilder modelFactory = (DefaultModelBuilder) context.getModelBuilder();

        // Create a new model with the same heap id as this; making it an "alternate" of this model's slot state.
        return modelFactory.newModel(alternateDimensions, getModelState());
    }

    @Override
    public <M extends Model> M newCopy() throws ModelException
    {

        final ModelBuilder factory = getContext().getModelBuilder();
        return factory.copyModel(this);

    }

    @Override
    public <E extends Model> E reference(final String linkSlotName) throws ModelException
    {

        return reference(linkSlotName, null, null);
    }

    @Override
    public <E extends Model> E reference(final String linkSlotName, final DimensionsBuilder dimensionsBuilder)
            throws ModelException
    {

        return reference(linkSlotName, dimensionsBuilder, null);
    }

    @Override
    public <E extends Model> E reference(final String linkSlotName, final DimensionsBuilder dimensionsBuilder,
                                         final Model parameter) throws ModelException
    {

        return visitLink(this, linkSlotName, dimensionsBuilder, parameter);
    }

    @Override
    public String setOriginServiceName(final String originServiceName)
    {

        return getModelState().setOriginServiceName(this, originServiceName);
    }

    @Override
    public Object setSlotValue(final String slotName, final Object newValue) throws ModelException
    {
        // NOTE: If this model is already a Proxy model's InvocationHandler, then calls to this method would be routed
        // to invoke instead, where an appropriate schema id value will be used for this call.

        return setSlotValue(slotName, newValue, null);
    }

    ModelState getModelState()
    {

        return _ModelState;
    }

    private final Keys buildKeys(final Model model)
    {

        return buildKeys(model.getSchemaUri(), model.getSlotMap(), new KeysBuilder());
    }

    private final Keys buildKeys(final URI schemaUri, final Map<String, Object> readOnlySlotMap,
                                 final KeysBuilder keysBuilder)
    {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Prototype prototype = schemaLoader.getPrototype(schemaUri);
        final SortedSet<String> keySlotNames = prototype.getDeclaredKeySlotNames();
        if (keySlotNames != null && !keySlotNames.isEmpty())
        {
            final Object keyValue;

            if (keySlotNames.size() == 1)
            {
                final String keySlotName = keySlotNames.first();
                if (readOnlySlotMap.containsKey(keySlotName))
                {
                    keyValue = readOnlySlotMap.get(keySlotName);
                }
                else
                {
                    keyValue = null;
                }
            }
            else
            {
                final SortedMap<String, Object> keySlots = new TreeMap<String, Object>();
                for (final String keySlotName : keySlotNames)
                {
                    final Object keySlotValue = readOnlySlotMap.get(keySlotName);
                    keySlots.put(keySlotName, keySlotValue);
                }

                keyValue = new CompositeKey(keySlots);
            }

            if (keyValue != null)
            {
                keysBuilder.addKey(schemaUri, keyValue);
            }

        }

        final Set<URI> baseSchemaUris = prototype.getAllBaseSchemaUris();
        if (baseSchemaUris != null && !baseSchemaUris.isEmpty())
        {
            for (final URI baseSchemaUri : baseSchemaUris)
            {
                buildKeys(baseSchemaUri, readOnlySlotMap, keysBuilder);
            }
        }

        return keysBuilder.toKeys();
    }

    private Object clearSlotValue(final Model model, final String slotName, final URI schemaUri)
    {

        final ModelState state = getModelState();
        return state.clearSlotValue(model, slotName, schemaUri);
    }

    private boolean containsSlotValue(final Model model, final String slotName, final URI schemaUri)
    {

        final ModelState state = getModelState();
        return state.containsSlotValue(model, slotName, schemaUri);
    }

    private Object getSlotValue(final Model model, final String slotName, final URI schemaUri)
    {

        return getSlotValue(model, slotName, schemaUri, true);
    }

    private Object getSlotValue(final Model model, final String slotName, final URI schemaUri, final boolean strict)
    {

        final ModelState state = getModelState();
        return state.getSlotValue(model, slotName, schemaUri, strict);
    }

    private Object getSlotValue(final String slotName, final URI schemaUri)
    {

        return getSlotValue(slotName, schemaUri, true);
    }

    private Object getSlotValue(final String slotName, final URI schemaUri, final boolean strict)
    {

        return getSlotValue(this, slotName, schemaUri, strict);
    }

    private void initKeySlots(final Model model, final Keys keys)
    {

        final SchemaLoader schemaLoader = getContext().getSchemaLoader();
        final URI documentSchemaUri = schemaLoader.getDocumentSchemaUri();

        URI uri = null;

        // Apply all of these Keys to the cached model (in case any Key values are "new").
        for (final URI keyedSchemaUri : keys.getKeyedSchemaUris())
        {
            final Object keyValue = keys.getValue(keyedSchemaUri);

            final Prototype keyedPrototype = schemaLoader.getPrototype(keyedSchemaUri);
            final SortedSet<String> keySlotNames = keyedPrototype.getDeclaredKeySlotNames();

            if (keySlotNames.size() == 1)
            {
                if (documentSchemaUri.equals(keyedSchemaUri))
                {
                    // Save the document key slot (uri) for last so that the hypermedia engine has all other keys
                    // available
                    // (to auto-generate the Link href values).
                    uri = (URI) keyValue;
                }
                else
                {
                    setSlotValue(model, keySlotNames.first(), keyValue, keyedSchemaUri, false);
                }
            }
            else if (keyValue instanceof CompositeKey)
            {
                final CompositeKey compositeKey = (CompositeKey) keyValue;
                final Map<String, Object> keySlots = compositeKey.getKeySlots();
                for (final String keySlotName : keySlots.keySet())
                {
                    setSlotValue(model, keySlotName, keySlots.get(keySlotName), keyedSchemaUri, false);
                }
            }
        }

        // See comment above regarding saving the uri key slot for last.
        if (uri != null)
        {
            setSlotValue(model, Document.SLOT_NAME_URI, uri, documentSchemaUri, false);
        }
    }

    private Object invokeReference(final Model model, final LinkProtoSlot linkProtoSlot, final Object[] args)
    {


        final int argCount = (args != null) ? args.length : 0;
        final boolean hasArgs = argCount > 0;
        final Object firstArg = (hasArgs) ? args[0] : null;

        DimensionsBuilder dimensionsBuilder = null;
        Model parameter = null;

        if (hasArgs)
        {
            if (firstArg instanceof DimensionsBuilder)
            {
                dimensionsBuilder = (DimensionsBuilder) firstArg;
                if (argCount == 2)
                {
                    parameter = (Model) args[1];
                }
            }
            else
            {
                parameter = (Model) firstArg;
            }
        }

        if (dimensionsBuilder == null)
        {
            dimensionsBuilder = new DimensionsBuilder(model.getDimensions());
            dimensionsBuilder.setSchemaUri(linkProtoSlot.getResponseSchemaUri());
        }

        return visitLink(model, linkProtoSlot.getName(), dimensionsBuilder, parameter);

    }

    private Object setSlotValue(final Model model, final String slotName, final Object newValue, final URI schemaUri)
    {

        return setSlotValue(model, slotName, newValue, schemaUri, true);
    }

    private Object setSlotValue(final Model model, final String slotName, final Object newValue, final URI schemaUri,
                                final boolean strict)
    {

        final ModelState state = getModelState();
        return state.setSlotValue(model, slotName, newValue, schemaUri, strict);
    }

    private Object setSlotValue(final String slotName, final Object newValue, final URI schemaUri)
    {

        return setSlotValue(slotName, newValue, schemaUri, true);
    }

    private Object setSlotValue(final String slotName, final Object newValue, final URI schemaUri, final boolean strict)
    {

        return setSlotValue(this, slotName, newValue, schemaUri, strict);
    }

    private <M extends Model> M visitLink(final Model model, final String linkSlotName,
                                          final DimensionsBuilder dimensionsBuilder, final Model parameter) throws ModelException
    {

        final Context context = getContext();
        return context.visitLink(model, linkSlotName, dimensionsBuilder, parameter);
    }

    @Override
    public boolean equals(final Object obj)
    {

        if (obj instanceof DefaultModel)
        {
            final DefaultModel other = (DefaultModel) obj;
            return Objects.equal(_Context, other._Context) && Objects.equal(_ModelState, other._ModelState)
                    && Objects.equal(_Dimensions, other._Dimensions);
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {

        return Objects.hashCode(_Context, _ModelState, _Dimensions);
    }

    @Override
    public String toString()
    {

        return AsciiArt.express(this);
    }

    /**
     * The names of model methods that need to be considered as special cases within invoke.
     */
    private static enum SpecialMethod
    {
        clearSlotValue,
        containsSlotValue,
        equals,
        getKeys,
        getPrototype,
        getSchemaUri,
        getSlotValue,
        initKeySlots,
        reference,
        setSlotValue;

        // TODO: Want a generic utility to help statically cache a map of Enum .toString() values to the actual Enum
        // values.

        private static final Map<String, SpecialMethod> STRING_MAP;

        static
        {
            final SpecialMethod[] values = SpecialMethod.values();
            STRING_MAP = new HashMap<String, SpecialMethod>(values.length);
            for (final SpecialMethod e : values)
            {
                SpecialMethod.STRING_MAP.put(e.toString(), e);
            }
        }

        public static final SpecialMethod fromString(final String string)
        {

            if (!SpecialMethod.STRING_MAP.containsKey(string))
            {
                return null;
            }

            return SpecialMethod.STRING_MAP.get(string);
        }

    }

    /**
     * Interface to the {@link DefaultModel}'s slot store and other matters of state.
     */
    public static interface ModelState extends Cloneable
    {

        Object clearSlotValue(final Model model, final String slotName, final URI schemaUri);

        ModelState clone();

        boolean containsSlotValue(final Model model, final String slotName, final URI schemaUri);

        /**
         * The unique heap id that corresponds to this model's slot storage.
         */
        UUID getHeapId(final Model model);

        String getOriginServiceName(final Model model);

        Object getSlotValue(final Model model, final String slotName, final URI schemaUri, final boolean strict);

        Map<String, Object> getValuedSlots(final Model model);

        String setOriginServiceName(final Model model, final String originServiceName);

        Object setSlotValue(final Model model, final String slotName, final Object newValue, final URI schemaUri,
                            final boolean strict);

    }
}
