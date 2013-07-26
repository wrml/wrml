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
package org.wrml.runtime.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.Link;
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.rest.LinkTemplate;
import org.wrml.model.rest.Method;
import org.wrml.model.rest.ResourceTemplate;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.DimensionsBuilder;
import org.wrml.runtime.Keys;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.util.AsciiArt;

import java.net.URI;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The WRML implementation of REST's "hypermedia as the engine of application state" (HATEOAS) <a href= "http://www.ics.uci.edu/~sloting/pubs/dissertation/rest_arch_style.htm"
 * >concept</a>.
 * <p/>
 * The {@link ApiNavigator} <em>digests</em> and {@link Api}'s modeled metadata to manifest {@link Resource}s and make all {@link Document}-related models' {@link Link}s
 * "interactive" at runtime.
 * <p/>
 * Due to the symmetrical nature of REST's uniform interface and its generally cool style, a {@link ApiNavigator} may be used by a referrer model (i.e. client-side) for
 * "lazy loading" relationships that enable atomic document model decomposition while maintaining a functional reference to shared documents. This class may also be used as the
 * primary engine to drive a Web server's resource request processing; at the "end point" of a {@link Link}'s reference.
 * <p/>
 * In summary, the {@link ApiNavigator} handle the execution of both ends of a {@link Link}-based reference.
 * <p/>
 * See <a href="http://en.wikipedia.org/wiki/HATEOAS">Wikipedia</a> or <a href="https://www.google.com/search?q=hateoas">Google</a> for more information about hypermedia systems.
 */
public class ApiNavigator
{

    public static final char PATH_SEPARATOR_CHAR = '/';

    public static final String PATH_SEPARATOR = String.valueOf(ApiNavigator.PATH_SEPARATOR_CHAR);

    public static final String DOCROOT_PATH = ApiNavigator.PATH_SEPARATOR;

    private static final Logger LOG = LoggerFactory.getLogger(ApiNavigator.class);

    private final Api _Api;

    private final Dimensions _DefaultApiDimensions;

    private final Dimensions _DefaultLinkRelationDimensions;

    private final Dimensions _DefaultResourceTemplateDimensions;

    private final ConcurrentHashMap<UUID, Resource> _AllResources;

    private final Resource _Docroot;

    private final SortedSet<ResourceMatchResult> _DocrootResults;

    private Dimensions _ApiDimensions;

    private Dimensions _LinkRelationDimensions;

    private Dimensions _ResourceTemplateDimensions;

    private Dimensions _SchemaDimensions;

    public ApiNavigator(final Api api)
    {

        if (api == null)
        {
            throw new ApiNavigatorException("The Api cannot be null.", null, this);
        }

        if (api.getUri() == null)
        {
            throw new ApiNavigatorException("The Api's URI cannot be null.", null, this);
        }

        _Api = api;

        _AllResources = new ConcurrentHashMap<UUID, Resource>();

        final Context context = api.getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        _DefaultApiDimensions = schemaLoader.getApiDimensions();
        _ApiDimensions = _DefaultApiDimensions;

        _DefaultResourceTemplateDimensions = new DimensionsBuilder(schemaLoader.getResourceTemplateSchemaUri()).toDimensions();
        _ResourceTemplateDimensions = _DefaultResourceTemplateDimensions;

        _SchemaDimensions = schemaLoader.getSchemaDimensions();

        _DefaultLinkRelationDimensions = new DimensionsBuilder(schemaLoader.getLinkRelationSchemaUri()).toDimensions();
        _LinkRelationDimensions = _DefaultLinkRelationDimensions;

        _Docroot = new Resource(this, _Api.getDocroot(), null);
        _DocrootResults = new TreeSet<>();
        _DocrootResults.add(new ResourceMatchResult(_Docroot, 10000));

        addResource(_Docroot);

    }

    public static final boolean isApiNavigable(final Api api)
    {

        if (api == null)
        {
            return false;
        }
        final URI apiUri = api.getUri();
        final ResourceTemplate docroot = api.getDocroot();

        final boolean isApiNavigable = ((apiUri != null) && (docroot != null));
        return isApiNavigable;

    }

    public Resource addResource(final UUID parentResourceTemplateId, final ResourceTemplate childResourceTemplate)
    {

        final Resource child = new Resource(this, childResourceTemplate, getResource(parentResourceTemplateId));
        addResource(child);
        return child;
    }

    public Map<UUID, Resource> getAllResources()
    {

        return _AllResources;
    }

