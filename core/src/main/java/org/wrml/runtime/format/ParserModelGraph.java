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
 * Copyright (C) 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
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
package org.wrml.runtime.format;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.wrml.model.Model;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.Embedded;

import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.DimensionsBuilder;
import org.wrml.runtime.Keys;
import org.wrml.runtime.schema.PropertyProtoSlot;
import org.wrml.runtime.schema.ProtoSlot;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.runtime.schema.ValueType;
import org.wrml.runtime.syntax.SyntaxHandler;
import org.wrml.runtime.syntax.SyntaxLoader;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract representation of a serialized graph of WRML models, which sort
 * of resembles a DOM.
 */
public final class ParserModelGraph extends ModelGraph
{

    private static final Logger LOG = LoggerFactory.getLogger(ParserModelGraph.class);

    /** The keys to apply to the graph's root model. */
    private final Keys _RootModelKeys;

    /** The dimensions to apply to the graph's root model. */
    private final DimensionsBuilder _RootModelDimensionsBuilder;

    /**
     * Follows the scope (nesting/indentation) of the graph's models (sets of
     * slots), and lists (of models or other "raw" types).
     */
    private final LinkedList<ScopeType> _ScopeStack;

    /** Follows the focus of the graph's models. */
    private final LinkedList<Model> _ModelStack;

    /** Follows the focus of the graph's (REST) Document models. */
    private final LinkedList<Model> _DocumentStack;

    /** Follows the bread-crumb-like scope of slots. */
    private final LinkedList<String> _SlotNameStack;

    /** Follows the nesting of lists. */
    private final LinkedList<List<Object>> _ListStack;

    /** Follows the model dimensions. */
    private final LinkedList<DimensionsBuilder> _DimensionsBuilderStack;

    private final Map<UUID, Model> _ShortcutModels;

    /**
     * Create a new model graph.
     * 
     * @param context
     *            The context to operate within.
     * 
     * @param rootModelDimensions
     *            The dimensions to use for the root model.
     */
    public ParserModelGraph(final Context context, final Keys rootModelKeys, final Dimensions rootModelDimensions)
    {
        super(context);

        if (rootModelDimensions == null)
        {
            throw new ModelGraphException("The root model dimensions cannot be null.", null, this);
        }

        _RootModelDimensionsBuilder = new DimensionsBuilder(rootModelDimensions);
        _RootModelKeys = rootModelKeys;

        // Create the graph's focus tracking stacks
        _ScopeStack = new LinkedList<ScopeType>();
        _ModelStack = new LinkedList<Model>();
        _DocumentStack = new LinkedList<Model>();
        _SlotNameStack = new LinkedList<String>();
        _ListStack = new LinkedList<List<Object>>();
        _DimensionsBuilderStack = new LinkedList<DimensionsBuilder>();
        _ShortcutModels = new HashMap<UUID, Model>();
    }

    public Object endList()
    {
        final List<Object> list = _ListStack.pop();
        _ScopeStack.pop();

        return endValue(list);
    }

