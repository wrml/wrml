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
package org.wrml.util;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.rest.LinkTemplate;
import org.wrml.model.rest.Method;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.format.ModelWritingException;
import org.wrml.runtime.format.SystemFormat;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.rest.ApiNavigator;
import org.wrml.runtime.rest.Resource;
import org.wrml.runtime.schema.SchemaLoader;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Set of functions to produce string representations (primarily for debugging) of WRML's core data structures.
 */
public class AsciiArt
{

    /**
     * <p>
     * The WRML ASCII art "logo"
     * </p>
     * <p/>
     * <p>
     * <p/>
     * 
     * <pre>
     *             __     __   ______   __    __   __
     *            /\ \  _ \ \ /\  == \ /\ "-./  \ /\ \
     *            \ \ \/ ".\ \\ \  __< \ \ \-./\ \\ \ \____
     *             \ \__/".~\_\\ \_\ \_\\ \_\ \ \_\\ \_____\
     *              \/_/   \/_/ \/_/ /_/ \/_/  \/_/ \/_____/
     * </pre>
     * <p/>
     * </p>
     */
    public final static String LOGO;

    private static final Logger LOG = LoggerFactory.getLogger(AsciiArt.class);

    private static final int ASCII_DEFAULT_VERTICAL_SPACING = 1;

    private static final int ASCII_DEFAULT_WIDTH = 2;

    private static final String ASCII_SPACES = " " + StringUtils.repeat(" ", ASCII_DEFAULT_WIDTH);

    private static final String ASCII_VERTICAL_SPACER = "|" + StringUtils.repeat(" ", ASCII_DEFAULT_WIDTH);

    private static final String ASCII_SUBLEVEL_INDENT = "+" + StringUtils.repeat("-", ASCII_DEFAULT_WIDTH);

    static
    {

        final StringBuilder logo = new StringBuilder();

        logo.append("\n             __     __     ______     __    __     __             ");
        logo.append('\n');
        logo.append("            /\\ \\  _ \\ \\   /\\  == \\   /\\ \"-./  \\   /\\ \\            ");
        logo.append('\n');
        logo.append("            \\ \\ \\/ \".\\ \\  \\ \\  __<   \\ \\ \\-./\\ \\  \\ \\ \\____       ");
        logo.append('\n');
        logo.append("             \\ \\__/\".~\\_\\  \\ \\_\\ \\_\\  \\ \\_\\ \\ \\_\\  \\ \\_____\\      ");
        logo.append('\n');
        logo.append("              \\/_/   \\/_/   \\/_/ /_/   \\/_/  \\/_/   \\/_____/      \n");
        LOGO = logo.toString();

    }

