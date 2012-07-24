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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.reflect.TypeUtils;

import org.wrml.event.EventManager;
import org.wrml.event.EventSource;
import org.wrml.model.MapEntry;
import org.wrml.model.Model;
import org.wrml.util.observable.DelegatingObservableList;
import org.wrml.util.transformer.ToStringTransformer;

/**
 * An abstract representation of a serialized graph of WRML models, which
 * sort of resembles a DOM.
 */
public final class ModelGraph implements EventSource<ModelGraphEventListener> {

    private static final String SCHEMA_ID_FIELD_NAME = "schemaId";

    /** The context to operate within. */
    private final Context _Context;

    /** Manages the implementation of {@link EventSource} */
    private final EventManager<ModelGraphEventListener> _EventManager;

    /** The dimensions to apply to the graph's root model. */
    private final Dimensions _RootModelDimensions;

    /**
     * Follows the scope (nesting/indentation) of the graph's models (sets of
     * fields), and lists (of models or other "raw" types).
     */
    private final Stack<ScopeType> _ScopeStack;

    /** Follows the focus of the graph's models. */
    private final Stack<Model> _ModelStack;

    /** Follows the bread-crumb-like scope of fields. */
    private final Stack<String> _FieldNameStack;

    /** Follows the nesting of lists. */
    private final Stack<List<Object>> _ListStack;

    /** Follows the model dimensions. */
    private final Stack<Dimensions> _DimensionsStack;

    /**
     * Create a new model graph.
     * 
     * @param context
     *            The context to operate within.
     * 
     * @param rootModelDimensions
     *            The dimensions to use for the root model.
     */
    public ModelGraph(final Context context, final Dimensions rootModelDimensions) {

        _Context = context;
        _RootModelDimensions = rootModelDimensions;

        // Create the graph's focus tracking stacks
        _ScopeStack = new Stack<ScopeType>();
        _ModelStack = new Stack<Model>();
        _FieldNameStack = new Stack<String>();
        _ListStack = new Stack<List<Object>>();
        _DimensionsStack = new Stack<Dimensions>();

        _EventManager = new EventManager<ModelGraphEventListener>(ModelGraphEventListener.class);
    }

    @Override
    public boolean addEventListener(ModelGraphEventListener eventListener) {
        return _EventManager.addEventListener(eventListener);
    }

    public Object endList() {
        final List<Object> list = _ListStack.pop();
        _ScopeStack.pop();

        return endValue(list);
    }

    public Model endModel() {
        final Model model = _ModelStack.pop();
        _ScopeStack.pop();

        return (Model) endValue(model);
    }

