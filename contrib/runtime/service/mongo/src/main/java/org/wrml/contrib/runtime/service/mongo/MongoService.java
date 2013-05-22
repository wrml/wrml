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

package org.wrml.contrib.runtime.service.mongo;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.Embedded;
import org.wrml.runtime.CompositeKey;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.ModelReadingException;
import org.wrml.runtime.format.SystemFormat;
import org.wrml.runtime.schema.*;
import org.wrml.runtime.search.SearchCriteria;
import org.wrml.runtime.service.AbstractService;
import org.wrml.runtime.service.ServiceConfiguration;
import org.wrml.runtime.service.ServiceException;
import org.wrml.runtime.syntax.SyntaxLoader;
import org.wrml.util.UniqueName;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.*;

/**
 * The marvellous mongoDB as a WRML Service.
 *
 * @see <a href="http://www.mongodb.org>mongoDB</a>
 * @see <a href="http://www.10gen.com">10gen</a>
 * @see <a href="http://api.mongodb.org/java">Java API for mongoDB</a>
 */
public class MongoService extends AbstractService
{

    public static final String DEFAULT_HOST = "localhost";

    public static final int DEFAULT_PORT = 27017;

    public static final String DEFAULT_DATABASE_NAME = "test";

    public static final String DEFAULT_URI_STRING = "mongodb://" + DEFAULT_HOST + ":" + DEFAULT_PORT + "/" + DEFAULT_DATABASE_NAME;

    public static final String MONGO_URI_SETTING_NAME = "uri";

    public static final String MONGO_COLLECTION_PREFIX_SETTING_NAME = "collectionPrefix";

    private static Logger LOG = LoggerFactory.getLogger(MongoService.class);

    private DB _Mongo;

    private String _CollectionPrefix;

    @Override
    public Model save(final Model model)
    {

        final URI schemaUri = model.getSchemaUri();
        final String collectionName = convertToCollectionName(schemaUri);

        final Keys keys = model.getKeys();
        DBObject mongoKeys = createMongoKeys(keys);

        if (!_Mongo.collectionExists(collectionName))
        {
            final DBCollection mongoCollection = _Mongo.getCollection(collectionName);

            final DBObject collectionIndex = new BasicDBObject();
            final Set<String> indexKeySet = mongoKeys.keySet();
            for (final String indexKey : indexKeySet)
            {
                collectionIndex.put(indexKey, 1);
            }

            final DBObject options = new BasicDBObject();
            options.put("background", true);

            mongoCollection.ensureIndex(collectionIndex, options);
        }

        final DBObject mongoObject = convertToMongoObject(model);

        final DBCollection mongoCollection = _Mongo.getCollection(collectionName);
        if (mongoCollection == null)
        {
            // Should not happen
            final String logMessage = getConfiguration().getName() + " - Collection should exist. Name:\n" + collectionName;

            LOG.error(logMessage);
            throw new ServiceException(logMessage, null, this);
        }


        final DBObject existingMongoObject = mongoCollection.findOne(mongoKeys);
        if (existingMongoObject != null)
        {
            mongoObject.put("_id", existingMongoObject.get("_id"));
        }

        String errorMessage = null;
        Throwable throwable = null;
        try
        {
            final WriteResult mongoWriteResult = mongoCollection.save(mongoObject);
            errorMessage = mongoWriteResult.getError();
        }
        catch (Throwable t)
        {
            errorMessage = t.getMessage();
            throwable = t;
        }

        if (errorMessage != null || throwable != null)
        {
            final String logMessage = getConfiguration().getName() + " - Error saving model (" + errorMessage + ").";

            LOG.error(logMessage);
            throw new ServiceException(logMessage, throwable, this);
        }

        return model;

    }

    @Override
    public Model get(final Keys keys, final Dimensions dimensions)
    {

        final URI schemaUri = dimensions.getSchemaUri();
        final String collectionName = convertToCollectionName(schemaUri);
        if (!_Mongo.collectionExists(collectionName))
        {
            LOG.debug(getConfiguration().getName() + " - Collection does not exist. Name:\n" + collectionName);
            return null;
        }

        final DBCollection mongoCollection = _Mongo.getCollection(collectionName);
        if (mongoCollection == null)
        {
            // Should not happen
            LOG.error(getConfiguration().getName() + " - Collection should exist. Name:\n" + collectionName);
            return null;
        }

        final DBObject mongoKeys = createMongoKeys(keys);
        final DBObject mongoObject = mongoCollection.findOne(mongoKeys);
        if (mongoObject == null)
        {
            LOG.debug(getConfiguration().getName() + " - Failed to find model. Keys:\n" + keys);
            return null;
        }

        Model model = null;

        try
        {
            model = convertToModel(mongoObject, keys, dimensions);
        }
        catch (ModelReadingException e)
        {
            LOG.error(e.getMessage(), e);
        }

        return model;
    }