    public Model endModel()
    {
        final Model model = _ModelStack.pop();
        _ScopeStack.pop();

        if (model instanceof Document)
        {
            _DocumentStack.pop();
        }

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
    public Object endValue(final Object rawValue)
    {

        /*
         * As a special case for in-line model typing, WRML allows the
         * "schemaUri" slot to be present "on the wire". This slot is
         * "dimensional" in nature, so we will store it in the dimensions
         * instead of the model itself.
         * 
         * Compare the slot's name and update the Dimensions accordingly.
         */
        if (!_SlotNameStack.isEmpty() && _SlotNameStack.peek().equals(Model.SLOT_NAME_SCHEMA_URI))
        {
            _DimensionsBuilderStack.peek().setSchemaUri(URI.create(String.valueOf(rawValue)));
            _SlotNameStack.pop();
            return null;
        }

        Object runtimeValue = null;
        boolean isModel = false;
        if (rawValue != null && rawValue instanceof Model)
        {
            isModel = true;

            /*
             * Convert "raw" (undefined) model values here to handle the root
             * model case.
             */
            runtimeValue = convertRawModelValue((Model) rawValue);
        }

        if (!_ScopeStack.isEmpty())
        {
            if (rawValue != null)
            {
                if (TypeUtils.isInstance(rawValue, String.class))
                {
                    runtimeValue = convertRawStringValue((String) rawValue);
                }
                else if (TypeUtils.isInstance(rawValue, Integer.class))
                {
                    runtimeValue = convertRawIntegerValue((Integer) rawValue);
                }
                else if (TypeUtils.isInstance(rawValue, List.class))
                {
                    runtimeValue = convertRawListValue((List<Object>) rawValue);
                }
                else if (!isModel)
                {
                    /*
                     * The other "raw" types are equivalent to their "runtime" type
                     * counterparts; no conversion necessary.
                     */
                    runtimeValue = rawValue;
                }
            }

            final ScopeType scopeType = _ScopeStack.peek();

            switch (scopeType)
            {
            case Model:
            {
                /*
                 * The graph is focused on the parent model; meaning that a slot
                 * should be set.
                 */
                final Model parentModel = _ModelStack.peek();
                final String slotName = _SlotNameStack.peek();

                if (!slotName.equals(Model.SLOT_NAME_HEAP_ID))
                {
                    parentModel.getSlotMap().put(slotName, runtimeValue);
                }
                else if (runtimeValue != null)
                {

                    // Remove the started model from the stack
                    _ModelStack.pop();
                    // Push this model onto the stack
                    final Model m = (Model) runtimeValue;
                    _ModelStack.push(m);
                }

                /*
                 * Now finished with this slot, shift focus.
                 */
                _SlotNameStack.pop();

                break;
            }
            case List:
            {

                /*
                 * The graph is focused on a list; meaning that an element
                 * should be added.
                 */

                final List<Object> list = _ListStack.peek();
                list.add(runtimeValue);

                break;
            }

            } // End of switch
        }

        return runtimeValue;
    }


    public Keys getRootModelKeys()
    {
        return _RootModelKeys;
    }

    /**
     * Create a new list and make it the focus of this graph. Note that lists
     * may not start graphs, so you must create a model and a slot before you
     * may create any graph lists.
     */
    public void startList()
    {
        if (_ScopeStack.isEmpty() || _ModelStack.isEmpty() || _SlotNameStack.isEmpty())
        {
            // TODO: Future, move hard-coded messages like this into a
            // "StringTable & MessageFormat" utility class

            final ModelGraphException e = new ModelGraphException(
                    "Graph lists may be created for a slot value of a model or as an element of another graph list; but the whole graph must be model-rooted.",
                    this);
            ParserModelGraph.LOG.error(e.getMessage(), e);
            throw e;

        }

        final List<Object> newList = new ArrayList<>();

        // Give the new list focus
        _ListStack.push(newList);
        _ScopeStack.push(ScopeType.List);

    }

    /**
     * Create a new model and make it the focus of this graph.
     */
    public void startModel()
    {
        final Context context = getContext();

        /*
         * Create a new model, with an "undefined" schema.
         */
        final Model newModel = context.getModelBuilder().newModel();
        DimensionsBuilder newModelDimensionsBuilder = null;

        if (_ScopeStack.isEmpty())
        {
            /*
             * This is the first model in the graph; the root model. The first
             * model's creation is how a graph begins.
             */

            /*
             * Use the specified root model's dimensions for this model.
             * 
             * Note that the root model's dimensions may have a requested schema
             * URI already set.
             * 
             * If however, during the course of creating this model's slots, the
             * graph discovers a "schemaUri" slot then we will trust the model to
             * tell us it's type.
             */

            newModelDimensionsBuilder = _RootModelDimensionsBuilder;
        }
        else
        {
            /*
             * Make an effort to find the best "default" schema id for the new
             * (child/nested) model by consulting the focused slot's prototype
             * (if available).
             * 
             * NOTE: If we have any scope at all, then the graph's rules state
             * that we must also have a parent model ("parent" in the model
             * graph hierarchy sense; not to be confused with schematic
             * inheritance and "base" schemas).
             */

            final String slotName = _SlotNameStack.peek();
            final DimensionsBuilder parentDimensionsBuilder = _DimensionsBuilderStack.peek();

            /*
             * Initialize the new dimensions from the (graph's) parent
             * dimensions
             */

            newModelDimensionsBuilder = new DimensionsBuilder(parentDimensionsBuilder.toDimensions())
                    .setSchemaUri(null);
            URI newModelSchemaUri = null;

            final URI parentSchemaUri = parentDimensionsBuilder.getSchemaUri();
            if (parentSchemaUri != null)
            {
                /*
                 * If the parent's schema URI is known, attempt to use it to
                 * determine the (default) schema URI of new model.
                 */

                final SchemaLoader schemaLoader = context.getSchemaLoader();
                final Prototype parentPrototype = schemaLoader.getPrototype(parentSchemaUri);
                if (parentPrototype.getLinkRelationUri(slotName) != null)
                {
                    newModelSchemaUri = schemaLoader.getTypeUri(ValueType.JAVA_TYPE_LINK);
                }
                else
                {
                    final ProtoSlot parentSlot = parentPrototype.getProtoSlot(slotName);

                    if (parentSlot != null)
                    {
                        /*
                         * The prototype slot associated with the parent model's
                         * schema may help us determine the default schematic
                         * type identity for the model we are about to create.
                         */

                        final ScopeType scopeType = _ScopeStack.peek();

                        switch (scopeType)
                        {
                        case Model:
                        {

                            if (parentSlot.getValueType() == ValueType.Model)
                            {
                                newModelSchemaUri = ((PropertyProtoSlot) parentSlot).getModelSchemaUri();
                            }

                            break;
                        }
                        case List:
                        {
                            if (parentSlot.getValueType() == ValueType.List)
                            {
                                final Type listElementType = ((PropertyProtoSlot) parentSlot).getListElementType();
                                if (ValueType.isModelType(listElementType))
                                {
                                    newModelSchemaUri = ((PropertyProtoSlot) parentSlot).getListElementSchemaUri();
                                }
                            }

                            break;
                        }

                        } // End of switch
                    }
                }
            }

            newModelDimensionsBuilder.setSchemaUri(newModelSchemaUri);
        }

        /*
         * Shift the graph's focus to the new model.
         */
        final Dimensions newModelDimensions = newModelDimensionsBuilder.toDimensions();

        // newModel.setDimensions(newModelDimensions);

        _ModelStack.push(newModel);
        _DimensionsBuilderStack.push(newModelDimensionsBuilder);
        _ScopeStack.push(ScopeType.Model);

        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final URI schemaUri = newModelDimensions.getSchemaUri();

        if (schemaUri != null)
        {
            Class<?> schemaInterface;
            try
            {
                schemaInterface = schemaLoader.getSchemaInterface(schemaUri);
            }
            catch (final ClassNotFoundException e)
            {
                ParserModelGraph.LOG.error(e.getMessage(), e);
                throw new ModelGraphException(e.getMessage(), e, this);
            }

            if (TypeUtils.isAssignable(schemaInterface, Document.class))
            {
                _DocumentStack.push(newModel);
            }
        }
    }

    /**
     * Create a new slot (must be called with model focus).
     * 
     * @param slotName
     *            the name of the slot to create storage for within the graph's
     *            focused model.
     */
    public void startSlot(final String slotName)
    {
        if (_ScopeStack.isEmpty() || _ModelStack.isEmpty() || (_ScopeStack.peek() != ScopeType.Model))
        {
            // TODO: Future, move hard-coded messages like this into a
            // "StringTable & MessageFormat" utility class

            final ModelGraphException e = new ModelGraphException("Slot must be created within a model's scope.", this);
            ParserModelGraph.LOG.error(e.getMessage(), e);
            throw e;
        }

        _SlotNameStack.push(slotName);

    }

    private Object convertRawIntegerValue(final Integer rawValue)
    {
        final DimensionsBuilder dimensionsBuilder = _DimensionsBuilderStack.peek();
        final URI modelSchemaUri = dimensionsBuilder.getSchemaUri();
        if (modelSchemaUri == null)
        {
            return rawValue;
        }

        final String slotName = _SlotNameStack.peek();
        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Prototype prototype = schemaLoader.getPrototype(modelSchemaUri);
        final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);
        final Type slotType = protoSlot.getHeapValueType();
        if (slotType.equals(Integer.class) || (slotType.equals(int.class) && rawValue != null))
        {
            return rawValue;
        }

        final ScopeType scopeType = _ScopeStack.peek();

        switch (scopeType)
        {
        case Model:
        {

            if (slotType.equals(Long.class) || (slotType.equals(long.class) && rawValue != null))
            {
                return new Long(rawValue);
            }
            if (slotType.equals(Double.class) || (slotType.equals(double.class) && rawValue != null))
            {
                return new Double(rawValue);
            }

            break;
        }
        case List:
        {

            if (protoSlot.getValueType() == ValueType.List)
            {
                final Type listElementType = ((PropertyProtoSlot) protoSlot).getListElementType();
                if (listElementType.equals(Long.class) || (listElementType.equals(long.class) && rawValue != null))
                {
                    return new Long(rawValue);
                }
                if (listElementType.equals(Double.class) || (listElementType.equals(double.class) && rawValue != null))
                {
                    return new Double(rawValue);
                }

            }

            break;
        }

        } // End of switch

        final ModelGraphException e = new ModelGraphException("Unable to transform schema (" + modelSchemaUri
                + ") slot named \"" + slotName + "\" raw String value \"" + rawValue + "\" to type " + slotType + ".",
                this);
        ParserModelGraph.LOG.error(e.getMessage(), e);
        throw e;

    }