    /**
     * Process a "raw" value (from a serialized model graph) into one that is
     * ready for the runtime.
     * 
     * @param rawValue
     *            The "raw" value to process. A raw value is used in
     *            serialization of model graphs.
     * 
     * @return The processed value, ready for runtime.
     */
    @SuppressWarnings("unchecked")
    public Object endValue(final Object rawValue) {

        /*
         * As a special case for in-line model typing, WRML allows the
         * "schemaId" field to be present "on the wire". This field
         * is "dimensional" in nature, so we will store it in the dimensions
         * instead of the model itself.
         * 
         * Compare the field's name to "schemaId" and update the
         * Dimensions accordingly.
         */
        if (!_FieldNameStack.isEmpty() && _FieldNameStack.peek().equals(SCHEMA_ID_FIELD_NAME)) {
            _DimensionsStack.peek().setRequestedSchemaId(URI.create(String.valueOf(rawValue)));
            _FieldNameStack.pop();
            return null;
        }

        /*
         * Raw Serialized Type ---> Runtime Type
         * ----------------
         * Integer ---> Integer
         * Float ---> Float
         * Long ---> Long
         * Boolean ---> Boolean
         * String ---> Text, Date, Choice, or "Other"
         * List ---> ObservableList or ObservableMap
         * Model ---> Model (subtype determined by Dimensions' schema id)
         * Native ---> Native (?)
         */

        Object runtimeValue = null;
        boolean isModel = false;
        if (TypeUtils.isInstance(rawValue, Model.class)) {
            isModel = true;

            /*
             * Convert "raw" (undefined) model values here to handle the root
             * model case.
             */
            runtimeValue = convertRawModelValue((Model) rawValue);

        }

        if (!_ScopeStack.isEmpty()) {

            if (TypeUtils.isInstance(rawValue, String.class)) {

                runtimeValue = convertRawStringValue((String) rawValue);

            }
            else if (TypeUtils.isInstance(rawValue, Integer.class)) {

                runtimeValue = convertRawIntegerValue((Integer) rawValue);

            }
            else if (TypeUtils.isInstance(rawValue, List.class)) {

                runtimeValue = convertRawListValue((List<Object>) rawValue);

            }
            else if (!isModel) {

                /*
                 * The other "raw" types are equivalent to their "runtime" type
                 * counterparts; no conversion necessary.
                 */

                runtimeValue = rawValue;
            }

            final ScopeType scopeType = _ScopeStack.peek();

            switch (scopeType) {

            case Model:

                /*
                 * The graph is focused on the parent model; meaning that a
                 * field should be set.
                 */

                final Model parentModel = _ModelStack.peek();
                final String fieldName = _FieldNameStack.peek();

                /*
                 * Set fields as the "undefined" (null) schema.
                 */
                parentModel.setFieldValue(null, fieldName, runtimeValue);

                /*
                 * Now finished with this field, shift focus.
                 */
                _FieldNameStack.pop();

                break;

            case List:

                /*
                 * The graph is focused on a list; meaning that an
                 * element should be added.
                 */

                final List<Object> list = _ListStack.peek();
                list.add(runtimeValue);

                break;
            }
        }

        if (_EventManager.isEventHearable()) {
            final ModelGraphEvent event = new ModelGraphEvent(this, rawValue, runtimeValue);
            _EventManager.fireEvent(ModelGraphEventListener.EventType.valueEnded, event);
        }

        return runtimeValue;

    }

    public Context getContext() {
        return _Context;
    }

    public Dimensions getFocusedDimensions() {
        if (_DimensionsStack.isEmpty()) {
            return null;
        }
        return _DimensionsStack.peek();
    }

    public String getFocusedFieldName() {
        if (_FieldNameStack.isEmpty()) {
            return null;
        }
        return _FieldNameStack.peek();
    }

    public List<?> getFocusedList() {
        if (_ListStack.isEmpty()) {
            return null;
        }
        return _ListStack.peek();
    }

    public Model getFocusedModel() {
        if (_ModelStack.isEmpty()) {
            return null;
        }
        return _ModelStack.peek();
    }

    /**
     * Returns the graph's root model dimensions.
     * 
     * @return the graph's root model dimensions.
     */
    public Dimensions getRootModelDimensions() {
        return _RootModelDimensions;
    }

    @Override
    public boolean removeEventListener(ModelGraphEventListener eventListener) {
        return _EventManager.removeEventListener(eventListener);
    }

    /**
     * Create a new field (must be called with model focus).
     * 
     * @param fieldName
     *            the name of the field to create storage for within the graph's
     *            focused model.
     */
    public void startField(final String fieldName) {

        if (_ScopeStack.isEmpty() || _ModelStack.isEmpty() || (_ScopeStack.peek() != ScopeType.Model)) {

            // TODO: Future, move hard-coded messages like this into a "StringTable & MessageFormat" utility class
            throw new ModelGraphException("Field must be created within a model's scope.", this);
        }

        _FieldNameStack.push(fieldName);

        if (_EventManager.isEventHearable()) {
            final ModelGraphEvent event = new ModelGraphEvent(this);
            _EventManager.fireEvent(ModelGraphEventListener.EventType.fieldStarted, event);
        }
    }

    /**
     * Create a new list and make it the focus of this graph. Note that lists
     * may not start graphs, so you must create a model and a field before you
     * may create any graph lists.
     */
    public void startList() {

        if (_ScopeStack.isEmpty() || _ModelStack.isEmpty() || _FieldNameStack.isEmpty()) {

            // TODO: Future, move hard-coded messages like this into a "StringTable & MessageFormat" utility class
            throw new ModelGraphException(
                    "Graph lists may be created for a field value of a model or as an element of another graph list; but the whole graph must be model-rooted.",
                    this);
        }

        final List<Object> newList = new DelegatingObservableList<Object>(new ArrayList<Object>());

        // Give the new list focus 
        _ListStack.push(newList);
        _ScopeStack.push(ScopeType.List);

        if (_EventManager.isEventHearable()) {
            final ModelGraphEvent event = new ModelGraphEvent(this);
            _EventManager.fireEvent(ModelGraphEventListener.EventType.listStarted, event);
        }
    }

