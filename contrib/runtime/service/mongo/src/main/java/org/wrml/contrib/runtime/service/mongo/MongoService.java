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
package org.wrml.contrib.runtime.service.mongo;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.model.schema.ComparisonOperator;
import org.wrml.model.schema.ValueType;
import org.wrml.runtime.CompositeKey;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.ModelReadingException;
import org.wrml.runtime.format.ModelWriteOptions;
import org.wrml.runtime.format.ModelWritingException;
import org.wrml.runtime.format.SystemFormat;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.runtime.search.SearchCriteria;
import org.wrml.runtime.search.SearchCriterion;
import org.wrml.runtime.service.AbstractService;
import org.wrml.runtime.service.ServiceConfiguration;
import org.wrml.runtime.service.ServiceException;
import org.wrml.runtime.syntax.SyntaxLoader;
import org.wrml.util.UniqueName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The marvellous mongoDB as a WRML Service.
 *
 * @see <a href="http://www.mongodb.org:>mongoDB</a>
 * @see <a href="http://www.10gen.com">10gen</a>
 * @see <a href="http://api.mongodb.org/java">Java API for mongoDB</a>
 */
public class MongoService extends AbstractService {

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
    public Model save(final Model model) {

        final URI schemaUri = model.getSchemaUri();
        final String collectionName = convertToCollectionName(schemaUri);

        final Keys keys = model.getKeys();
        DBObject mongoKeys = createMongoKeys(keys);

        if (!_Mongo.collectionExists(collectionName)) {
            final DBCollection mongoCollection = _Mongo.getCollection(collectionName);

            final DBObject collectionIndex = new BasicDBObject();
            final Set<String> indexKeySet = mongoKeys.keySet();
            for (final String indexKey : indexKeySet) {
                collectionIndex.put(indexKey, 1);
            }

            final DBObject options = new BasicDBObject();
            options.put("background", true);

            mongoCollection.ensureIndex(collectionIndex, options);
        }

        final DBObject mongoObject;
        try {
            mongoObject = convertToMongoObject(model);
        }
        catch (ModelWritingException e) {
            throw new ServiceException("Failed to convert WRML model instance to a mongoDB object.", e, this);
        }

        final DBCollection mongoCollection = _Mongo.getCollection(collectionName);
        if (mongoCollection == null) {
            // Should not happen
            final String logMessage = getConfiguration().getName() + " - Collection should exist. Name:\n" + collectionName;

            LOG.error(logMessage);
            throw new ServiceException(logMessage, null, this);
        }


        final DBObject existingMongoObject = mongoCollection.findOne(mongoKeys);
        if (existingMongoObject != null) {
            mongoObject.put("_id", existingMongoObject.get("_id"));
        }

        String errorMessage = null;
        Throwable throwable = null;
        try {
            final WriteResult mongoWriteResult = mongoCollection.save(mongoObject);
            errorMessage = mongoWriteResult.getError();
        }
        catch (Throwable t) {
            errorMessage = t.getMessage();
            throwable = t;
        }

        if (errorMessage != null || throwable != null) {
            final String logMessage = getConfiguration().getName() + " - Error saving model (" + errorMessage + ").";

            LOG.error(logMessage);
            throw new ServiceException(logMessage, throwable, this);
        }

        // TODO: Should this return the saved model instead (using get?)?
        return model;

    }

    @Override
    public Model get(final Keys keys, final Dimensions dimensions) {

        final URI schemaUri = dimensions.getSchemaUri();
        final String collectionName = convertToCollectionName(schemaUri);
        if (!_Mongo.collectionExists(collectionName)) {
            LOG.debug(getConfiguration().getName() + " - Collection does not exist. Name:\n" + collectionName);
            return null;
        }

        final DBCollection mongoCollection = _Mongo.getCollection(collectionName);
        if (mongoCollection == null) {
            // Should not happen
            LOG.error(getConfiguration().getName() + " - Collection should exist. Name:\n" + collectionName);
            return null;
        }

        final DBObject mongoKeys = createMongoKeys(keys);
        final DBObject mongoObject = mongoCollection.findOne(mongoKeys);
        if (mongoObject == null) {
            LOG.debug(getConfiguration().getName() + " - Failed to find model. Keys:\n" + keys);
            return null;
        }

        Model model = null;

        try {
            model = convertToModel(mongoObject, keys, dimensions);
        }
        catch (ModelReadingException e) {
            LOG.error(e.getMessage(), e);
        }

        return model;
    }

