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
package org.wrml.runtime.format.application.vnd.wrml.wrmldoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.wrml.model.Model;
import org.wrml.model.rest.*;
import org.wrml.model.rest.status.ApiNotFoundErrorReport;
import org.wrml.model.rest.status.DocumentNotFoundErrorReport;
import org.wrml.model.rest.status.ErrorReport;
import org.wrml.model.rest.status.ResourceNotFoundErrorReport;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.application.vnd.wrml.complete.api.CompleteApiBuilder;
import org.wrml.runtime.format.application.vnd.wrml.complete.schema.CompleteSchemaBuilder;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.rest.ApiNavigator;
import org.wrml.runtime.rest.Resource;
import org.wrml.runtime.schema.ProtoSlot;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.util.UniqueName;

import java.io.IOException;
import java.net.URI;
import java.util.*;

public class JstreeDataBuilder {

    public static final String JSTREE_WRML_THEME_PATH = "jstree/themes/wrml/";

    private final String _ThemeDocroot;

    public JstreeDataBuilder(final String docroot) {
        _ThemeDocroot = docroot + JSTREE_WRML_THEME_PATH;
    }

    public String getThemeDocroot() {
        return _ThemeDocroot;
    }

    public ObjectNode buildJstreeData(final ObjectMapper objectMapper, final Context context) throws IOException {
        final ObjectNode jstreeDataNode = objectMapper.createObjectNode();

        final ObjectNode openDocumentDialogNode = buildOpenDocumentDialogData(objectMapper, context);
        jstreeDataNode.put(PropertyName.openDocumentDialog.name(), openDocumentDialogNode);

        return jstreeDataNode;
    }

    private ObjectNode buildOpenDocumentDialogData(final ObjectMapper objectMapper, final Context context) {

        final ObjectNode openDocumentDialogNode = objectMapper.createObjectNode();

        final ObjectNode coreNode = objectMapper.createObjectNode();
        openDocumentDialogNode.put(PropertyName.core.name(), coreNode);

        final ArrayNode dataArrayNode = objectMapper.createArrayNode();
        coreNode.put(PropertyName.data.name(), dataArrayNode);

        final ApiLoader apiLoader = context.getApiLoader();
        final SortedSet<URI> loadedApiUris = apiLoader.getLoadedApiUris();
        for (URI apiUri : loadedApiUris) {

            final ApiNavigator apiNavigator = apiLoader.getLoadedApiNavigator(apiUri);
            final Map<UUID, Resource> allResources = apiNavigator.getAllResources();
            if (allResources.isEmpty()) {
                continue;
            }

            final Resource docrootResource = apiNavigator.getDocroot();
            final ObjectNode docrootResourceDataNode = buildResourceDataNode(objectMapper, context, docrootResource);

            if (docrootResourceDataNode.size() > 0) {

                final ObjectNode apiDataNode = objectMapper.createObjectNode();
                dataArrayNode.add(apiDataNode);

                final Api api = apiNavigator.getApi();
                String apiTitle = api.getTitle();

                if (apiTitle == null || apiTitle.isEmpty()) {
                    apiTitle = apiUri.toString();
                }

                apiDataNode.put(PropertyName.text.name(), apiTitle);
                apiDataNode.put(PropertyName.icon.name(), _ThemeDocroot + "api.png");

                final ObjectNode dataNode = objectMapper.createObjectNode();
                apiDataNode.put(PropertyName.data.name(), dataNode);

                dataNode.put(PropertyName.type.name(), "api");

                final ObjectNode apiNode = objectMapper.createObjectNode();
                dataNode.put(PropertyName.api.name(), apiNode);

                apiNode.put(PropertyName.title.name(), api.getTitle());
                apiNode.put(PropertyName.uri.name(), apiUri.toString());

                final ArrayNode resourceArrayNode = objectMapper.createArrayNode();
                resourceArrayNode.add(docrootResourceDataNode);
                apiDataNode.put(PropertyName.children.name(), resourceArrayNode);
            }

        }

        return openDocumentDialogNode;
    }