    public Api getApi()
    {

        return _Api;
    }

    public Dimensions getApiDimensions()
    {

        return _ApiDimensions;
    }

    public void setApiDimensions(final Dimensions apiDimensions)
    {

        _ApiDimensions = apiDimensions;
    }

    public Set<LinkRelation> getApiLinkRelations()
    {

        final Api api = getApi();
        final Context context = api.getContext();
        final ApiLoader apiLoader = context.getApiLoader();
        final Set<LinkRelation> allLinkRelations = new LinkedHashSet<>();
        final List<LinkTemplate> linkTemplates = getApi().getLinkTemplates();
        for (final LinkTemplate linkTemplate : linkTemplates)
        {
            final URI linkRelationUri = linkTemplate.getLinkRelationUri();
            if (linkRelationUri != null)
            {
                final LinkRelation linkRelation = apiLoader.loadLinkRelation(linkRelationUri);
                if (linkRelation != null)
                {
                    allLinkRelations.add(linkRelation);
                }
            }
        }

        return allLinkRelations;
    }

    public Set<Schema> getApiSchemas()
    {

        final SchemaLoader schemaLoader = getApi().getContext().getSchemaLoader();
        final Set<Schema> allSchemas = new LinkedHashSet<>();
        final List<LinkTemplate> linkTemplates = getApi().getLinkTemplates();
        for (final LinkTemplate linkTemplate : linkTemplates)
        {
            final URI requestSchemaUri = linkTemplate.getRequestSchemaUri();
            if (requestSchemaUri != null)
            {
                final Schema schema = schemaLoader.load(requestSchemaUri);
                if (schema != null)
                {
                    allSchemas.add(schema);
                }
            }

            final URI responseSchemaUri = linkTemplate.getResponseSchemaUri();
            if (responseSchemaUri != null)
            {
                final Schema schema = schemaLoader.load(responseSchemaUri);
                if (schema != null)
                {
                    allSchemas.add(schema);
                }
            }

        }

        final Map<UUID, Resource> allResources = getAllResources();
        for (final Resource resource : allResources.values())
        {
            final URI defaultSchemaUri = resource.getDefaultSchemaUri();
            if (defaultSchemaUri != null)
            {
                final Schema schema = schemaLoader.load(defaultSchemaUri);
                if (schema != null)
                {
                    allSchemas.add(schema);
                }

            }
        }

        return allSchemas;
    }

    public URI getApiUri()
    {

        return getApi().getUri();
    }

    public Dimensions getDefaultApiDimensions()
    {

        return _DefaultApiDimensions;
    }

    public Dimensions getDefaultLinkRelationDimensions()
    {

        return _DefaultLinkRelationDimensions;
    }

    public Resource getDocroot()
    {

        return _Docroot;
    }

    /**
     * Helper function that returns the {@link Resource} associated with the link's endpoint.
     */
    public Resource getEndpointResource(final URI linkRelationUri, final URI referrerDocumentUri)
    {

        if (linkRelationUri == null)
        {
            throw new ApiNavigatorException("The link's relation URI cannot be null.", null, this);
        }

        /*
         * If the referrer Document is associated with our Api, as we expect at this point, then the document URI will match one of our Api's described resources. Fetch the
         * metadata description of the document's corresponding resource.
         */
        final UUID referrerResourceTemplateId = getResourceTemplateId(referrerDocumentUri);
        if (referrerResourceTemplateId == null)
        {
            /*
             * throw new ApiNavigatorException( "The referring document is not a representation of any resources described by this " + getClass().getName() + "'s " +
             * Api.class.getName() + " (" + getApi() + ").", null, this);
             */
            return null;
        }

        /*
         * Use the pre-computed Resource to speed up the api metadata analysis.
         */
        final Resource referrerResource = getResource(referrerResourceTemplateId);

        /*
         * The referrer document's associated ResourceTemplate must have an "outbound" LinkTemplate which references the same LinkRelation (by id) that is referenced by this
         * method's Link param.
         */
        final LinkTemplate linkTemplate = referrerResource.getLinkTemplates().get(linkRelationUri);
        if (linkTemplate == null)
        {

            // TODO: Strict mode?
            /*
             * throw new ApiNavigatorException("The referring document's resource (" + referrerResource + ") is not linked to any other resources with the " +
             * LinkRelation.class.getName() + " URI (" + linkRelationUri + ").", null, this);
             */
            return null;
        }

        /*
         * The link template has two ends, the referrer end and the pointy end (end point).
         */
        final UUID endpointResourceTemplateId = linkTemplate.getEndPointId();

        /*
         * Get the resource on the other end of the LinkTemplate.
         */
        final Resource endPointResource = getResource(endpointResourceTemplateId);
        return endPointResource;
    }

