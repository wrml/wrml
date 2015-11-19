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
import org.wrml.model.rest.*;
import org.wrml.runtime.Context;
import org.wrml.runtime.Keys;
import org.wrml.runtime.schema.LinkProtoSlot;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;

import java.net.URI;
import java.util.*;

/**
 * A helper utility to build Api models.
 */
public final class ApiBuilder {

    private final Context _Context;

    private final Api _Api;

    public ApiBuilder(final Context context) {

        this((Api) context.newModel(Api.class));
    }

    public ApiBuilder(final Api api) {

        _Context = api.getContext();
        _Api = api;

    }

    public ApiBuilder uri(final URI uri) {

        _Api.setUri(uri);
        return this;
    }

    public ApiBuilder description(final String description) {

        _Api.setDescription(description);
        return this;
    }

    public ApiBuilder title(final String title) {

        _Api.setTitle(title);
        return this;
    }

    public ApiNavigator navigate() {

        return (ApiNavigator.isApiNavigable(_Api)) ? new ApiNavigator(_Api) : null;
    }

    @Override
    public String toString() {

        final ApiNavigator navigator = navigate();
        if (navigator != null) {
            return navigator.toString();
        }

        return _Api.toString();
    }

    public Api toApi() {

        return _Api;
    }

    public Context getContext() {

        return _Context;
    }

    public ApiNavigator load() {

        final ApiLoader apiLoader = _Context.getApiLoader();
        return apiLoader.loadApi(_Api);
    }

    public ApiBuilder resource(final String fullPath) {

        return resource(fullPath, (UUID) null);
    }

    public ApiBuilder resource(final String fullPath, final UUID resourceTemplateId) {

        return resource(fullPath, resourceTemplateId, (URI) null);
    }

    public ApiBuilder resource(final String fullPath, final UUID resourceTemplateId, final Class<?> defaultSchemaInterface) {

        return resource(fullPath, resourceTemplateId, defaultSchemaInterface, false);
    }

    public ApiBuilder resource(final String fullPath, final Class<?> defaultSchemaInterface) {

        return resource(fullPath, null, defaultSchemaInterface);
    }

    public ApiBuilder resource(final String fullPath, final UUID resourceTemplateId, final Class<?> defaultSchemaInterface, final boolean addDefaultLinks) {

        final URI defaultSchemaUri = (defaultSchemaInterface != null) ? getContext().getSchemaLoader().getTypeUri(defaultSchemaInterface) : null;
        return resource(fullPath, resourceTemplateId, defaultSchemaUri, addDefaultLinks);
    }

    public ApiBuilder resource(final String fullPath, final Class<?> defaultSchemaInterface, final boolean addDefaultLinks) {

        return resource(fullPath, null, defaultSchemaInterface, addDefaultLinks);
    }

    public ApiBuilder resource(final String fullPath, final UUID resourceTemplateId, final URI defaultSchemaUri) {

        return resource(fullPath, resourceTemplateId, defaultSchemaUri, false);
    }

    public ApiBuilder resource(final String fullPath, final URI defaultSchemaUri) {

        return resource(fullPath, null, defaultSchemaUri);
    }