    public static String express(final ApiNavigator apiNavigator)
    {

        if (apiNavigator == null)
        {
            return "";
        }

        final StringBuilder stringBuilder = new StringBuilder();

        final Api api = apiNavigator.getApi();

        final URI apiUri = api.getUri();

        stringBuilder.append("API URI: ").append(apiUri);
        stringBuilder.append("\nAPI TITLE: ").append(api.getTitle());
        stringBuilder.append("\nAPI DESCRIPTION: ").append(api.getDescription());
        stringBuilder.append("\nAPI RESOURCES:\n");

        final ApiNavigatorAsciiTree asciiTree = AsciiArt.createApiNavigatorAsciiTree(apiNavigator);
        stringBuilder.append(AsciiArt.expressAsciiTree(asciiTree));

        final List<LinkTemplate> linkTemplates = api.getLinkTemplates();

        stringBuilder.append("\nLINK TEMPLATE COUNT: ").append(linkTemplates.size());

        if (linkTemplates.size() > 0)
        {
            stringBuilder.append("\nLINK TEMPLATES:\n\n");

            final Context context = api.getContext();
            final ApiLoader apiLoader = context.getApiLoader();
            final SchemaLoader schemaLoader = context.getSchemaLoader();
            final List<String> linkTemplateStrings = new ArrayList<>(linkTemplates.size());

            for (final LinkTemplate linkTemplate : linkTemplates)
            {
                final StringBuilder sb = new StringBuilder();
                final UUID referrerId = linkTemplate.getReferrerId();
                final Resource referrerResource = apiNavigator.getResource(referrerId);
                final String referrerResourceRelativePath = referrerResource.getPathText();

                final URI linkRelationUri = linkTemplate.getLinkRelationUri();
                final LinkRelation linkRelation = apiLoader.loadLinkRelation(linkRelationUri);
                final Method method = linkRelation.getMethod();
                final String methodProtocolName = method.getProtocolGivenName();

                final UUID endpointId = linkTemplate.getEndPointId();
                final Resource endpointResource = apiNavigator.getResource(endpointId);
                final String endpointResourceRelativePath = endpointResource.getPathText();

                final Set<URI> responseSchemaUris = endpointResource.getResponseSchemaUris(method);
                final int responseSchemaCount;
                final List<Schema> responseSchemas;
                if (responseSchemaUris != null)
                {
                    responseSchemas = new ArrayList<>(responseSchemaUris.size());
                    for (final URI responseSchemaUri : responseSchemaUris)
                    {
                        final Schema schema = schemaLoader.load(responseSchemaUri);
                        responseSchemas.add(schema);
                    }

                    responseSchemaCount = responseSchemas.size();
                }
                else
                {
                    responseSchemaCount = 0;
                    responseSchemas = null;
                }

                final Set<URI> requestSchemaUris = endpointResource.getRequestSchemaUris(method);
                final int requestSchemaCount;
                final List<Schema> requestSchemas;
                if (requestSchemaUris != null)
                {

                    requestSchemas = new ArrayList<>(requestSchemaUris.size());
                    for (final URI requestSchemaUri : requestSchemaUris)
                    {
                        final Schema schema = schemaLoader.load(requestSchemaUri);
                        requestSchemas.add(schema);
                    }

                    requestSchemaCount = requestSchemaUris.size();
                }
                else
                {
                    requestSchemaCount = 0;
                    requestSchemas = null;
                }

                sb.append(referrerResourceRelativePath).append(" --[").append(methodProtocolName).append("]").append("--> ").append(endpointResourceRelativePath);
                sb.append(" : ");

                if (responseSchemaCount == 0)
                {
                    sb.append("void ");
                }
                else
                {
                    for (int i = 0; i < responseSchemaCount; i++)
                    {
                        final Schema schema = responseSchemas.get(i);
                        final String returnType = schema.getUniqueName().getLocalName();
                        sb.append(returnType).append(" ");
                        if (i < responseSchemaCount - 1)
                        {
                            sb.append("| ");
                        }
                    }
                }

                sb.append(linkRelation.getTitle());

                if (requestSchemaCount == 0)
                {
                    sb.append("(");
                }
                else
                {
                    sb.append("( ");

                    for (int i = 0; i < requestSchemaCount; i++)
                    {
                        final Schema schema = requestSchemas.get(i);
                        final String paramType = schema.getUniqueName().getLocalName();
                        sb.append(paramType).append(" ");
                        if (i < requestSchemaCount - 1)
                        {
                            sb.append("| ");
                        }
                    }
                }

                sb.append(");\n");

                linkTemplateStrings.add(sb.toString());

            }

            Collections.sort(linkTemplateStrings);
            for (final String linkTemplateString : linkTemplateStrings)
            {
                stringBuilder.append(linkTemplateString);
            }

        }

        return stringBuilder.toString();
    }

    public static String express(final Model model)
    {

        final Context context = model.getContext();
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        try
        {
            context.writeModel(byteOut, model, SystemFormat.json.getFormatUri());
        }
        catch (final ModelWritingException e)
        {
            LOG.error("Unable to express the model " + model.getHeapId() + ", returning null.", e);
            return null;
        }

        final byte[] modelBytes = byteOut.toByteArray();
        return new String(modelBytes);

    }

    public static String express(final Object object)
    {

        final ObjectWriter objectWriter = new ObjectMapper().writer(new DefaultPrettyPrinter());
        try
        {
            return objectWriter.writeValueAsString(object);
        }
        catch (final Exception e)
        {
            LOG.warn(e.getMessage());
        }

        return null;
    }

    private static ApiNavigatorAsciiTree createApiNavigatorAsciiTree(final ApiNavigator apiNavigator)
    {

        return new ApiNavigatorAsciiTree(apiNavigator);
    }

    public static <N extends AsciiTreeNode<N>> String expressAsciiTree(final N n)
    {

        return expressAsciiTree(n, ASCII_DEFAULT_VERTICAL_SPACING, ASCII_DEFAULT_WIDTH, 0, null);
    }