    public Dimensions getLinkRelationDimensions()
    {

        return _LinkRelationDimensions;
    }

    public void setLinkRelationDimensions(final Dimensions linkRelationDimensions)
    {

        _LinkRelationDimensions = linkRelationDimensions;
    }

    public Resource getResource(final URI uri)
    {

        final UUID resourceTemplateId = getResourceTemplateId(uri);
        return getResource(resourceTemplateId);
    }

    /**
     * Get the Resource with the specified id. A {@link Resource} is the runtime counterpart/equivalent of a {@link ResourceTemplate}.
     * 
     * @param resourceTemplateId
     *            The {@link URI} that identifies the {@link Resource}'s associated {@link ResourceTemplate}.
     * @return The {@link Resource} associated with the specified {@link ResourceTemplate}'s id.
     */
    public Resource getResource(final UUID resourceTemplateId)
    {
        if ((resourceTemplateId == null) || !(_AllResources.containsKey(resourceTemplateId)))
        {
            return null;
        }

        return _AllResources.get(resourceTemplateId);
    }

    public Dimensions getResourceTemplateDimensions()
    {

        return _ResourceTemplateDimensions;
    }

    public void setResourceTemplateDimensions(final Dimensions resourceTemplateDimensions)
    {

        _ResourceTemplateDimensions = resourceTemplateDimensions;
    }

    public UUID getResourceTemplateId(final URI uri)
    {

        final SortedSet<ResourceMatchResult> results = match(uri);

        if (results == null || results.isEmpty())
        {
            final URI apiUri = getApiUri();
            ApiNavigator.LOG.error("1 This ApiNavigator has charted \"{}\", which is not a match for the specified URI: {}", new Object[] {apiUri, uri});

            throw new ApiNavigatorException("This ApiNavigator has charted \"" + apiUri + "\", which is not a match for the specified URI: " + uri + ".", null, this,
                    Status.NOT_FOUND);
        }

        final ResourceMatchResult result = results.first();
        final Resource resource = result.getResource();
        return resource.getResourceTemplateId();
    }

    public SortedSet<Parameter> getSurrogateKeyComponents(final URI uri, final Prototype prototype)
    {

        final SortedSet<ResourceMatchResult> results = match(uri);

        if (results == null || results.isEmpty())
        {
            final URI apiUri = getApiUri();
            ApiNavigator.LOG.error("2 This ApiNavigator has charted \"{}\", which is not a match for the specified URI: {}", new Object[] {apiUri, uri});
            throw new ApiNavigatorException("This ApiNavigator has charted \"" + apiUri + "\", which is not a match for the specified URI: " + uri + ".", null, this,
                    Status.NOT_FOUND);
        }

        SortedSet<Parameter> surrogateKeyComponents = null;

        for (final ResourceMatchResult result : results)
        {
            final Resource endPointResource = result.getResource();

            final Set<URI> responseSchemaUris = endPointResource.getResponseSchemaUris(Method.Get);
            if (responseSchemaUris == null)
            {
                continue;
            }

            boolean isCompatibleResource = false;
            for (final URI responseSchemaUri : responseSchemaUris)
            {
                if (prototype.isAssignableFrom(responseSchemaUri))
                {
                    isCompatibleResource = true;
                    break;
                }
            }

            if (!isCompatibleResource)
            {
                continue;
            }

            final UriTemplate uriTemplate = endPointResource.getUriTemplate();
            surrogateKeyComponents = uriTemplate.getParameters(uri);

            if (surrogateKeyComponents != null && !surrogateKeyComponents.isEmpty())
            {
                break;
            }
        }

        return surrogateKeyComponents;
    }

    @Override
    public String toString()
    {

        return AsciiArt.express(this);
    }

