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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;

import org.wrml.model.Model;
import org.wrml.model.rest.Embedded;
import org.wrml.model.rest.Link;

import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.schema.PropertyProtoSlot;
import org.wrml.runtime.schema.ProtoSlot;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.runtime.schema.ValueType;
import org.wrml.runtime.syntax.SyntaxHandler;
import org.wrml.runtime.syntax.SyntaxLoader;

public class PrinterModelGraph extends ModelGraph
{

    private final Model _RootModel;

    private final ModelNode _RootModelNode;

    private final Map<UUID, ModelNode> _ModelNodes;

    private final Set<URI> _ExcludedSchemaUris;

    public PrinterModelGraph(final Model rootModel)
    {
        this(rootModel, null);
    }

    public PrinterModelGraph(final Model rootModel, final Set<URI> excludedSchemaUris)
    {
        super(rootModel.getContext());

        _RootModel = rootModel;
        _ModelNodes = new WeakHashMap<UUID, ModelNode>();
        _ExcludedSchemaUris = new HashSet<URI>();

        if (excludedSchemaUris != null)
        {
            _ExcludedSchemaUris.addAll(excludedSchemaUris);
        }

        if (_ExcludedSchemaUris.contains(rootModel.getSchemaUri()))
        {
            throw new ModelGraphException("The root model's schema is excluded from the graph.", this);
        }

        _RootModelNode = new ModelNode(null, _RootModel);
    }

    public Model getRootModel()
    {
        return _RootModel;
    }

    public ModelNode getRootModelNode()
    {
        return _RootModelNode;
    }

    public class ListNode extends Node
    {

        private final List<Object> _PrintableElements;

        private boolean _ElementSchemaUriRequired;

        private URI _MonomorphicSchemaUri;

        ListNode(final Node parent, final List<?> list)
        {
            super(parent);

            _PrintableElements = new ArrayList<Object>(list.size());

            boolean isMonomorphic = true;
            for (final Object element : list)
            {
                final Object printableElement = makeValuePrintable(element);
                if (printableElement == null)
                {
                    continue;
                }

                _PrintableElements.add(printableElement);

                if (printableElement instanceof ModelNode)
                {
                    final URI schemaUri = ((ModelNode) printableElement).getModel().getSchemaUri();

                    if (isMonomorphic && _MonomorphicSchemaUri == null)
                    {
                        _MonomorphicSchemaUri = schemaUri;
                    }
                    else if (_MonomorphicSchemaUri != null && !_MonomorphicSchemaUri.equals(schemaUri))
                    {
                        isMonomorphic = false;
                        _MonomorphicSchemaUri = null;
                    }
                }
                else
                {
                    isMonomorphic = false;
                    _MonomorphicSchemaUri = null;
                }

            }

        }

        public URI getMonomorphicSchemaUri()
        {
            return _MonomorphicSchemaUri;
        }

        public List<Object> getPrintableElements()
        {
            return _PrintableElements;
        }

        public boolean isElementSchemaUriRequired()
        {

            return _ElementSchemaUriRequired;
        }

        public void setElementSchemaUriRequired(final boolean elementSchemaUriRequired)
        {
            _ElementSchemaUriRequired = elementSchemaUriRequired;

        }

    }

    public class ModelNode extends Node
    {

        private final Model _Model;

        private final Map<String, Object> _PrintableSlots;

        private boolean _HeapIdRequired;

        private boolean _SchemaUriRequired;

        ModelNode(final Node parent, final Model model)
        {
            super(parent);

            _Model = model;

            // Track this model node to avoid circular model reference
            // serialization
            _ModelNodes.put(_Model.getHeapId(), this);

            _PrintableSlots = makeSlotsPrintable(getRawSlots());
        }

        public Model getModel()
        {
            return _Model;
        }

        public Map<String, Object> getPrintableSlots()
        {
            return _PrintableSlots;
        }

        public boolean isHeapIdRequired()
        {
            return _HeapIdRequired;
        }

        public boolean isSchemaUriRequired()
        {
            return _SchemaUriRequired;
        }

        public void setHeapIdRequired(final boolean heapIdRequired)
        {
            _HeapIdRequired = heapIdRequired;
        }

        public void setSchemaUriRequired(final boolean schemaUriRequired)
        {
            _SchemaUriRequired = schemaUriRequired;
        }

