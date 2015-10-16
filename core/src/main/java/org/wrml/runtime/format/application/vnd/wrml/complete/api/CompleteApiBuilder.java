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
package org.wrml.runtime.format.application.vnd.wrml.complete.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.wrml.model.Model;
import org.wrml.model.rest.*;
import org.wrml.model.schema.ValueType;
import org.wrml.runtime.Context;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.application.vnd.wrml.complete.schema.CompleteSchemaBuilder;
import org.wrml.runtime.format.application.vnd.wrml.complete.schema.CompleteSchemaFormatter;
import org.wrml.runtime.rest.*;
import org.wrml.runtime.schema.*;
import org.wrml.runtime.syntax.SyntaxLoader;
import org.wrml.util.UniqueName;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CompleteApiBuilder {

    private final CompleteSchemaBuilder _CompleteSchemaBuilder;

    public CompleteApiBuilder(final CompleteSchemaBuilder completeSchemaBuilder) {
        _CompleteSchemaBuilder = completeSchemaBuilder;
    }

    public CompleteSchemaBuilder getCompleteSchemaBuilder() {
        return _CompleteSchemaBuilder;
    }

    public ObjectNode buildCompleteApi(final ObjectMapper objectMapper, final Api api) {
        final Context context = api.getContext();

        final URI apiUri = api.getUri();
        final ApiLoader apiLoader = context.getApiLoader();
        final ApiNavigator apiNavigator = apiLoader.getLoadedApiNavigator(apiUri);

        final ObjectNode apiNode = objectMapper.createObjectNode();
        apiNode.put(PropertyName.uri.name(), apiUri.toString());
        apiNode.put(PropertyName.title.name(), api.getTitle());
        apiNode.put(PropertyName.description.name(), api.getDescription());
        apiNode.put(PropertyName.version.name(), api.getVersion());

        final Map<URI, ObjectNode> schemaNodes = new HashMap<>();
        final Map<URI, LinkRelation> linkRelationCache = new HashMap<>();

        final Map<UUID, Resource> allResources = apiNavigator.getAllResources();
        final SortedMap<String, Resource> orderedResources = new TreeMap<>();

        for (final Resource resource : allResources.values()) {
            orderedResources.put(resource.getPathText(), resource);
        }

        final ArrayNode allResourcesNode = objectMapper.createArrayNode();
        for (final Resource resource : orderedResources.values()) {
            final ObjectNode resourceNode = buildResourceNode(objectMapper, schemaNodes, linkRelationCache, apiNavigator, resource);
            allResourcesNode.add(resourceNode);
        }

        apiNode.put(PropertyName.allResources.name(), allResourcesNode);
        return apiNode;
    }

    public ObjectNode buildEmbeddedApi(final ObjectMapper objectMapper, final Document document) {

        final ObjectNode apiNode = objectMapper.createObjectNode();

        final Context context = document.getContext();
        final URI uri = document.getUri();
        final ApiLoader apiLoader = context.getApiLoader();
        final ApiNavigator apiNavigator = apiLoader.getParentApiNavigator(uri);
        final Api api = apiNavigator.getApi();
        final Resource endpointResource = apiNavigator.getResource(uri);

        final URI apiUri = api.getUri();

        apiNode.put(PropertyName.uri.name(), apiUri.toString());
        apiNode.put(PropertyName.title.name(), api.getTitle());
        apiNode.put(PropertyName.description.name(), api.getDescription());
        apiNode.put(PropertyName.version.name(), api.getVersion());

        final Map<URI, ObjectNode> schemaNodes = new HashMap<>();
        final Map<URI, LinkRelation> linkRelationCache = new HashMap<>();

        final ObjectNode endpointResourceNode = buildResourceNode(objectMapper, schemaNodes, linkRelationCache, apiNavigator, endpointResource);
        apiNode.put(PropertyName.resource.name(), endpointResourceNode);

        return apiNode;
    }

    public ObjectNode buildCompleteLinkRelation(final ObjectMapper objectMapper, final LinkRelation linkRelation) {

        final Context context = linkRelation.getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();

        final ObjectNode linkRelationNode = objectMapper.createObjectNode();

        linkRelationNode.put(PropertyName.uri.name(), syntaxLoader.formatSyntaxValue(linkRelation.getUri()));

        final UniqueName linkRelationUniqueName = linkRelation.getUniqueName();
        linkRelationNode.put(PropertyName.uniqueName.name(), syntaxLoader.formatSyntaxValue(linkRelationUniqueName));
        linkRelationNode.put(PropertyName.title.name(), linkRelation.getTitle());

        final String description = linkRelation.getDescription();
        if (StringUtils.isNotBlank(description)) {
            linkRelationNode.put(PropertyName.description.name(), description);
        }

        linkRelationNode.put(PropertyName.version.name(), linkRelation.getVersion());
        linkRelationNode.put(PropertyName.method.name(), linkRelation.getMethod().getProtocolGivenName());

        final URI responseSchemaUri = linkRelation.getResponseSchemaUri();
        if (responseSchemaUri != null) {
            final ObjectNode responseSchemaNode = _CompleteSchemaBuilder.buildEmbeddedSchema(objectMapper, responseSchemaUri, schemaLoader, null);
            linkRelationNode.put(PropertyName.responseSchema.name(), responseSchemaNode);
        }

        final URI requestSchemaUri = linkRelation.getRequestSchemaUri();
        if (requestSchemaUri != null) {
            final ObjectNode requestSchemaNode = _CompleteSchemaBuilder.buildEmbeddedSchema(objectMapper, requestSchemaUri, schemaLoader, null);
            linkRelationNode.put(PropertyName.requestSchema.name(), requestSchemaNode);
        }

        final String linkRelationName = linkRelationUniqueName.getLocalName();
        final String signature = buildLinkSignature(context, linkRelationName, responseSchemaUri, requestSchemaUri, null);
        linkRelationNode.put(PropertyName.signature.name(), signature);

        return linkRelationNode;
    }


    private ObjectNode buildResourceNode(final ObjectMapper objectMapper, final Map<URI, ObjectNode> schemaNodes, final Map<URI, LinkRelation> linkRelationCache, final ApiNavigator apiNavigator, final Resource resource) {

        final Context context = apiNavigator.getApi().getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();


        final ObjectNode resourceNode = objectMapper.createObjectNode();

        resourceNode.put(PropertyName.id.name(), syntaxLoader.formatSyntaxValue(resource.getResourceTemplateId()));
        resourceNode.put(PropertyName.pathSegment.name(), resource.getPathSegment());
        resourceNode.put(PropertyName.fullPath.name(), resource.getPathText());

        final String parentPathText = resource.getParentPathText();
        if (parentPathText != null) {
            resourceNode.put(PropertyName.parentPath.name(), parentPathText);
        }

        resourceNode.put(PropertyName.uriTemplate.name(), resource.getUriTemplate().getUriTemplateString());

        final URI defaultSchemaUri = resource.getDefaultSchemaUri();
        Prototype defaultPrototype = null;
        if (defaultSchemaUri != null) {
            final ObjectNode defaultSchemaNode = getSchemaNode(objectMapper, schemaNodes, defaultSchemaUri, schemaLoader);
            resourceNode.put(PropertyName.defaultSchema.name(), defaultSchemaNode);
            defaultPrototype = schemaLoader.getPrototype(defaultSchemaUri);
        }

        final ArrayNode referencesNode = buildReferencesArrayNode(objectMapper, schemaNodes, linkRelationCache, resource, defaultPrototype);
        if (referencesNode.size() > 0) {
            resourceNode.put(PropertyName.references.name(), referencesNode);
        }

        final ObjectNode linksNode = buildLinksNode(objectMapper, schemaNodes, linkRelationCache, apiNavigator, resource, defaultPrototype);
        if (linksNode.size() > 0) {
            resourceNode.put(PropertyName.links.name(), linksNode);
        }


        return resourceNode;
    }

    private ArrayNode buildReferencesArrayNode(final ObjectMapper objectMapper, final Map<URI, ObjectNode> schemaNodes, final Map<URI, LinkRelation> linkRelationCache, final Resource resource, final Prototype defaultPrototype) {

        final Context context = resource.getApiNavigator().getApi().getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();

        final URI defaultSchemaUri = (defaultPrototype != null) ? defaultPrototype.getSchemaUri() : null;
        final String defaultSchemaName = (defaultPrototype != null) ? defaultPrototype.getUniqueName().getLocalName() : null;

        final ArrayNode referencesNode = objectMapper.createArrayNode();

        final ConcurrentHashMap<URI, LinkTemplate> referenceTemplates = resource.getReferenceTemplates();
        final Set<URI> referenceRelationUris = referenceTemplates.keySet();

        if (referenceTemplates != null && !referenceTemplates.isEmpty()) {

            String selfResponseSchemaName = null;

            List<String> resourceParameterList = null;
            final UriTemplate uriTemplate = resource.getUriTemplate();
            final String[] parameterNames = uriTemplate.getParameterNames();
            if (parameterNames != null && parameterNames.length > 0) {

                resourceParameterList = new ArrayList<>();

                for (int i = 0; i < parameterNames.length; i++) {
                    final String parameterName = parameterNames[i];

                    URI keyedSchemaUri = null;

                    if (defaultPrototype != null) {
                        final Set<String> allKeySlotNames = defaultPrototype.getAllKeySlotNames();
                        if (allKeySlotNames != null && allKeySlotNames.contains(parameterName)) {
                            keyedSchemaUri = defaultSchemaUri;
                        }
                    }

                    if (keyedSchemaUri == null) {

                        final Set<URI> referenceLinkRelationUris = resource.getReferenceLinkRelationUris(Method.Get);
                        if (referenceLinkRelationUris != null && !referenceLinkRelationUris.isEmpty()) {
                            for (URI linkRelationUri : referenceLinkRelationUris) {
                                final LinkTemplate referenceTemplate = referenceTemplates.get(linkRelationUri);
                                final URI responseSchemaUri = referenceTemplate.getResponseSchemaUri();
                                final Prototype responseSchemaPrototype = schemaLoader.getPrototype(responseSchemaUri);
                                if (responseSchemaPrototype != null) {
                                    final Set<String> allKeySlotNames = responseSchemaPrototype.getAllKeySlotNames();
                                    if (allKeySlotNames != null && allKeySlotNames.contains(parameterName)) {
                                        keyedSchemaUri = responseSchemaUri;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    String parameterTypeString = "?";

                    if (keyedSchemaUri != null) {

                        final Prototype keyedPrototype = schemaLoader.getPrototype(keyedSchemaUri);
                        final ProtoSlot keyProtoSlot = keyedPrototype.getProtoSlot(parameterName);
                        if (keyProtoSlot instanceof PropertyProtoSlot) {
                            final PropertyProtoSlot keyPropertyProtoSlot = (PropertyProtoSlot) keyProtoSlot;
                            final ValueType parameterValueType = keyPropertyProtoSlot.getValueType();
                            final Type parameterHeapType = keyPropertyProtoSlot.getHeapValueType();
                            switch (parameterValueType) {
                                case Text: {
                                    if (!String.class.equals(parameterHeapType)) {
                                        final Class<?> syntaxClass = (Class<?>) parameterHeapType;
                                        parameterTypeString = syntaxClass.getSimpleName();
                                    }
                                    else {
                                        parameterTypeString = parameterValueType.name();
                                    }

                                    break;
                                }
                                case SingleSelect: {
                                    final Class<?> choicesEnumClass = (Class<?>) parameterHeapType;

                                    if (choicesEnumClass.isEnum()) {
                                        parameterTypeString = choicesEnumClass.getSimpleName();
                                    }
                                    else {
                                        // ?
                                        parameterTypeString = parameterValueType.name();
                                    }

                                    break;
                                }
                                default: {
                                    parameterTypeString = parameterValueType.name();
                                    break;
                                }
                            }
                        }

                    }

                    resourceParameterList.add(parameterTypeString + " " + parameterName);
                }
            }


            for (final Method method : Method.values()) {
                for (final URI linkRelationUri : referenceRelationUris) {

                    final LinkTemplate referenceTemplate = referenceTemplates.get(linkRelationUri);
                    final LinkRelation linkRelation = getLinkRelation(context, linkRelationCache, linkRelationUri);

                    if (method != linkRelation.getMethod()) {
                        continue;
                    }


                    final ObjectNode referenceNode = objectMapper.createObjectNode();
                    referencesNode.add(referenceNode);

                    referenceNode.put(PropertyName.method.name(), method.getProtocolGivenName());
                    referenceNode.put(PropertyName.rel.name(), syntaxLoader.formatSyntaxValue(linkRelationUri));

                    final String relationTitle = linkRelation.getTitle();
                    referenceNode.put(PropertyName.relationTitle.name(), relationTitle);

                    final URI responseSchemaUri = referenceTemplate.getResponseSchemaUri();
                    String responseSchemaName = null;
                    if (responseSchemaUri != null) {
                        final ObjectNode responseSchemaNode = getSchemaNode(objectMapper, schemaNodes, responseSchemaUri, schemaLoader);
                        referenceNode.put(PropertyName.responseSchema.name(), responseSchemaNode);

                        responseSchemaName = responseSchemaNode.get(CompleteSchemaBuilder.PropertyName.localName.name()).asText();
                    }

                    final URI requestSchemaUri = referenceTemplate.getRequestSchemaUri();

                    String requestSchemaName = null;
                    if (requestSchemaUri != null) {
                        final ObjectNode requestSchemaNode = getSchemaNode(objectMapper, schemaNodes, requestSchemaUri, schemaLoader);
                        referenceNode.put(PropertyName.requestSchema.name(), requestSchemaNode);

                        requestSchemaName = requestSchemaNode.get(CompleteSchemaBuilder.PropertyName.localName.name()).asText();
                    }

                    final StringBuilder signatureBuilder = new StringBuilder();

                    if (responseSchemaName != null) {
                        signatureBuilder.append(responseSchemaName);
                    }
                    else {
                        signatureBuilder.append("void");
                    }

                    signatureBuilder.append(" ");

                    String functionName = relationTitle;

                    if (SystemLinkRelation.self.getUri().equals(linkRelationUri)) {
                        functionName = "get" + responseSchemaName;
                        selfResponseSchemaName = responseSchemaName;
                    }
                    else if (SystemLinkRelation.save.getUri().equals(linkRelationUri)) {
                        functionName = "save" + responseSchemaName;
                    }
                    else if (SystemLinkRelation.delete.getUri().equals(linkRelationUri)) {
                        functionName = "delete";
                        if (defaultSchemaName != null) {
                            functionName += defaultSchemaName;
                        }
                        else if (selfResponseSchemaName != null) {
                            functionName += selfResponseSchemaName;
                        }
                    }

                    signatureBuilder.append(functionName).append(" ( ");

                    String parameterString = null;
                    if (resourceParameterList != null) {
                        final StringBuilder parameterStringBuilder = new StringBuilder();
                        final int parameterCount = resourceParameterList.size();
                        for (int i = 0; i < parameterCount; i++) {
                            final String parameter = resourceParameterList.get(i);
                            parameterStringBuilder.append(parameter);
                            if (i < parameterCount - 1) {
                                parameterStringBuilder.append(" , ");
                            }
                        }

                        parameterString = parameterStringBuilder.toString();
                        signatureBuilder.append(parameterString);
                    }

                    if (requestSchemaName != null) {
                        if (StringUtils.isNotBlank(parameterString)) {
                            signatureBuilder.append(" , ");
                        }

                        signatureBuilder.append(requestSchemaName);

                        signatureBuilder.append(" ");

                        final String parameterName = Character.toLowerCase(requestSchemaName.charAt(0)) + requestSchemaName.substring(1);
                        signatureBuilder.append(parameterName);
                    }

                    signatureBuilder.append(" ) ");

                    final String signature = signatureBuilder.toString();
                    referenceNode.put(PropertyName.signature.name(), signature);
                }

            }

        }

        return referencesNode;
    }


    private ObjectNode buildLinksNode(final ObjectMapper objectMapper, final Map<URI, ObjectNode> schemaNodes, final Map<URI, LinkRelation> linkRelationCache, final ApiNavigator apiNavigator, final Resource resource, final Prototype defaultPrototype) {

        final Context context = apiNavigator.getApi().getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();

        final URI defaultSchemaUri = (defaultPrototype != null) ? defaultPrototype.getSchemaUri() : null;

        final ObjectNode linksNode = objectMapper.createObjectNode();

        final Set<URI> responseSchemaUris = new HashSet<>();
        if (defaultSchemaUri != null) {
            responseSchemaUris.add(defaultSchemaUri);
        }

        for (final Method method : Method.values()) {
            final Set<URI> methodResponseSchemaUris = resource.getResponseSchemaUris(method);
            if (methodResponseSchemaUris != null && !methodResponseSchemaUris.isEmpty()) {
                responseSchemaUris.addAll(methodResponseSchemaUris);
            }
        }

        final ConcurrentHashMap<URI, LinkTemplate> linkTemplates = resource.getLinkTemplates();

        for (final URI schemaUri : responseSchemaUris) {
            final Prototype prototype = schemaLoader.getPrototype(schemaUri);
            final SortedMap<URI, LinkProtoSlot> linkProtoSlots = prototype.getLinkProtoSlots();
            if (linkProtoSlots == null || linkProtoSlots.isEmpty()) {
                continue;
            }

            final ObjectNode linkTemplatesNode = objectMapper.createObjectNode();

            final Set<URI> linkRelationUris = linkProtoSlots.keySet();
            for (final URI linkRelationUri : linkRelationUris) {
                final LinkProtoSlot linkProtoSlot = linkProtoSlots.get(linkRelationUri);

                if (schemaLoader.getDocumentSchemaUri().equals(linkProtoSlot.getDeclaringSchemaUri())) {
                    // Skip over the built-in system-level link relations (which are all self-referential).
                    continue;
                }

                final ObjectNode linkTemplateNode = objectMapper.createObjectNode();
                final String linkSlotName = linkProtoSlot.getName();
                linkTemplatesNode.put(linkSlotName, linkTemplateNode);

                final LinkRelation linkRelation = getLinkRelation(context, linkRelationCache, linkRelationUri);
                final Method method = linkRelation.getMethod();

                linkTemplateNode.put(PropertyName.method.name(), method.getProtocolGivenName());
                linkTemplateNode.put(PropertyName.rel.name(), syntaxLoader.formatSyntaxValue(linkRelationUri));
                linkTemplateNode.put(PropertyName.relationTitle.name(), linkRelation.getTitle());

                final URI responseSchemaUri;
                final URI requestSchemaUri;

                if (linkTemplates.containsKey(linkRelationUri)) {
                    final LinkTemplate linkTemplate = linkTemplates.get(linkRelationUri);
                    responseSchemaUri = linkTemplate.getResponseSchemaUri();
                    requestSchemaUri = linkTemplate.getRequestSchemaUri();

                    final UUID endPointId = linkTemplate.getEndPointId();
                    if (endPointId != null) {
                        final Resource endPointResource = apiNavigator.getResource(endPointId);

                        final ObjectNode endPointNode = objectMapper.createObjectNode();

                        endPointNode.put(PropertyName.id.name(), syntaxLoader.formatSyntaxValue(endPointId));
                        endPointNode.put(PropertyName.pathSegment.name(), endPointResource.getPathSegment());
                        endPointNode.put(PropertyName.fullPath.name(), endPointResource.getPathText());
                        endPointNode.put(PropertyName.uriTemplate.name(), endPointResource.getUriTemplate().getUriTemplateString());

                        linkTemplateNode.put(PropertyName.endpoint.name(), endPointNode);
                    }

                }
                else {
                    responseSchemaUri = linkProtoSlot.getResponseSchemaUri();
                    requestSchemaUri = linkProtoSlot.getRequestSchemaUri();
                }

                if (responseSchemaUri != null) {
                    final ObjectNode responseSchemaNode = getSchemaNode(objectMapper, schemaNodes, responseSchemaUri, schemaLoader);
                    linkTemplateNode.put(PropertyName.responseSchema.name(), responseSchemaNode);
                }

                if (requestSchemaUri != null) {
                    final ObjectNode requestSchemaNode = getSchemaNode(objectMapper, schemaNodes, requestSchemaUri, schemaLoader);
                    linkTemplateNode.put(PropertyName.requestSchema.name(), requestSchemaNode);
                }

                final String signature = buildLinkSignature(context, linkSlotName, responseSchemaUri, requestSchemaUri, schemaUri);
                linkTemplateNode.put(PropertyName.signature.name(), signature);
            }

            if (linkTemplatesNode.size() > 0) {
                final ObjectNode schemaNode = objectMapper.createObjectNode();
                final ObjectNode schemaDetailsNode = getSchemaNode(objectMapper, schemaNodes, schemaUri, schemaLoader);
                schemaNode.put(PropertyName.schema.name(), schemaDetailsNode);
                schemaNode.put(PropertyName.linkTemplates.name(), linkTemplatesNode);
                linksNode.put(prototype.getUniqueName().getLocalName(), schemaNode);
            }
        }


        return linksNode;
    }

    private String buildLinkSignature(final Context context, final String linkFunctionName, final URI responseSchemaUri, final URI requestSchemaUri, final URI thisSchemaUri) {

        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final StringBuilder signatureBuilder = new StringBuilder();

        if (responseSchemaUri != null) {
            final Prototype responsePrototype = schemaLoader.getPrototype(responseSchemaUri);
            final String responseSchemaName = responsePrototype.getUniqueName().getLocalName();
            signatureBuilder.append(responseSchemaName);
        }
        else {
            signatureBuilder.append("void");
        }

        signatureBuilder.append(" ");

        signatureBuilder.append(linkFunctionName).append(" ( ");

        if (requestSchemaUri != null && !requestSchemaUri.equals(thisSchemaUri)) {
            final Prototype requestPrototype = schemaLoader.getPrototype(requestSchemaUri);
            final String requestSchemaName = requestPrototype.getUniqueName().getLocalName();

            signatureBuilder.append(requestSchemaName);

            signatureBuilder.append(" ");

            final String parameterName = Character.toLowerCase(requestSchemaName.charAt(0)) + requestSchemaName.substring(1);
            signatureBuilder.append(parameterName);
        }

        signatureBuilder.append(" ) ");

        final String signature = signatureBuilder.toString();
        return signature;
    }

    private LinkRelation getLinkRelation(final Context context, final Map<URI, LinkRelation> linkRelationCache, final URI linkRelationUri) {

        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final LinkRelation linkRelation;
        if (linkRelationCache.containsKey(linkRelationUri)) {
            linkRelation = linkRelationCache.get(linkRelationUri);
        }
        else {
            final Keys linkRelationKeys = context.getApiLoader().buildDocumentKeys(linkRelationUri, schemaLoader.getLinkRelationSchemaUri());
            linkRelation = context.getModel(linkRelationKeys, schemaLoader.getLinkRelationDimensions());
            linkRelationCache.put(linkRelationUri, linkRelation);
        }

        return linkRelation;
    }


    private ObjectNode getSchemaNode(final ObjectMapper objectMapper, final Map<URI, ObjectNode> schemaNodes, final URI schemaUri, final SchemaLoader schemaLoader) {

        final ObjectNode schemaNode;
        if (schemaNodes.containsKey(schemaUri)) {
            schemaNode = schemaNodes.get(schemaUri);
        }
        else {
            schemaNode = _CompleteSchemaBuilder.buildEmbeddedSchema(objectMapper, schemaUri, schemaLoader, null);
            schemaNodes.put(schemaUri, schemaNode);
        }

        return schemaNode;
    }


    public enum PropertyName {
        allResources,
        defaultSchema,
        description,
        endpoint,
        fullPath,
        id,
        links,
        linkTemplates,
        method,
        parentPath,
        pathSegment,
        references,
        rel,
        relationTitle,
        requestSchema,
        resource,
        responseSchema,
        schema,
        signature,
        title,
        uniqueName,
        uri,
        uriTemplate,
        version;

    }

    public enum LinkTemplateType {
        Reference,
        Link;
    }


}
