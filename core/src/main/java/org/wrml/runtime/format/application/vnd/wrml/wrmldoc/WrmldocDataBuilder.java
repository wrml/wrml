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
import org.wrml.model.format.Format;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.rest.status.ApiNotFoundErrorReport;
import org.wrml.model.rest.status.DocumentNotFoundErrorReport;
import org.wrml.model.rest.status.ErrorReport;
import org.wrml.model.rest.status.ResourceNotFoundErrorReport;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.FormatLoader;
import org.wrml.runtime.format.Formatter;
import org.wrml.runtime.format.application.vnd.wrml.complete.api.CompleteApiBuilder;
import org.wrml.runtime.format.application.vnd.wrml.complete.schema.CompleteSchemaBuilder;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.schema.SchemaLoader;

import java.io.IOException;
import java.net.URI;
import java.util.SortedSet;

public class WrmldocDataBuilder {

    public static final String DEFAULT_DOCROOT = "/_wrml/wrmldoc/";

    private final CompleteApiBuilder _CompleteApiBuilder;
    private final JstreeDataBuilder _JstreeDataBuilder;
    private final String _Docroot;

    public WrmldocDataBuilder(final CompleteApiBuilder completeApiBuilder) {
        this(completeApiBuilder, null);
    }

    public WrmldocDataBuilder(final CompleteApiBuilder completeApiBuilder, final String docroot) {
        _CompleteApiBuilder = completeApiBuilder;

        if (docroot != null) {
            _Docroot = docroot;
        }
        else {
            _Docroot = DEFAULT_DOCROOT;
        }

        _JstreeDataBuilder = new JstreeDataBuilder(_Docroot);
    }

    public CompleteApiBuilder getCompleteApiBuilder() {
        return _CompleteApiBuilder;
    }

    public String getDocroot() {
        return _Docroot;
    }

    public ObjectNode buildWrmldocData(final ObjectMapper objectMapper, final Model model) throws IOException {

        final CompleteApiBuilder completeApiBuilder = getCompleteApiBuilder();
        final CompleteSchemaBuilder completeSchemaBuilder = completeApiBuilder.getCompleteSchemaBuilder();
        final Context context = model.getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final URI schemaUri = model.getSchemaUri();
        final String titleSlotName = completeSchemaBuilder.getTitleSlotName(schemaUri, schemaLoader);
        String documentTitle = "Untitled";
        String documentIcon = _Docroot + "img/model.png";

        if (StringUtils.isNotBlank(titleSlotName)) {
            documentTitle = String.valueOf(model.getSlotValue(titleSlotName));
        }

        final ObjectNode wrmlDataNode = objectMapper.createObjectNode();

        wrmlDataNode.put(PropertyName.schemaUri.name(), schemaUri.toString());
        wrmlDataNode.put(PropertyName.docroot.name(), getDocroot());
        wrmlDataNode.put(PropertyName.documentTitle.name(), documentTitle);

        ObjectNode modelNode = (ObjectNode) objectMapper.readTree(model.toString());
        boolean buildSchemaNode = false;
        ObjectNode schemaNode = null;
        ObjectNode apiNode = null;
        ObjectNode relationNode = null;

        if (model instanceof Schema) {
            final Schema schema = (Schema) model;
            schemaNode = completeSchemaBuilder.buildCompleteSchema(objectMapper, schema);
            documentIcon = _Docroot + "img/schema.png";
        }
        else if (model instanceof Api) {
            apiNode = _CompleteApiBuilder.buildCompleteApi(objectMapper, (Api) model);
            documentIcon = _Docroot + "img/api.png";
        }
        else if (model instanceof LinkRelation) {
            relationNode = completeApiBuilder.buildCompleteLinkRelation(objectMapper, (LinkRelation) model);
            documentIcon = _Docroot + "img/linkRelation.png";
        }
        else if (model instanceof ErrorReport) {

            if (model instanceof ApiNotFoundErrorReport) {
                documentIcon = _Docroot + "img/apiNotFound.png";
            }
            else if (model instanceof ResourceNotFoundErrorReport) {
                documentIcon = _Docroot + "img/resourceNotFound.png";
            }
            else if (model instanceof DocumentNotFoundErrorReport) {
                documentIcon = _Docroot + "img/documentNotFound.png";
            }
            else {
                buildSchemaNode = true;
            }
        }
        else {
            buildSchemaNode = true;
        }

        if (apiNode == null && model instanceof Document) {
            apiNode = _CompleteApiBuilder.buildEmbeddedApi(objectMapper, (Document) model);
        }

        if (buildSchemaNode) {
            final Keys schemaKeys = context.getApiLoader().buildDocumentKeys(schemaUri, schemaLoader.getSchemaSchemaUri());
            final Schema schema = context.getModel(schemaKeys, schemaLoader.getSchemaDimensions());
            schemaNode = completeSchemaBuilder.buildCompleteSchema(objectMapper, schema);
        }

        if (schemaNode == null) {
            schemaNode = objectMapper.createObjectNode();
        }
        if (apiNode == null) {
            apiNode = objectMapper.createObjectNode();
        }
        if (relationNode == null) {
            relationNode = objectMapper.createObjectNode();
        }

        wrmlDataNode.put(PropertyName.documentIcon.name(), documentIcon);
        wrmlDataNode.put(PropertyName.model.name(), modelNode);
        wrmlDataNode.put(PropertyName.schema.name(), schemaNode);
        wrmlDataNode.put(PropertyName.api.name(), apiNode);
        wrmlDataNode.put(PropertyName.relation.name(), relationNode);

        final ObjectNode jstreeDataNode = _JstreeDataBuilder.buildJstreeData(objectMapper, context);
        wrmlDataNode.put(PropertyName.jstree.name(), jstreeDataNode);

        final ObjectNode formatsDataNode = buildFormatsData(objectMapper, model);
        wrmlDataNode.put(PropertyName.formats.name(), formatsDataNode);

        return wrmlDataNode;
    }


    public ObjectNode buildFormatsData(final ObjectMapper objectMapper, final Model model) {

        final ObjectNode formatsNode = objectMapper.createObjectNode();

        final Context context = model.getContext();
        final FormatLoader formatLoader = context.getFormatLoader();
        final SortedSet<URI> formatUris = formatLoader.getLoadedFormatUris();
        final URI defaultFormatUri = formatLoader.getDefaultFormatUri();

        for (final URI formatUri : formatUris) {
            final Formatter formatter = formatLoader.getFormatter(formatUri);
            if (formatter.isApplicableTo(model.getSchemaUri())) {

                final Format format = formatLoader.loadFormat(formatUri);
                final String mediaTypeString = String.valueOf(format.getMediaType());
                final ObjectNode formatNode = objectMapper.createObjectNode();
                formatsNode.put(mediaTypeString, formatNode);

                formatNode.put("uri", String.valueOf(formatUri));
                formatNode.put("title", format.getTitle());
                formatNode.put("mediaType", mediaTypeString);
                formatNode.put("fileExtension", format.getFileExtension());
                formatNode.put("isDefault", formatUri.equals(defaultFormatUri));

            }
        }

        return formatsNode;
    }


    public enum PropertyName {
        docroot,
        documentTitle,
        documentIcon,
        schemaUri,
        model,
        schema,
        api,
        relation,
        jstree,
        formats;
    }
}