    public ApiBuilder resource(final String fullPath, final UUID resourceTemplateId, final URI defaultSchemaUri, final boolean addDefaultLinks) {

        final Context context = getContext();

        if (fullPath == null) {
            throw new IllegalArgumentException("The resource path cannot be null.");
        }

        String path = fullPath.trim();
        if (fullPath.isEmpty()) {
            path = UriTemplate.PATH_SEPARATOR;
        }

        if (!path.startsWith(UriTemplate.PATH_SEPARATOR)) {
            path = UriTemplate.PATH_SEPARATOR + path;
        }

        final UUID resourceId = (resourceTemplateId != null) ? resourceTemplateId : UUID.randomUUID();

        ResourceTemplate docroot = _Api.getDocroot();
        if (docroot == null) {
            docroot = context.newModel(ResourceTemplate.class);
            docroot.setPathSegment("");
            docroot.setUniqueId(UUID.randomUUID());
            _Api.setDocroot(docroot);

            if (path.equals(UriTemplate.PATH_SEPARATOR)) {
                docroot.setUniqueId(resourceId);
                if (defaultSchemaUri != null) {
                    docroot.setDefaultSchemaUri(defaultSchemaUri);
                    if (addDefaultLinks) {
                        addDefaultSchemaLinkTemplates(docroot);
                    }
                }

                return this;
            }
        }

        ResourceTemplate parent = docroot;

        final String[] pathSegments = StringUtils.split(path, UriTemplate.PATH_SEPARATOR_CHAR);
        for (int i = 0; i < pathSegments.length; i++) {
            final String pathSegment = pathSegments[i];
            final boolean isLastSegment = (i == (pathSegments.length - 1));

            ResourceTemplate segmentTemplate = null;
            final List<ResourceTemplate> children = parent.getChildren();

            for (final ResourceTemplate resourceTemplate : children) {
                if (pathSegment.equals(resourceTemplate.getPathSegment())) {
                    segmentTemplate = resourceTemplate;
                    break;
                }
            }

            if (segmentTemplate == null) {
                segmentTemplate = context.newModel(ResourceTemplate.class);
                segmentTemplate.setPathSegment(pathSegment);
                segmentTemplate.setUniqueId(UUID.randomUUID());
                children.add(segmentTemplate);

                if (isLastSegment) {
                    segmentTemplate.setUniqueId(resourceId);
                    if (defaultSchemaUri != null) {
                        segmentTemplate.setDefaultSchemaUri(defaultSchemaUri);
                        if (addDefaultLinks) {
                            addDefaultSchemaLinkTemplates(segmentTemplate);
                        }
                    }
                }
            }

            parent = segmentTemplate;
        }

        return this;
    }

    public ApiBuilder resource(final String fullPath, final URI defaultSchemaUri, final boolean addDefaultLinks) {

        return resource(fullPath, UUID.randomUUID(), defaultSchemaUri, addDefaultLinks);
    }

    public ApiBuilder link(final String referrerFullPath, final URI linkRelationUri, final String endpointFullPath, final Class<?> responseSchemaInterface) {

        return link(referrerFullPath, linkRelationUri, endpointFullPath, responseSchemaInterface, null);
    }

    public ApiBuilder link(final String referrerFullPath, final URI linkRelationUri, final String endpointFullPath, final Class<?> responseSchemaInterface, final Class<?> requestSchemaInterface) {

        final URI responseSchemaUri = (responseSchemaInterface != null) ? getContext().getSchemaLoader().getTypeUri(responseSchemaInterface) : null;
        final URI requestSchemaUri = (requestSchemaInterface != null) ? getContext().getSchemaLoader().getTypeUri(requestSchemaInterface) : null;
        return link(referrerFullPath, linkRelationUri, endpointFullPath, responseSchemaUri, requestSchemaUri);
    }

    public ApiBuilder link(final String referrerFullPath, final URI linkRelationUri, final String endpointFullPath, final URI responseSchemaUri) {

        return link(referrerFullPath, linkRelationUri, endpointFullPath, responseSchemaUri, null);
    }

    public ApiBuilder link(final String referrerFullPath, final URI linkRelationUri, final String endpointFullPath, final URI responseSchemaUri, final URI requestSchemaUri) {

        if (referrerFullPath == null) {
            throw new IllegalArgumentException("The referrer full path cannot be null.");
        }

        if (endpointFullPath == null) {
            throw new IllegalArgumentException("The enpdoint full path cannot be null.");
        }

        final Context context = getContext();

        final UUID referrerResourceTemplateId = getResourceTemplateId(referrerFullPath);
        if (referrerResourceTemplateId == null) {
            throw new IllegalArgumentException("The resource template was not found for: " + referrerFullPath);
        }

        final UUID endpointResourceTemplateId = getResourceTemplateId(endpointFullPath);
        if (endpointResourceTemplateId == null) {
            throw new IllegalArgumentException("The resource template was not found for: " + endpointFullPath);
        }

        link(referrerResourceTemplateId, linkRelationUri, endpointResourceTemplateId, responseSchemaUri, requestSchemaUri);

        return this;
    }