    private static <N extends AsciiTreeNode<N>> String expressAsciiTree(final N asciiTreeNode, final int verticalSpacing, final int width, int tabLevel,
            Map<Integer, Integer> levelChildren)
    {

        StringBuilder builder = new StringBuilder();
        if (levelChildren == null)
        {
            levelChildren = new TreeMap<Integer, Integer>();
        }

        // vertical spacer lines
        for (int v = 0; v < verticalSpacing; v++)
        {
            for (int i = 0; i < tabLevel; i++)
            {
                String output = ASCII_VERTICAL_SPACER;
                if (i < (tabLevel - 1) && MapUtils.getInteger(levelChildren, i) == null)
                {
                    output = ASCII_SPACES;
                }
                builder.append(output);
            }
            builder.append('\n');
        }

        // content line
        for (int i = 0; i < tabLevel; i++)
        {
            String output = ASCII_SUBLEVEL_INDENT;
            if (i < tabLevel - 1)
            {
                output = ASCII_VERTICAL_SPACER;
                if (MapUtils.getInteger(levelChildren, i) == null)
                {
                    output = ASCII_SPACES;
                }

            }
            builder.append(output);
        }
        builder.append(asciiTreeNode.getText()).append('\n');
        int size = asciiTreeNode.getChildNodes().size();
        if (size > 1)
        {
            levelChildren.put(tabLevel, size);
        }
        for (int j = 0; j < size; j++)
        {
            N nextChild = asciiTreeNode.getChildNodes().get(j);

            builder.append(expressAsciiTree(nextChild, verticalSpacing, width, tabLevel + 1, levelChildren));
            if (j == (size - 1))
            {
                levelChildren.remove(tabLevel);
            }
        }

        return builder.toString();
    }

    protected static <N extends AsciiTreeNode<N>> String expressAsciiTree(final AsciiTree<N> asciiTree)
    {

        return AsciiArt.expressAsciiTree(asciiTree.getRootNode(), ASCII_DEFAULT_VERTICAL_SPACING, ASCII_DEFAULT_WIDTH, 0, null);
    }

    private static interface AsciiTree<N extends AsciiTreeNode<N>>
    {

        int getBranchWidth();

        N getRootNode();

        int getVerticalSpacing();
    }

    protected static interface AsciiTreeNode<N extends AsciiTreeNode<N>>
    {

        List<N> getChildNodes();

        String getText();

    }

    private static final class ApiNavigatorAsciiTree implements AsciiTree<ResourceAsciiTreeNode>
    {

        private final ApiNavigator _ApiNavigator;

        ApiNavigatorAsciiTree(final ApiNavigator apiNavigator)
        {

            _ApiNavigator = apiNavigator;
        }

        public ApiNavigator getApiNavigator()
        {

            return _ApiNavigator;
        }

        @Override
        public int getBranchWidth()
        {

            return ASCII_DEFAULT_WIDTH;
        }

        @Override
        public ResourceAsciiTreeNode getRootNode()
        {

            return new ResourceAsciiTreeNode(getApiNavigator().getDocroot());
        }

        @Override
        public int getVerticalSpacing()
        {

            return ASCII_DEFAULT_VERTICAL_SPACING;
        }

    }

    public static class ResourceAsciiTreeNode implements AsciiTreeNode<ResourceAsciiTreeNode>
    {

        private final Resource _Resource;

        private List<ResourceAsciiTreeNode> _ChildNodes;

        ResourceAsciiTreeNode(final Resource resource)
        {

            _Resource = resource;
        }

        @Override
        public List<ResourceAsciiTreeNode> getChildNodes()
        {

            if (_ChildNodes == null)
            {
                _ChildNodes = new ArrayList<ResourceAsciiTreeNode>();
                final Map<String, Resource> literalPathResources = _Resource.getLiteralPathSubresources();
                if (MapUtils.isNotEmpty(literalPathResources))
                {
                    final Map<String, Resource> sortedliteralPathResources = new TreeMap<>(literalPathResources);
                    for (final String literalPath : sortedliteralPathResources.keySet())
                    {
                        final Resource literalPathResource = sortedliteralPathResources.get(literalPath);
                        _ChildNodes.add(new ResourceAsciiTreeNode(literalPathResource));
                    }
                }

                final Map<String, Resource> variablePathResources = _Resource.getVariablePathSubresources();
                if (MapUtils.isNotEmpty(variablePathResources))
                {

                    final Map<String, Resource> sortedVariablePathResources = new TreeMap<>(variablePathResources);

                    for (final String variablePath : sortedVariablePathResources.keySet())
                    {
                        final Resource variablePathResource = sortedVariablePathResources.get(variablePath);
                        _ChildNodes.add(new ResourceAsciiTreeNode(variablePathResource));
                    }
                }
            }
            return _ChildNodes;
        }

        public Resource getResource()
        {

            return _Resource;
        }

        @Override
        public String getText()
        {

            String pathSegment = getResource().getPathSegment();
            pathSegment = (pathSegment != null) ? pathSegment : "";
            return "/" + pathSegment;
        }

    }
}
