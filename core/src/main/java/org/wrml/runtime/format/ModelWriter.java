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
package org.wrml.runtime.format;

import org.wrml.model.Model;
import org.wrml.model.Named;
import org.wrml.runtime.Context;
import org.wrml.runtime.format.PrinterModelGraph.ListNode;
import org.wrml.runtime.format.PrinterModelGraph.ModelNode;
import org.wrml.runtime.format.PrinterModelGraph.ModelReferenceNode;
import org.wrml.runtime.format.PrinterModelGraph.Node;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.runtime.schema.ValueType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;

/**
 * Writes models using a configurable, format-specific {@link ModelPrinter} with
 * the specified {@link ModelWriteOptions}.
 *
 * @see PluggableFormatter
 */
class ModelWriter
{

    /**
     * The context to operate within.
     */
    private final Context _Context;

    private final URI _FormatUri;

    private final ModelPrinterFactory _ModelPrinterFactory;

    /**
     * The default options to use if none are specified.
     */
    private ModelWriteOptions _DefaultWriteOptions;

    /**
     * Creates a new model writer.
     *
     * @param context The context to operate within.
     */
    public ModelWriter(final Context context, final URI formatUri, final ModelPrinterFactory printerFactory)
    {

        _Context = context;

        _FormatUri = formatUri;
        _ModelPrinterFactory = printerFactory;

    }

    public Context getContext()
    {

        return _Context;
    }

    public ModelWriteOptions getDefaultWriteOptions()
    {

        if (_DefaultWriteOptions == null)
        {
            _DefaultWriteOptions = new ModelWriteOptions();

            // TODO: Change this default to false.
            _DefaultWriteOptions.setPrettyPrint(true);
        }

        return _DefaultWriteOptions;
    }

    public URI getFormatUri()
    {

        return _FormatUri;
    }

    public ModelPrinterFactory getModelPrinterFactory()
    {

        return _ModelPrinterFactory;
    }

    @Override
    public String toString()
    {

        return getClass().getSimpleName() + " [Format = " + _FormatUri + "]";
    }

    public void writeModel(final OutputStream out, final Model model) throws ModelWriterException
    {

        writeModel(out, model, getDefaultWriteOptions());
    }

    public void writeModel(final OutputStream out, final Model model, ModelWriteOptions writeOptions)
            throws ModelWriterException
    {

        if (out == null)
        {
            throw new ModelWriterException("The output stream cannot be null.", null, this);
        }

        if (model == null)
        {
            throw new ModelWriterException("The model cannot be null.", null, this);
        }

        if (writeOptions == null)
        {
            writeOptions = getDefaultWriteOptions();
        }

        final ModelPrinterFactory printerFactory = getModelPrinterFactory();
        final ModelPrinter printer;
        try
        {
            printer = printerFactory.createModelPrinter(out, writeOptions);
        }
        catch (final IOException e)
        {
            throw new ModelWriterException(e.getMessage(), e, this);
        }
        catch (final ModelPrinterException e)
        {
            throw new ModelWriterException(e.getMessage(), e, this);
        }

        final PrinterModelGraph printerModelGraph = new PrinterModelGraph(model, writeOptions.getExcludedSchemaUris());
        final ModelNode rootModelNode = printerModelGraph.getRootModelNode();

        try
        {
            printModel(printer, rootModelNode);
            printer.close();
        }
        catch (final IOException e)
        {
            throw new ModelWriterException("Encountered an I/O related problem while attempting to write a model.", e,
                    this);
        }
        catch (final Exception e)
        {
            throw new ModelWriterException("Encountered an issue while attempting to write a model.", e, this);
        }

    }

    private void printList(final ModelPrinter printer, final ListNode listNode) throws IOException,
            ModelPrinterException
    {

        final List<Object> printableElements = listNode.getPrintableElements();
        if (printableElements == null || printableElements.isEmpty())
        {
            return;
        }

        printer.printListStart(printableElements);

        final boolean isElementSchemaUriRequired = listNode.isElementSchemaUriRequired();
        for (final Object element : printableElements)
        {

            if (isElementSchemaUriRequired && element instanceof ModelNode)
            {
                ((ModelNode) element).setSchemaUriRequired(true);
            }

            printValue(printer, listNode, null, element);
        }

        printer.printListEnd(printableElements);
    }