    public ApiBuilder autoLink() {

        final ApiNavigator apiNavigator = navigate();
        final Resource docroot = apiNavigator.getDocroot();

        autoLink(docroot);

        return this;
    }

    public ApiBuilder docroot(final UUID docrootResourceTemplateId) {
        final SchemaLoader schemaLoader = getContext().getSchemaLoader();
        return resource("/", docrootResourceTemplateId, schemaLoader.getApiSchemaUri(), true);
    }


    private void addDefaultSchemaLinkTemplates(final ResourceTemplate resourceTemplate) {

        final URI defaultSchemaUri = resourceTemplate.getDefaultSchemaUri();
        if (defaultSchemaUri == null) {
            return;
        }

        final Context context = getContext();
        final ApiLoader apiLoader = context.getApiLoader();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final URI documentSchemaUriConstant = schemaLoader.getDocumentSchemaUri();
        final Prototype prototype = schemaLoader.getPrototype(defaultSchemaUri);
        final UUID resourceTemplateId = resourceTemplate.getUniqueId();
        final SortedMap<String, URI> schemaLinkRelationUris = prototype.getLinkRelationUris();

        final List<LinkTemplate> defaultLinkTemplates = new ArrayList<>(schemaLinkRelationUris.size());

        for (final URI linkRelationUri : schemaLinkRelationUris.values()) {
            final LinkTemplate linkTemplate = context.newModel(LinkTemplate.class);

            linkTemplate.setReferrerId(resourceTemplateId);
            linkTemplate.setLinkRelationUri(linkRelationUri);

            final Keys linkRelationKeys = apiLoader.buildDocumentKeys(linkRelationUri, schemaLoader.getLinkRelationSchemaUri());
            final LinkRelation linkRelation = context.getModel(linkRelationKeys, schemaLoader.getLinkRelationDimensions());

            if (linkRelation == null) {
                throw new NullPointerException("The link relation: " + linkRelationUri + " was not found");
            }

            final Method method = linkRelation.getMethod();
            if (method == Method.Save) {
                final URI linkRelationRequestSchemaUri = linkRelation.getRequestSchemaUri();
                if (linkRelationRequestSchemaUri == null || linkRelationRequestSchemaUri.equals(defaultSchemaUri) || linkRelationRequestSchemaUri.equals(documentSchemaUriConstant)) {
                    linkTemplate.setRequestSchemaUri(defaultSchemaUri);
                    linkTemplate.setEndPointId(resourceTemplateId);
                }
                else {
                    linkTemplate.setRequestSchemaUri(linkRelationRequestSchemaUri);
                }

            }

            if (method == Method.Get || method == Method.Save) {
                final URI linkRelationResponseSchemaUri = linkRelation.getResponseSchemaUri();
                if (linkRelationResponseSchemaUri == null || linkRelationResponseSchemaUri.equals(defaultSchemaUri) || linkRelationResponseSchemaUri.equals(documentSchemaUriConstant)) {
                    linkTemplate.setResponseSchemaUri(defaultSchemaUri);
                    linkTemplate.setEndPointId(resourceTemplateId);
                }
                else {
                    linkTemplate.setResponseSchemaUri(linkRelationResponseSchemaUri);
                }
            }

            if (!method.isEntityAllowedInRequestMessage() && !method.isEntityAllowedInResponseMessage()) {
                linkTemplate.setEndPointId(resourceTemplateId);
            }

            defaultLinkTemplates.add(linkTemplate);
        }

        _Api.getLinkTemplates().addAll(defaultLinkTemplates);

    }