    private ObjectNode buildResourceDataNode(final ObjectMapper objectMapper, final Context context, final Resource resource) {

        final ObjectNode resourceDataNode = objectMapper.createObjectNode();
        boolean addDataProperties = false;

        final ArrayNode childrenArrayNode = objectMapper.createArrayNode();

        final Set<URI> responseSchemaUris = resource.getResponseSchemaUris(Method.Get);
        if (responseSchemaUris != null && !responseSchemaUris.isEmpty()) {

            addDataProperties = true;

            final SchemaLoader schemaLoader = context.getSchemaLoader();
            for (URI schemaUri : responseSchemaUris) {

                final Prototype prototype = schemaLoader.getPrototype(schemaUri);
                final UniqueName schemaName = prototype.getUniqueName();
                final ObjectNode childSchemaDataNode = objectMapper.createObjectNode();
                childSchemaDataNode.put(PropertyName.text.name(), schemaName.getLocalName());
                childSchemaDataNode.put(PropertyName.icon.name(), _ThemeDocroot + "schema.png");

                final ObjectNode dataNode = objectMapper.createObjectNode();
                childSchemaDataNode.put(PropertyName.data.name(), dataNode);

                dataNode.put(PropertyName.type.name(), "schema");

                final ObjectNode schemaNode = objectMapper.createObjectNode();
                dataNode.put(PropertyName.schema.name(), schemaNode);

                schemaNode.put(PropertyName.title.name(), schemaName.getLocalName());
                schemaNode.put(PropertyName.uri.name(), schemaUri.toString());

                final ArrayNode keysArrayNode = objectMapper.createArrayNode();
                schemaNode.put(PropertyName.keys.name(), keysArrayNode);

                final String[] uriParameterNames = resource.getUriTemplate().getParameterNames();
                final Set<String> allKeySlotNames = prototype.getAllKeySlotNames();

                for (String parameterName : uriParameterNames) {
                    final String keySlotName = parameterName;
                    if (allKeySlotNames.contains(keySlotName)) {
                        final ProtoSlot keyProtoSlot = prototype.getProtoSlot(keySlotName);
                        final ObjectNode keyNode = objectMapper.createObjectNode();
                        keysArrayNode.add(keyNode);

                        keyNode.put(PropertyName.title.name(), keyProtoSlot.getTitle());
                        keyNode.put(PropertyName.name.name(), keyProtoSlot.getName());
                        keyNode.put(PropertyName.type.name(), keyProtoSlot.getValueType().name());
                    }
                }

                childrenArrayNode.add(childSchemaDataNode);
            }

        }

        final ResourceTemplate resourceTemplate = resource.getResourceTemplate();
        final List<ResourceTemplate> childResourceTemplates = resourceTemplate.getChildren();
        for (ResourceTemplate childResourceTemplate : childResourceTemplates) {
            final UUID childResourceTemplateId = childResourceTemplate.getUniqueId();
            final Resource childResource = resource.getApiNavigator().getResource(childResourceTemplateId);
            final ObjectNode childResouceDataNode = buildResourceDataNode(objectMapper, context, childResource);

            if (childResouceDataNode.size() > 0) {
                childrenArrayNode.add(childResouceDataNode);
            }
        }

        if (childrenArrayNode.size() > 0) {
            addDataProperties = true;
            resourceDataNode.put(PropertyName.children.name(), childrenArrayNode);
        }

        if (addDataProperties) {
            resourceDataNode.put(PropertyName.text.name(), resource.getPathSegment());
            resourceDataNode.put(PropertyName.icon.name(), _ThemeDocroot + "resource.png");
            final ObjectNode stateNode = objectMapper.createObjectNode();
            resourceDataNode.put(PropertyName.state.name(), stateNode);
            stateNode.put(PropertyName.opened.name(), true);

            final ObjectNode dataNode = objectMapper.createObjectNode();
            resourceDataNode.put(PropertyName.data.name(), dataNode);

            dataNode.put(PropertyName.type.name(), "resource");

            final ObjectNode resourceNode = objectMapper.createObjectNode();
            dataNode.put(PropertyName.resource.name(), resourceNode);

            final String pathText = resource.getPathText();
            resourceNode.put(PropertyName.title.name(), pathText);
            resourceNode.put(PropertyName.path.name(), pathText);

        }

        return resourceDataNode;
    }


    public enum PropertyName {
        openDocumentDialog,
        core,
        data,
        id,
        text,
        icon,
        state,
        opened,
        disabled,
        selected,
        children,
        li_attr,
        a_attr,
        type,
        title,
        name,
        uri,
        path,
        keys,
        api,
        resource,
        schema;
    }

}