        private Map<String, Object> getRawSlots()
        {

            final Model model = getModel();

            Map<String, Object> rawSlots = new HashMap<>(model.getSlotMap());

            if (model instanceof Embedded)
            {
                rawSlots.remove(Embedded.SLOT_NAME_DOCUMENT_URI);
            }

            final Dimensions dimensions = model.getDimensions();

            final List<String> excludedSlotNames = dimensions.getExcludedSlotNames();
            if (excludedSlotNames != null && !excludedSlotNames.isEmpty())
            {
                for (final String excludedSlotName : excludedSlotNames)
                {
                    rawSlots.remove(excludedSlotName);
                }
            }
            else
            {
                final List<String> includedSlotNames = dimensions.getIncludedSlotNames();
                if (includedSlotNames != null && !includedSlotNames.isEmpty())
                {

                    final Map<String, Object> includedSlots = new HashMap<String, Object>(includedSlotNames.size());
                    for (final String includedSlotName : includedSlotNames)
                    {
                        if (rawSlots.containsKey(includedSlotName))
                        {
                            final Object value = rawSlots.get(includedSlotName);
                            includedSlots.put(includedSlotName, value);
                        }
                    }

                    rawSlots = includedSlots;
                }
            }

            return rawSlots;
        }

        private Map<String, Object> makeSlotsPrintable(final Map<String, Object> rawSlots)
        {

            final Model model = getModel();
            final URI schemaUri = model.getSchemaUri();

            final Set<String> slotNameSet = rawSlots.keySet();
            if (slotNameSet.isEmpty())
            {
                return rawSlots;
            }

            final SchemaLoader schemaLoader = getContext().getSchemaLoader();
            final Prototype prototype = model.getPrototype();

            final SortedSet<String> slotNames = new TreeSet<String>(slotNameSet);

            if (prototype != null)
            {
                final Collection<String> aliases = prototype.getSlotAliases();
                for (final String alias : aliases)
                {
                    final String realSlotName = prototype.getRealSlotName(alias);
                    if (realSlotName != null && rawSlots.containsKey(realSlotName))
                    {
                        slotNames.add(alias);
                    }
                }
            }
            
            for (final String slotName : slotNames)
            {

                String realSlotName = slotName;

                ProtoSlot protoSlot = null;
                if (prototype != null)
                {
                    protoSlot = prototype.getProtoSlot(slotName);
                    realSlotName = protoSlot.getRealName();

                    final URI slotDeclaringSchemaUri = protoSlot.getDeclaringSchemaUri();

                    if (_ExcludedSchemaUris.contains(slotDeclaringSchemaUri))
                    {
                        rawSlots.remove(realSlotName);
                        continue;
                    }
                }

                final Object slotValue = rawSlots.get(realSlotName);

                final Object printableValue = makeValuePrintable(slotValue);

                if (printableValue == null && slotValue != null)
                {

                    // The printable value signals slot removal by turning the initially non-null slot value into a null
                    // printable value.

                    rawSlots.remove(realSlotName);
                }
                else
                {
                    rawSlots.put(slotName, printableValue);

                    if (schemaUri == null)
                    {
                        continue;
                    }

                    if (protoSlot != null && !(protoSlot instanceof PropertyProtoSlot))
                    {
                        continue;
                    }

                    final PropertyProtoSlot propertyProtoSlot = (PropertyProtoSlot) protoSlot;
                    if (printableValue instanceof ModelNode)
                    {

                        final URI declaredSchemaUri = propertyProtoSlot.getModelSchemaUri();
                        Class<?> declaredSchemaIterface;
                        try
                        {
                            declaredSchemaIterface = schemaLoader.getSchemaInterface(declaredSchemaUri);
                        }
                        catch (final ClassNotFoundException e)
                        {
                            throw new ModelGraphException(e.getMessage(), e, PrinterModelGraph.this);
                        }

                        final ModelNode modelNode = (ModelNode) printableValue;
                        final URI actualSchemaUri = modelNode.getModel().getSchemaUri();
                        Class<?> actualSchemaInterface;
                        try
                        {
                            actualSchemaInterface = schemaLoader.getSchemaInterface(actualSchemaUri);
                        }
                        catch (final ClassNotFoundException e)
                        {
                            throw new ModelGraphException(e.getMessage(), e, PrinterModelGraph.this);
                        }

                        if (schemaLoader.isSubschema(declaredSchemaIterface, actualSchemaInterface))
                        {
                            modelNode.setSchemaUriRequired(true);
                        }

                    }
                    else if (printableValue instanceof ListNode)
                    {

                        final ListNode listNode = (ListNode) printableValue;
                        final Type declaredElementType = propertyProtoSlot.getListElementType();
                        final URI monomorphicSchemaUri = listNode.getMonomorphicSchemaUri();

                        if (monomorphicSchemaUri == null)
                        {
                            continue;
                        }

                        final Type monomorphicElementType;
                        try
                        {
                            monomorphicElementType = schemaLoader.getSchemaInterface(monomorphicSchemaUri);
                        }
                        catch (final ClassNotFoundException e)
                        {
                            throw new ModelGraphException(e.getMessage(), e, PrinterModelGraph.this);
                        }

                        if (schemaLoader.isSubschema(declaredElementType, monomorphicElementType))
                        {
                            listNode.setElementSchemaUriRequired(true);
                        }

                    }

                }
            }

            // Now all are printable
            return rawSlots;
        }
    }