    private UUID getResourceTemplateId(final String fullPath) {

        if (fullPath == null) {
            throw new IllegalArgumentException("The resource path cannot be null.");
        }

        String path = fullPath.trim();
        if (fullPath.isEmpty()) {
            path = UriTemplate.PATH_SEPARATOR;
        }

        if (!path.startsWith(UriTemplate.PATH_SEPARATOR)) {
            path = UriTemplate.PATH_SEPARATOR + path;
        }

        final ResourceTemplate docroot = _Api.getDocroot();
        if (docroot == null) {
            return null;
        }

        if (path.equals(UriTemplate.PATH_SEPARATOR)) {
            return docroot.getUniqueId();
        }

        ResourceTemplate parent = docroot;

        final String[] pathSegments = StringUtils.split(path, UriTemplate.PATH_SEPARATOR_CHAR);
        for (int i = 0; i < pathSegments.length; i++) {
            final String pathSegment = pathSegments[i];
            final boolean isLastSegment = (i == (pathSegments.length - 1));

            ResourceTemplate segmentTemplate = null;
            final List<ResourceTemplate> children = parent.getChildren();

            for (final ResourceTemplate resourceTemplate : children) {
                if (pathSegment.equals(resourceTemplate.getPathSegment())) {
                    segmentTemplate = resourceTemplate;
                    break;
                }
            }

            if (segmentTemplate == null) {
                return null;
            }

            if (isLastSegment) {
                return segmentTemplate.getUniqueId();
            }

            parent = segmentTemplate;
        }

        return null;
    }

    private LinkTemplate getLinkTemplate(final String referrerFullPath, final URI linkRelationUri, final String endpointFullPath) {

        if (referrerFullPath == null) {
            throw new IllegalArgumentException("The referrer full path cannot be null.");
        }

        if (endpointFullPath == null) {
            throw new IllegalArgumentException("The enpdoint full path cannot be null.");
        }

        final UUID referrerResourceTemplateId = getResourceTemplateId(referrerFullPath);
        if (referrerResourceTemplateId == null) {
            throw new IllegalArgumentException("The resource template was not found for: " + referrerFullPath);
        }

        final UUID endpointResourceTemplateId = getResourceTemplateId(endpointFullPath);
        if (endpointResourceTemplateId == null) {
            throw new IllegalArgumentException("The resource template was not found for: " + endpointFullPath);
        }

        final List<LinkTemplate> linkTemplates = _Api.getLinkTemplates();
        for (final LinkTemplate linkTemplate : linkTemplates) {
            if (linkTemplate.getReferrerId().equals(referrerResourceTemplateId) &&
                    linkTemplate.getLinkRelationUri().equals(linkRelationUri) &&
                    linkTemplate.getEndPointId().equals(endpointResourceTemplateId)) {
                return linkTemplate;
            }
        }

        return null;
    }

    private void link(final UUID referrerResourceTemplateId, final URI linkRelationUri, final UUID endpointResourceTemplateId, final URI responseSchemaUri, final URI requestSchemaUri) {

        final Context context = getContext();
        final ApiLoader apiLoader = context.getApiLoader();
        final LinkTemplate linkTemplate = context.newModel(LinkTemplate.class);
        linkTemplate.setReferrerId(referrerResourceTemplateId);
        linkTemplate.setEndPointId(endpointResourceTemplateId);
        linkTemplate.setLinkRelationUri(linkRelationUri);

        final LinkRelation linkRelation = apiLoader.loadLinkRelation(linkRelationUri);

        final Method method = linkRelation.getMethod();
        if (method.isEntityAllowedInResponseMessage() && responseSchemaUri != null) {
            linkTemplate.setResponseSchemaUri(responseSchemaUri);
        }

        if (method.isEntityAllowedInRequestMessage() && requestSchemaUri != null) {
            linkTemplate.setRequestSchemaUri(requestSchemaUri);
        }

        _Api.getLinkTemplates().add(linkTemplate);
    }

    private void autoLink(final Resource resource) {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final URI defaultSchemaUri = resource.getDefaultSchemaUri();
        if (defaultSchemaUri != null) {
            final Prototype defaultPrototype = schemaLoader.getPrototype(defaultSchemaUri);
            autoLink(resource, defaultPrototype);
        }

        final Set<URI> responseSchemaUris = resource.getResponseSchemaUris(Method.Get);
        if (responseSchemaUris != null) {
            for (final URI responseSchemaUri : responseSchemaUris) {
                final Prototype responsePrototype = schemaLoader.getPrototype(responseSchemaUri);
                autoLink(resource, responsePrototype);
            }

        }

        final List<Resource> allChildResources = resource.getAllChildResources();
        for (final Resource subresource : allChildResources) {
            autoLink(subresource);
        }

    }

