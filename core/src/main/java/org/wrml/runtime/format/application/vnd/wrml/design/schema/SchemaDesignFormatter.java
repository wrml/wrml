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
import org.apache.commons.lang3.StringUtils;
import org.wrml.model.Model;
import org.wrml.model.Named;
import org.wrml.model.Titled;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.rest.Method;
import org.wrml.model.schema.Schema;
import org.wrml.model.schema.ValueType;
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
import org.wrml.util.UniqueName;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;

/**
 * Format for WRML schemas for use in design tools/apps.
 */
public class SchemaDesignFormatter extends AbstractFormatter {


    public SchemaDesignFormatter() {

    }


    @Override
    public boolean isApplicableTo(final URI schemaUri) {

        final SchemaLoader schemaLoader = getContext().getSchemaLoader();
        return (schemaLoader.getSchemaSchemaUri().equals(schemaUri));
    }

    @Override
    public <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys, final Dimensions rootModelDimensions) throws ModelReadingException {

        throw new UnsupportedOperationException("The \"readModel\" operation is not supported by the \"" + getFormatUri() + "\" format.");
    }

    @Override
    public void writeModel(final OutputStream out, final Model model, final ModelWriteOptions writeOptions) throws ModelWritingException {

        if (!(model instanceof Schema)) {
            throw new ModelWritingException("The \"" + getFormatUri() + "\" format cannot write the model.", null, this);
        }

        final Schema schema = (Schema) model;
        final ObjectNode rootNode;
        final ObjectWriter objectWriter;

        try {

            // TODO: Should this ObjectMapper be stored in a field?
            final ObjectMapper objectMapper = new ObjectMapper();
            rootNode = createSchemaDesignObjectNode(objectMapper, schema);
            objectWriter = objectMapper.writer(new DefaultPrettyPrinter());
            objectWriter.writeValue(out, rootNode);
        }
        catch (final Exception e) {
            throw new ModelWritingException(getClass().getSimpleName()
                    + " encounter an error while attempting to write a SchemaDesign.  Message: " + e.getMessage(), null, this);

        }

    }

    public static ObjectNode createSchemaDesignObjectNode(final ObjectMapper objectMapper, final Schema schema) {

        final Context context = schema.getContext();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final ObjectNode rootNode = objectMapper.createObjectNode();

        final URI schemaUri = schema.getUri();
        final Prototype prototype = schemaLoader.getPrototype(schemaUri);

        rootNode.put(PropertyName.uri.name(), syntaxLoader.formatSyntaxValue(schemaUri));
        rootNode.put(PropertyName.title.name(), schema.getTitle());
        rootNode.put(PropertyName.description.name(), schema.getDescription());
        rootNode.put(PropertyName.version.name(), schema.getVersion());

        final String titleSlotName = getTitleSlotName(schemaUri, schemaLoader);
        if (titleSlotName != null) {
            rootNode.put(PropertyName.titleSlotName.name(), titleSlotName);
        }


        final UniqueName uniqueName = schema.getUniqueName();
        final ObjectNode uniqueNameNode = objectMapper.createObjectNode();
        uniqueNameNode.put(PropertyName.fullName.name(), uniqueName.getFullName());
        uniqueNameNode.put(PropertyName.namespace.name(), uniqueName.getNamespace());
        uniqueNameNode.put(PropertyName.localName.name(), uniqueName.getLocalName());
        rootNode.put(PropertyName.uniqueName.name(), uniqueNameNode);

        final Set<URI> declaredBaseSchemaUris = prototype.getDeclaredBaseSchemaUris();
        if (declaredBaseSchemaUris != null && !declaredBaseSchemaUris.isEmpty()) {
            final Set<URI> addedBaseSchemaUris = new LinkedHashSet<>();
            final ArrayNode baseSchemasNode = objectMapper.createArrayNode();
            rootNode.put(PropertyName.baseSchemas.name(), baseSchemasNode);

            for (final URI baseSchemaUri : declaredBaseSchemaUris) {
                if (!addedBaseSchemaUris.contains(baseSchemaUri)) {
                    final ObjectNode baseSchemaNode = buildSchemaNode(objectMapper, baseSchemaUri, schemaLoader, addedBaseSchemaUris);
                    baseSchemasNode.add(baseSchemaNode);
                    addedBaseSchemaUris.add(baseSchemaUri);
                }
            }
        }

        final Set<String> keySlotNames = prototype.getDeclaredKeySlotNames();
        if (keySlotNames != null && !keySlotNames.isEmpty()) {
            final ArrayNode keyPropertyNamesNode = objectMapper.createArrayNode();

            for (final String keySlotName : keySlotNames) {
                keyPropertyNamesNode.add(keySlotName);
            }

            if (keyPropertyNamesNode.size() > 0) {
                rootNode.put(PropertyName.keyPropertyNames.name(), keyPropertyNamesNode);
            }
        }

        final Set<String> allKeySlotNames = prototype.getAllKeySlotNames();
        final ArrayNode allKeySlotNamesNode = objectMapper.createArrayNode();
        rootNode.put(PropertyName.allKeySlotNames.name(), allKeySlotNamesNode);

        final ObjectNode keySlotMap = objectMapper.createObjectNode();
        rootNode.put(PropertyName.keys.name(), keySlotMap);

        final String uriSlotName = PropertyName.uri.name();
        if (allKeySlotNames.contains(uriSlotName)) {
            allKeySlotNamesNode.add(uriSlotName);

            final ObjectNode slot = createSlot(objectMapper, prototype, uriSlotName);
            keySlotMap.put(uriSlotName, slot);
        }

        for (final String keySlotName : allKeySlotNames) {
            if (!Document.SLOT_NAME_URI.equals(keySlotName)) {
                allKeySlotNamesNode.add(keySlotName);

                final ObjectNode slot = createSlot(objectMapper, prototype, keySlotName);
                keySlotMap.put(keySlotName, slot);
            }
        }

        rootNode.put(PropertyName.keyCount.name(), keySlotMap.size());

        final SortedSet<String> allSlotNames = prototype.getAllSlotNames();

        if (allSlotNames != null && !allSlotNames.isEmpty()) {

            final ObjectNode slotMapNode = objectMapper.createObjectNode();
            rootNode.put(PropertyName.slots.name(), slotMapNode);

            final ArrayNode propertyNamesNode = objectMapper.createArrayNode();

            for (final String slotName : allSlotNames) {
                final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);
                if (protoSlot instanceof LinkProtoSlot) {
                    continue;
                }

                if (allKeySlotNames.contains(slotName)) {
                    // Skip key slots (handled separately)
                    continue;
                }

                if (protoSlot.getDeclaringSchemaUri().equals(schemaUri)) {
                    propertyNamesNode.add(slotName);
                }

                final ObjectNode slotNode = createSlot(objectMapper, prototype, slotName);

                if (slotNode != null) {
                    slotMapNode.put(slotName, slotNode);
                }

            }
            if (propertyNamesNode.size() > 0) {
                rootNode.put(PropertyName.propertyNames.name(), propertyNamesNode);
            }

            rootNode.put(PropertyName.slotCount.name(), slotMapNode.size());
        }


        final Set<String> comparablePropertyNames = prototype.getComparableSlotNames();
        if (comparablePropertyNames != null && !comparablePropertyNames.isEmpty()) {
            final ArrayNode comparablePropertyNamesNode = objectMapper.createArrayNode();

            for (final String comparablePropertyName : comparablePropertyNames) {
                comparablePropertyNamesNode.add(comparablePropertyName);
            }

            if (comparablePropertyNamesNode.size() > 0) {
                rootNode.put(PropertyName.comparablePropertyNames.name(), comparablePropertyNamesNode);
            }
        }

        final Collection<LinkProtoSlot> linkProtoSlots = prototype.getLinkProtoSlots().values();
        if (linkProtoSlots != null && !linkProtoSlots.isEmpty()) {
            final ArrayNode linkNamesNode = objectMapper.createArrayNode();
            final ObjectNode linksMapNode = objectMapper.createObjectNode();
            rootNode.put(PropertyName.links.name(), linksMapNode);

            for (final LinkProtoSlot linkProtoSlot : linkProtoSlots) {

                if (linkProtoSlot.getDeclaringSchemaUri().equals(schemaUri)) {
                    linkNamesNode.add(linkProtoSlot.getName());
                }

                final ObjectNode linkNode = objectMapper.createObjectNode();

                String linkTitle = linkProtoSlot.getTitle();
                if (linkTitle == null) {
                    linkTitle = linkProtoSlot.getName();
                }

                linkNode.put(PropertyName.name.name(), linkProtoSlot.getName());
                linkNode.put(PropertyName.title.name(), linkTitle);

                final Method method = linkProtoSlot.getMethod();
                final URI linkRelationUri = linkProtoSlot.getLinkRelationUri();
                final URI declaringSchemaUri = linkProtoSlot.getDeclaringSchemaUri();

                linkNode.put(PropertyName.rel.name(), syntaxLoader.formatSyntaxValue(linkRelationUri));

                final Keys linkRelationKeys = context.getApiLoader().buildDocumentKeys(linkRelationUri, schemaLoader.getLinkRelationSchemaUri());
                final LinkRelation linkRelation = context.getModel(linkRelationKeys, schemaLoader.getLinkRelationDimensions());

                linkNode.put(PropertyName.relationTitle.name(), linkRelation.getTitle());
                linkNode.put(PropertyName.description.name(), linkProtoSlot.getDescription());
                linkNode.put(PropertyName.method.name(), method.getProtocolGivenName());
                linkNode.put(PropertyName.declaringSchemaUri.name(), syntaxLoader.formatSyntaxValue(declaringSchemaUri));

                URI requestSchemaUri = linkProtoSlot.getRequestSchemaUri();
                if (schemaLoader.getDocumentSchemaUri().equals(requestSchemaUri)) {
                    if (SystemLinkRelation.self.getUri().equals(linkRelationUri) || SystemLinkRelation.save.getUri().equals(linkRelationUri)) {
                        requestSchemaUri = schemaUri;
                    }
                }

                if (requestSchemaUri == null && method == Method.Save) {
                    requestSchemaUri = schemaUri;
                }

                if (requestSchemaUri != null) {
                    linkNode.put(PropertyName.requestSchemaUri.name(), syntaxLoader.formatSyntaxValue(requestSchemaUri));

                    final Schema requestSchema = schemaLoader.load(requestSchemaUri);
                    if (requestSchema != null) {
                        linkNode.put(PropertyName.requestSchemaTitle.name(), requestSchema.getTitle());
                    }
                }

                URI responseSchemaUri = linkProtoSlot.getResponseSchemaUri();
                if (schemaLoader.getDocumentSchemaUri().equals(responseSchemaUri)) {
                    if (SystemLinkRelation.self.getUri().equals(linkRelationUri) || SystemLinkRelation.save.getUri().equals(linkRelationUri)) {
                        responseSchemaUri = schemaUri;
                    }
                }

                if (responseSchemaUri != null) {
                    linkNode.put(PropertyName.responseSchemaUri.name(), syntaxLoader.formatSyntaxValue(responseSchemaUri));

                    final Schema responseSchema = schemaLoader.load(responseSchemaUri);
                    if (responseSchema != null) {
                        linkNode.put(PropertyName.responseSchemaTitle.name(), responseSchema.getTitle());
                    }
                }

                linksMapNode.put(linkTitle, linkNode);

            }

            if (linkNamesNode.size() > 0) {
                rootNode.put(PropertyName.linkNames.name(), linkNamesNode);
            }

            rootNode.put(PropertyName.linkCount.name(), linksMapNode.size());

        }


        return rootNode;
    }


    public static String getTitleSlotName(final URI schemaUri, final SchemaLoader schemaLoader) {
        //
        // Attempt to (heuristically) determine the "title" slot for the model
        //
        final Prototype prototype = schemaLoader.getPrototype(schemaUri);
        if (prototype == null) {
            return null;
        }


        String titleSlotName = prototype.getTitleSlotName();
        if (StringUtils.isNotBlank(titleSlotName)) {
            return titleSlotName;
        }

        final Class<?> schemaInterface = prototype.getSchemaBean().getIntrospectedClass();
        if (Titled.class.isAssignableFrom(schemaInterface)) {
            return Titled.SLOT_NAME_TITLE;
        }
        else if (Named.class.isAssignableFrom(schemaInterface)) {
            return Named.SLOT_NAME_NAME;
        }
        else {

            final Set<String> allKeySlotNames = prototype.getAllKeySlotNames();
            for (final String keySlotName : allKeySlotNames) {
                if (keySlotName.equals(Document.SLOT_NAME_URI)) {
                    continue;
                }

                // TODO: Change this logic to account for composite keys
                titleSlotName = keySlotName;
                break;
            }

        }

        return titleSlotName;
    }


    private static ObjectNode createSlot(final ObjectMapper objectMapper, final Prototype prototype, final String slotName) {

        final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);
        final SchemaLoader schemaLoader = prototype.getSchemaLoader();
        final Context context = schemaLoader.getContext();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();

        final ValueType valueType = protoSlot.getValueType();
        if (valueType == ValueType.Link) {
            return null;
        }

        final Type heapValueType = protoSlot.getHeapValueType();

        final URI declaringSchemaUri = protoSlot.getDeclaringSchemaUri();

        final ObjectNode slotNode = objectMapper.createObjectNode();
        slotNode.put(PropertyName.name.name(), slotName);
        slotNode.put(PropertyName.title.name(), protoSlot.getTitle());
        slotNode.put(PropertyName.type.name(), valueType.name());
        slotNode.put(PropertyName.description.name(), protoSlot.getDescription());
        slotNode.put(PropertyName.declaringSchemaUri.name(), syntaxLoader.formatSyntaxValue(declaringSchemaUri));

        if (protoSlot instanceof PropertyProtoSlot) {
            final PropertyProtoSlot propertyProtoSlot = (PropertyProtoSlot) protoSlot;
            final Object defaultValue = propertyProtoSlot.getDefaultValue();
            if (defaultValue != null) {
                slotNode.put(PropertyName.defaultValue.name(), syntaxLoader.formatSyntaxValue(defaultValue));
            }
        }

        switch (valueType) {
            case Text: {

                final ObjectNode syntaxNode = buildSyntaxNode(objectMapper, heapValueType, syntaxLoader);
                slotNode.put(PropertyName.syntax.name(), syntaxNode);

                final PropertyProtoSlot textPropertyProtoSlot = (PropertyProtoSlot) protoSlot;
                final boolean isMultiline = textPropertyProtoSlot.isMultiline();
                if (isMultiline) {
                    slotNode.put(PropertyName.multiline.name(), isMultiline);
                }

                break;
            }
            case List: {
                final PropertyProtoSlot listPropertyProtoSlot = (PropertyProtoSlot) protoSlot;

                final ObjectNode elementNode = objectMapper.createObjectNode();
                slotNode.put(PropertyName.element.name(), elementNode);


                final Type elementType = listPropertyProtoSlot.getListElementType();
                final ValueType elementValueType = schemaLoader.getValueType(elementType);
                elementNode.put(PropertyName.type.name(), elementValueType.name());

                if (elementValueType == ValueType.Model) {
                    final URI elementSchemaUri = listPropertyProtoSlot.getListElementSchemaUri();
                    if (elementSchemaUri != null) {
                        final ObjectNode schemaNode = buildSchemaNode(objectMapper, elementSchemaUri, schemaLoader, null);
                        elementNode.put(PropertyName.schema.name(), schemaNode);
                    }
                }
                else if (elementValueType == ValueType.Text) {
                    final ObjectNode syntaxNode = buildSyntaxNode(objectMapper, elementType, syntaxLoader);
                    elementNode.put(PropertyName.syntax.name(), syntaxNode);

                }

                break;
            }
            case Model: {
                final ObjectNode schemaNode = buildSchemaNode(objectMapper, ((PropertyProtoSlot) protoSlot).getModelSchemaUri(), schemaLoader, null);
                slotNode.put(PropertyName.schema.name(), schemaNode);
                break;
            }
            case SingleSelect: {
                final ObjectNode choicesNode = buildChoicesNode(objectMapper, heapValueType, schemaLoader);
                slotNode.put(PropertyName.choices.name(), choicesNode);
                break;
            }
        }


        return slotNode;
    }


    public static ObjectNode buildSchemaNode(final ObjectMapper objectMapper, final URI schemaUri, final SchemaLoader schemaLoader, final Set<URI> addedBaseSchemaUris) {

        final Prototype prototype = schemaLoader.getPrototype(schemaUri);
        if (prototype == null) {
            return null;
        }

        final ObjectNode schemaNode = objectMapper.createObjectNode();
        schemaNode.put(PropertyName.localName.name(), prototype.getUniqueName().getLocalName());
        schemaNode.put(PropertyName.title.name(), prototype.getTitle());
        schemaNode.put(PropertyName.uri.name(), schemaUri.toString());
        schemaNode.put(PropertyName.version.name(), prototype.getVersion());

        String titleSlotName = prototype.getTitleSlotName();
        if (StringUtils.isNotBlank(titleSlotName)) {
            schemaNode.put(PropertyName.titleSlotName.name(), titleSlotName);
        }
        else {
            titleSlotName = getTitleSlotName(schemaUri, schemaLoader);
            if (StringUtils.isNotBlank(titleSlotName)) {
                schemaNode.put(PropertyName.titleSlotName.name(), titleSlotName);
            }
        }

        final Set<String> allSlotNames = prototype.getAllSlotNames();
        if (allSlotNames != null && !allSlotNames.isEmpty()) {
            final ArrayNode propertyNamesNode = objectMapper.createArrayNode();

            for (final String slotName : allSlotNames) {
                final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);
                if (protoSlot instanceof LinkProtoSlot) {
                    continue;
                }

                if (protoSlot.getDeclaringSchemaUri().equals(schemaUri)) {
                    propertyNamesNode.add(slotName);
                }
            }
            if (propertyNamesNode.size() > 0) {
                schemaNode.put(PropertyName.propertyNames.name(), propertyNamesNode);
            }
        }


        final Set<String> keySlotNames = prototype.getDeclaredKeySlotNames();
        if (keySlotNames != null && !keySlotNames.isEmpty()) {
            final ArrayNode keyPropertyNamesNode = objectMapper.createArrayNode();

            for (final String keySlotName : keySlotNames) {
                keyPropertyNamesNode.add(keySlotName);
            }

            if (keyPropertyNamesNode.size() > 0) {
                schemaNode.put(PropertyName.keyPropertyNames.name(), keyPropertyNamesNode);
            }
        }

        final Set<String> allKeySlotNames = prototype.getAllKeySlotNames();
        final ArrayNode allKeySlotNamesNode = objectMapper.createArrayNode();
        schemaNode.put(PropertyName.allKeySlotNames.name(), allKeySlotNamesNode);

        for (final String keySlotName : allKeySlotNames) {
            allKeySlotNamesNode.add(keySlotName);
        }


        final Set<String> comparablePropertyNames = prototype.getComparableSlotNames();
        if (comparablePropertyNames != null && !comparablePropertyNames.isEmpty()) {
            final ArrayNode comparablePropertyNamesNode = objectMapper.createArrayNode();

            for (final String comparablePropertyName : comparablePropertyNames) {
                comparablePropertyNamesNode.add(comparablePropertyName);
            }

            if (comparablePropertyNamesNode.size() > 0) {
                schemaNode.put(PropertyName.comparablePropertyNames.name(), comparablePropertyNamesNode);
            }
        }

        final Map<URI, LinkProtoSlot> linkProtoSlots = prototype.getLinkProtoSlots();
        if (linkProtoSlots != null && !linkProtoSlots.isEmpty()) {
            final ArrayNode linkNamesNode = objectMapper.createArrayNode();

            for (final LinkProtoSlot linkProtoSlot : linkProtoSlots.values()) {
                if (linkProtoSlot.getDeclaringSchemaUri().equals(schemaUri)) {
                    linkNamesNode.add(linkProtoSlot.getName());
                }
            }

            if (linkNamesNode.size() > 0) {
                schemaNode.put(PropertyName.linkNames.name(), linkNamesNode);
            }
        }

        final Set<URI> declaredBaseSchemaUris = prototype.getDeclaredBaseSchemaUris();
        if (declaredBaseSchemaUris != null && !declaredBaseSchemaUris.isEmpty() && addedBaseSchemaUris != null) {

            final ArrayNode baseSchemasNode = objectMapper.createArrayNode();
            for (final URI baseSchemaUri : declaredBaseSchemaUris) {
                if (!addedBaseSchemaUris.contains(baseSchemaUri)) {
                    final ObjectNode baseSchemaNode = buildSchemaNode(objectMapper, baseSchemaUri, schemaLoader, addedBaseSchemaUris);
                    baseSchemasNode.add(baseSchemaNode);
                    addedBaseSchemaUris.add(baseSchemaUri);
                }
            }

            if (baseSchemasNode.size() > 0) {
                schemaNode.put(PropertyName.baseSchemas.name(), baseSchemasNode);
            }
        }

        return schemaNode;
    }


    private static ObjectNode buildSyntaxNode(final ObjectMapper objectMapper, final Type heapValueType, final SyntaxLoader syntaxLoader) {

        if (!String.class.equals(heapValueType) && heapValueType instanceof Class<?>) {


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

    private static ObjectNode buildChoicesNode(final ObjectMapper objectMapper, final Type heapValueType, final SchemaLoader schemaLoader) {

        if (heapValueType == null || !(heapValueType instanceof Class<?>)) {
            return null;
        }

        final Class<?> choicesEnumClass = (Class<?>) heapValueType;

        if (!choicesEnumClass.isEnum()) {
            return null;
        }

        final URI choicesUri = schemaLoader.getTypeUri(choicesEnumClass);
        final String choicesName = choicesEnumClass.getSimpleName();
        final ObjectNode choicesNode = objectMapper.createObjectNode();

        choicesNode.put(PropertyName.title.name(), choicesName);
        choicesNode.put(PropertyName.uri.name(), choicesUri.toString());


        // TODO: Only embed the choices once per schema to lighten the download?
        final Object[] enumConstants = choicesEnumClass.getEnumConstants();
        if (enumConstants != null && enumConstants.length > 0) {
            final ArrayNode valuesNode = objectMapper.createArrayNode();

            choicesNode.put(PropertyName.values.name(), valuesNode);

            for (final Object enumConstant : enumConstants) {
                final String choice = String.valueOf(enumConstant);
                valuesNode.add(choice);
            }
        }


        return choicesNode;
    }


    public enum PropertyName {

        allKeySlotNames,
        baseSchemas,
        choices,
        comparablePropertyNames,
        declaringSchemaUri,
        defaultValue,
        description,
        element,
        fullName,
        keyCount,
        keyPropertyNames,
        keys,
        linkCount,
        linkNames,
        links,
        localName,
        method,
        multiline,
        name,
        namespace,
        propertyNames,
        rel,
        relationTitle,
        requestSchemaUri,
        requestSchemaTitle,
        responseSchemaUri,
        responseSchemaTitle,
        schema,
        slotCount,
        slots,
        syntax,
        title,
        titleSlotName,
        type,
        uniqueName,
        uri,
        values,
        version;

    }
}