    public class ModelReferenceNode extends Node
    {

        private final UUID _TargetHeapId;

        ModelReferenceNode(final Node parent, final UUID targetHeapId)
        {
            super(parent);
            _TargetHeapId = targetHeapId;
        }

        public ModelNode getReferencedNode()
        {
            return _ModelNodes.get(getTargetHeapId());
        }

        public UUID getTargetHeapId()
        {
            return _TargetHeapId;
        }

    }

    public abstract class Node
    {

        private final Node _Parent;

        Node(final Node parent)
        {
            _Parent = parent;
        }

        public final Node getParent()
        {
            return _Parent;
        }

        public Object makeValuePrintable(final Object value)
        {

            if (value == null)
            {
                return null;
            }

            final Context context = getContext();
            final SchemaLoader schemaLoader = context.getSchemaLoader();
            final SyntaxLoader syntaxLoader = context.getSyntaxLoader();

            final Class<?> heapValueType = value.getClass();
            final ValueType valueType = schemaLoader.getValueType(heapValueType);

            Object printableValue = value;

            switch (valueType)
            {

            case Date:
            {

                final SyntaxHandler<Date> dateSyntaxHandler = syntaxLoader.getSyntaxHandler(Date.class);
                final String dateString = dateSyntaxHandler.formatSyntaxValue((Date) value);
                printableValue = dateString;
                break;

            }
            case Link:
            {
                final Link link = (Link) value;
                printableValue = new ModelNode(this, link);

                break;
            }
            case List:
            {

                final List<?> list = (List<?>) value;
                final ListNode listNode = new ListNode(this, list);
                printableValue = listNode;
                break;
            }
            case Model:
            {
                final Model model = (Model) value;

                // Use a Map to track model heap ids and ensure that models
                // containing themselves are not infinitely printed.

                final UUID heapId = model.getHeapId();
                if (_ModelNodes.containsKey(heapId))
                {

                    final ModelNode targetModelNode = _ModelNodes.get(heapId);

                    // The target model will need to include its heap id so that
                    // it can be referred to.
                    targetModelNode.setHeapIdRequired(true);

                    printableValue = new ModelReferenceNode(this, heapId);
                }
                else
                {
                    printableValue = new ModelNode(this, model);
                }

                break;
            }
            case SingleSelect:
            {
                final String textValue = ((Enum<?>) value).name();
                printableValue = textValue;
                break;
            }
            case Text:
            {

                if (value instanceof String)
                {
                    printableValue = value;
                }
                else
                {
                    @SuppressWarnings("rawtypes")
                    final SyntaxHandler syntaxHandler = syntaxLoader.getSyntaxHandler(heapValueType);

                    if (syntaxHandler != null)
                    {
                        @SuppressWarnings("unchecked")
                        final String textValue = syntaxHandler.formatSyntaxValue(value);
                        printableValue = textValue;
                    }
                    else
                    {
                        printableValue = null;
                    }
                }

                break;
            }
            default:
            {
                break;
            }

            } // End of switch

            return printableValue;
        }

    }

}