    /**
     * Create a new model and make it the focus of this graph.
     */
    public void startModel() {

        final Context context = getContext();
        final ModelHeap modelHeap = context.getModelHeap();

        /*
         * Create a new model, with an "undefined" schema.
         */
        final ModelReference newModel = modelHeap.createModelReference();
        Dimensions newModelDimensions = null;

        if (_ScopeStack.isEmpty()) {

            /*
             * This is the first model in the graph; the root model. The first
             * model's creation is how a graph begins.
             */

            /*
             * Use the specified root model's dimensions for this model.
             * 
             * Note that the root model's dimensions may have a requested
             * schema id already set.
             * 
             * If however, during the course of creating this model's fields,
             * the graph discovers a "schemaId" field then we will trust the
             * model to tell us it's type.
             */

            newModelDimensions = _RootModelDimensions;
        }
        else {

            /*
             * Make an effort to find the best "default" schema id for the new
             * (child/nested) model by consulting the focused field's prototype
             * (if available).
             * 
             * NOTE: If we have any scope at all, then the graph's rules state
             * that we must also have a parent model ("parent" in the model
             * graph hierarchy sense; not to be confused with schematic
             * inheritance and "base" schemas).
             */

            final String fieldName = _FieldNameStack.peek();
            final Dimensions parentDimensions = _DimensionsStack.peek();

            /*
             * Initialize the new dimensions from the (graph's) parent
             * dimensions
             */
            newModelDimensions = new Dimensions(parentDimensions);
            URI newModelSchemaId = null;

            final URI parentSchemaId = parentDimensions.getRequestedSchemaId();
            if (parentSchemaId != null) {

                /*
                 * If the parent's schema id is known, attempt to use it to
                 * determine the (default) schema id of new model.
                 */

                final SchemaLoader schemaLoader = context.getSchemaLoader();
                final Prototype parentPrototype = schemaLoader.getPrototype(parentSchemaId);
                final Prototype.Field parentField = parentPrototype.getPrototypeField(fieldName);

                if (parentField != null) {

                    /*
                     * The prototype field associated with the parent model's
                     * schema may help us determine the default schematic type
                     * identity for the model we are about to create.
                     */

                    final ScopeType scopeType = _ScopeStack.peek();

                    switch (scopeType) {

                    case Model:

                        if (parentField.isModelType()) {
                            newModelSchemaId = parentField.getModelSchemaId();
                        }
                        else {
                            // TODO: Handle MapEntry model's key and value types
                        }

                        break;

                    case List:

                        if (parentField.isListOfModels()) {
                            newModelSchemaId = parentField.getListElementSchemaId();
                        }
                        else if (parentField.isMapType()) {
                            newModelSchemaId = schemaLoader.getSchemaId(MapEntry.class.getCanonicalName());
                        }

                        break;
                    }
                }
            }

            newModelDimensions.setRequestedSchemaId(newModelSchemaId);
        }

        /*
         * Shift the graph's focus to the new model.
         */
        newModel.setDimensions(newModelDimensions);
        _ModelStack.push(newModel);
        _DimensionsStack.push(newModelDimensions);
        _ScopeStack.push(ScopeType.Model);

        if (_EventManager.isEventHearable()) {
            final ModelGraphEvent event = new ModelGraphEvent(this);
            _EventManager.fireEvent(ModelGraphEventListener.EventType.modelStarted, event);
        }
    }