    private void autoLink(final Resource referrerResource, final Prototype prototype) {

        final Context context = getContext();
        final ApiLoader apiLoader = context.getApiLoader();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Collection<LinkProtoSlot> linkProtoSlots = prototype.getLinkProtoSlots().values();
        for (final LinkProtoSlot linkProtoSlot : linkProtoSlots) {

            URI linkResponseSchemaUri = linkProtoSlot.getResponseSchemaUri();
            if (linkResponseSchemaUri == null) {
                continue;
            }

            final URI requestSchemaUri = linkProtoSlot.getRequestSchemaUri();

            final URI linkRelationUri = linkProtoSlot.getLinkRelationUri();
            final LinkRelation linkRelation = apiLoader.loadLinkRelation(linkRelationUri);

            final UUID referrerResourceTemplateId = referrerResource.getResourceTemplateId();

            final Resource endpointResource = findSuitableLinkEndpoint(referrerResource, linkRelation, linkResponseSchemaUri);
            if (endpointResource != null) {
                final UUID endpointResourceTemplateId = endpointResource.getResourceTemplateId();

                if (linkResponseSchemaUri.equals(schemaLoader.getDocumentSchemaUri())) {
                    final URI defaultSchemaUri = endpointResource.getDefaultSchemaUri();
                    if (defaultSchemaUri != null) {
                        linkResponseSchemaUri = defaultSchemaUri;
                    }

                }

                link(referrerResourceTemplateId, linkRelationUri, endpointResourceTemplateId, linkResponseSchemaUri, requestSchemaUri);
            }
        }
    }

    private Resource findSuitableLinkEndpoint(final Resource referrerResource, final LinkRelation linkRelation, final URI linkResponseSchemaUri) {

        if (isSuitableLinkEndpoint(referrerResource, linkRelation, referrerResource, linkResponseSchemaUri)) {
            return referrerResource;
        }

        final ApiNavigator apiNavigator = referrerResource.getApiNavigator();
        final Map<UUID, Resource> allResourceMap = apiNavigator.getAllResources();

        final SortedSet<Resource> resourceSet = new TreeSet<>(allResourceMap.values());
        for (final Resource resource : resourceSet) {
            if (resource == referrerResource) {
                continue;
            }

            if (isSuitableLinkEndpoint(referrerResource, linkRelation, resource, linkResponseSchemaUri)) {
                return resource;
            }
        }

        return null;
    }

    private boolean isSuitableLinkEndpoint(final Resource referrerResource, final LinkRelation linkRelation, final Resource endpointResource, final URI linkResponseSchemaUri) {

        if (referrerResource == null || linkRelation == null || endpointResource == null || linkResponseSchemaUri == null) {
            return false;
        }

        if (linkRelation.getUri().equals(SystemLinkRelation.self.getUri()) && (referrerResource == endpointResource)) {
            return true;
        }


        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();


        final URI defaultSchemaUri = endpointResource.getDefaultSchemaUri();
        if (defaultSchemaUri != null) {
            if (defaultSchemaUri.equals(linkResponseSchemaUri)) {
                return true;
            }

            final Prototype defaultSchemaPrototype = schemaLoader.getPrototype(defaultSchemaUri);
            if (defaultSchemaPrototype.isAssignableFrom(linkResponseSchemaUri)) {
                return true;
            }

        }


        final Method method = linkRelation.getMethod();
        final Set<URI> responseSchemaUris = endpointResource.getResponseSchemaUris(method);
        if (responseSchemaUris != null) {
            if (responseSchemaUris.contains(linkResponseSchemaUri)) {
                return true;
            }

            if (responseSchemaUris.size() > 0) {

                for (final URI responseSchemaUri : responseSchemaUris) {
                    final Prototype responseSchemaPrototype = schemaLoader.getPrototype(responseSchemaUri);
                    if (responseSchemaPrototype.isAssignableFrom(linkResponseSchemaUri)) {
                        return true;
                    }
                }

            }
        }

        return false;
    }

}