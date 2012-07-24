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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.wrml.event.EventManager;
import org.wrml.event.EventSource;
import org.wrml.model.Model;
import org.wrml.model.ModelEvent;
import org.wrml.model.ModelEventListener;
import org.wrml.model.schema.Schema;
import org.wrml.util.UniversallyUnique;
import org.wrml.util.observable.DelegatingObservableList;
import org.wrml.util.observable.DelegatingObservableMap;
import org.wrml.util.observable.MapEventListener;
import org.wrml.util.observable.ObservableMap;
import org.wrml.util.observable.Observables;

/**
 * This is the runtime "storage of record" for Models so that no matter how
 * many times (and ways) a Model is referenced at runtime, it can be
 * consistently updated and viewed without duplicated effort. To ModelHeap helps
 * accomplish this goal, by providing an easy and logical way to access fields,
 * in the area that most programming platforms might call "the heap".
 * 
 * A core component of the WRML runtime, the heap holds the {@link Model}
 * instance data (field values) and manages the {@link ModelReference}s, which
 * provide "POJO proxy views" of the modeled data. The ModelHeap is the de facto
 * cache of all models. It "manages" all of the {@link Model} data in order to
 * provide a convenient hook to learn about the comings and goings of models in
 * and out of the runtime.
 * 
 * The ModelHeap must be used to create new models so that their "identity" can
 * be properly managed by the framework. The runtime works with the ModelHeap to
 * ensure that only one Model instance (of a given {@link Schema}) will be
 * associated with a given identity (URI). This is a key component of the MVC
 * framework used by the runtime and offered to apps.
 * 
 * Internally the heap is sharded by model "type", communicated using schema ids
 * (schema document URIs). The heap creates its the typed shards as needed to
 * hold data of the shard's type. Each shard acts as monomorphic "table",
 * holding all of the model field values associated with a specific
 * {@link Schema}. Thus the schema id ({@link URI}) acts as the "top-level" key
 * to the shards themselves.
 */
public final class ModelHeap implements EventSource<ModelHeapEventListener> {

    /** The runtime context for this heap */
    private final Context _Context;

    /** Manages the implementation of {@link EventSource} */
    private final EventManager<ModelHeapEventListener> _EventManager;

    private final boolean _StrictlyStatic;

    /** Shard to hold all of the models with undefined (null) schema id */
    private final Shard _UndefinedShard;

    /** Map of schema id to shard */
    private final ObservableMap<URI, Shard> _Shards;

    /**
     * Creates a new ModelHeap to hold model state (field values).
     * 
     * @param context
     *            The runtime context of this heap.
     * 
     * @param strictlyStatic
     *            True if the heap should restrict the setting of field values
     *            to those fields named by the schema.
     */
    public ModelHeap(Context context, final boolean strictlyStatic) {

        _Context = context;
        _EventManager = new EventManager<ModelHeapEventListener>(ModelHeapEventListener.class);
        _StrictlyStatic = strictlyStatic;
        _UndefinedShard = new Shard(null);

        // TODO: Does the WeakHashMap make sense here?
        // TODO: Set initial size of the internal map?

        _Shards = Observables.observableMap(new WeakHashMap<URI, Shard>());
    }

    @Override
    public boolean addEventListener(ModelHeapEventListener eventListener) {
        if (_EventManager.addEventListener(eventListener)) {

            // TODO: Listen to the Shard

            return true;
        }

        return false;
    }

    public boolean containsHeapId(URI schemaId, UUID heapId) {
        final Shard shard = getShard(schemaId);
        if (shard == null) {
            return false;
        }

        return shard.containsHeapId(heapId);
    }

    public boolean containsKey(URI schemaId, Object key) {
        final Shard shard = getShard(schemaId);
        if (shard == null) {
            return false;
        }

        return shard.containsKey(key);
    }

    public Context getContext() {
        return _Context;
    }

    public Map<String, Object> getFields(URI schemaId, Model model) {
        final Shard shard = getShard(schemaId);
        if (shard == null) {
            return Collections.emptyMap();
        }

        return shard.getFields(model);

    }

