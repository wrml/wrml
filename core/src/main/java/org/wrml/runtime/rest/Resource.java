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

import com.google.common.collect.ComparisonChain;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.model.rest.*;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.schema.*;
import org.wrml.runtime.syntax.SyntaxLoader;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A runtime manifestation of a specific {@link Api}'s specific {@link ResourceTemplate} (REST API URI tree node).
 */
public class Resource implements Comparable<Resource> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Resource.class);

    private static final String TO_STRING_FORMAT = "{\"Resource\" : { \"ResourceTemplateId\" : \"%s\",\"UriTemplate\" : %s,\"FullPath\" : \"%s\"}}";

    /**
     * The {@link ApiNavigator} that owns us.
     */
    private final ApiNavigator _ApiNavigator;

    private final ResourceTemplate _ResourceTemplate;

    private final UriTemplate _UriTemplate;

    private final Resource _ParentResource;

    private final String _FullPath;

    private final String _ParentPath;

    private final ConcurrentHashMap<String, Resource> _LiteralPathSubresources;

    private final ConcurrentHashMap<String, Resource> _VariablePathSubresources;

    /**
     * The ways in which resources may reference us.
     */
    private final ConcurrentHashMap<URI, LinkTemplate> _ReferenceTemplates;

    private final Set<Method> _ReferenceMethods;

    private final ConcurrentHashMap<Method, Set<URI>> _ReferenceTemplateMethodToLinkRelationUrisMap;

    private final ConcurrentHashMap<Method, Set<URI>> _ReferenceTemplateMethodToRequestSchemaUrisMap;

    private final ConcurrentHashMap<Method, Set<URI>> _ReferenceTemplateMethodToResponseSchemaUriMap;

    /**
     * The ways in which we may link to (or reference) resources.
     */
    private final ConcurrentHashMap<URI, LinkTemplate> _LinkTemplates;

    /**
     * <p>
     * The {@link Resource} constructor compiles a "chunk" of the {@link Api} metadata; an individual
     * {@link ResourceTemplate}. It is part of a runtime-constructed tree structure that represents each URI path '/' as
     * a hierarchical tree of {@link Resource} nodes.
     * </p>
     * <p/>
     * <p>
     * If an {@link Api} were a regex input string, and an {@link ApiNavigator} was its corresponding Regex compilation;
     * then a {@link Resource} would be a subexpression, a nested component within the compiled (optimized) regex. The
     * {@link Resource} (along with the {@link ApiNavigator}) compile {@link Api} metadata so that it is ready to be
     * used by the runtime for "pattern matching" (request routing by the framework).
     * </p>
     */
    Resource(final ApiNavigator apiNavigator, final ResourceTemplate resourceTemplate, final Resource parentResource) {

        if (apiNavigator == null) {
            throw new ResourceException("The apiNavigator may not be null", null, this);
        }

        if (resourceTemplate == null) {
            throw new ResourceException("The resource template may not be null", null, this);
        }

        _ApiNavigator = apiNavigator;
        _ResourceTemplate = resourceTemplate;
        _ParentResource = parentResource;
        _FullPath = getFullPath(parentResource);
        if (_ParentResource != null) {
            _ParentPath = _ParentResource.getPathText();
        }
        else {
            _ParentPath = null;
        }

        final Api api = apiNavigator.getApi();
        final Context context = api.getContext();
        final ApiLoader apiLoader = context.getApiLoader();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();

        final URI apiUri = api.getUri();

        final String uriTemplateString = StringUtils.join(apiUri.toString(), _FullPath);
        LOGGER.debug("creating resource with uriTemplateString={} and _FullPath={}", uriTemplateString, _FullPath);

        _UriTemplate = new UriTemplate(syntaxLoader, uriTemplateString);
        _LiteralPathSubresources = new ConcurrentHashMap<String, Resource>();
        _VariablePathSubresources = new ConcurrentHashMap<String, Resource>();
        _LinkTemplates = new ConcurrentHashMap<URI, LinkTemplate>();

        // The reference templates are API metadata that describe possible "request/link" types that may target this
        // resource as an endpoint.
        _ReferenceTemplates = new ConcurrentHashMap<URI, LinkTemplate>();

        _ReferenceMethods = Collections.newSetFromMap(new ConcurrentHashMap<Method, Boolean>());

        _ReferenceTemplateMethodToLinkRelationUrisMap = new ConcurrentHashMap<Method, Set<URI>>();
        _ReferenceTemplateMethodToRequestSchemaUrisMap = new ConcurrentHashMap<Method, Set<URI>>();
        _ReferenceTemplateMethodToResponseSchemaUriMap = new ConcurrentHashMap<Method, Set<URI>>();

        final UUID resourceTemplateId = _ResourceTemplate.getUniqueId();

        final List<LinkTemplate> linkTemplates = api.getLinkTemplates();
        for (final LinkTemplate linkTemplate : linkTemplates) {

            final URI linkRelationUri = linkTemplate.getLinkRelationUri();
            if (linkRelationUri == null) {
                continue;
            }

            final UUID endPointId = linkTemplate.getEndPointId();
            if (endPointId != null && endPointId.equals(resourceTemplateId)) {
                _ReferenceTemplates.put(linkRelationUri, linkTemplate);

                final LinkTemplate reference = linkTemplate;

                // Each reference has an associate link relation which is it's "metafunction".

                final SchemaLoader schemaLoader = context.getSchemaLoader();
                final URI documentSchemaUriConstant = schemaLoader.getDocumentSchemaUri();

                final Keys relKeys = apiLoader.buildDocumentKeys(linkRelationUri, schemaLoader.getLinkRelationSchemaUri());

                final Dimensions relDimensions = apiNavigator.getLinkRelationDimensions();
                final LinkRelation rel = context.getModel(relKeys, relDimensions);

                if (rel == null) {
                    throw new ResourceException("The link relation: " + linkRelationUri + " was not found", null, this);
                }

                // The interaction method associated with the link relation matches the parameter.

                final Method requestMethod = rel.getMethod();
                _ReferenceMethods.add(requestMethod);

                if (!_ReferenceTemplateMethodToLinkRelationUrisMap.containsKey(requestMethod)) {
                    _ReferenceTemplateMethodToLinkRelationUrisMap.put(requestMethod, new LinkedHashSet<URI>());
                }

                final Set<URI> linkRelationUris = _ReferenceTemplateMethodToLinkRelationUrisMap.get(requestMethod);
                linkRelationUris.add(linkRelationUri);

                if (!_ReferenceTemplateMethodToRequestSchemaUrisMap.containsKey(requestMethod)) {
                    _ReferenceTemplateMethodToRequestSchemaUrisMap.put(requestMethod, new LinkedHashSet<URI>());
                }

                final Set<URI> requestSchemaUris = _ReferenceTemplateMethodToRequestSchemaUrisMap.get(requestMethod);

                // The API's reference template may have defined its own API-specific argument type
                final URI referenceRequestSchemaUri = reference.getRequestSchemaUri();
                if (referenceRequestSchemaUri != null) {
                    requestSchemaUris.add(referenceRequestSchemaUri);
                }

                // The reference's link relation may have defined a generic, reusable argument type
                final URI relRequestSchemaUri = rel.getRequestSchemaUri();
                if (relRequestSchemaUri != null && !documentSchemaUriConstant.equals(relRequestSchemaUri)) {
                    requestSchemaUris.add(relRequestSchemaUri);
                }

                if (!_ReferenceTemplateMethodToResponseSchemaUriMap.containsKey(requestMethod)) {
                    _ReferenceTemplateMethodToResponseSchemaUriMap.put(requestMethod, new LinkedHashSet<URI>());
                }

                final Set<URI> responseSchemaUris = _ReferenceTemplateMethodToResponseSchemaUriMap.get(requestMethod);

                // The API's reference template may have defined its own API-specific response type
                final URI referenceResponseSchemaUri = reference.getResponseSchemaUri();
                if (referenceResponseSchemaUri != null) {
                    responseSchemaUris.add(referenceResponseSchemaUri);
                }

                // The reference's link relation may have defined a generic, reusable response type
                final URI relResponseSchemaUri = rel.getResponseSchemaUri();
                if (relResponseSchemaUri != null && !documentSchemaUriConstant.equals(relResponseSchemaUri)) {
                    responseSchemaUris.add(relResponseSchemaUri);
                }

            }

            final UUID referrerId = linkTemplate.getReferrerId();
            if (referrerId != null && referrerId.equals(resourceTemplateId)) {
                _LinkTemplates.put(linkRelationUri, linkTemplate);
            }

        }

    }

    private final String getFullPath(final Resource parentResource) {

        final StringBuffer sb = new StringBuffer();
        boolean appendPathSeparator = true;

        if (parentResource != null && parentResource.getPathText() != null) {
            final String text = parentResource.getPathText();
            sb.append(text);
            if (text.endsWith(ApiNavigator.PATH_SEPARATOR)) {
                appendPathSeparator = false;
            }
        }

        if (appendPathSeparator) {
            sb.append(ApiNavigator.PATH_SEPARATOR);
        }

        final String pathSegment = getPathSegment();

        if (StringUtils.isNotEmpty(pathSegment)) {
            sb.append(pathSegment);
        }

        return sb.toString();
    }

    /**
     * @return a flattened {@link List} of all child and sub-child {@link Resource}s (recursive).
     */
    public List<Resource> getAllChildResources() {

        final List<Resource> allChildResources = new LinkedList<>();
        final List<ResourceTemplate> childResourceTemplates = this._ResourceTemplate.getChildren();
        for (final ResourceTemplate childResourceTemplate : childResourceTemplates) {
            final Resource childResource = this._ApiNavigator.getResource(childResourceTemplate.getUniqueId());
            if (childResource != null) {
                allChildResources.add(childResource);
                allChildResources.addAll(childResource.getAllChildResources());
            }
        }

        return allChildResources;
    }

    /**
     * @return the {@link ApiNavigator} that owns this {@link Resource}.
     */
    public ApiNavigator getApiNavigator() {

        return _ApiNavigator;
    }


    public URI getDefaultDocumentUri() {

        final UriTemplate uriTemplate = getUriTemplate();


        final String[] parameterNames = uriTemplate.getParameterNames();
        final Map<String, Object> parameterMap = new LinkedHashMap<>();

        if (parameterNames != null && parameterNames.length > 0) {

            final Api api = getApiNavigator().getApi();
            final Context context = api.getContext();
            final SchemaLoader schemaLoader = context.getSchemaLoader();

            final URI defaultSchemaUri = getDefaultSchemaUri();
            final Prototype defaultPrototype = (defaultSchemaUri != null) ? schemaLoader.getPrototype(defaultSchemaUri) : null;

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

                    final ConcurrentHashMap<URI, LinkTemplate> referenceTemplates = getReferenceTemplates();

                    if (referenceTemplates != null && !referenceTemplates.isEmpty()) {

                        final Set<URI> referenceLinkRelationUris = getReferenceLinkRelationUris(Method.Get);
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
                }

                Object defaultValue = null;

                if (keyedSchemaUri != null) {

                    final Prototype keyedPrototype = schemaLoader.getPrototype(keyedSchemaUri);
                    final ProtoSlot keyProtoSlot = keyedPrototype.getProtoSlot(parameterName);
                    if (keyProtoSlot instanceof PropertyProtoSlot) {
                        final PropertyProtoSlot keyPropertyProtoSlot = (PropertyProtoSlot) keyProtoSlot;

                        // TODO: Allow more fine grain control of the default parameter value

                        defaultValue = keyPropertyProtoSlot.getDefaultValue();

                        if (defaultValue == null) {
                            defaultValue = keyPropertyProtoSlot.getValueType().getDefaultValue();
                        }

                    }
                }

                parameterMap.put(parameterName, defaultValue);
            }
        }

        return uriTemplate.evaluate(parameterMap, true);
    }

    public URI getDefaultSchemaUri() {

        return getResourceTemplate().getDefaultSchemaUri();
    }

    public URI getDocumentUri(final Document document) {

        final UriTemplate uriTemplate = getUriTemplate();
        final String[] parameterNames = uriTemplate.getParameterNames();
        if (parameterNames == null) {
            return uriTemplate.evaluate(null);
        }
        else {
            final Map<String, Object> parameterMap = new LinkedHashMap<>();
            for (final String parameterName : parameterNames) {
                if (!document.containsSlotValue(parameterName)) {
                    return null;
                }

                Object parameterValue = document.getSlotValue(parameterName);
                if (parameterValue == null) {
                    return null;
                }

                parameterMap.put(parameterName, parameterValue);
            }
            return uriTemplate.evaluate(parameterMap);
        }

    }


    /**
     * A mapping of {@link LinkRelation} id ({@link URI}) to {@link LinkTemplate} model instance. This (conceptual) set
     * of {@link LinkTemplate}s represent the ways in which we may link to (reference) resources.
     *
     * @return A map of link relation id to link template, which communicates the ways that this {@link Resource} may
     * reference other {@link Resource}s.
     */
    public ConcurrentHashMap<URI, LinkTemplate> getLinkTemplates() {

        return _LinkTemplates;
    }

    public ConcurrentHashMap<String, Resource> getLiteralPathSubresources() {

        return _LiteralPathSubresources;
    }

    public Resource getParentResource() {

        return _ParentResource;
    }

    public List<Resource> getPath(final boolean includeDocroot) {

        final List<Resource> path = new ArrayList<>();
        Resource parent = getParentResource();

        if (!includeDocroot && parent == null) {
            return path;
        }

        path.add(this);

        if (parent == null) {
            return path;
        }

        while (parent != null) {
            path.add(parent);
            parent = parent.getParentResource();
        }

        if (!includeDocroot) {
            path.remove(path.size() - 1);
        }

        Collections.reverse(path);

        return path;
    }

    public String getPathSegment() {

        return this._ResourceTemplate.getPathSegment();
    }

    public String getPathText() {

        return _FullPath;
    }

    public String getParentPathText() {

        return _ParentPath;
    }

    public Set<URI> getReferenceLinkRelationUris(final Method requestMethod) {

        return _ReferenceTemplateMethodToLinkRelationUrisMap.get(requestMethod);
    }

    /**
     * A mapping of {@link LinkRelation} id ({@link URI}) to {@link LinkTemplate} model instance. This (conceptual) set
     * of {@link LinkTemplate}s represent the ways in which other resources may link to (reference) us.
     *
     * @return The ways that we may be referenced by other resources.
     */
    public ConcurrentHashMap<URI, LinkTemplate> getReferenceTemplates() {

        return _ReferenceTemplates;
    }

    public Set<Method> getReferenceMethods() {
        return _ReferenceMethods;
    }

    public Set<URI> getRequestSchemaUris(final Method requestMethod) {

        return _ReferenceTemplateMethodToRequestSchemaUrisMap.get(requestMethod);
    }

    public ResourceTemplate getResourceTemplate() {

        return _ResourceTemplate;
    }

    public UUID getResourceTemplateId() {

        if (_ResourceTemplate == null) {
            return null;
        }
        return _ResourceTemplate.getUniqueId();
    }

    public Set<URI> getResponseSchemaUris(final Method requestMethod) {

        return _ReferenceTemplateMethodToResponseSchemaUriMap.get(requestMethod);
    }

    /**
     * Generates the "href" URI used to refer to this resource from the specified referrer {@link Model} instance using
     * the specified {@link LinkRelation} {@link URI} value.
     */
    public URI getHrefUri(final Model referrer, final URI referenceRelationUri) {

        if (referrer == null) {
            return null;
        }

        /*
         * Given the nature of the Api's ResourceTemplate metadata tree, the runtime resource can determine its own
         * UriTemplate (and it only needs to determine/compute this once).
         */

        final UriTemplate uriTemplate = getUriTemplate();
        if (uriTemplate == null) {
            return null;
        }

        /*
         * Get the end point id's templated parameter names, for example: a UriTemplate might have slots that look like
         * {teamId} or {highScoreId} or {name} appearing where legal (according to UriTemplate syntax, see:
         * http://tools.ietf.org/html/rfc6570). A fixed URI, meaning a UriTemplate with no variables, will return null
         * here.
         */
        final String[] uriTemplateParameterNames = this._UriTemplate.getParameterNames();

        Map<String, Object> parameterMap = null;

        if (uriTemplateParameterNames != null && uriTemplateParameterNames.length > 0) {

            // Get the Link slot's bindings, which may be used to provide an alternative source for one or more URI
            // template parameter values.
            final Prototype referrerPrototype = referrer.getPrototype();

            final SortedMap<URI, LinkProtoSlot> linkProtoSlots = referrerPrototype.getLinkProtoSlots();

            Map<String, ProtoValueSource> linkSlotBindings = null;

            if (linkProtoSlots != null && !linkProtoSlots.isEmpty()) {
                final LinkProtoSlot linkProtoSlot = linkProtoSlots.get(referenceRelationUri);
                if (linkProtoSlot != null) {
                    linkSlotBindings = linkProtoSlot.getLinkSlotBindings();
                }
            }

            parameterMap = new LinkedHashMap<>(uriTemplateParameterNames.length);

            for (final String paramName : uriTemplateParameterNames) {

                final Object paramValue;

                if (linkSlotBindings != null && linkSlotBindings.containsKey(paramName)) {
                    // The link slot has declared a binding to an alternate source for this URI template parameter's
                    // value.
                    final ProtoValueSource valueSource = linkSlotBindings.get(paramName);
                    paramValue = valueSource.getValue(referrer);
                }
                else {
                    // By default, if the end point's UriTemplate has parameters (blanks) to fill in, then by convention
                    // we
                    // assume that the referrer model has the corresponding slot values to match the UriTemplate's
                    // inputs/slots.
                    //
                    // Put simply, (by default) referrer model slot names "match" UriTemplate param names.
                    //
                    // This enforces that the model's own represented resource state is the only thing used to
                    // (automatically) drive the link-based graph traversal (aka HATEOAS). Its also a simple convention
                    // that
                    // is (reasonably) easy to comprehend, hopefully even intuitive.

                    paramValue = referrer.getSlotValue(paramName);
                }

                parameterMap.put(paramName, paramValue);
            }
        }

        final URI uri = this._UriTemplate.evaluate(parameterMap);
        return uri;

    }

    public UriTemplate getUriTemplate() {

        return _UriTemplate;
    }

    /**
     * @return a sorted map of variable path child resources (i.e. {keySlotName})
     */
    public ConcurrentHashMap<String, Resource> getVariablePathSubresources() {

        return _VariablePathSubresources;
    }

    public boolean isDocroot() {

        return (getParentResource() == null);
    }

    /**
     * Adds a resource to this resource's list of subresources, differentiating based on its inclusion of the {
     * character whether it's a literal or variable subresource
     *
     * @param subresource
     */
    void addSubresource(final Resource subresource) {

        final String pathSegment = subresource.getPathSegment();
        if (StringUtils.containsAny(pathSegment, '{')) {
            addVariablePathSubresource(pathSegment, subresource);
        }
        else {
            addLiteralPathSubresource(pathSegment, subresource);
        }
    }

    private void addLiteralPathSubresource(final String pathSegment, final Resource subresource) {

        _LiteralPathSubresources.put(pathSegment, subresource);
    }

    private void addVariablePathSubresource(final String pathSegment, final Resource subresource) {

        _VariablePathSubresources.put(pathSegment, subresource);
    }

    @Override
    public String toString() {

        return String.format(TO_STRING_FORMAT, getResourceTemplateId(), _UriTemplate, _FullPath);
    }

    @Override
    public int compareTo(final Resource otherResource) {

        return ComparisonChain.start().compare(this._FullPath, otherResource._FullPath).result();
    }


    public SortedSet<Parameter> getSurrogateKeyComponents(final URI uri, final Prototype prototype) {

        final Set<URI> responseSchemaUris = getResponseSchemaUris(Method.Get);
        if (responseSchemaUris == null) {
            return null;
        }

        boolean isCompatibleResource = false;
        for (final URI responseSchemaUri : responseSchemaUris) {
            if (prototype.isAssignableFrom(responseSchemaUri)) {
                isCompatibleResource = true;
                break;
            }
        }

        if (!isCompatibleResource) {
            return null;
        }

        final UriTemplate uriTemplate = getUriTemplate();
        return uriTemplate.getParameters(uri);

    }

}
