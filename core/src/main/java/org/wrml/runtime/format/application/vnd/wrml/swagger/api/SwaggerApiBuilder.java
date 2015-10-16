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
package org.wrml.runtime.format.application.vnd.wrml.swagger.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.wrml.model.rest.*;
import org.wrml.model.schema.Schema;
import org.wrml.model.schema.ValueType;
import org.wrml.runtime.Context;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.application.schema.json.JsonSchema;
import org.wrml.runtime.format.application.schema.json.JsonSchemaLoader;
import org.wrml.runtime.rest.*;
import org.wrml.runtime.schema.PropertyProtoSlot;
import org.wrml.runtime.schema.ProtoSlot;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.util.UniqueName;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SwaggerApiBuilder {

    public ObjectNode buildSwaggerApi(final ObjectMapper objectMapper, final Api api) {
        final Context context = api.getContext();
        final ApiLoader apiLoader = context.getApiLoader();
        final ObjectNode rootNode = objectMapper.createObjectNode();

        final URI apiUri = api.getUri();
        final ApiNavigator apiNavigator = apiLoader.getLoadedApiNavigator(apiUri);

        rootNode.put("swagger", "2.0");

        final ObjectNode infoNode = objectMapper.createObjectNode();
        rootNode.put("info", infoNode);

        infoNode.put("description", api.getDescription());
        infoNode.put("version", String.valueOf(api.getVersion()));
        infoNode.put("title", api.getTitle());

        // TODO Add this to WRML API?
        //infoNode.put("termsOfService", "");

        // TODO Add this to WRML API?
        //final ObjectNode contactNode = objectMapper.createObjectNode();
        //infoNode.put("contact", contactNode);
        //contactNode.put("email", "");

        // TODO Add this to WRML API?
        //final ObjectNode licenseNode = objectMapper.createObjectNode();
        //infoNode.put("license", licenseNode);
        //licenseNode.put("name", "");
        //licenseNode.put("url", "");

        rootNode.put("host", apiUri.getHost());

        // TODO Research/remember: Can WRML APIs have base paths?
        //rootNode.put("basePath", "");

        final ArrayNode tagDefinitionsNode = objectMapper.createArrayNode();

        final ArrayNode schemesNode = objectMapper.createArrayNode();
        rootNode.put("schemes", schemesNode);
        schemesNode.add("http");

        final Map<UUID, Resource> allResources = apiNavigator.getAllResources();
        final Map<URI, LinkRelation> allLinkRelations = new HashMap<>();

        if (allResources.size() > 0) {
            final SortedMap<String, Resource> displayOrderResources = new TreeMap<>();

            for (final Resource resource : allResources.values()) {
                displayOrderResources.put(resource.getPathText(), resource);
            }

            final ObjectNode pathsNode = objectMapper.createObjectNode();


            for (final Resource resource : displayOrderResources.values()) {
                if (resource.getReferenceMethods().size() > 0) {
                    addPathObjectNode(context, objectMapper, pathsNode, resource, allLinkRelations, tagDefinitionsNode);
                }
            }

            if (pathsNode.size() > 0) {
                rootNode.put("paths", pathsNode);
            }

        }

        final Set<Schema> allSchemas = apiNavigator.getApiSchemas();
        if (allSchemas.size() > 0) {
            final ObjectNode definitionsNode = objectMapper.createObjectNode();
            rootNode.put("definitions", definitionsNode);

            for (final Schema schema : allSchemas) {
                addSchemaDefinitionObjectNode(objectMapper, definitionsNode, schema);
                addSchemaTagDefinitionObjectNode(objectMapper, tagDefinitionsNode, schema);
            }

        }

        // TODO Add this to WRML API?
        //final ObjectNode externalDocsNode = objectMapper.createObjectNode();
        //rootNode.put("externalDocs", externalDocsNode);
        //externalDocsNode.put("description", "");
        //externalDocsNode.put("url", "");

        if (tagDefinitionsNode.size() > 0) {
            rootNode.put("tags", tagDefinitionsNode);
        }

        return rootNode;
    }


    private void addPathObjectNode(
            final Context context,
            final ObjectMapper objectMapper,
            final ObjectNode pathsNode,
            final Resource resource,
            final Map<URI, LinkRelation> allLinkRelations,
            final ArrayNode tagDefinitionsNode
    ) {

        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final URI defaultSchemaUri = resource.getDefaultSchemaUri();
        Prototype defaultPrototype = null;
        if (defaultSchemaUri != null) {
            defaultPrototype = schemaLoader.getPrototype(defaultSchemaUri);
        }

        final String defaultSchemaName = (defaultPrototype != null) ? defaultPrototype.getUniqueName().getLocalName() : null;

        final String path = resource.getPathText();
        final ObjectNode pathNode = objectMapper.createObjectNode();
        pathsNode.put(path, pathNode);

        final Set<Method> referenceMethods = resource.getReferenceMethods();

        final ConcurrentHashMap<URI, LinkTemplate> referenceTemplates = resource.getReferenceTemplates();

        final Set<URI> referenceRelationUris = referenceTemplates.keySet();
        String selfResponseSchemaName = null;

        final List<Method> displayOrderMethods = Arrays.asList(Method.Get, Method.Save, Method.Invoke, Method.Delete);
        for (Method method : displayOrderMethods) {
            if (referenceMethods.contains(method)) {

                for (final URI linkRelationUri : referenceRelationUris) {

                    final LinkTemplate referenceTemplate = referenceTemplates.get(linkRelationUri);

                    final LinkRelation linkRelation = getLinkRelation(context, allLinkRelations, linkRelationUri);

                    if (method != linkRelation.getMethod()) {
                        continue;
                    }

                    final ObjectNode methodNode = objectMapper.createObjectNode();
                    final String methodName = method.getProtocolGivenName().toLowerCase();
                    pathNode.put(methodName, methodNode);

                    final ResourceTemplate resourceTemplate = resource.getResourceTemplate();
                    final String description = resourceTemplate.getDescription();

                    final String relationTitle = linkRelation.getTitle();
                    String operationId = relationTitle;

                    final URI requestSchemaUri = referenceTemplate.getRequestSchemaUri();
                    Prototype requestPrototype = null;
                    if (requestSchemaUri != null) {
                        requestPrototype = schemaLoader.getPrototype(requestSchemaUri);
                    }

                    final URI responseSchemaUri = referenceTemplate.getResponseSchemaUri();
                    Prototype responsePrototype = null;
                    if (responseSchemaUri != null) {
                        responsePrototype = schemaLoader.getPrototype(responseSchemaUri);
                    }

                    final String responseSchemaName = (responsePrototype != null) ? responsePrototype.getUniqueName().getLocalName() : null;

                    if (SystemLinkRelation.self.getUri().equals(linkRelationUri)) {
                        selfResponseSchemaName = responseSchemaName;
                        operationId = "get";
                        if (responseSchemaName != null) {
                            operationId += responseSchemaName;
                        }
                        else if (defaultSchemaName != null) {
                            operationId += defaultSchemaName;
                        }

                    }
                    else if (SystemLinkRelation.save.getUri().equals(linkRelationUri)) {
                        operationId = "save";
                        if (responseSchemaName != null) {
                            operationId += responseSchemaName;
                        }
                        else if (defaultSchemaName != null) {
                            operationId += defaultSchemaName;
                        }

                    }
                    else if (SystemLinkRelation.delete.getUri().equals(linkRelationUri)) {
                        operationId = "delete";
                        if (defaultSchemaName != null) {
                            operationId += defaultSchemaName;
                        }
                        else if (selfResponseSchemaName != null) {
                            operationId += selfResponseSchemaName;
                        }
                    }

                    if (description != null) {
                        methodNode.put("summary", description);
                    }

                    //methodNode.put("description", description);
                    methodNode.put("operationId", operationId);

                    final ArrayNode consumesNode = objectMapper.createArrayNode();
                    methodNode.put("consumes", consumesNode);
                    consumesNode.add("application/json");

                    final ArrayNode producesNode = objectMapper.createArrayNode();
                    methodNode.put("produces", producesNode);
                    producesNode.add("application/json");

                    final ArrayNode parametersNode = objectMapper.createArrayNode();

                    final UriTemplate uriTemplate = resource.getUriTemplate();
                    final String[] parameterNames = uriTemplate.getParameterNames();
                    if (parameterNames != null && parameterNames.length > 0) {

                        for (int i = 0; i < parameterNames.length; i++) {
                            final String parameterName = parameterNames[i];

                            final ObjectNode parameterNode = objectMapper.createObjectNode();
                            parametersNode.add(parameterNode);

                            parameterNode.put("in", "path");
                            parameterNode.put("name", parameterName);
                            parameterNode.put("required", true);

                            URI keyedSchemaUri = null;

                            if (defaultPrototype != null) {
                                final Set<String> allKeySlotNames = defaultPrototype.getAllKeySlotNames();
                                if (allKeySlotNames != null && allKeySlotNames.contains(parameterName)) {
                                    keyedSchemaUri = defaultSchemaUri;
                                }
                            }

                            if (keyedSchemaUri == null && responsePrototype != null) {
                                final Set<String> allKeySlotNames = responsePrototype.getAllKeySlotNames();
                                if (allKeySlotNames != null && allKeySlotNames.contains(parameterName)) {
                                    keyedSchemaUri = responseSchemaUri;
                                }
                            }

                            if (keyedSchemaUri != null) {

                                final Prototype keyedPrototype = schemaLoader.getPrototype(keyedSchemaUri);
                                final ProtoSlot keyProtoSlot = keyedPrototype.getProtoSlot(parameterName);
                                if (keyProtoSlot instanceof PropertyProtoSlot) {
                                    final PropertyProtoSlot keyPropertyProtoSlot = (PropertyProtoSlot) keyProtoSlot;
                                    final ValueType parameterValueType = keyPropertyProtoSlot.getValueType();

                                    final String parameterTypeString;

                                    String parameterFormat = null;

                                    switch (parameterValueType) {
                                        case Integer:
                                            parameterTypeString = "integer";
                                            parameterFormat = "int32";
                                            break;
                                        case Boolean:
                                            parameterTypeString = "boolean";
                                            break;
                                        case Long:
                                            parameterTypeString = "integer";
                                            parameterFormat = "int64";
                                            break;
                                        case Double:
                                            parameterTypeString = "number";
                                            parameterFormat = "double";
                                            break;
                                        default: {
                                            parameterTypeString = "string";
                                            break;
                                        }
                                    }

                                    // TODO Add parameter description
                                    //parameterNode.put("description", );

                                    parameterNode.put("type", parameterTypeString);

                                    if (parameterFormat != null) {
                                        parameterNode.put("format", parameterFormat);
                                    }

                                }

                            }


                        }
                    }

                    if (requestPrototype != null) {

                        final ObjectNode parameterNode = objectMapper.createObjectNode();
                        parametersNode.add(parameterNode);

                        parameterNode.put("in", "body");
                        parameterNode.put("name", "body");
                        parameterNode.put("required", true);

                        final ObjectNode schemaNode = objectMapper.createObjectNode();
                        parameterNode.put("schema", schemaNode);
                        schemaNode.put("$ref", "#/definitions/" + requestPrototype.getUniqueName().getLocalName());
                    }


                    if (parametersNode.size() > 0) {
                        methodNode.put("parameters", parametersNode);
                    }

                    final ObjectNode responsesNode = objectMapper.createObjectNode();
                    methodNode.put("responses", responsesNode);

                    final ObjectNode successNode = objectMapper.createObjectNode();
                    responsesNode.put("200", successNode);
                    successNode.put("description", "OK");

                    if (responsePrototype != null) {
                        final ObjectNode schemaNode = objectMapper.createObjectNode();
                        successNode.put("schema", schemaNode);
                        schemaNode.put("$ref", "#/definitions/" + responsePrototype.getUniqueName().getLocalName());
                    }

                    final ArrayNode tagsNode = objectMapper.createArrayNode();

                    final Prototype tagPrototype;
                    if (defaultPrototype != null) {
                        tagPrototype = defaultPrototype;
                    }
                    else if (responsePrototype != null) {
                        tagPrototype = responsePrototype;
                    }
                    else if (requestPrototype != null) {
                        tagPrototype = requestPrototype;
                    }
                    else {
                        tagPrototype = null;
                    }

                    String tagSchemaName = null;
                    if (tagPrototype != null) {
                        tagSchemaName = tagPrototype.getUniqueName().getLocalName().toLowerCase();
                    }
                    else if (selfResponseSchemaName != null) {
                        tagSchemaName = selfResponseSchemaName.toLowerCase();
                    }

                    if (tagSchemaName != null) {
                        tagsNode.add(tagSchemaName);
                    }

                    if (tagsNode.size() > 0) {
                        methodNode.put("tags", tagsNode);
                    }

                }
            }
        }
    }

    private void addSchemaDefinitionObjectNode(final ObjectMapper objectMapper, final ObjectNode definitionsNode, final Schema schema) {
        final Context context = schema.getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final URI schemaUri = schema.getUri();
        final Prototype prototype = schemaLoader.getPrototype(schemaUri);
        final String schemaName = prototype.getUniqueName().getLocalName();

        if (definitionsNode.has(schemaName)) {
            return;
        }

        // TODO Modify JsonSchema code to allow for relative reference by Schema local name
        final JsonSchemaLoader jsonSchemaLoader = schemaLoader.getJsonSchemaLoader();
        final JsonSchema jsonSchema = jsonSchemaLoader.load(schema, true);
        final ObjectNode jsonSchemaRootNode = jsonSchema.getRootNode();

        definitionsNode.put(schemaName, jsonSchemaRootNode);

        SortedSet<String> allSlotNames = prototype.getAllSlotNames();
        for (String slotName : allSlotNames) {
            final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);
            if (!(protoSlot instanceof PropertyProtoSlot)) {
                continue;
            }
            final PropertyProtoSlot propertyProtoSlot = (PropertyProtoSlot) protoSlot;
            final ValueType propertyValueType = propertyProtoSlot.getValueType();
            URI relatedSchemaUri = null;
            if (propertyValueType == ValueType.Model) {
                final URI modelSchemaUri = propertyProtoSlot.getModelSchemaUri();
                relatedSchemaUri = modelSchemaUri;
            }
            else if (propertyValueType == ValueType.List) {
                final URI listElementSchemaUri = propertyProtoSlot.getListElementSchemaUri();
                relatedSchemaUri = listElementSchemaUri;
            }

            if (relatedSchemaUri != null) {
                final Schema relatedSchema = schemaLoader.load(relatedSchemaUri);
                addSchemaDefinitionObjectNode(objectMapper, definitionsNode, relatedSchema);
            }
        }

    }

    private void addSchemaTagDefinitionObjectNode(final ObjectMapper objectMapper, final ArrayNode tagDefinitionsNode, final Schema schema) {
        final Context context = schema.getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final URI schemaUri = schema.getUri();
        final Prototype prototype = schemaLoader.getPrototype(schemaUri);
        final UniqueName schemaUniqueName = prototype.getUniqueName();
        final String schemaTagName = schemaUniqueName.getLocalName().toLowerCase();
        final ObjectNode schemaTagNode = objectMapper.createObjectNode();

        schemaTagNode.put("name", schemaTagName);
        tagDefinitionsNode.add(schemaTagNode);
    }

    private LinkRelation getLinkRelation(final Context context, final Map<URI, LinkRelation> allLinkRelations, final URI linkRelationUri) {

        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final LinkRelation linkRelation;
        if (allLinkRelations.containsKey(linkRelationUri)) {
            linkRelation = allLinkRelations.get(linkRelationUri);
        }
        else {
            final Keys linkRelationKeys = context.getApiLoader().buildDocumentKeys(linkRelationUri, schemaLoader.getLinkRelationSchemaUri());
            linkRelation = context.getModel(linkRelationKeys, schemaLoader.getLinkRelationDimensions());
            allLinkRelations.put(linkRelationUri, linkRelation);
        }

        return linkRelation;
    }

/*

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

    */

}