    private void printModel(final ModelPrinter printer, final ModelNode modelNode) throws IOException,
            ModelPrinterException
    {

        final Model model = modelNode.getModel();

        printer.printModelStart(model);

        // Used to hold the names of the "preliminary" slots; special slots that are printed first for
        // clarity/readability.
        final Set<String> preliminarySlotNameSet = new LinkedHashSet<>();

        if (modelNode.isHeapIdRequired())
        {
            final Object printableHeapId = modelNode.makeValuePrintable(model.getHeapId());
            printSlot(printer, modelNode, Model.SLOT_NAME_HEAP_ID, printableHeapId);
            preliminarySlotNameSet.add(Model.SLOT_NAME_HEAP_ID);
        }

        if (modelNode.isSchemaUriRequired())
        {
            final Object printableSchemaUri = modelNode.makeValuePrintable(model.getSchemaUri());
            printSlot(printer, modelNode, Model.SLOT_NAME_SCHEMA_URI, printableSchemaUri);
            preliminarySlotNameSet.add(Model.SLOT_NAME_SCHEMA_URI);
        }

        final Map<String, Object> printableSlots = modelNode.getPrintableSlots();

        final Prototype prototype = model.getPrototype();
        final Set<String> allKeySlotNames;
        if (prototype != null)
        {
            allKeySlotNames = prototype.getAllKeySlotNames();
        }
        else
        {
            allKeySlotNames = Collections.emptySet();
        }

        for (final String keySlotName : allKeySlotNames)
        {
            if (!printableSlots.containsKey(keySlotName))
            {
                continue;
            }

            printSlot(printer, modelNode, keySlotName, printableSlots.get(keySlotName));
            preliminarySlotNameSet.add(keySlotName);
        }

        if (printableSlots.containsKey(Named.SLOT_NAME_NAME)
                && !preliminarySlotNameSet.contains(Named.SLOT_NAME_NAME))
        {
            printSlot(printer, modelNode, Named.SLOT_NAME_NAME, printableSlots.get(Named.SLOT_NAME_NAME));
            preliminarySlotNameSet.add(Named.SLOT_NAME_NAME);
        }

        for (final String slotName : printableSlots.keySet())
        {

            if (preliminarySlotNameSet.contains(slotName))
            {
                // Already printed this slot (see above)
                continue;
            }

            final Object slotValue = printableSlots.get(slotName);

            printSlot(printer, modelNode, slotName, slotValue);

        }

        printer.printModelEnd(model);

    }

    private void printSlot(final ModelPrinter printer, final ModelNode modelNode, final String slotName,
                           final Object slotValue) throws IOException, ModelPrinterException
    {

        if (slotValue instanceof ModelNode)
        {

            final ModelNode embeddedModelNode = (ModelNode) slotValue;

            final Map<String, Object> subslots = embeddedModelNode.getPrintableSlots();

            if (!embeddedModelNode.isHeapIdRequired() && !embeddedModelNode.isSchemaUriRequired()
                    && (subslots == null || subslots.isEmpty()))
            {
                return;
            }
        }
        else if (slotValue instanceof ListNode)
        {

            final ListNode listNode = (ListNode) slotValue;
            final List<Object> printableElements = listNode.getPrintableElements();
            if (printableElements == null || printableElements.isEmpty())
            {
                return;
            }
        }

        printer.printSlotName(slotName);
        printValue(printer, modelNode, slotName, slotValue);

    }

    private void printValue(final ModelPrinter printer, final Node node, final String slotName, final Object value)
            throws IOException, ModelPrinterException
    {

        if (value == null)
        {
            printer.printNullValue();
            return;
        }

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Class<?> heapValueType = value.getClass();
        final ValueType valueType = schemaLoader.getValueType(heapValueType);

        boolean printed = true;
        switch (valueType)
        {

            case Boolean:
            {
                if (value.equals(Boolean.TRUE))

                {
                    printer.printBooleanValue(true);
                }
                else
                {
                    printer.printBooleanValue(false);
                }
                break;

            }
            case Double:
            {
                printer.printDoubleValue(((Double) value).doubleValue());
                break;
            }
            case Integer:
            {
                printer.printIntegerValue(((Integer) value).intValue());
                break;
            }
            case Long:
            {
                printer.printLongValue(((Long) value).longValue());
                break;
            }
            case Text:
            {
                printer.printTextValue(String.valueOf(value));
                break;
            }
            default:
            {
                printed = false;
                break;
            }

        } // End of switch

        if (printed)
        {
            return;
        }

        if (value instanceof ModelNode)
        {
            printModel(printer, (ModelNode) value);
        }
        else if (value instanceof ModelReferenceNode)
        {
            final ModelReferenceNode modelReferenceNode = (ModelReferenceNode) value;
            final Model referencedModel = modelReferenceNode.getReferencedNode().getModel();
            printer.printModelStart(referencedModel);
            printer.printSlotName(Model.SLOT_NAME_HEAP_ID);
            printer.printTextValue(modelReferenceNode.getTargetHeapId().toString());
            printer.printModelEnd(referencedModel);
        }
        else if (value instanceof ListNode)
        {
            printList(printer, (ListNode) value);
        }
        else
        {
            printer.printNullValue();
        }

    }
}
