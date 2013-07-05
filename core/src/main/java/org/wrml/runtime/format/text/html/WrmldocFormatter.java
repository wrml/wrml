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
import org.apache.commons.lang3.StringUtils;
import org.wrml.model.Model;
import org.wrml.model.rest.*;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.*;
import org.wrml.runtime.format.application.vnd.wrml.design.schema.SchemaDesignFormatter;
import org.wrml.runtime.rest.*;
import org.wrml.runtime.schema.*;
import org.wrml.runtime.syntax.SyntaxLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    public static final String MINIFY_SETTING_NAME = "minify";

    //public static final String DEFAULT_DOCROOT = "http://www.wrml.org/wrmldoc/";

    public static final String DEFAULT_DOCROOT = "/_wrml/wrmldoc/";

    public static final String SHELL_PAGE_TEMPLATE_RESOURCE = "index.html";

    public static final String EMPTY_OBJECT = "{}";

    private Map<String, MessageFormat> _Templates;

    private String _Docroot;

    private boolean _IsSourceCodeMinified;

    public WrmldocFormatter()
    {
        // Toggle this to minify (or not) the JS and CSS used in the web app.
        _IsSourceCodeMinified = false;
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

        final String dotMin = (_IsSourceCodeMinified) ? ".min" : "";

        final ObjectMapper objectMapper = new ObjectMapper();
        try
        {

            final String modelValue;
            final String schemaValue;

            if (model instanceof Schema)
            {
                modelValue = EMPTY_OBJECT;

                final Schema schema = (Schema) model;
                final ObjectNode schemaNode = SchemaDesignFormatter.createSchemaDesignObjectNode(objectMapper, schema);
                schemaValue = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaNode);
            }
            else if (model instanceof Api)
            {
                modelValue = EMPTY_OBJECT;
                schemaValue = EMPTY_OBJECT;
            }
            else
            {
                modelValue = model.toString();

                final ObjectNode schemaNode = SchemaDesignFormatter.buildSchemaNode(objectMapper, schemaUri, schemaLoader, null);
                schemaValue = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaNode);
            }

            final ObjectNode apiNode = buildApiNode(objectMapper, model);
            final String apiValue = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(apiNode);

            final MessageFormat pageTemplate = getTemplate(SHELL_PAGE_TEMPLATE_RESOURCE);
            final String renderedPage = renderPage(pageTemplate, _Docroot, dotMin, schemaUri.toString(), modelValue, schemaValue, apiValue);

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
        final Resource endpointResource = apiNavigator.getResource(uri);

        final URI apiUri = api.getUri();

        apiNode.put(PropertyName.uri.name(), apiUri.toString());
        apiNode.put(PropertyName.title.name(), api.getTitle());
        apiNode.put(PropertyName.description.name(), api.getDescription());
        apiNode.put(PropertyName.version.name(), api.getVersion());

        final Map<URI, ObjectNode> schemaNodes = new HashMap<>();

        if (document instanceof Api)
        {

            final Map<UUID, Resource> allResources = apiNavigator.getAllResources();
            final SortedMap<String, Resource> orderedResources = new TreeMap<>();

            for (final Resource resource : allResources.values())
            {
                orderedResources.put(resource.getPathText(), resource);
            }

            final ArrayNode allResourcesNode = objectMapper.createArrayNode();
            for (final Resource resource : orderedResources.values())
            {
                final ObjectNode resourceNode = buildResourceNode(objectMapper, schemaNodes, resource);
                allResourcesNode.add(resourceNode);
            }

            apiNode.put(PropertyName.allResources.name(), allResourcesNode);
        }
        else
        {
            final ObjectNode endpointResourceNode = buildResourceNode(objectMapper, schemaNodes, endpointResource);
            apiNode.put(PropertyName.resource.name(), endpointResourceNode);
        }

        return apiNode;
    }

    protected ObjectNode buildResourceNode(final ObjectMapper objectMapper, final Map<URI, ObjectNode> schemaNodes, final Resource resource)
    {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();


        final ObjectNode resourceNode = objectMapper.createObjectNode();

        resourceNode.put(PropertyName.id.name(), syntaxLoader.formatSyntaxValue(resource.getResourceTemplateId()));
        resourceNode.put(PropertyName.pathSegment.name(), resource.getPathSegment());
        resourceNode.put(PropertyName.fullPath.name(), resource.getPathText());

        final String parentPathText = resource.getParentPathText();
        if (parentPathText != null)
        {
            resourceNode.put(PropertyName.parentPath.name(), parentPathText);
        }

        resourceNode.put(PropertyName.uriTemplate.name(), resource.getUriTemplate().getUriTemplateString());

        final URI defaultSchemaUri = resource.getDefaultSchemaUri();
        String defaultSchemaName = null;
        Prototype defaultPrototype = null;
        if (defaultSchemaUri != null)
        {

            final ObjectNode defaultSchemaNode = getSchemaNode(objectMapper, schemaNodes, defaultSchemaUri, schemaLoader);
            resourceNode.put(PropertyName.defaultSchema.name(), defaultSchemaNode);

            defaultPrototype = schemaLoader.getPrototype(defaultSchemaUri);
            defaultSchemaName = defaultSchemaNode.get(SchemaDesignFormatter.PropertyName.localName.name()).asText();
        }

        final ArrayNode referencesNode = objectMapper.createArrayNode();

        final ConcurrentHashMap<URI, LinkTemplate> referenceTemplates = resource.getReferenceTemplates();
        final Set<URI> referenceRelationUris = referenceTemplates.keySet();

        final Map<URI, LinkRelation> linkRelationCache = new HashMap<>();

        if (referenceTemplates != null && !referenceTemplates.isEmpty())
        {

            String selfResponseSchemaName = null;

            List<String> resourceParameterList = null;
            final UriTemplate uriTemplate = resource.getUriTemplate();
            final String[] parameterNames = uriTemplate.getParameterNames();
            if (parameterNames != null && parameterNames.length > 0)
            {

                resourceParameterList = new ArrayList<>();

                for (int i = 0; i < parameterNames.length; i++)
                {
                    final String parameterName = parameterNames[i];

                    URI keyedSchemaUri = null;

                    if (defaultPrototype != null)
                    {
                        final Set<String> allKeySlotNames = defaultPrototype.getAllKeySlotNames();
                        if (allKeySlotNames != null && allKeySlotNames.contains(parameterName))
                        {
                            keyedSchemaUri = defaultSchemaUri;
                        }
                    }

                    if (keyedSchemaUri == null)
                    {

                        final Set<URI> referenceLinkRelationUris = resource.getReferenceLinkRelationUris(Method.Get);
                        if (referenceLinkRelationUris != null && !referenceLinkRelationUris.isEmpty())
                        {
                            for (URI linkRelationUri : referenceLinkRelationUris)
                            {
                                final LinkTemplate referenceTemplate = referenceTemplates.get(linkRelationUri);
                                final URI responseSchemaUri = referenceTemplate.getResponseSchemaUri();
                                final Prototype responseSchemaPrototype = schemaLoader.getPrototype(responseSchemaUri);
                                if (responseSchemaPrototype != null)
                                {
                                    final Set<String> allKeySlotNames = responseSchemaPrototype.getAllKeySlotNames();
                                    if (allKeySlotNames != null && allKeySlotNames.contains(parameterName))
                                    {
                                        keyedSchemaUri = responseSchemaUri;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    String parameterTypeString = "?";

                    if (keyedSchemaUri != null)
                    {

                        final Prototype keyedPrototype = schemaLoader.getPrototype(keyedSchemaUri);
                        final ProtoSlot keyProtoSlot = keyedPrototype.getProtoSlot(parameterName);
                        if (keyProtoSlot instanceof PropertyProtoSlot)
                        {
                            final PropertyProtoSlot keyPropertyProtoSlot = (PropertyProtoSlot) keyProtoSlot;
                            final ValueType parameterValueType = keyPropertyProtoSlot.getValueType();
                            final Type parameterHeapType = keyPropertyProtoSlot.getHeapValueType();
                            switch (parameterValueType)
                            {
                                case Text:
                                {
                                    if (!String.class.equals(parameterHeapType))
                                    {
                                        final Class<?> syntaxClass = (Class<?>) parameterHeapType;
                                        parameterTypeString =  syntaxClass.getSimpleName();
                                    }
                                    else
                                    {
                                        parameterTypeString = parameterValueType.name();
                                    }

                                    break;
                                }
                                case SingleSelect:
                                {
                                    final Class<?> choicesEnumClass = (Class<?>) parameterHeapType;

                                    if (choicesEnumClass.isEnum())
                                    {
                                        parameterTypeString = choicesEnumClass.getSimpleName();
                                    }
                                    else
                                    {
                                        // ?
                                        parameterTypeString = parameterValueType.name();
                                    }

                                    break;
                                }
                                default:
                                {
                                    parameterTypeString = parameterValueType.name();
                                    break;
                                }
                            }
                        }

                    }

                    resourceParameterList.add(parameterTypeString + " " + parameterName);
                }
            }


            for (final Method method : Method.values())
            {
                for (final URI linkRelationUri : referenceRelationUris)
                {

                    final LinkTemplate referenceTemplate = referenceTemplates.get(linkRelationUri);
                    final LinkRelation linkRelation;

                    if (linkRelationCache.containsKey(linkRelationUri))
                    {
                        linkRelation = linkRelationCache.get(linkRelationUri);
                    }
                    else
                    {

                        final Keys linkRelationKeys = context.getApiLoader().buildDocumentKeys(linkRelationUri, schemaLoader.getLinkRelationSchemaUri());
                        linkRelation = context.getModel(linkRelationKeys, schemaLoader.getLinkRelationDimensions());
                        linkRelationCache.put(linkRelationUri, linkRelation);
                    }

                    if (method != linkRelation.getMethod())
                    {
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
                    if (responseSchemaUri != null)
                    {
                        final ObjectNode responseSchemaNode = getSchemaNode(objectMapper, schemaNodes, responseSchemaUri, schemaLoader);
                        referenceNode.put(PropertyName.responseSchema.name(), responseSchemaNode);

                        responseSchemaName = responseSchemaNode.get(SchemaDesignFormatter.PropertyName.localName.name()).asText();
                    }

                    final URI requestSchemaUri = referenceTemplate.getRequestSchemaUri();

                    String requestSchemaName = null;
                    if (requestSchemaUri != null)
                    {
                        final ObjectNode requestSchemaNode = getSchemaNode(objectMapper, schemaNodes, requestSchemaUri, schemaLoader);
                        referenceNode.put(PropertyName.requestSchema.name(), requestSchemaNode);

                        requestSchemaName = requestSchemaNode.get(SchemaDesignFormatter.PropertyName.localName.name()).asText();
                    }

                    final StringBuilder signatureBuilder = new StringBuilder();

                    if (responseSchemaName != null)
                    {
                        signatureBuilder.append(responseSchemaName);
                    }
                    else
                    {
                        signatureBuilder.append("void");
                    }

                    signatureBuilder.append(" ");

                    String functionName = relationTitle;

                    if (SystemLinkRelation.self.getUri().equals(linkRelationUri))
                    {
                        functionName = "get" + responseSchemaName;
                        selfResponseSchemaName = responseSchemaName;
                    }
                    else if (SystemLinkRelation.save.getUri().equals(linkRelationUri))
                    {
                        functionName = "save" + responseSchemaName;
                    }
                    else if (SystemLinkRelation.delete.getUri().equals(linkRelationUri))
                    {
                        functionName = "delete";
                        if (defaultSchemaName != null)
                        {
                            functionName += defaultSchemaName;
                        }
                        else if (selfResponseSchemaName != null)
                        {
                            functionName += selfResponseSchemaName;
                        }
                    }

                    signatureBuilder.append(functionName).append(" ( ");

                    String parameterString = null;
                    if (resourceParameterList != null)
                    {
                        final StringBuilder parameterStringBuilder = new StringBuilder();
                        final int parameterCount = resourceParameterList.size();
                        for (int i = 0; i < parameterCount; i++)
                        {
                            final String parameter = resourceParameterList.get(i);
                            parameterStringBuilder.append(parameter);
                            if (i < parameterCount - 1)
                            {
                                parameterStringBuilder.append(" , ");
                            }
                        }

                        parameterString = parameterStringBuilder.toString();
                        signatureBuilder.append(parameterString);
                    }

                    if (requestSchemaName != null)
                    {
                        if (StringUtils.isNotBlank(parameterString))
                        {
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


        if (referencesNode.size() > 0)
        {
            resourceNode.put(PropertyName.references.name(), referencesNode);
        }


        return resourceNode;
    }

    protected ObjectNode getSchemaNode(final ObjectMapper objectMapper, final Map<URI, ObjectNode> schemaNodes, final URI schemaUri, final SchemaLoader schemaLoader)
    {

        final ObjectNode schemaNode;
        if (schemaNodes.containsKey(schemaUri))
        {
            schemaNode = schemaNodes.get(schemaUri);
        }
        else
        {
            schemaNode = SchemaDesignFormatter.buildSchemaNode(objectMapper, schemaUri, schemaLoader, null);
            schemaNodes.put(schemaUri, schemaNode);
        }

        return schemaNode;
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

        if (settings.containsKey(MINIFY_SETTING_NAME))
        {
            _IsSourceCodeMinified = Boolean.valueOf(settings.get(MINIFY_SETTING_NAME));
        }


    }

    private enum PropertyName
    {
        allResources,
        defaultSchema,
        description,
        fullPath,
        id,
        links,
        method,
        parentPath,
        pathSegment,
        references,
        rel,
        relationTitle,
        requestSchema,
        resource,
        responseSchema,
        signature,
        title,
        uri,
        uriTemplate,
        version;

    }

    private enum LinkTemplateType
    {
        Reference,
        Link;
    }
}