    public <M extends Model> M visitLink(final Link link, final Model referrer, final URI referrerUri, final DimensionsBuilder dimensionsBuilder, final Model parameter)
    {

        if (referrer == null)
        {
            throw new ApiNavigatorException("The referrer cannot be null.", null, this);
        }

        if (referrerUri == null)
        {
            throw new ApiNavigatorException("The referrer's Document URI cannot be null.", null, this);
        }

        if (link == null)
        {
            throw new ApiNavigatorException("The link cannot be null.", null, this);
        }

        final Model embeddedModel = link.getDoc();
        if (embeddedModel != null)
        {
            return (M) embeddedModel;
        }

        final URI referrerSchemaUri = referrer.getSchemaUri();
        final URI referenceRelationUri = link.getRel();

        final ApiLoader apiLoader = getApi().getContext().getApiLoader();
        final LinkRelation linkRelation = apiLoader.loadLinkRelation(referenceRelationUri);
        if (linkRelation == null)
        {
            throw new ApiNavigatorException("The link relation cannot be null.", null, this);
        }

        final Method method = linkRelation.getMethod();

        final Resource endPointResource = getEndpointResource(referenceRelationUri, referrerUri);
        if (endPointResource == null)
        {
            throw new ApiNavigatorException("The end point cannot be null.", null, this);
        }

        URI uri = link.getHref();

        if (uri == null)
        {

            uri = endPointResource.getUri(referrer, referenceRelationUri);

            if (uri == null)
            {
                throw new ApiNavigatorException("The end point's document URI (link's href) cannot be null.", null, this);
            }
        }

        final Api api = getApi();
        final Context context = api.getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final DimensionsBuilder responseDimensionsBuilder;
        if (dimensionsBuilder == null)
        {
            final URI responseSchemaUri = getDefaultResponseSchemaUri(method, uri);

            responseDimensionsBuilder = new DimensionsBuilder(responseSchemaUri);
        }
        else
        {
            responseDimensionsBuilder = dimensionsBuilder;
        }

        responseDimensionsBuilder.setReferrerUri(referrerUri);
        URI schemaUri = responseDimensionsBuilder.getSchemaUri();

        if (method == Method.Get && (schemaUri == null || schemaUri.equals(schemaLoader.getDocumentSchemaUri())))
        {
            if (referrerUri != null && referrerUri.equals(uri))
            {
                schemaUri = referrerSchemaUri;
            }
        }

        if (schemaUri != null)
        {
            responseDimensionsBuilder.setSchemaUri(schemaUri);
        }

        final Dimensions responseDimensions = apiLoader.buildDocumentDimensions(method, uri, responseDimensionsBuilder);
        final Keys keys = apiLoader.buildDocumentKeys(uri, responseDimensions.getSchemaUri());

        Set<URI> requestSchemaUris = endPointResource.getRequestSchemaUris(method);
        if (parameter != null)
        {
            // Determine if the parameter is allowed

            final URI parameterSchemaUri = parameter.getSchemaUri();
            if (requestSchemaUris.isEmpty())
            {
                throw new ApiNavigatorException("The " + linkRelation.getUri() + " does not allow any parameter to be passed to resource: " + endPointResource, null, this);
            }
            else if (!requestSchemaUris.contains(parameterSchemaUri))
            {

                boolean isParameterSubType = false;
                for (final URI requestSchemaUri : requestSchemaUris)
                {
                    final Prototype requestPrototype = schemaLoader.getPrototype(requestSchemaUri);
                    if (requestPrototype.isAssignableFrom(parameterSchemaUri))
                    {
                        isParameterSubType = true;
                        break;
                    }
                }

                if (!isParameterSubType)
                {
                    throw new ApiNavigatorException("The " + linkRelation.getUri() + " does not allow a " + parameterSchemaUri + " parameter to be passed to resource: "
                            + endPointResource, null, this);
                }
            }
        }

        Model param = parameter;

        // Handle special case for "Save" links to enable the referrer model automatically passes itself as the parameter.
        if (method == Method.Save && param == null && (!requestSchemaUris.isEmpty()))
        {
            for (final URI requestSchemaUri : requestSchemaUris)
            {

                Class<?> requestSchemaInterface;
                try
                {
                    requestSchemaInterface = schemaLoader.getSchemaInterface(requestSchemaUri);
                }
                catch (final ClassNotFoundException e)
                {
                    throw new ApiNavigatorException("Failed to load the schema interface for: \"" + requestSchemaUri + "\"", e, this);
                }

                // Determine if the referrer may be inferred as a (this) parameter.

                Class<?> referrerSchemaInterface;
                try
                {
                    referrerSchemaInterface = schemaLoader.getSchemaInterface(referrerSchemaUri);
                }
                catch (final ClassNotFoundException e)
                {
                    throw new ApiNavigatorException("Failed to load the schema interface for referrer schema id: \"" + referrerSchemaUri + "\"", e, this);
                }

                if (requestSchemaInterface.isAssignableFrom(referrerSchemaInterface))
                {
                    /*
                     * The param was null and the referrer's type matches the link's content-type expectation, so set the referrer as the param.
                     */
                    param = referrer;
                    break;
                }
            }
        }

        return context.request(method, keys, responseDimensions, param);

    }