    private Object convertRawIntegerValue(Integer rawValue) {

        final Dimensions dimensions = _DimensionsStack.peek();
        final URI schemaId = dimensions.getRequestedSchemaId();
        if (schemaId == null) {
            return rawValue;
        }

        final String fieldName = _FieldNameStack.peek();
        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Prototype prototype = schemaLoader.getPrototype(schemaId);
        final Prototype.Field field = prototype.getPrototypeField(fieldName);
        final Type fieldType = field.getProperty().getType();
        if (fieldType.equals(Integer.class)) {
            return rawValue;
        }

        final ScopeType scopeType = _ScopeStack.peek();

        switch (scopeType) {

        case Model:

            if (fieldType.equals(Long.class)) {
                return new Long(rawValue);
            }

            break;

        case List:

            if (field.isListType()) {
                final Type listElementType = field.getListElementType();
                if (listElementType.equals(Long.class)) {
                    return new Long(rawValue);
                }
            }
            else if (field.isMapType()) {
                // TODO: Handle map entry models with String transformed key or value types
            }

            break;
        }

        throw new ModelGraphException("Unable to transform schema (" + schemaId + ") field named \"" + fieldName
                + "\" raw String value \"" + rawValue + "\" to type " + fieldType + ".", this);
    }

    private Object convertRawListValue(List<Object> rawValue) {
        // TODO: Create a ObservableList or ObservableMap with the right generic types
        return rawValue;
    }

    private Object convertRawModelValue(Model model) {
        final Dimensions dimensions = _DimensionsStack.pop();
        final Model runtimeModel = model.getAlternate(dimensions);

        final URI schemaId = dimensions.getRequestedSchemaId();
        if (schemaId != null) {

            /*
             * The model has "alternated" from an undefined to a defined schema,
             * we need to iterate over the fields (stored in the undefined
             * shard) and set them through the model's new (schema-aware)
             * interface.
             */

            final Context context = getContext();
            final SchemaLoader schemaLoader = context.getSchemaLoader();
            final Prototype prototype = schemaLoader.getPrototype(schemaId);
            final ModelHeap heap = context.getModelHeap();
            final Map<String, Object> undefinedFields = heap.getFields(null, runtimeModel);
            for (final String fieldName : undefinedFields.keySet()) {
                final Prototype.Field protoField = prototype.getPrototypeField(fieldName);
                final URI declaringSchemaId = protoField.getDeclaringSchemaId();
                final Object fieldValue = undefinedFields.get(fieldName);
                runtimeModel.setFieldValue(declaringSchemaId, fieldName, fieldValue);
                heap.unsetField(null, runtimeModel, fieldName);
            }
        }

        return runtimeModel;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object convertRawStringValue(String rawValue) {

        final Dimensions dimensions = _DimensionsStack.peek();
        final URI schemaId = dimensions.getRequestedSchemaId();
        if (schemaId == null) {
            return rawValue;
        }

        final String fieldName = _FieldNameStack.peek();
        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Prototype prototype = schemaLoader.getPrototype(schemaId);
        final Prototype.Field field = prototype.getPrototypeField(fieldName);
        final Type fieldType = field.getProperty().getType();
        if (fieldType.equals(String.class)) {
            return rawValue;
        }

        final ScopeType scopeType = _ScopeStack.peek();

        switch (scopeType) {

        case Model:
            if (TypeUtils.isAssignable(fieldType, Enum.class)) {
                return Enum.valueOf((Class<Enum>) fieldType, rawValue);
            }
            else if (fieldType instanceof Class<?>) {
                final ToStringTransformer<?> toStringTransformer = context.getToStringTransformer((Class<?>) fieldType);
                if (toStringTransformer != null) {
                    return toStringTransformer.bToA(rawValue);
                }
            }

            break;

        case List:

            if (field.isListType()) {
                final Type listElementType = field.getListElementType();
                if (TypeUtils.isAssignable(listElementType, Enum.class)) {
                    return Enum.valueOf((Class<Enum>) fieldType, rawValue);
                }
                else if (listElementType instanceof Class<?>) {
                    final ToStringTransformer<?> toStringTransformer = context
                            .getToStringTransformer((Class<?>) listElementType);
                    if (toStringTransformer != null) {
                        return toStringTransformer.bToA(rawValue);
                    }
                }
            }
            else if (field.isMapType()) {
                // TODO: Handle map entry models with String transformed key or value types
            }

            break;
        }

        throw new ModelGraphException("Unable to transform schema (" + schemaId + ") field named \"" + fieldName
                + "\" raw String value \"" + rawValue + "\" to type " + fieldType + ".", this);
    }

    private enum ScopeType {
        Model,
        List;
    }

}