    public UUID getHeapId(URI schemaId, Object key) {
        final Shard shard = getShard(schemaId);
        if (shard == null) {
            return null;
        }

        return shard.getHeapId(key);
    }

    public Object getKey(URI schemaId, UUID heapId) {
        final Shard shard = getShard(schemaId);
        if (shard == null) {
            return null;
        }

        return shard.getKey(heapId);
    }

    public boolean isStrictlyStatic() {
        return _StrictlyStatic;
    }

    @Override
    public boolean removeEventListener(ModelHeapEventListener eventListener) {

        if (_EventManager.removeEventListener(eventListener)) {

            // TODO: Detach listener(s) from the Shard

            return true;
        }

        return false;
    }

    boolean addModelEventListener(Model model, URI schemaId, ModelEventListener eventListener) {
        final Shard shard = getShard(schemaId);
        if (shard == null) {
            return false;
        }

        return shard.addModelEventListener(model, eventListener);
    }

    ModelReference createModelReference() {
        return createModelReference(null);
    }

    ModelReference createModelReference(UUID heapId) {
        final Context context = getContext();

        if (heapId == null) {
            heapId = UniversallyUnique.IdFactory.createId();
        }

        return new ModelReference(context, heapId);
    }

    Object getFieldValue(final Model model, final URI schemaId, final String fieldName) {

        if (schemaId == null) {
            return _UndefinedShard.getFieldValue(model, fieldName, null);
        }

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Prototype prototype = schemaLoader.getPrototype(schemaId);
        final Prototype.Field protoField = prototype.getPrototypeField(fieldName);

        if (protoField == null) {
            if (isStrictlyStatic()) {
                return null;
            }
            else {
                return _UndefinedShard.getFieldValue(model, fieldName, null);
            }
        }

        Shard shard = null;
        if (!_Shards.containsKey(schemaId)) {
            return protoField.getDefaultValue();
        }
        else {
            shard = _Shards.get(schemaId);
        }

        return shard.getFieldValue(model, fieldName, protoField);
    }

    Shard getShard(URI schemaId) {
        if (schemaId == null) {
            return getUndefinedShard();
        }
        return _Shards.get(schemaId);
    }

    Shard getUndefinedShard() {
        return _UndefinedShard;
    }

    boolean removeModelEventListener(Model model, URI schemaId, ModelEventListener eventListener) {
        final Shard shard = getShard(schemaId);
        if (shard == null) {
            return false;
        }

        return shard.removeModelEventListener(model, eventListener);
    }

    Object setFieldValue(final Model model, URI schemaId, final String fieldName, final Object newValue) {

        if (schemaId == null) {
            return _UndefinedShard.setFieldValue(model, fieldName, newValue, null);
        }

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Prototype prototype = schemaLoader.getPrototype(schemaId);
        final Prototype.Field protoField = prototype.getPrototypeField(fieldName);

        if (protoField == null) {
            if (isStrictlyStatic()) {
                throw new NoSuchFieldException("There is no field named \"" + fieldName + "\" in model: " + model,
                        null, model, fieldName);
            }
            else {
                return _UndefinedShard.setFieldValue(model, fieldName, newValue, null);
            }
        }

        protoField.validateWrite(model, newValue);

        Shard shard = null;
        if (!_Shards.containsKey(schemaId)) {
            shard = new Shard(schemaId);
            _Shards.put(schemaId, shard);

            if (_EventManager.isEventHearable()) {
                final ModelHeapEvent event = new ModelHeapEvent(this, schemaId);
                _EventManager.fireEvent(ModelHeapEventListener.EventType.shardCreated, event);
            }
        }
        else {
            shard = _Shards.get(schemaId);
        }

        final Object oldValue = shard.setFieldValue(model, fieldName, newValue, protoField);

        if (_EventManager.isEventHearable()) {
            final ModelHeapEvent event = new ModelHeapEvent(this, model.getHeapId(), schemaId, fieldName);
            _EventManager.fireEvent(ModelHeapEventListener.EventType.fieldValueSet, event);
        }

        return oldValue;
    }

    void unsetField(URI schemaId, Model model, String fieldName) {
        final Shard shard = getShard(schemaId);
        if (shard == null) {
            return;
        }

        shard.unsetField(model, fieldName);
    }