    public final URI getDefaultResponseSchemaUri(final Method requestMethod, final URI uri)
    {

        final Resource resource = getResource(uri);
        final ResourceTemplate resourceTemplate = resource.getResourceTemplate();
        final URI resourceTemplateDefaultSchemaUri = resourceTemplate.getDefaultSchemaUri();
        if (resourceTemplateDefaultSchemaUri != null)
        {
            return resourceTemplateDefaultSchemaUri;
        }

        final Set<URI> responseSchemaUris = resource.getResponseSchemaUris(requestMethod);
        if (responseSchemaUris != null && !responseSchemaUris.isEmpty())
        {
            return responseSchemaUris.iterator().next();
        }
        else
        {
            throw new ApiNavigatorException("The method used is not supported by the api. METHOD [" + requestMethod + "]", null, this);
        }
    }

    private void addResource(final Resource resource)
    {

        final ResourceTemplate resourceTemplate = resource.getResourceTemplate();

        final UUID resourceTemplateId = resourceTemplate.getUniqueId();
        if (resourceTemplateId == null)
        {
            throw new ApiNavigatorException("The ResourceTemplate id cannot be null. (Resource: " + resource + ")", null, this);
        }

        if (_AllResources.containsKey(resourceTemplateId))
        {
            return;
        }

        _AllResources.put(resourceTemplateId, resource);
        final List<ResourceTemplate> subresourceTemplates = resourceTemplate.getChildren();

        for (final ResourceTemplate subresourceTemplate : subresourceTemplates)
        {
            final Resource subresource = new Resource(this, subresourceTemplate, resource);
            resource.addSubresource(subresource);
            addResource(subresource);
        }

    }

    private Dimensions getSchemaDimensions()
    {

        return _SchemaDimensions;
    }

    public void setSchemaDimensions(final Dimensions schemaDimensions)
    {

        _SchemaDimensions = schemaDimensions;
    }

    /**
     * Determine which resource(s) match the requested resource id.
     * <p/>
     * Note: This needs to be as fast as possible because it is used during client request handling.
     */
    private SortedSet<ResourceMatchResult> match(final URI uri)
    {

        ApiNavigator.LOG.debug("Attempting match on URI {}", new Object[] {uri});

        if (uri == null)
        {
            ApiNavigator.LOG.error("3 This ApiNavigator cannot locate a resource with a *null* identifier.");
            throw new ApiNavigatorException("This ApiNavigator cannot locate a resource with a *null* identifier.", null, this);

        }

        final URI apiUri = getApiUri();

        if (!uri.toString().startsWith(apiUri.toString()))
        {
            ApiNavigator.LOG.error("4 This ApiNavigator has charted \"" + apiUri + "\", which does not manage the specified resource (" + uri + ")");
            throw new ApiNavigatorException("This ApiNavigator has charted \"" + apiUri + "\", which does not manage the specified resource (" + uri + ")", null, this,
                    Status.NOT_FOUND);
        }

        final String path = uri.getPath();

        final SortedSet<ResourceMatchResult> results = matchPath(path);

        if (results == null || results.isEmpty() || results.size() == 1)
        {
            return results;
        }

        int resultsTiedForFirst = 0;
        final int highestScore = results.first().getScore();
        for (final ResourceMatchResult result : results)
        {
            final int score = result.getScore();
            if (score == highestScore)
            {
                resultsTiedForFirst++;
            }
            else
            {
                break;
            }
        }

        // TODO there's no differentiation here; either a lot of logic missing, or a lot of unnecessary logic
        if (resultsTiedForFirst == 1)
        {
            return results;
        }

        return results;
    }