    @Override
    public void delete(final Keys keys, final Dimensions dimensions)
    {

        final DBObject mongoKeys = createMongoKeys(keys);

        for (final URI schemaUri : keys.getKeyedSchemaUris())
        {

            final String collectionName = convertToCollectionName(schemaUri);
            if (!_Mongo.collectionExists(collectionName))
            {
                continue;
            }

            final DBCollection mongoCollection = _Mongo.getCollection(collectionName);
            if (mongoCollection == null)
            {
                continue;
            }

            final DBObject mongoObject = mongoCollection.findOne(mongoKeys);

            if (mongoObject != null)
            {
                final WriteResult mongoWriteResult = mongoCollection.remove(mongoObject);

                final String errorMessage = mongoWriteResult.getError();
                if (errorMessage != null)
                {
                    LOG.error(getConfiguration().getName() + " - Error deleting model (" + errorMessage + "). Keys:\n" + keys);
                }
            }
        }
    }

    @Override
    public Set<Model> search(final SearchCriteria searchCriteria) throws UnsupportedOperationException
    {

        final Set<Model> resultSet = new LinkedHashSet<>();


        final QueryBuilder mongoQueryBuilder = new QueryBuilder();


        // TODO: Implement search


        return resultSet;
    }

    @Override
    protected void initFromConfiguration(final ServiceConfiguration config)
    {

        if (config == null)
        {
            final ServiceException e = new ServiceException("The config cannot be null.", null, this);
            LOG.error(e.getMessage(), e);
            throw e;
        }


        final Map<String, String> settings = config.getSettings();
        String mongoUriString = DEFAULT_URI_STRING;

        if (settings != null)
        {
            if (settings.containsKey(MONGO_URI_SETTING_NAME))
            {
                mongoUriString = settings.get(MONGO_URI_SETTING_NAME);
            }

            if (settings.containsKey(MONGO_COLLECTION_PREFIX_SETTING_NAME))
            {
                _CollectionPrefix = settings.get(MONGO_COLLECTION_PREFIX_SETTING_NAME);
            }
        }

        // TODO: Look into MongoClientURI replacement
        final MongoURI mongoUri = new MongoURI(mongoUriString);
        try
        {
            _Mongo = mongoUri.connectDB();

            if (!_Mongo.isAuthenticated() && mongoUri.getPassword() != null)
            {
                _Mongo.authenticate(mongoUri.getUsername(), mongoUri.getPassword());
            }

        }
        catch (MongoException | UnknownHostException ex)
        {
            final String logMessage = "Error creating connection to Mongo: " + _Mongo;
            LOG.error(logMessage);
            throw new ServiceException(logMessage, ex, this);
        }
    }

    private DBObject createMongoKeys(final Keys keys)
    {

        // The mongoDB object that will hold the "serialized" keys structure.
        final DBObject mongoKeys = new BasicDBObject();

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        for (final URI keyedSchemaUri : keys.getKeyedSchemaUris())
        {

            if (keys.getCount() > 1 && keyedSchemaUri.equals(schemaLoader.getDocumentSchemaUri()))
            {
                // To promote de-coupling of REST API Design from this back-end storage Service, skip Document's URI if we can.
                continue;
            }

            // This is the key'd value

            final Object keyValue = keys.getValue(keyedSchemaUri);
            final Prototype prototype = schemaLoader.getPrototype(keyedSchemaUri);
            final SortedSet<String> keySlotNames = prototype.getDeclaredKeySlotNames();
            if (keySlotNames == null)
            {
                // Should not happen
                continue;
            }

            if (keySlotNames.size() == 1)
            {
                // This is a simple key with only one slot (not a compound key).
                final String keySlotName = keySlotNames.first();

                final Object mongoValue = convertToMongoValue(keyValue);
                mongoKeys.put(keySlotName, mongoValue);
            }
            else if (keyValue instanceof CompositeKey)
            {
                final CompositeKey compositeKey = (CompositeKey) keyValue;

                // This compound key manages its own names to values mapping, which makes this simple.
                final Map<String, Object> compositeKeySlots = compositeKey.getKeySlots();
                final Set<String> compositeKeySlotNames = compositeKeySlots.keySet();
                for (final String compositeKeySlotName : compositeKeySlotNames)
                {

                    final Object compositeKeySlotValue = compositeKeySlots.get(compositeKeySlotName);
                    final Object mongoValue = convertToMongoValue(compositeKeySlotValue);
                    mongoKeys.put(compositeKeySlotName, mongoValue);
                }
            }
        }

        return mongoKeys;
    }

