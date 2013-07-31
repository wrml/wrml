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

import com.rits.cloning.Cloner;
import org.wrml.model.Model;
import org.wrml.model.schema.ValueType;
import org.wrml.runtime.DefaultModel.ModelState;
import org.wrml.runtime.schema.*;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultModelBuilder implements ModelBuilder
{

    private static final Cloner CLONER = new Cloner();

    private Context _Context;

    public DefaultModelBuilder()
    {

    }

    @Override
    public <M extends Model> M copyModel(final Model model)
    {

        final DefaultModel defaultModel = (DefaultModel) model;
        final Model clone = new DefaultModel(model.getContext(), defaultModel.getModelState().clone());
        return clone.newAlternate(model.getDimensions());
    }

    @Override
    public Context getContext()
    {

        return _Context;
    }

    @Override
    public void init(final Context context)
    {

        _Context = context;
    }

    @Override
    public final Dimensions newDimensions(final Class<?> schemaInterface) throws ContextException
    {

        return newDimensions(getSchemaLoader().getTypeUri(schemaInterface));
    }

    @Override
    public final Dimensions newDimensions(final String schemaInterfaceName) throws ContextException
    {

        return newDimensions(getSchemaLoader().getTypeUri(schemaInterfaceName));
    }

    @Override
    public final Dimensions newDimensions(final URI schemaUri) throws ContextException
    {

        final Dimensions dimensions = new DimensionsBuilder(schemaUri).toDimensions();
        return dimensions;
    }

    @Override
    public final Model newModel() throws ModelBuilderException
    {

        return new DefaultModel(getSchemaLoader().getContext(), new DefaultModelState());
    }

    @Override
    public final <M extends Model> M newModel(final Class<?> schemaInterface) throws ModelBuilderException
    {

        return newModel(newDimensions(schemaInterface));
    }

    @Override
    public final <M extends Model> M newModel(final Dimensions dimensions) throws ModelBuilderException
    {

        return newModel(dimensions, (ModelState) null);
    }

    @Override
    public <M extends Model> M newModel(final Dimensions dimensions, final ConcurrentHashMap<String, Object> slots)
            throws ModelBuilderException
    {

        return newModel(dimensions, new DefaultModelState(UUID.randomUUID(), slots));
    }

    @Override
    public final <M extends Model> M newModel(final String schemaInterfaceName) throws ModelBuilderException
    {

        return newModel(newDimensions(schemaInterfaceName));
    }

    @Override
    public final <M extends Model> M newModel(final URI schemaUri) throws ModelBuilderException
    {

        return newModel(newDimensions(schemaUri));
    }

    protected final <M extends Model> M newModel(final Dimensions dimensions, final ModelState existingState)
            throws ModelBuilderException
    {

        if (dimensions == null)
        {
            throw new ModelBuilderException("The dimensions cannot be null.", null, this);
        }

        final SchemaLoader schemaLoader = getSchemaLoader();
        final Context context = schemaLoader.getContext();
        final URI schemaUri = dimensions.getSchemaUri();
        final Prototype prototype = schemaLoader.getPrototype(schemaUri);

        final ModelState modelState;
        final boolean setDefaults;

        if (existingState != null)
        {
            modelState = existingState;
            setDefaults = false;
        }
        else
        {
            modelState = new DefaultModelState(UUID.randomUUID());
            setDefaults = true;

        }

        final DefaultModel model = new DefaultModel(context, modelState);
        if (setDefaults)
        {
            setDefaultValues(model, prototype);
        }

        Class<?>[] schemaInterfaceArray = null;
        Class<?> schemaInterface = null;
        try
        {
            schemaInterface = schemaLoader.getSchemaInterface(schemaUri);
        }
        catch (final ClassNotFoundException e)
        {
            throw new ModelBuilderException("Unable to load the Java class representation of: " + schemaUri, e, this);
        }

        if (schemaInterface != null)
        {
            if (!schemaInterface.isInterface())
            {
                throw new ModelBuilderException(
                        "The requested schema already exists as a Java class (it is an implementation, not an interface).",
                        null, this);
            }

            if (prototype.isAbstract())
            {
                throw new ModelBuilderException("The requested schema (" + schemaUri
                        + ") is *abstract* and may never exist as a model.", null, this);
            }

            if (ValueType.JAVA_TYPE_MODEL.isAssignableFrom(schemaInterface))
            {
                // The requested schema already extends Model (as expected)
                schemaInterfaceArray = new Class<?>[]{schemaInterface};
            }
            else
            {
                // The requested shema interface does not extend Model, add it to the Proxy's list manually.
                schemaInterfaceArray = new Class<?>[]{schemaInterface, ValueType.JAVA_TYPE_MODEL};
            }
        }

        model.setDimensions(dimensions);

        // Defer to Java's cool Proxy class to work its magic.
        // This alternate instance implements the Java interface(s) associated with the Dimension-specified WRML schema.
        @SuppressWarnings("unchecked")
        final M typedModel = (M) Proxy.newProxyInstance((ClassLoader) schemaLoader, schemaInterfaceArray, model);

        // Formally initialize the typed model by running the slot values through the constraints.
        final Map<String, Object> slotMap = typedModel.getSlotMap();
        for (final String slotName : slotMap.keySet())
        {
            final Object slotValue = slotMap.get(slotName);
            final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);

            if (protoSlot != null && protoSlot.getValueType() != ValueType.List)
            {
                typedModel.setSlotValue(slotName, slotValue);
            }
        }

        return typedModel;

    }

    protected void setDefaultValues(final Model model, final Prototype prototype)
    {

        final SortedSet<String> slotNames = prototype.getAllSlotNames();
        for (final String slotName : slotNames)
        {
            final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);
            if (protoSlot instanceof PropertyProtoSlot && !protoSlot.isAlias())
            {
                final PropertyProtoSlot propertyProtoSlot = (PropertyProtoSlot) protoSlot;
                final Object defaultValue = propertyProtoSlot.getDefaultValue();
                if (defaultValue != null)
                {
                    model.setSlotValue(slotName, defaultValue);
                }
            }
        }
    }

    private SchemaLoader getSchemaLoader()
    {

        return getContext().getSchemaLoader();
    }

    protected class DefaultModelState implements ModelState
    {

        private final UUID _HeapId;

        private final ConcurrentHashMap<String, Object> _Slots;

        private String _OriginServiceName;

        public DefaultModelState()
        {

            this(UUID.randomUUID());
        }

        protected DefaultModelState(final DefaultModelState source)
        {

            //this(UUID.randomUUID(), CLONER.deepClone(source._Slots));
            this(UUID.randomUUID(), source._Slots);
            _OriginServiceName = source._OriginServiceName;
        }

        protected DefaultModelState(final UUID heapId)
        {

            this(heapId, new ConcurrentHashMap<String, Object>());
        }

        protected DefaultModelState(final UUID heapId, final ConcurrentHashMap<String, Object> slots)
        {

            _HeapId = heapId;
            _Slots = slots;
        }

        @Override
        public Object clearSlotValue(final Model model, final String slotName, final URI schemaUri)
        {

            String realSlotName = slotName;
            if (schemaUri != null)
            {
                final SchemaLoader schemaLoader = getSchemaLoader();
                final Prototype prototype = schemaLoader.getPrototype(schemaUri);
                if (prototype != null)
                {
                    final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);
                    if (protoSlot != null && protoSlot.isAlias())
                    {
                        realSlotName = protoSlot.getRealName();
                    }
                }
            }

            if (_Slots.containsKey(realSlotName))
            {
                final Object oldValue = _Slots.remove(realSlotName);
                return oldValue;

            }
            return null;
        }

        @Override
        public DefaultModelState clone()
        {

            return new DefaultModelState(this);
        }

        @Override
        public boolean containsSlotValue(final Model model, final String slotName, final URI schemaUri)
        {

            String realSlotName = slotName;
            if (schemaUri != null)
            {
                final SchemaLoader schemaLoader = getSchemaLoader();
                final Prototype prototype = schemaLoader.getPrototype(schemaUri);
                if (prototype != null)
                {
                    final ProtoSlot protoSlot = prototype.getProtoSlot(slotName, false);
                    if (protoSlot != null && protoSlot.isAlias())
                    {
                        realSlotName = protoSlot.getRealName();
                    }
                }
            }

            return _Slots.containsKey(realSlotName);
        }

        @Override
        public boolean equals(final Object obj)
        {

            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final DefaultModelState other = (DefaultModelState) obj;
            if (_HeapId == null)
            {
                if (other._HeapId != null)
                {
                    return false;
                }
            }
            else if (!_HeapId.equals(other._HeapId))
            {
                return false;
            }
            return true;
        }

        @Override
        public UUID getHeapId(final Model model)
        {

            return _HeapId;
        }

        @Override
        public String getOriginServiceName(final Model model)
        {

            return _OriginServiceName;
        }

        @Override
        public Object getSlotValue(final Model model, final String slotName, final URI schemaUri, final boolean strict)
        {

            String realSlotName = slotName;
            if (schemaUri != null)
            {
                final SchemaLoader schemaLoader = getSchemaLoader();
                final Prototype prototype = schemaLoader.getPrototype(schemaUri);
                if (prototype != null)
                {
                    final ProtoSlot protoSlot = prototype.getProtoSlot(slotName, strict);
                    if (protoSlot != null && protoSlot.isAlias())
                    {
                        realSlotName = protoSlot.getRealName();
                    }
                }
            }

            if (_Slots.containsKey(realSlotName))
            {
                return _Slots.get(realSlotName);
            }

            if (schemaUri == null)
            {
                return null;
            }

            final SchemaLoader schemaLoader = getSchemaLoader();

            final Prototype prototype = schemaLoader.getPrototype(schemaUri);
            final ProtoSlot protoSlot = prototype.getProtoSlot(realSlotName, strict);

            if (protoSlot == null)
            {
                return null;
            }

            /*
            if (protoSlot instanceof LinkProtoSlot)
            {
                final LinkProtoSlot linkProtoSlot = (LinkProtoSlot) protoSlot;
                final Link link = newModel(schemaLoader.getLinkSchemaUri());
                link.setRel(linkProtoSlot.getProtoRel().getUri());
                _Slots.put(realSlotName, link);
                return link;
            }
            */
            if (protoSlot.getValueType() == ValueType.List)
            {
                // Lazily create the List slot value when requested

                final List<?> emptyList = new LinkedList<>();
                _Slots.put(realSlotName, emptyList);
                return emptyList;
            }
            else if (protoSlot instanceof PropertyProtoSlot)
            {
                // return the prototype's default

                return ((PropertyProtoSlot) protoSlot).getDefaultValue();
            }

            return null;
        }

        @Override
        public Map<String, Object> getValuedSlots(final Model model)
        {

            return _Slots;
        }

        @Override
        public int hashCode()
        {

            final int prime = 31;
            int result = 1;
            result = prime * result + ((_HeapId == null) ? 0 : _HeapId.hashCode());
            return result;
        }

        @Override
        public String setOriginServiceName(final Model model, final String originServiceName)
        {

            final String oldOriginServiceName = _OriginServiceName;
            _OriginServiceName = originServiceName;
            return oldOriginServiceName;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public Object setSlotValue(final Model model, final String slotName, final Object newValue, final URI schemaUri, final boolean strict)
        {

            String realSlotName = slotName;
            Object oldValue = null;
            Prototype prototype = model.getPrototype();
            ProtoSlot protoSlot = null;

            if (schemaUri != null)
            {

                final SchemaLoader schemaLoader = getSchemaLoader();
                if (prototype == null)
                {
                    prototype = schemaLoader.getPrototype(schemaUri);
                }

                protoSlot = prototype.getProtoSlot(slotName, strict);
                if (protoSlot == null)
                {
                    return null;
                }

                realSlotName = protoSlot.getRealName();
                oldValue = _Slots.get(realSlotName);

                final PropertyProtoSlot propertyProtoSlot = (protoSlot instanceof PropertyProtoSlot) ? (PropertyProtoSlot) protoSlot
                        : null;

                if (propertyProtoSlot != null)
                {

                    if (oldValue == null)
                    {
                        oldValue = propertyProtoSlot.getDefaultValue();
                    }

                    propertyProtoSlot.validateNewValue(model, newValue);

                    if (protoSlot.getValueType() == ValueType.List && newValue instanceof Collection)
                    {
                        final List list = (List) getSlotValue(model, realSlotName, schemaUri, strict);
                        if (list != newValue)
                        {
                            list.clear();
                            list.addAll((Collection) newValue);
                            return list;
                        }

                        return newValue;
                    }
                }
            }


            if (newValue == null)
            {
                _Slots.remove(realSlotName);
            }
            else
            {
                _Slots.put(realSlotName, newValue);
            }

            return oldValue;
        }

        @Override
        public String toString()
        {

            return getClass().getSimpleName() + " { heapId : " + _HeapId + ", slots : " + _Slots + ", origin : " + _OriginServiceName + "}";
        }


    }

}
