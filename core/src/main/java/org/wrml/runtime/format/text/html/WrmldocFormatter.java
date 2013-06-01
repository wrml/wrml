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
package org.wrml.runtime.format.text.html;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.wrml.model.Model;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.Method;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.*;
import org.wrml.runtime.format.application.vnd.wrml.design.schema.SchemaDesignFormatter;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.rest.ApiNavigator;
import org.wrml.runtime.rest.Resource;
import org.wrml.runtime.schema.SchemaLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The wrmldoc formatter is spiritually akin to the <code>javadoc</code> tool in that it generates HTML from models and metadata.
 *
 * @see SystemFormat#html
 * @see <a href="http://www.wrml.org/wrmldoc/archive/common">First Introduced</a>
 * @see <a href="http://www.wrml.org/java/api/wrml-core-1.0">Javadoc Doclet Prototype</a>
 */
public class WrmldocFormatter extends AbstractFormatter
{

    public static final String DOCROOT_SETTING_NAME = "docroot";

    public static final String DEFAULT_DOCROOT = "http://www.wrml.org/wrmldoc/";

    public static final String SHELL_PAGE_TEMPLATE_RESOURCE = "index.html";

    public static final String INDEX_HEAD_TEMPLATE_PATH = "js/templates/indexHead.ejs";

    public static final String INDEX_BODY_TEMPLATE_PATH = "js/templates/indexBody.ejs";

    private Map<String, MessageFormat> _Templates;

    private String _Docroot;

    public WrmldocFormatter()
    {

    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys, final Dimensions rootModelDimensions) throws ModelReadingException, UnsupportedOperationException
    {

        throw new UnsupportedOperationException("The \"readModel\" operation is not supported by the \"" + getFormatUri() + "\" format.");
    }

    @Override
    public void writeModel(final OutputStream out, final Model model, final ModelWriteOptions writeOptions) throws ModelWritingException, UnsupportedOperationException
    {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final URI schemaUri = model.getSchemaUri();

        try
        {

            final ObjectMapper objectMapper = new ObjectMapper();

            final ObjectNode indexHeadTemplateNode = objectMapper.createObjectNode();
            indexHeadTemplateNode.put("url", _Docroot + INDEX_HEAD_TEMPLATE_PATH);
            final String indexHeadTemplateValue = indexHeadTemplateNode.toString();

            final ObjectNode indexBodyTemplateNode = objectMapper.createObjectNode();
            indexBodyTemplateNode.put("url", _Docroot + INDEX_BODY_TEMPLATE_PATH);
            final String indexBodyTemplateValue = indexBodyTemplateNode.toString();

            final String modelValue = model.toString();

            final Schema schema = schemaLoader.load(schemaUri);
            final ByteArrayOutputStream schemaBytes = new ByteArrayOutputStream();
            context.writeModel(schemaBytes, schema, SystemFormat.vnd_wrml_design_schema.getFormatUri());
            final String schemaValue = schemaBytes.toString();


            final ObjectNode apiNode = buildApiNode(objectMapper, model);
            final String apiValue = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(apiNode);

            final MessageFormat pageTemplate = getTemplate(SHELL_PAGE_TEMPLATE_RESOURCE);
            final String renderedPage = renderPage(pageTemplate, _Docroot, indexHeadTemplateValue, indexBodyTemplateValue, modelValue, schemaValue, apiValue);

            IOUtils.write(renderedPage, out);

        }
        catch (IOException e)
        {
            throw new ModelWritingException(e.getMessage(), e, this);
        }

    }

    public MessageFormat getTemplate(String templateName) throws IOException
    {

        if (!_Templates.containsKey(templateName))
        {
            final InputStream templateStream = getClass().getResourceAsStream(templateName);
            final String templateSource = IOUtils.toString(templateStream);

            _Templates.put(templateName, new MessageFormat(templateSource));
        }

        return _Templates.get(templateName);

    }


    protected String renderPage(final MessageFormat template, Object... params) throws IOException
    {

        final String renderedPage = template.format(params);
        return renderedPage;
    }