    /**
     * Map of {@link String} (field names) to {@link Object} (field values).
     * 
     * Instances of this simple internal class hold *all* of a WRML
     * program's
     * runtime data (model field values).
     * 
     * The internal {@link Shard} class maps model heap ids to instances of
     * FieldMap.
     */
    private static class FieldMap extends DelegatingObservableMap<String, Object> {

        public FieldMap(Class<MapEventListener> listenerClass, Map<String, Object> delegate) {
            super(listenerClass, delegate);
        }

        public FieldMap(Map<String, Object> delegate) {
            super(delegate);
        }
    }

    /**
     * The WRML solution to model singularity (aka instance folding) is to
     * use the field values (named by schema keys) to determine
     * schema-type-centric uniqueness among {@link Model} instances sharing
     * the same schema.
     */
    private static class KeyedHeapIds {

        /** Maps (model) heap ids to key values */
        private final ObservableMap<UUID, Object> _HeapIdToKey;

        /** Maps (model) key values to heap id */
        private final ObservableMap<Object, UUID> _KeyToHeapId;

        public KeyedHeapIds() {
            _HeapIdToKey = Observables.observableMap(new ConcurrentHashMap<UUID, Object>());
            _KeyToHeapId = Observables.observableMap(new ConcurrentHashMap<Object, UUID>());
        }

        public boolean containsHeapId(UUID heapId) {
            return _HeapIdToKey.containsKey(heapId);
        }

        public boolean containsKey(Object key) {
            return _KeyToHeapId.containsKey(key);
        }

        public UUID getHeapId(Object key) {
            if (_KeyToHeapId.containsKey(key)) {
                return _KeyToHeapId.get(key);
            }
            return null;
        }

        public Object getKey(UUID heapId) {
            if (_HeapIdToKey.containsKey(heapId)) {
                return _HeapIdToKey.get(heapId);
            }
            return null;
        }

        public synchronized void put(UUID heapId, Object key) {
            _HeapIdToKey.put(heapId, key);
            _KeyToHeapId.put(key, heapId);
        }

        public synchronized boolean remove(UUID heapId) {
            if (!_HeapIdToKey.containsKey(heapId)) {
                return false;
            }

            final Object key = _HeapIdToKey.get(heapId);
            _HeapIdToKey.remove(heapId);

            if ((key == null) || !_KeyToHeapId.containsKey(key)) {
                return false;
            }

            return (_KeyToHeapId.remove(key) != null);
        }
    }

    private static class Shard {

        private final URI _SchemaId;
        private final ObservableMap<UUID, FieldMap> _ModelFields;
        private final KeyedHeapIds _KeyedHeapIds;
        private final Map<UUID, EventManager<ModelEventListener>> _EventManagers;
        private List<Shard> _EphemeralShards;

        Shard(final URI schemaId) {

            _SchemaId = schemaId;

            // TODO: Does the WeakHashMap make sense here?
            // TODO: Set initial size of the internal map?

            _ModelFields = Observables.observableMap(new WeakHashMap<UUID, FieldMap>());
            _KeyedHeapIds = new KeyedHeapIds();
            _EventManagers = new HashMap<UUID, EventManager<ModelEventListener>>();
        }

        @Override
        public String toString() {
            return "Shard [schemaId = " + getSchemaId() + "]";
        }

        boolean addModelEventListener(Model model, ModelEventListener eventListener) {

            final UUID heapId = model.getHeapId();
            if (!containsHeapId(heapId)) {
                return false;
            }

            EventManager<ModelEventListener> eventManager = null;
            if (!_EventManagers.containsKey(heapId)) {
                eventManager = new EventManager<ModelEventListener>(ModelEventListener.class);
                _EventManagers.put(heapId, eventManager);
            }

            eventManager = _EventManagers.get(heapId);
            return eventManager.addEventListener(eventListener);
        }

        boolean containsHeapId(UUID heapId) {
            return _ModelFields.containsKey(heapId);
        }

        boolean containsKey(Object key) {
            return _KeyedHeapIds.containsKey(key);
        }