    @Override
    public void delete(final Keys keys, final Dimensions dimensions) {

        final DBObject mongoKeys = createMongoKeys(keys);

        for (final URI schemaUri : keys.getKeyedSchemaUris()) {

            final String collectionName = convertToCollectionName(schemaUri);
            if (!_Mongo.collectionExists(collectionName)) {
                continue;
            }

            final DBCollection mongoCollection = _Mongo.getCollection(collectionName);
            if (mongoCollection == null) {
                continue;
            }

            final DBObject mongoObject = mongoCollection.findOne(mongoKeys);

            if (mongoObject != null) {
                final WriteResult mongoWriteResult = mongoCollection.remove(mongoObject);

                final String errorMessage = mongoWriteResult.getError();
                if (errorMessage != null) {
                    LOG.error(getConfiguration().getName() + " - Error deleting model (" + errorMessage + "). Keys:\n" + keys);
                }
            }
        }
    }

    @Override
    public Set<Model> search(final SearchCriteria searchCriteria) throws UnsupportedOperationException {

        // Identify the mongo collection to query.
        final Dimensions resultDimensions = searchCriteria.getResultDimensions();

        final URI schemaUri = resultDimensions.getSchemaUri();
        final String collectionName = convertToCollectionName(schemaUri);
        if (!_Mongo.collectionExists(collectionName)) {
            LOG.debug(getConfiguration().getName() + " - Collection does not exist. Name:\n" + collectionName);
            return null;
        }

        final DBCollection mongoCollection = _Mongo.getCollection(collectionName);
        if (mongoCollection == null) {
            // Should not happen
            LOG.error(getConfiguration().getName() + " - Collection should exist. Name:\n" + collectionName);
            return null;
        }

        // Build the mongo query object.
        final DBObject mongoQuery = createMongoQuery(searchCriteria);
        if (mongoQuery == null) {
            LOG.warn(getConfiguration().getName() + " - Query could not be created for: " + searchCriteria);
            return null;
        }

        // Build the mongo projection (fields to return).
        DBObject mongoKeys = null;
        final Set<String> projectionSlotNames = searchCriteria.getProjectionSlotNames();
        if (projectionSlotNames != null && !projectionSlotNames.isEmpty()) {
            for (final String projectionSlotName : projectionSlotNames) {
                mongoKeys.put(projectionSlotName, 1);
            }
        }

        // Query mongo
        final DBCursor cursor = mongoCollection.find(mongoQuery, mongoKeys);
        final int resultLimit = searchCriteria.getResultLimit();

        if (resultLimit > 0) {
            cursor.limit(resultLimit);
        }

        // TODO: Support skipping to an offset
        //cursor.skip(offset);

        // Build model results
        final Set<Model> resultSet = new LinkedHashSet<>();

        try {
            while (cursor.hasNext()) {
                final DBObject mongoObject = cursor.next();
                final Model model;

                try {
                    model = convertToModel(mongoObject, null, resultDimensions);
                    // Note: Context will set URI value in Document models.
                }
                catch (ModelReadingException e) {
                    LOG.error(e.getMessage(), e);
                    continue;
                }

                resultSet.add(model);
            }
        }
        finally {
            cursor.close();
        }

        return resultSet;
    }