    protected ObjectNode buildApiNode(final ObjectMapper objectMapper, final Model model)
    {

        final ObjectNode apiNode = objectMapper.createObjectNode();
        if (!(model instanceof Document))
        {
            return apiNode;
        }

        final Context context = getContext();

        final Document document = (Document) model;
        final URI uri = document.getUri();
        final ApiLoader apiLoader = context.getApiLoader();
        final ApiNavigator apiNavigator = apiLoader.getParentApiNavigator(uri);
        final Api api = apiNavigator.getApi();
        final Resource resource = apiNavigator.getResource(uri);

        final URI apiUri = api.getUri();

        apiNode.put(PropertyName.uri.name(), apiUri.toString());
        apiNode.put(PropertyName.title.name(), api.getTitle());
        apiNode.put(PropertyName.description.name(), api.getDescription());
        apiNode.put(PropertyName.version.name(), api.getVersion());

        final ObjectNode resourceNode = buildResourceNode(objectMapper, resource);
        apiNode.put(PropertyName.resource.name(), resourceNode);

        return apiNode;
    }

    protected ObjectNode buildResourceNode(final ObjectMapper objectMapper, final Resource resource)
    {

        final SchemaLoader schemaLoader = getContext().getSchemaLoader();
        final ObjectNode resourceNode = objectMapper.createObjectNode();
        resourceNode.put(PropertyName.pathSegment.name(), resource.getPathSegment());
        resourceNode.put(PropertyName.uriTemplate.name(), resource.getUriTemplate().getUriTemplateString());
        resourceNode.put(PropertyName.id.name(), resource.getResourceTemplateId().toString());

        final ObjectNode allowNode = objectMapper.createObjectNode();
        resourceNode.put(PropertyName.allow.name(), allowNode);

        for (final Method method : Method.values())
        {

            final Set<URI> referenceLinkRelationUris = resource.getReferenceLinkRelationUris(method);
            if (referenceLinkRelationUris == null || referenceLinkRelationUris.isEmpty())
            {
                continue;
            }

            final ObjectNode methodNode = objectMapper.createObjectNode();
            allowNode.put(method.getProtocolGivenName(), methodNode);
            methodNode.put(PropertyName.title.name(), method.getProtocolGivenName());


            final Set<URI> responseSchemaUris = resource.getResponseSchemaUris(method);
            if (responseSchemaUris != null && !responseSchemaUris.isEmpty())
            {
                final ArrayNode responseSchemaArrayNode = objectMapper.createArrayNode();
                methodNode.put(PropertyName.responseSchemas.name(), responseSchemaArrayNode);
                for (final URI responseSchemaUri : responseSchemaUris)
                {
                    final ObjectNode schemaNode = SchemaDesignFormatter.buildSchemaNode(objectMapper, responseSchemaUri, schemaLoader);
                    responseSchemaArrayNode.add(schemaNode);
                }
            }



            final Set<URI> requestSchemaUris = resource.getRequestSchemaUris(method);
            if (requestSchemaUris != null && !requestSchemaUris.isEmpty())
            {
                final ArrayNode requestSchemaArrayNode = objectMapper.createArrayNode();
                methodNode.put(PropertyName.requestSchemas.name(), requestSchemaArrayNode);
                for (final URI requestSchemaUri : requestSchemaUris)
                {
                    final ObjectNode schemaNode = SchemaDesignFormatter.buildSchemaNode(objectMapper, requestSchemaUri, schemaLoader);
                    requestSchemaArrayNode.add(schemaNode);
                }
            }

        }


        return resourceNode;
    }

    @Override
    protected void initFromConfiguration(final FormatterConfiguration config)
    {

        final Map<String, String> settings = config.getSettings();
        if (settings == null)
        {
            throw new NullPointerException("The settings cannot be null.");
        }

        _Templates = new HashMap<>();

        try
        {
            getTemplate(SHELL_PAGE_TEMPLATE_RESOURCE);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("The shell page template could not be read from: " + SHELL_PAGE_TEMPLATE_RESOURCE);
        }

        _Docroot = DEFAULT_DOCROOT;
        if (settings.containsKey(DOCROOT_SETTING_NAME))
        {
            _Docroot = settings.get(DOCROOT_SETTING_NAME);
        }
    }

    private enum PropertyName
    {
        allow,
        description,
        id,
        pathSegment,
        requestSchemas,
        resource,
        responseSchemas,
        title,
        uri,
        uriTemplate,
        version;

    }

}
