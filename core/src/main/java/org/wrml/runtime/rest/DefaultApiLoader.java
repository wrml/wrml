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
import org.apache.http.client.utils.URIUtils;
import org.wrml.model.Model;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.rest.Method;
import org.wrml.runtime.*;
import org.wrml.runtime.schema.ProtoSlot;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.runtime.syntax.SyntaxLoader;
import org.wrml.util.AsciiArt;
import org.wrml.util.UniqueName;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultApiLoader implements ApiLoader
{

    private static final String SYSTEM_API_DOCROOT_FULL_PATH = "/";

    private static final String SYSTEM_API_PRIMARY_ENDPOINT_FULL_PATH = SYSTEM_API_DOCROOT_FULL_PATH + "{uniqueName}";

    private final ConcurrentHashMap<URI, Api> _Apis;

    private final ConcurrentHashMap<URI, LinkRelation> _LinkRelations;

    private final ConcurrentHashMap<URI, ApiNavigator> _SystemApiNavigators;

    private final ConcurrentHashMap<URI, ApiNavigator> _ApiNavigators;

    private Context _Context;

    public DefaultApiLoader()
    {

        _Apis = new ConcurrentHashMap<URI, Api>();
        _LinkRelations = new ConcurrentHashMap<URI, LinkRelation>();

        _SystemApiNavigators = new ConcurrentHashMap<URI, ApiNavigator>();
        _ApiNavigators = new ConcurrentHashMap<URI, ApiNavigator>();
    }

    @Override
    public Dimensions buildDocumentDimensions(final Method method, final URI uri, final DimensionsBuilder dimensionsBuilder)
    {

        if (method == null)
        {
            throw new NullPointerException("The request method cannot be null.");
        }

        if (uri == null)
        {
            throw new NullPointerException("The request method cannot be null.");
        }

        URI schemaUri = dimensionsBuilder.getSchemaUri();

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        ApiNavigator apiNavigator = null;
        if (!schemaLoader.getApiSchemaUri().equals(schemaUri))
        {
            apiNavigator = getParentApiNavigator(uri);
        }

        if (apiNavigator != null)
        {
            final Resource resource = apiNavigator.getResource(uri);
            // Is the method allowed?
            final Set<URI> schemaUris = resource.getResponseSchemaUris(method);

            final URI documentSchemaUriConstant = schemaLoader.getDocumentSchemaUri();
            final URI modelSchemaUriConstant = schemaLoader.getTypeUri(Model.class);

            if (schemaUris != null)
            {

                if (schemaUri != null && schemaUris.isEmpty())
                {
                    // error, method not supported
                    throw new ApiLoaderException("The method " + "[" + method + "]" + " is not supported by the api.", null, this);
                }
                else if (!schemaUris.isEmpty() && (schemaUri == null || schemaUri.equals(documentSchemaUriConstant) || schemaUri.equals(modelSchemaUriConstant)))
                {
                    schemaUri = schemaUris.iterator().next();
                }

                if (schemaUri != null && !schemaUris.contains(schemaUri) && !schemaUri.equals(documentSchemaUriConstant) && !schemaUri.equals(modelSchemaUriConstant))
                {
                    // Error, unsupported schema id
                    throw new ApiLoaderException("The schema " + "[" + schemaUri + "]" + " is not supported by the api.", null, this);
                }
            }

            if (schemaUri == null || schemaUri.equals(documentSchemaUriConstant) || schemaUri.equals(modelSchemaUriConstant))
            {
                schemaUri = resource.getDefaultSchemaUri();
            }
        }

        dimensionsBuilder.setSchemaUri(schemaUri);

        final String queryPart = uri.getQuery();
        if (StringUtils.isNotEmpty(queryPart))
        {
            final Map<String, String> queryParameters = dimensionsBuilder.getQueryParameters();
            if (queryParameters.isEmpty())
            {
                final String[] queryParams = queryPart.split("&");
                for (String queryParam : queryParams)
                {
                    final String[] queryParamNameValuePair = queryParam.split("=");
                    final String queryParamName = queryParamNameValuePair[0];
                    final String queryParamValue = queryParamNameValuePair[1];
                    queryParameters.put(queryParamName, queryParamValue);
                }
            }
        }

        return dimensionsBuilder.toDimensions();
    }

    @Override
    public final Keys buildDocumentKeys(final URI uri, final URI schemaUri)
    {

        if (uri == null)
        {
            return null;
        }

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final KeysBuilder keysBuilder = new KeysBuilder(schemaLoader.getDocumentSchemaUri(), uri);

        if (!schemaLoader.getApiSchemaUri().equals(schemaUri))
        {

            final Prototype prototype = schemaLoader.getPrototype(schemaUri);
            if (prototype != null)
            {
                final Object documentSurrogateKeyValue = decipherDocumentSurrogateKeyValue(uri, prototype);
                if (documentSurrogateKeyValue != null)
                {
                    keysBuilder.addKey(prototype.getSchemaUri(), documentSurrogateKeyValue);
                }
            }
        }

        return keysBuilder.toKeys();
    }

    @Override
    public ApiLoaderConfiguration getConfig()
    {

        return getContext().getConfig().getApiLoader();
    }

    @Override
    public final Context getContext()
    {

        return _Context;
    }

    @Override
    public final URI getDefaultResponseSchemaUri(final Method requestMethod, final URI uri)
    {

        final ApiNavigator apiNavigator = getParentApiNavigator(uri);

        if (apiNavigator == null)
        {
            return null;
        }

        try
        {
            return apiNavigator.getDefaultResponseSchemaUri(requestMethod, uri);
        }
        catch (ApiNavigatorException e)
        {
            throw new ApiLoaderException(e.getMessage(), e, this);
        }
    }

    @Override
    public Api getLoadedApi(final Keys keys)
    {

        final SchemaLoader schemaLoader = getContext().getSchemaLoader();
        final URI uri = (URI) keys.getValue(schemaLoader.getDocumentSchemaUri());
        if (uri != null && _Apis.containsKey(uri))
        {
            return _Apis.get(uri);
        }

        return null;
    }

    @Override
    public final ApiNavigator getLoadedApiNavigator(final URI apiUri)
    {

        if (_SystemApiNavigators.containsKey(apiUri))
        {
            return _SystemApiNavigators.get(apiUri);
        }

        return _ApiNavigators.get(apiUri);
    }

    @Override
    public Set<Api> getLoadedApis()
    {

        return new LinkedHashSet<>(_Apis.values());
    }

    @Override
    public final SortedSet<URI> getLoadedApiUris()
    {

        return new TreeSet<>(_Apis.keySet());
    }

    @Override
    public LinkRelation getLoadedLinkRelation(final Keys keys)
    {

        final SchemaLoader schemaLoader = getContext().getSchemaLoader();
        final URI uri = (URI) keys.getValue(schemaLoader.getDocumentSchemaUri());
        if (uri == null)
        {
            return null;
        }

        if (_LinkRelations.containsKey(uri))
        {
            return _LinkRelations.get(uri);
        }
        return null;
    }

    @Override
    public Set<LinkRelation> getLoadedLinkRelations()
    {

        return new LinkedHashSet<>(_LinkRelations.values());
    }

    @Override
    public SortedSet<URI> getLoadedLinkRelationUris()
    {

        return new TreeSet<>(_LinkRelations.keySet());
    }

    @Override
    public final ApiNavigator getParentApiNavigator(final URI uri)
    {

        // NOTE: This method needs to be as speedy as possible as it is called with every request as part of the "routing" process

        if (uri == null)
        {
            throw new NullPointerException("The uri is null; cannot locate the parent REST API.");
        }

        // TODO: Change this code to allow for REST API's with arbitrary paths (WRML-276)
        URI parentApiUri = URIUtils.resolve(uri, ApiNavigator.DOCROOT_PATH).normalize();
        final String uriString = parentApiUri.toString();
        if (uriString.endsWith("/"))
        {
            parentApiUri = URI.create(uriString.substring(0, uriString.length() - 1));
        }

        return getLoadedApiNavigator(parentApiUri);
    }

    @Override
    public void init(final Context context)
    {

        if (context == null)
        {
            throw new ApiLoaderException("The WRML context cannot be null.", null, this);
        }

        _Context = context;

        loadSystemLinkRelations();
        loadSystemApis();
    }

    @Override
    public void loadInitialState()
    {

        loadConfiguredApis();
    }

    @Override
    public ApiNavigator loadApi(final Api api) throws ApiLoaderException
    {

        final URI apiUri = api.getUri();
        if (apiUri == null)
        {
            throw new ApiLoaderException("The API's URI cannot be null.", null, this);
        }

        if (_SystemApiNavigators.containsKey(apiUri))
        {
            // The API's URI matches a pre-loaded System API.
            return _SystemApiNavigators.get(apiUri);
        }

        _Apis.put(apiUri, api);

        final ApiNavigator apiNavigator = new ApiNavigator(api);
        _ApiNavigators.put(apiUri, apiNavigator);
        return apiNavigator;
    }

    @Override
    public ApiNavigator loadApi(final URI apiUri)
    {

        if (apiUri == null)
        {
            return null;
        }

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Keys keys = buildDocumentKeys(apiUri, schemaLoader.getApiSchemaUri());
        final Dimensions dimensions = schemaLoader.getApiDimensions();

        final Api api = context.getModel(keys, dimensions);
        if (api == null)
        {
            throw new ApiLoaderException("The API associated with Keys:\n" + keys + "\n... and Dimensions:\n" + dimensions + " could not be loaded.", null, this);
        }

        return loadApi(api);
    }

    @Override
    public void loadLinkRelation(final LinkRelation linkRelation) throws ApiLoaderException
    {

        final URI linkRelationUri = linkRelation.getUri();
        if (linkRelationUri == null)
        {
            throw new ApiLoaderException("The Link Relation's URI cannot be null.", null, this);
        }

        if (StringUtils.isEmpty(linkRelation.getTitle()))
        {

            final String title = linkRelation.getUniqueName().getLocalName();
            linkRelation.setTitle(title);
        }

        _LinkRelations.put(linkRelationUri, linkRelation);
    }

    @Override
    public LinkRelation loadLinkRelation(final URI linkRelationUri)
    {

        if (linkRelationUri == null)
        {
            return null;
        }

        // LinkRelations are not re-loadable
        if (_LinkRelations.containsKey(linkRelationUri))
        {
            return _LinkRelations.get(linkRelationUri);
        }

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Keys keys = buildDocumentKeys(linkRelationUri, schemaLoader.getLinkRelationSchemaUri());
        final Dimensions dimensions = schemaLoader.getLinkRelationDimensions();

        final LinkRelation linkRelation = context.getModel(keys, dimensions);
        if (linkRelation == null)
        {
            throw new ApiLoaderException("The LinkRelation associated with Keys:\n" + keys + "\n... and Dimensions:\n" + dimensions + " could not be loaded.", null, this);
        }

        loadLinkRelation(linkRelation);
        return linkRelation;
    }

    @Override
    public String toString()
    {

        String result = AsciiArt.express(this);
        /*if (StringUtils.isEmpty(result))
        {
            result = com.google.common.base.Objects.toStringHelper(this) //
                    // .add("_Context", _Context) // infinite recursion SOE if included
                    .add("_Apis", _Apis) //
                    .add("_ApiNavigators", _ApiNavigators) //
                    .add("_LinkRelations", _LinkRelations) //
                    .add("_SystemApiNavigators", _SystemApiNavigators) //
                    .toString();
        } */
        return result;
    }

    protected Object decipherDocumentSurrogateKeyValue(final URI uri, final Prototype prototype)
    {

        final ApiNavigator apiNavigator = getParentApiNavigator(uri);

        if (apiNavigator == null)
        {
            return null;
        }

        final SortedSet<Parameter> surrogateKeyComponents = apiNavigator.getSurrogateKeyComponents(uri, prototype);
        if (surrogateKeyComponents == null || surrogateKeyComponents.isEmpty())
        {
            return null;
        }

        final Set<String> allKeySlotNames = prototype.getAllKeySlotNames();

        final Object surrogateKeyValue;
        if (surrogateKeyComponents.size() == 1)
        {
            final Parameter surrogateKeyPair = surrogateKeyComponents.first();

            final String slotName = surrogateKeyPair.getName();
            if (!allKeySlotNames.contains(slotName))
            {
                return null;
            }

            final String slotTextValue = surrogateKeyPair.getValue();
            final Object slotValue = parseSlotValueSyntacticText(prototype, slotName, slotTextValue);

            surrogateKeyValue = slotValue;
        }
        else
        {

            final SortedMap<String, Object> keySlots = new TreeMap<String, Object>();

            for (final Parameter surrogateKeyPair : surrogateKeyComponents)
            {

                final String slotName = surrogateKeyPair.getName();
                if (!allKeySlotNames.contains(slotName))
                {
                    continue;
                }

                final String slotTextValue = surrogateKeyPair.getValue();
                final Object slotValue = parseSlotValueSyntacticText(prototype, slotName, slotTextValue);

                if (slotValue == null)
                {
                    continue;
                }

                keySlots.put(slotName, slotValue);

            }

            if (keySlots.size() == 1)
            {
                surrogateKeyValue = keySlots.get(keySlots.firstKey());
            }
            else
            {
                surrogateKeyValue = new CompositeKey(keySlots);
            }
        }

        return surrogateKeyValue;

    }

    protected final Dimensions getApiDimensions()
    {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        return schemaLoader.getApiDimensions();
    }

    protected final Dimensions getLinkRelationDimensions()
    {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        return schemaLoader.getLinkRelationDimensions();
    }

    private void loadConfiguredApis()
    {

        final ApiLoaderConfiguration config = getConfig();
        if (config != null)
        {
            final URI[] apiUriArray = config.getApis();
            if (apiUriArray != null && apiUriArray.length > 0)
            {
                for (final URI apiUri : apiUriArray)
                {
                    loadApi(apiUri);
                }
            }
        }
    }

    private void loadSystemApis()
    {

        final Context context = getContext();

        for (final SystemApi systemApi : SystemApi.values())
        {

            final ApiBuilder apiBuilder = new ApiBuilder(context);

            apiBuilder.uri(systemApi.getUri()).title(systemApi.getTitle()).description(systemApi.getDescription());

            apiBuilder.resource(SYSTEM_API_DOCROOT_FULL_PATH, systemApi.getDocrootId());
            apiBuilder.resource(SYSTEM_API_PRIMARY_ENDPOINT_FULL_PATH, systemApi.getPrimaryEndpointId(), systemApi.getDefaultSchemaInterface(), true);

            // TODO: Rework the SchemaNamespace
            /*
             * if (systemApi == SystemApi.Schema) { apiBuilder.link(SYSTEM_API_DOCROOT_FULL_PATH, SystemLinkRelation.self.getUri(), SYSTEM_API_DOCROOT_FULL_PATH,
             * SchemaNamespace.class); apiBuilder.link(SYSTEM_API_DOCROOT_FULL_PATH, SystemLinkRelation.element.getUri(), SYSTEM_API_PRIMARY_ENDPOINT_FULL_PATH, Schema.class);
             * apiBuilder.link(SYSTEM_API_DOCROOT_FULL_PATH, SystemLinkRelation.child.getUri(), SYSTEM_API_PRIMARY_ENDPOINT_FULL_PATH, SchemaNamespace.class);
             * apiBuilder.link(SYSTEM_API_PRIMARY_ENDPOINT_FULL_PATH, SystemLinkRelation.element.getUri(), SYSTEM_API_PRIMARY_ENDPOINT_FULL_PATH, Schema.class);
             * apiBuilder.link(SYSTEM_API_PRIMARY_ENDPOINT_FULL_PATH, SystemLinkRelation.child.getUri(), SYSTEM_API_PRIMARY_ENDPOINT_FULL_PATH, SchemaNamespace.class); }
             */

            final Api api = apiBuilder.toApi();
            final URI apiUri = api.getUri();

            _Apis.put(apiUri, api);

            final ApiNavigator systemApiNavigator = new ApiNavigator(api);
            _SystemApiNavigators.put(apiUri, systemApiNavigator);
        }
    }

    private void loadSystemLinkRelations()
    {

        final Context context = getContext();

        // NOTE: The SchemaLoader is not loaded at this point so we need to get the Document schema's URI the hard(coded) way.
        final String documentSchemaPath = "/" + Document.class.getName().replace('.', '/');
        final URI documentSchemaUri = SystemApi.Schema.getUri().resolve(documentSchemaPath);

        for (final SystemLinkRelation systemLinkRelation : SystemLinkRelation.values())
        {
            final LinkRelation linkRelation = context.newModel(LinkRelation.class);

            final UniqueName uniqueName = systemLinkRelation.getUniqueName();
            linkRelation.setUniqueName(uniqueName);
            linkRelation.setMethod(systemLinkRelation.getMethod());
            linkRelation.setUri(systemLinkRelation.getUri());
            linkRelation.setTitle(uniqueName.getLocalName());

            // TODO: Fix the system API designs so that this doesn't break them.
            /*
            if (systemLinkRelation == SystemLinkRelation.self)
            {
                linkRelation.setResponseSchemaUri(documentSchemaUri);
            }
            else if (systemLinkRelation == SystemLinkRelation.save)
            {
                linkRelation.setResponseSchemaUri(documentSchemaUri);
                linkRelation.setRequestSchemaUri(documentSchemaUri);
            }
            */

            loadLinkRelation(linkRelation);
        }
    }

    private Object parseSlotValueSyntacticText(final Prototype prototype, final String slotName, final String slotTextValue)
    {

        final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);
        if (protoSlot == null)
        {
            return null;
        }

        final Type slotType = protoSlot.getHeapValueType();

        final SyntaxLoader syntaxLoader = getContext().getSyntaxLoader();

        final Object slotValue = syntaxLoader.parseSyntacticText(slotTextValue, slotType);
        return slotValue;
    }

}