    @Override
    protected void initFromConfiguration(final ServiceConfiguration config) {

        if (config == null) {
            final ServiceException e = new ServiceException("The config cannot be null.", null, this);
            LOG.error(e.getMessage(), e);
            throw e;
        }


        final Map<String, String> settings = config.getSettings();
        String mongoUriString = DEFAULT_URI_STRING;

        if (settings != null) {
            if (settings.containsKey(MONGO_URI_SETTING_NAME)) {
                mongoUriString = settings.get(MONGO_URI_SETTING_NAME);
            }

            if (settings.containsKey(MONGO_COLLECTION_PREFIX_SETTING_NAME)) {
                _CollectionPrefix = settings.get(MONGO_COLLECTION_PREFIX_SETTING_NAME);
            }
        }

        // TODO: Look into MongoClientURI replacement
        final MongoURI mongoUri = new MongoURI(mongoUriString);
        try {
            _Mongo = mongoUri.connectDB();

            if (!_Mongo.isAuthenticated() && mongoUri.getPassword() != null) {
                _Mongo.authenticate(mongoUri.getUsername(), mongoUri.getPassword());
            }

        }
        catch (MongoException | UnknownHostException ex) {
            final String logMessage = "Error creating connection to Mongo: " + _Mongo;
            LOG.error(logMessage);
            throw new ServiceException(logMessage, ex, this);
        }
    }

    private DBObject createMongoKeys(final Keys keys) {

        // The mongoDB object that will hold the "serialized" keys structure.
        final DBObject mongoKeys = new BasicDBObject();

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        for (final URI keyedSchemaUri : keys.getKeyedSchemaUris()) {

            if (keys.getCount() > 1 && keyedSchemaUri.equals(schemaLoader.getDocumentSchemaUri())) {
                // To promote de-coupling of REST API Design from this back-end storage Service, skip Document's URI if we can.
                continue;
            }

            // This is the key'd value

            final Object keyValue = keys.getValue(keyedSchemaUri);
            final Prototype prototype = schemaLoader.getPrototype(keyedSchemaUri);
            final SortedSet<String> keySlotNames = prototype.getDeclaredKeySlotNames();
            if (keySlotNames == null) {
                // Should not happen
                continue;
            }

            if (keySlotNames.size() == 1) {
                // This is a simple key with only one slot (not a compound key).
                final String keySlotName = keySlotNames.first();

                final Object mongoValue = convertToMongoValue(keyValue);
                mongoKeys.put(keySlotName, mongoValue);
            }
            else if (keyValue instanceof CompositeKey) {
                final CompositeKey compositeKey = (CompositeKey) keyValue;

                // This compound key manages its own names to values mapping, which makes this simple.
                final Map<String, Object> compositeKeySlots = compositeKey.getKeySlots();
                final Set<String> compositeKeySlotNames = compositeKeySlots.keySet();
                for (final String compositeKeySlotName : compositeKeySlotNames) {

                    final Object compositeKeySlotValue = compositeKeySlots.get(compositeKeySlotName);
                    final Object mongoValue = convertToMongoValue(compositeKeySlotValue);
                    mongoKeys.put(compositeKeySlotName, mongoValue);
                }
            }
        }

        return mongoKeys;
    }

    private DBObject createMongoQuery(final SearchCriteria searchCriteria) {

        QueryBuilder queryBuilder = null;

        final List<SearchCriterion> and = searchCriteria.getAnd();
        if (and != null && !and.isEmpty()) {

            queryBuilder = new QueryBuilder();

            for (final SearchCriterion searchCriterion : and) {

                final String referenceSlot = searchCriterion.getReferenceSlot();
                queryBuilder.and(referenceSlot);
                addQueryCriterion(searchCriterion, queryBuilder);

            }

        }

        final List<SearchCriterion> or = searchCriteria.getOr();
        if (or != null && !or.isEmpty()) {

            final DBObject[] orQueryCriterionArray = new DBObject[or.size()];
            for (int i = 0; i < or.size(); i++) {
                final SearchCriterion searchCriterion = or.get(i);
                final String referenceSlot = searchCriterion.getReferenceSlot();
                final QueryBuilder orQueryCriterionBuilder = QueryBuilder.start(referenceSlot);
                addQueryCriterion(searchCriterion, orQueryCriterionBuilder);
                orQueryCriterionArray[i] = orQueryCriterionBuilder.get();
            }

            final QueryBuilder orQueryBuilder = new QueryBuilder();
            orQueryBuilder.or(orQueryCriterionArray);

            if (queryBuilder != null) {
                // AND the OR clause together with the AND query
                queryBuilder.and(orQueryBuilder.get());
            }
            else {
                queryBuilder = orQueryBuilder;
            }
        }

        if (queryBuilder == null) {
            return null;
        }

        final DBObject mongoQuery = queryBuilder.get();
        return mongoQuery;
    }