    private SortedSet<ResourceMatchResult> matchPath(String path)
    {

        // TODO Is this needed? The path should be sanitized before getting this far....
        path = StringUtils.trim(path);
        if (path.length() == 0 || ApiNavigator.DOCROOT_PATH.equals(path))
        {
            return _DocrootResults;
        }

        final SortedSet<ResourceMatchResult> results = new TreeSet<ResourceMatchResult>();
        final String[] pathSegments = StringUtils.split(path, ApiNavigator.PATH_SEPARATOR_CHAR);
        // TODO Refactor out of C-style method calling? Why not return results? MSM: Current approach uses recursion;
        // but still could return results. Note that all of this is private implementation detail.
        matchPathSegment(_Docroot, pathSegments, 0, 0, results);

        ApiNavigator.LOG.debug("The path \"{}\" matches *{}* results.", path, results.size());
        return results;
    }

    private void matchPathSegment(final Resource resource, final String[] pathSegments, final int segmentIndex, int score, final SortedSet<ResourceMatchResult> results)
    {

        if (resource == null)
        {
            // No resource branch to investigate.
            return;
        }

        final int segmentCount = pathSegments.length;

        if (segmentIndex >= segmentCount)
        {
            // No segments left to match.
            return;
        }

        final Map<String, Resource> literalPathSubresources = resource.getLiteralPathSubresources();
        final Map<String, Resource> variablePathSubresources = resource.getVariablePathSubresources();

        if (literalPathSubresources == null && variablePathSubresources == null)
        {
            // Still have segments to go, but there is nothing to match them
            // against.
            return;
        }

        final String segment = pathSegments[segmentIndex];
        final int nextSegmentIndex = segmentIndex + 1;
        final boolean isLastSegment = nextSegmentIndex == segmentCount;

        score += nextSegmentIndex;

        if (literalPathSubresources != null && literalPathSubresources.containsKey(segment))
        {

            final Resource literalPathSubresource = literalPathSubresources.get(segment);

            if (isLastSegment)
            {

                // The last segment is significant because it means we can add a
                // matching result with a bonus score and then return the result
                // set without any further recursion.

                // TODO this should only be added if there's a link template here, else improper match.
                // e.g. /capricas matching on /capricas/{capricaNumber} with score 11 when there's
                // /{key} that matches with link with score 7, giving wrong order in results

                final Map<URI, LinkTemplate> linkTemplates = literalPathSubresource.getLinkTemplates();
                if (!linkTemplates.isEmpty())
                {
                    final ResourceMatchResult result = new ResourceMatchResult(literalPathSubresource, score + 10);
                    results.add(result);
                }
            }
            else
            {
                // There are more paths following ours.
                matchPathSegment(literalPathSubresource, pathSegments, nextSegmentIndex, score + 10, results);
            }
        }

        if (variablePathSubresources != null)
        {

            for (final String variablePathSegment : variablePathSubresources.keySet())
            {
                final Resource variablePathSubresource = variablePathSubresources.get(variablePathSegment);

                int bonus = 0;
                if (isLastSegment)
                {
                    if (variablePathSubresource.getLiteralPathSubresources() == null && variablePathSubresource.getVariablePathSubresources() == null)
                    {
                        bonus += 1;
                    }
                }

                // TODO verify this behavior
                if (!variablePathSubresource.getLinkTemplates().isEmpty())
                {
                    final ResourceMatchResult result = new ResourceMatchResult(variablePathSubresource, score + 5 + bonus);
                    results.add(result);
                }

                if (!isLastSegment)
                {
                    matchPathSegment(variablePathSubresource, pathSegments, nextSegmentIndex, score + 5, results);
                }
            }
        }

    }

    private static class ResourceMatchResult implements Comparable<ResourceMatchResult>
    {

        /**
         * When used on sets of results, the highest scoring results will sort as first. Note that the higher scores are *always* greater positive numbers.
         */
        public static Comparator<ResourceMatchResult> HIGHEST_SCORE_FIRST = new Comparator<ResourceMatchResult>()
        {

            @Override
            public int compare(final ResourceMatchResult result1, final ResourceMatchResult result2)
            {

                if (result1 == result2)
                {
                    return 0;
                }
                return Integer.signum(result2.getScore() - result1.getScore());
            }
        };

        private final Resource _Resource;

        private final int _Score;

        ResourceMatchResult(final Resource resource, final int score)
        {

            _Resource = resource;
            _Score = score;

        }

        @Override
        public final int compareTo(final ResourceMatchResult other)
        {

            return ResourceMatchResult.HIGHEST_SCORE_FIRST.compare(this, other);
        }

        public Resource getResource()
        {

            return _Resource;
        }

        public int getScore()
        {

            return _Score;
        }

        @Override
        public String toString()
        {

            return "Resource: " + _Resource.toString() + "\nScore: " + _Score;
        }
    }
}