    private Object convertRawListValue(final List<Object> rawValue)
    {
        return rawValue;
    }

    private Object convertRawModelValue(final Model model)
    {
        final DimensionsBuilder dimensionsBuilder = _DimensionsBuilderStack.pop();

        final URI schemaUri = dimensionsBuilder.getSchemaUri();
        if (schemaUri == null || model == null)
        {
            return model;
        }

        final Dimensions dimensions = dimensionsBuilder.toDimensions();
        final Model typedModel = model.newAlternate(dimensions);

        if (typedModel instanceof Embedded)
        {
            final Model documentModel = _DocumentStack.peek();
            if (documentModel == null)
            {

                final ModelGraphException e = new ModelGraphException("Model: " + typedModel
                        + " must be embedded within a Document.", this);
                ParserModelGraph.LOG.error(e.getMessage(), e);
                throw e;
            }

            // Embedded models need a pointer to their enclosing Document.

            // Set the Embedded's document uri slot
            final URI uri = (URI) documentModel.getSlotMap().get(Document.SLOT_NAME_URI);
            typedModel.setSlotValue(Embedded.SLOT_NAME_DOCUMENT_URI, uri);
        }

        if (_ModelStack.isEmpty())
        {
            // This is the root model, include all of its requested key values in the slot map.
            final Keys keys = getRootModelKeys();
            if (keys != null)
            {
                typedModel.initKeySlots(keys);
            }
        }

        return typedModel;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object convertRawStringValue(final String rawValue)
    {
        final DimensionsBuilder dimensionsBuilder = _DimensionsBuilderStack.peek();
        final URI schemaUri = dimensionsBuilder.getSchemaUri();
        if (schemaUri == null)
        {
            return rawValue;
        }

        final ScopeType scopeType = _ScopeStack.peek();
        final String slotName = _SlotNameStack.peek();

        // Check if the slotName is heapId
        if (slotName.equals(Model.SLOT_NAME_HEAP_ID) && scopeType.equals(ScopeType.Model))
        {
            LOG.debug("Current slot is a Heap Id {}", rawValue);
            final UUID heapId = UUID.fromString(rawValue);
            if (_ShortcutModels.containsKey(heapId))
            {
                // Need to stuff this into the current model location....

                return _ShortcutModels.get(heapId);
            }
            else
            {
                // Assumes scope type is Model

                LOG.debug("Creating new model with schema {} with heapId {}", new Object[] { schemaUri, heapId });

                // Pull the created reference from the model stack and put into the map
                final Model parentModel = _ModelStack.peek();
                _ShortcutModels.put(heapId, parentModel);

                // Return null if we're in the first occurrence
                return null;
            }
        }

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();

        final Prototype prototype = schemaLoader.getPrototype(schemaUri);

        // TODO fix this for HEAP ID's {
        final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);

        final Type slotType = protoSlot.getHeapValueType();
        if (slotType.equals(String.class))
        {
            return rawValue;
        }

        switch (scopeType)
        {
        case Model:
        {
            if (TypeUtils.isAssignable(slotType, Enum.class))
            {
                return Enum.valueOf((Class<Enum>) slotType, rawValue);
            }
            else if (slotType instanceof Class<?>)
            {

                final SyntaxHandler<?> syntaxHandler = syntaxLoader.getSyntaxHandler((Class<?>) slotType);
                if (syntaxHandler != null)
                {
                    return syntaxHandler.parseSyntacticText(rawValue);
                }
            }

            break;
        }
        case List:
        {

            if (protoSlot.getValueType() == ValueType.List)
            {
                final Type listElementType = ((PropertyProtoSlot) protoSlot).getListElementType();
                if (String.class.equals(listElementType))
                {
                    return rawValue;
                }
                else if (TypeUtils.isAssignable(listElementType, Enum.class))
                {
                    return Enum.valueOf((Class<Enum>) listElementType, rawValue);
                }
                else if (listElementType instanceof Class<?>)
                {
                    final SyntaxHandler<?> syntaxHandler = syntaxLoader.getSyntaxHandler((Class<?>) listElementType);
                    if (syntaxHandler != null)
                    {
                        return syntaxHandler.parseSyntacticText(rawValue);
                    }
                }
            }
            break;
        }

        } // End of switch

        final ModelGraphException e = new ModelGraphException("Unable to transform schema (" + schemaUri
                + ") slot named \"" + slotName + "\" raw String value \"" + rawValue + "\" to type " + slotType + ".",
                this);
        ParserModelGraph.LOG.error(e.getMessage(), e);
        throw e;
    }

    public enum ScopeType
    {
        Model,
        List;
    }

}
