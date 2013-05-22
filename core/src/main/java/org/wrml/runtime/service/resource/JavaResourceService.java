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

package org.wrml.runtime.service.resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.model.rest.Document;
import org.wrml.runtime.*;
import org.wrml.runtime.format.SystemFormat;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.runtime.service.AbstractService;
import org.wrml.runtime.service.ServiceConfiguration;
import org.wrml.runtime.service.ServiceException;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Java's resource loading capability as a WRML {@link org.wrml.runtime.service.Service}.
 */
public class JavaResourceService extends AbstractService
{

    private static final Logger LOG = LoggerFactory.getLogger(JavaResourceService.class);

    private final String RESOURCE_OWNER_CLASS_NAME_SETTING_NAME = "resourceOwnerClassName";

    private final String RESOURCE_ROOT_DIRECTORY_PATH_SETTING_NAME = "resourceRootDirectoryPath";

    private Class<?> _ResourceOwnerClass;

    private String _ResourceRootDirectoryPath;

    public static final Keys buildModelResourceKeys(final Context context, final Class<?> resourceOwner, final String resourceName)
    {

        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final URI schemaUri = schemaLoader.getTypeUri(JavaResource.class);
        final SortedMap<String, Object> keySlots = new TreeMap<>();
        keySlots.put(JavaResource.RESOURCE_OWNER_CLASS_NAME_SLOT_NAME, resourceOwner.getName());
        keySlots.put(JavaResource.RESOURCE_NAME_SLOT_NAME, resourceName);
        final CompositeKey compositeKey = new CompositeKey(keySlots);
        final Keys keys = new KeysBuilder().addKey(schemaUri, compositeKey).toKeys();
        return keys;
    }

    public static final InputStream getModelResourceInputStream(final Context context, final Keys keys)
    {

        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final URI schemaUri = schemaLoader.getTypeUri(JavaResource.class);

        final CompositeKey compositeKey = keys.getValue(schemaUri);

        if (compositeKey == null)
        {
            return null;
        }

        final Map<String, Object> keySlots = compositeKey.getKeySlots();
        final String resourceOwnerClassName = (String) keySlots.get(JavaResource.RESOURCE_OWNER_CLASS_NAME_SLOT_NAME);
        final String resourceName = (String) keySlots.get(JavaResource.RESOURCE_NAME_SLOT_NAME);

        try
        {
            final Class<?> resourceOwnerClass = Class.forName(resourceOwnerClassName);
            return resourceOwnerClass.getResourceAsStream(resourceName);
        }
        catch (final Exception t)
        {
            return null;
        }

    }

    @Override
    protected void initFromConfiguration(final ServiceConfiguration config)
    {

        final Map<String, String> settings = config.getSettings();
        if (settings != null)
        {
            final String resourceOwnerClassName = settings.get(RESOURCE_OWNER_CLASS_NAME_SETTING_NAME);
            if (resourceOwnerClassName != null && !resourceOwnerClassName.isEmpty())
            {
                try
                {
                    _ResourceOwnerClass = Class.forName(resourceOwnerClassName);
                }
                catch (ClassNotFoundException e)
                {
                    throw new ServiceException(e.getMessage(), e, this);
                }
            }

            final String resourceRootDirectoryPath = settings.get(RESOURCE_ROOT_DIRECTORY_PATH_SETTING_NAME);
            if (resourceRootDirectoryPath != null && !resourceRootDirectoryPath.isEmpty())
            {
                _ResourceRootDirectoryPath = resourceRootDirectoryPath;
                if (!_ResourceRootDirectoryPath.endsWith("/"))
                {
                    _ResourceRootDirectoryPath += "/";
                }
            }
        }
    }

    @Override
    public Model get(final Keys keys, final Dimensions dimensions)
    {

        final Context context = getContext();
        final URI uri = context.getKeyValue(keys, Document.class);
        if (uri != null && _ResourceOwnerClass != null && _ResourceRootDirectoryPath != null)
        {

            return getModelResource(uri, keys, dimensions);
        }
        else
        {

            final InputStream in = getModelResourceInputStream(context, keys);
            try
            {
                final Model model = context.readModel(in, keys, dimensions);
                return model;
            }
            catch (Exception t)
            {
                throw new ServiceException(t.getMessage(), t, this);
            }
        }

    }

    private Model getModelResource(final URI uri, final Keys keys, final Dimensions dimensions)
    {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final String uriString = uri.toString();
        String relativeResourcePath = StringUtils.substringAfter(uriString, "://");
        relativeResourcePath = StringUtils.substringBefore(relativeResourcePath, "?");
        relativeResourcePath = StringUtils.replaceChars(relativeResourcePath, ".:", "/");
        String fullResourcePath = _ResourceRootDirectoryPath + relativeResourcePath;

        if (schemaLoader.getApiSchemaUri().equals(dimensions.getSchemaUri()))
        {
            fullResourcePath = fullResourcePath + "/_wrml/api";
        }

        final String modelFileExtension = "." + SystemFormat.json.getFileExtension();
        fullResourcePath = fullResourcePath + modelFileExtension;

        LOG.debug("Attempting to open stream to resource {}", new Object[]{fullResourcePath});

        final InputStream in = _ResourceOwnerClass.getResourceAsStream(fullResourcePath);

        try
        {
            final Model model = context.readModel(in, keys, dimensions, SystemFormat.json.getFormatUri());
            return model;
        }
        catch (Throwable t)
        {
            throw new ServiceException(t.getMessage(), t, this);
        }
    }

    // This seems pretty silly to bother...
    /*
     * @Override public Set<Model> search(final SearchCriteria searchCriteria) throws UnsupportedOperationException {
     * 
     * final Model referrer = searchCriteria.getReferrer();
     * 
     * 
     * URI referrerUri = null; if (referrer instanceof Document) { referrerUri = ((Document) referrer).getUri(); } else if (referrer instanceof Embedded) { referrerUri =
     * ((Embedded) referrer).getDocumentUri(); } else { return Collections.EMPTY_SET; }
     * 
     * final Context context = getContext(); final String referrerCollectionSlotName = searchCriteria.getReferrerCollectionSlotName(); final Prototype referrerPrototype =
     * referrer.getPrototype(); final CollectionPropertyProtoSlot collectionPropertyProtoSlot = referrerPrototype.getProtoSlot(referrerCollectionSlotName); final URI
     * linkRelationUri = collectionPropertyProtoSlot.getLinkRelationUri();
     * 
     * final ApiLoader apiLoader = context.getApiLoader(); final ApiNavigator apiNavigator = apiLoader.getParentApiNavigator(referrerUri); final Resource endpointResource =
     * apiNavigator.getEndpointResource(linkRelationUri, referrerUri);
     * 
     * final UriTemplate uriTemplate = endpointResource.getUriTemplate(); final String[] parameterNames = uriTemplate.getParameterNames();
     * 
     * final LinkedHashSet<Model> resultSet = new LinkedHashSet<>();
     * 
     * 
     * for (final SearchCriterion searchCriterion : searchCriteria.getAnd()) { final ComparisonOperator comparisonOperator = searchCriterion.getComparisonOperator(); if
     * (comparisonOperator == ComparisonOperator.equalToAny) { searchCriterion.getComparisonValue(); }
     * 
     * 
     * }
     * 
     * return resultSet; }
     */
}