    private String convertToCollectionName(final URI schemaUri)
    {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Prototype prototype = schemaLoader.getPrototype(schemaUri);
        final UniqueName uniqueName = prototype.getUniqueName();

        // TODO: Length limitations suggest that this be short: http://docs.mongodb.org/manual/reference/limits/
        // TODO: Is this unique enough?
        final String collectionName = uniqueName.getLocalName();

        if (_CollectionPrefix == null)
        {
            return collectionName;
        }

        return _CollectionPrefix + collectionName;
    }

    private Model convertToModel(final DBObject mongoObject, final Keys keys, final Dimensions dimensions) throws ModelReadingException
    {

        mongoObject.removeField("_id");

        // Is JSON serialization fast enough here?
        final String jsonStringRepresentation = JSON.serialize(mongoObject);
        final byte[] jsonStringBytes = jsonStringRepresentation.getBytes();
        final InputStream inStream = new ByteArrayInputStream(jsonStringBytes);

        final Context context = getContext();
        final Model model = context.readModel(inStream, keys, dimensions, SystemFormat.json.getFormatUri());
        return model;
    }

    private DBObject convertToMongoObject(final Model model)
    {

        return convertToMongoObject(model, null);
    }

    private DBObject convertToMongoObject(final Model model, final Set<String> projection)
    {

        final DBObject mongoObject = new BasicDBObject();

        final Prototype prototype = model.getPrototype();
        final SchemaLoader schemaLoader = prototype.getSchemaLoader();

        final Set<String> slotNames = prototype.getAllSlotNames();

        for (final String slotName : slotNames)
        {

            final ProtoSlot protoSlot = prototype.getProtoSlot(slotName);

            // Don't convert link slots or the "dynamically filled" collection slots (they are both managed by the WRML runtime).
            if (protoSlot instanceof LinkProtoSlot || protoSlot instanceof CollectionPropertyProtoSlot)
            {
                continue;
            }

            // Don't bother with non-projected slots or null slot values
            if ((projection == null || projection.isEmpty() || projection.contains(slotName)) && model.containsSlotValue(slotName))
            {

                // Omit Document and Embedded URI values
                if ((!prototype.isDocument() || !Document.SLOT_NAME_URI.equals(slotName)) &&
                        (!prototype.isAssignableFrom(schemaLoader.getEmbeddedSchemaUri()) && !Embedded.SLOT_NAME_DOCUMENT_URI.equals(slotName)))
                {

                    final Object slotValue = model.getSlotValue(slotName);
                    final Object mongoValue = convertToMongoValue(slotValue);

                    if (mongoValue != null)
                    {
                        mongoObject.put(slotName, mongoValue);
                    }
                }
            }
        }

        return mongoObject;
    }

    private Object convertToMongoValue(final Object value)
    {

        final Context context = getContext();

        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final ValueType valueType = schemaLoader.getValueType(value.getClass());

        final Object mongoValue;

        switch (valueType)
        {
            case SingleSelect:
            case Text:

                final SyntaxLoader syntaxLoader = context.getSyntaxLoader();
                mongoValue = syntaxLoader.formatSyntaxValue(value);
                break;

            case List:

                final List javaList = (List) value;
                final BasicDBList mongoList = new BasicDBList();
                for (final Object listElement : javaList)
                {
                    final Object mongoListElement = convertToMongoValue(listElement);
                    mongoList.add(mongoListElement);
                }

                mongoValue = mongoList;
                break;

            case Link:
            case Model:

                mongoValue = convertToMongoObject((Model) value);
                break;

            default:
                mongoValue = value;
                break;
        }

        return mongoValue;
    }


}