    private void addQueryCriterion(final SearchCriterion searchCriterion, final QueryBuilder queryBuilder) {

        final ComparisonOperator comparisonOperator = searchCriterion.getComparisonOperator();
        final Object comparisonValue = searchCriterion.getComparisonValue();
        switch (comparisonOperator) {

            case containsAll: {
                queryBuilder.all(comparisonValue);
                break;
            }

            case equalTo: {
                queryBuilder.equals(comparisonValue);
                break;
            }

            case equalToAny: {
                queryBuilder.in(comparisonValue);
                break;
            }

            case exists: {
                queryBuilder.exists(true);
                break;
            }

            case greaterThan: {
                queryBuilder.greaterThan(comparisonValue);
                break;
            }

            case greaterThanOrEqualTo: {
                queryBuilder.greaterThanEquals(comparisonValue);
                break;
            }

            case lessThan: {
                queryBuilder.lessThan(comparisonValue);
                break;
            }

            case lessThanOrEqualTo: {
                queryBuilder.lessThanEquals(comparisonValue);
                break;
            }

            case notEqualTo: {
                queryBuilder.notEquals(comparisonValue);
                break;
            }

            case notEqualToAny: {
                queryBuilder.notIn(comparisonValue);
                break;
            }

            case notExists: {
                queryBuilder.exists(false);
                break;
            }

            case regex: {
                final Pattern regexPattern = searchCriterion.getRegexPattern();
                if (regexPattern != null) {
                    queryBuilder.regex(regexPattern);
                }

                break;
            }

        }
    }

    private String convertToCollectionName(final URI schemaUri) {

        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final Prototype prototype = schemaLoader.getPrototype(schemaUri);
        final UniqueName uniqueName = prototype.getUniqueName();

        // TODO: Length limitations suggest that this be short: http://docs.mongodb.org/manual/reference/limits/
        // TODO: Is this unique enough?
        // TODO: Use namespace/package abbrevs e.g. org.wrml.model.rest.Api becomes: o_w_m_r_Api
        final String collectionName = uniqueName.getLocalName();

        if (_CollectionPrefix == null) {
            return collectionName;
        }

        return _CollectionPrefix + collectionName;
    }

    private Model convertToModel(final DBObject mongoObject, final Keys keys, final Dimensions dimensions) throws ModelReadingException {

        mongoObject.removeField("_id");

        // Is JSON serialization fast enough here?
        final String jsonStringRepresentation = JSON.serialize(mongoObject);
        final byte[] jsonStringBytes = jsonStringRepresentation.getBytes();
        final InputStream inStream = new ByteArrayInputStream(jsonStringBytes);

        final Context context = getContext();
        final Model model = context.readModel(inStream, keys, dimensions, SystemFormat.json.getFormatUri());
        return model;
    }

    private DBObject convertToMongoObject(final Model model) throws ModelWritingException {

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Context context = getContext();
        final ModelWriteOptions modelWriteOptions = new ModelWriteOptions();

        modelWriteOptions.setDocumentKeyExcludedIfSecondary(true);
        modelWriteOptions.setEmbeddedDocumentUriExcluded(true);
        modelWriteOptions.setLinksExcluded(true);
        modelWriteOptions.setCollectionsExcluded(true);

        context.writeModel(out, model, modelWriteOptions, SystemFormat.json.getFormatUri());

        final byte[] jsonStringBytes = out.toByteArray();
        final String jsonStringRepresentation = new String(jsonStringBytes);
        DBObject mongoObject = (DBObject) JSON.parse(jsonStringRepresentation);
        return mongoObject;
    }


    private Object convertToMongoValue(final Object value) {

        final Context context = getContext();

        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final ValueType valueType = schemaLoader.getValueType(value.getClass());

        final Object mongoValue;

        switch (valueType) {
            case SingleSelect:
            case Text:

                final SyntaxLoader syntaxLoader = context.getSyntaxLoader();
                mongoValue = syntaxLoader.formatSyntaxValue(value);
                break;

            default:
                mongoValue = value;
                break;
        }

        return mongoValue;
    }

}