        Map<String, Object> getFields(Model model) {

            final UUID heapId = model.getHeapId();
            if (!containsHeapId(heapId)) {
                return Collections.emptyMap();
            }
            final FieldMap fieldMap = _ModelFields.get(heapId);
            final Map<String, Object> fields = new TreeMap<String, Object>();
            fields.putAll(fieldMap);
            return fields;
        }

        Object getFieldValue(final Model model, final String fieldName, final Prototype.Field protoField) {
            final UUID heapId = model.getHeapId();

            // TODO: Consider the model's dimensions (look up value based on Locale for example)

            FieldMap fieldMap = null;
            if (!_ModelFields.containsKey(heapId)) {
                return (protoField != null) ? protoField.getDefaultValue() : null;
            }
            else {
                fieldMap = _ModelFields.get(heapId);
            }

            if (!fieldMap.containsKey(fieldName)) {
                return (protoField != null) ? protoField.getDefaultValue() : null;
            }

            Object fieldValue = fieldMap.get(fieldName);
            if (fieldValue == null) {

                if (protoField.isListType()) {

                    // Lazily create the List field value when requested

                    fieldValue = new DelegatingObservableList<Object>(new ArrayList<Object>());
                    fieldMap.put(fieldName, fieldValue);
                }
                else if (protoField.isMapType()) {

                    // Lazily create the Map field value when requested

                    fieldValue = new DelegatingObservableMap<Object, Object>(new HashMap<Object, Object>());
                    fieldMap.put(fieldName, fieldValue);
                }
            }

            return fieldValue;
        }

        UUID getHeapId(Object key) {
            return _KeyedHeapIds.getHeapId(key);
        }

        Object getKey(UUID heapId) {
            return _KeyedHeapIds.getKey(heapId);
        }

        URI getSchemaId() {
            return _SchemaId;
        }

        boolean removeModelEventListener(Model model, ModelEventListener eventListener) {

            final UUID heapId = model.getHeapId();
            if (!containsHeapId(heapId)) {
                return false;
            }

            EventManager<ModelEventListener> eventManager = null;
            if (!_EventManagers.containsKey(heapId)) {
                return false;
            }

            eventManager = _EventManagers.get(heapId);
            final boolean result = eventManager.removeEventListener(eventListener);
            if (result && (eventManager.getEventListenerCount() == 0)) {
                /*
                 * There are no more listeners for this model (with this
                 * schema), unmap the event manager so that it may be GC'ed.
                 */
                _EventManagers.remove(heapId);
            }

            return result;
        }

        Object setFieldValue(final Model model, final String fieldName, final Object newValue,
                final Prototype.Field protoField) {

            final UUID heapId = model.getHeapId();

            FieldMap fieldMap = null;
            if (!_ModelFields.containsKey(heapId)) {
                final Map<String, Object> internalFieldMap = new HashMap<String, Object>();
                fieldMap = new FieldMap(internalFieldMap);
                _ModelFields.put(heapId, fieldMap);
            }
            else {
                fieldMap = _ModelFields.get(heapId);
            }

            final Object oldvalue = fieldMap.put(fieldName, newValue);

            if (_EventManagers.containsKey(heapId)) {
                final EventManager<ModelEventListener> eventManager = _EventManagers.get(heapId);
                if (eventManager.isEventHearable()) {
                    final ModelEvent event = new ModelEvent(model, fieldName);
                    eventManager.fireEvent(ModelEventListener.EventType.fieldValueSet, event);
                }
            }

            return oldvalue;
        }

        void unsetField(Model model, String fieldName) {
            final UUID heapId = model.getHeapId();
            if (!containsHeapId(heapId)) {
                return;
            }

            final FieldMap fieldMap = _ModelFields.get(heapId);
            fieldMap.remove(fieldName);

            _KeyedHeapIds.remove(heapId);

            if (_EventManagers.containsKey(heapId)) {
                final EventManager<ModelEventListener> eventManager = _EventManagers.get(heapId);
                if (eventManager.isEventHearable()) {
                    final ModelEvent event = new ModelEvent(model, fieldName);
                    eventManager.fireEvent(ModelEventListener.EventType.fieldValueUnset, event);
                }
            }
        }
    }

}
