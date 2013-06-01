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
package org.wrml.runtime.format.application.vnd.wrml.design.schema;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.wrml.model.Model;
import org.wrml.model.Named;
import org.wrml.model.Titled;
import org.wrml.model.rest.Document;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.AbstractFormatter;
import org.wrml.runtime.format.ModelReadingException;
import org.wrml.runtime.format.ModelWriteOptions;
import org.wrml.runtime.format.ModelWritingException;
import org.wrml.runtime.rest.SystemLinkRelation;
import org.wrml.runtime.schema.*;
import org.wrml.runtime.syntax.SyntaxLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

/**
 * Format for WRML schemas for use in design tools/apps.
 */
public class SchemaDesignFormatter extends AbstractFormatter
{


    public SchemaDesignFormatter()
    {

    }


    @Override
    public boolean isApplicableTo(final URI schemaUri)
    {

        final SchemaLoader schemaLoader = getContext().getSchemaLoader();
        return (schemaLoader.getSchemaSchemaUri().equals(schemaUri));
    }

    @Override
    public <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys, final Dimensions rootModelDimensions) throws ModelReadingException
    {

        throw new UnsupportedOperationException("The \"readModel\" operation is not supported by the \"" + getFormatUri() + "\" format.");
    }

    @Override
    public void writeModel(final OutputStream out, final Model model, final ModelWriteOptions writeOptions) throws ModelWritingException
    {

        if (!(model instanceof Schema))
        {
            throw new ModelWritingException("The \"" + getFormatUri() + "\" format cannot write the model.", null, this);
        }

        final Schema schema = (Schema) model;
        final ObjectNode rootNode;
        final ObjectWriter objectWriter;

        try
        {

            // TODO: Should this ObjectMapper be stored in a field?
            final ObjectMapper objectMapper = new ObjectMapper();
            rootNode = createSchemaDesignObjectNode(objectMapper, schema);
            objectWriter = objectMapper.writer(new DefaultPrettyPrinter());
            objectWriter.writeValue(out, rootNode);
        }
        catch (final Exception e)
        {
            throw new ModelWritingException(getClass().getSimpleName()
                    + " encounter an error while attempting to write a SchemaDesign.  Message: " + e.getMessage(), null, this);

        }

    }

    private ObjectNode createSchemaDesignObjectNode(final ObjectMapper objectMapper, final Schema schema)
    {

        final Context context = schema.getContext();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final ObjectNode rootNode = objectMapper.createObjectNode();

        final URI schemaUri = schema.getUri();

        rootNode.put(PropertyName.uri.name(), syntaxLoader.formatSyntaxValue(schemaUri));
        rootNode.put(PropertyName.title.name(), schema.getTitle());
        rootNode.put(PropertyName.description.name(), schema.getDescription());

        final String guessedTitleSlot = guessTitleSlot(schemaUri, schemaLoader);
        if (guessedTitleSlot != null)
        {
            rootNode.put(PropertyName.titleSlotName.name(), guessedTitleSlot);
        }

        final Prototype prototype = schemaLoader.getPrototype(schemaUri);
        final ArrayNode allKeySlotNamesNode = objectMapper.createArrayNode();
        rootNode.put(PropertyName.allKeySlotNames.name(), allKeySlotNamesNode);
        final Set<String> allKeySlotNames = prototype.getAllKeySlotNames();

        final ObjectNode keySlotMap = objectMapper.createObjectNode();
        rootNode.put(PropertyName.keys.name(), keySlotMap);

        final String uriSlotName = PropertyName.uri.name();
        if (allKeySlotNames.contains(uriSlotName))
        {
            allKeySlotNamesNode.add(uriSlotName);

            final ObjectNode slot = createSlot(objectMapper, prototype, uriSlotName);
            keySlotMap.put(uriSlotName, slot);
        }


        for (final String keySlotName : allKeySlotNames)
        {
            if (!Document.SLOT_NAME_URI.equals(keySlotName))
            {
                allKeySlotNamesNode.add(keySlotName);

                final ObjectNode slot = createSlot(objectMapper, prototype, keySlotName);
                keySlotMap.put(keySlotName, slot);
            }
        }


        final ObjectNode slotMapNode = objectMapper.createObjectNode();
        rootNode.put(PropertyName.slots.name(), slotMapNode);

        final SortedSet<String> allSlotNames = prototype.getAllSlotNames();
        for (final String slotName : allSlotNames)
        {
            final ObjectNode slotNode = createSlot(objectMapper, prototype, slotName);

            if (slotNode != null)
            {
                slotMapNode.put(slotName, slotNode);
            }
        }

        final ObjectNode linksMapNode = objectMapper.createObjectNode();
        rootNode.put(PropertyName.links.name(), linksMapNode);
        final Collection<LinkProtoSlot> linkProtoSlots = prototype.getLinkProtoSlots().values();
        for (final LinkProtoSlot linkProtoSlot : linkProtoSlots)
        {

            final ObjectNode linkNode = objectMapper.createObjectNode();

            String linkTitle = linkProtoSlot.getTitle();
            if (linkTitle == null)
            {
                linkTitle = linkProtoSlot.getName();
            }

            linkNode.put(PropertyName.title.name(), linkTitle);


            final URI linkRelationUri = linkProtoSlot.getLinkRelationUri();

            linkNode.put(PropertyName.rel.name(), syntaxLoader.formatSyntaxValue(linkRelationUri));
            linkNode.put(PropertyName.method.name(), linkProtoSlot.getMethod().getProtocolGivenName());

            URI responseSchemaUri = linkProtoSlot.getResponseSchemaUri();
            if (schemaLoader.getDocumentSchemaUri().equals(responseSchemaUri))
            {
                if (SystemLinkRelation.self.getUri().equals(linkRelationUri) || SystemLinkRelation.save.getUri().equals(linkRelationUri))
                {
                    responseSchemaUri = schemaUri;
                }
            }

            if (responseSchemaUri != null)
            {
                linkNode.put(PropertyName.responseSchemaUri.name(), syntaxLoader.formatSyntaxValue(responseSchemaUri));

                final Schema responseSchema = schemaLoader.load(responseSchemaUri);
                if (responseSchema != null)
                {
                    linkNode.put(PropertyName.responseSchemaTitle.name(), responseSchema.getTitle());
                }
            }

            linksMapNode.put(linkTitle, linkNode);
        }

        return rootNode;
    }


    public static String guessTitleSlot(final URI schemaUri, final SchemaLoader schemaLoader)
    {
        //
        // Attempt to (heuristically) determine the "title" slot for the model
        //
        final Prototype prototype = schemaLoader.getPrototype(schemaUri);
        if (prototype == null)
        {
            return null;
        }

        final Class<?> schemaInterface = prototype.getSchemaBean().getIntrospectedClass();
        if (Titled.class.isAssignableFrom(schemaInterface))
        {
            return Titled.SLOT_NAME_TITLE;
        }
        else if (Named.class.isAssignableFrom(schemaInterface))
        {
            return Named.SLOT_NAME_NAME;
        }
        else
        {

            String titleKeySlot = null;
            boolean hasUriSlot = false;
            final Set<String> allKeySlotNames = prototype.getAllKeySlotNames();
            for (final String keySlotName : allKeySlotNames)
            {
                if (keySlotName.equals(Document.SLOT_NAME_URI))
                {
                    hasUriSlot = true;
                    continue;
                }

                // TODO: Change this logic to account for composite keys
                titleKeySlot = keySlotName;
                break;
            }

            if (titleKeySlot != null)
            {
                return titleKeySlot;
            }

            if (hasUriSlot)
            {
                return Document.SLOT_NAME_URI;
            }

        }

        return null;
    }


    private ObjectNode createSlot(final ObjectMapper objectMapper, final Prototype prototype, final String slotName)
    {

        final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);
        final SchemaLoader schemaLoader = prototype.getSchemaLoader();
        final Context context = schemaLoader.getContext();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();

        final ValueType valueType = protoSlot.getValueType();
        if (valueType == ValueType.Link)
        {
            return null;
        }

        final Type heapValueType = protoSlot.getHeapValueType();

        final ObjectNode slotNode = objectMapper.createObjectNode();
        slotNode.put(PropertyName.name.name(), slotName);
        slotNode.put(PropertyName.title.name(), protoSlot.getTitle());
        slotNode.put(PropertyName.type.name(), valueType.name());
        slotNode.put(PropertyName.description.name(), protoSlot.getDescription());

        switch (valueType)
        {
            case Text:
            {
                final ObjectNode syntaxNode = buildSyntaxNode(objectMapper, heapValueType, syntaxLoader);
                slotNode.put(PropertyName.syntax.name(), syntaxNode);

                break;
            }
            case List:
            {
                final PropertyProtoSlot listPropertyProtoSlot = (PropertyProtoSlot) protoSlot;

                final ObjectNode elementNode = objectMapper.createObjectNode();
                slotNode.put(PropertyName.element.name(), elementNode);


                final Type elementType = listPropertyProtoSlot.getListElementType();
                final ValueType elementValueType = schemaLoader.getValueType(elementType);
                elementNode.put(PropertyName.type.name(), elementValueType.name());

                if (elementValueType == ValueType.Model)
                {
                    final URI elementSchemaUri = listPropertyProtoSlot.getListElementSchemaUri();
                    if (elementSchemaUri != null)
                    {
                        final ObjectNode schemaNode = buildSchemaNode(objectMapper, elementSchemaUri, schemaLoader);
                        elementNode.put(PropertyName.schema.name(), schemaNode);
                    }
                }
                else if (elementValueType == ValueType.Text)
                {
                    final ObjectNode syntaxNode = buildSyntaxNode(objectMapper, elementType, syntaxLoader);
                    elementNode.put(PropertyName.syntax.name(), syntaxNode);

                }

                break;
            }
            case Model:
            {
                final ObjectNode schemaNode = buildSchemaNode(objectMapper, ((PropertyProtoSlot) protoSlot).getModelSchemaUri(), schemaLoader);
                slotNode.put(PropertyName.schema.name(), schemaNode);
                break;
            }
            case SingleSelect:
            {

                break;
            }
        }


        return slotNode;
    }

    public static ObjectNode buildSchemaNode(final ObjectMapper objectMapper, final URI schemaUri, final SchemaLoader schemaLoader)
    {


        final Prototype prototype = schemaLoader.getPrototype(schemaUri);
        if (prototype != null)
        {

            final ObjectNode schemaNode = objectMapper.createObjectNode();
            schemaNode.put(PropertyName.title.name(), prototype.getTitle());
            schemaNode.put(PropertyName.uri.name(), schemaUri.toString());

            final String titleSlotName = guessTitleSlot(schemaUri, schemaLoader);
            if (titleSlotName != null)
            {
                schemaNode.put(PropertyName.titleSlotName.name(), titleSlotName);
            }


            return schemaNode;
        }

        return null;
    }


    private ObjectNode buildSyntaxNode(final ObjectMapper objectMapper, final Type heapValueType, final SyntaxLoader syntaxLoader)
    {

        if (!String.class.equals(heapValueType) && heapValueType instanceof Class<?>)
        {


            // TODO: Make it easy/possible to get the Syntax Document's title and uri

            final Class<?> heapValueClass = (Class<?>) heapValueType;
            final URI syntaxUri = syntaxLoader.getSyntaxUri(heapValueClass);
            final String syntaxName = heapValueClass.getSimpleName();
            final ObjectNode syntaxNode = objectMapper.createObjectNode();

            syntaxNode.put(PropertyName.title.name(), syntaxName);
            syntaxNode.put(PropertyName.uri.name(), syntaxUri.toString());
            return syntaxNode;
        }
        return null;
    }


    private enum PropertyName
    {

        allKeySlotNames,
        description,
        keys,
        links,
        element,
        method,
        name,
        rel,
        responseSchemaUri,
        responseSchemaTitle,
        schema,
        slots,
        syntax,
        title,
        titleSlotName,
        type,
        uri;

    }
}
