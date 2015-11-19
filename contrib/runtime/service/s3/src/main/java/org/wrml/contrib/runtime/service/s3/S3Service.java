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
package org.wrml.contrib.runtime.service.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.ModelFormattingException;
import org.wrml.runtime.format.ModelWriteOptions;
import org.wrml.runtime.format.ModelWritingException;
import org.wrml.runtime.format.SystemFormat;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.runtime.service.AbstractService;
import org.wrml.runtime.service.ServiceConfiguration;
import org.wrml.runtime.service.ServiceException;
import org.wrml.util.UniqueName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * S3 as a WRML Service.
 *
 * @see <a href="https://aws.amazon.com/s3">S3</a>
 * @see <a href="http://aws.amazon.com/developers/getting-started/java/">Getting Started with the AWS SDK for Java</a>
 * @see <a href="http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc">Java API for S3</a>
 */
public class S3Service extends AbstractService {

    private static Logger LOG = LoggerFactory.getLogger(S3Service.class);

    private static final String DEFAULT_BUCKET_NAME = "wrml";

    private static final String DEFAULT_ROOT_FOLDER_PATH = "models";

    public static final String BUCKET_NAME_SETTING_NAME = "bucketName";

    public static final String ROOT_FOLDER_PATH_SETTING_NAME = "rootFolderPath";

    private String _BucketName;

    private String _RootFolderPath;

    private AmazonS3 _S3;

    @Override
    public Model get(Keys keys, Dimensions dimensions) {
        final String s3Key = formatKey(keys, dimensions);
        return getModel(s3Key, keys, dimensions);
    }

    @Override
    public Model save(Model model) throws UnsupportedOperationException {

        final Context context = getContext();

        final Keys keys = model.getKeys();
        final Dimensions dimensions = model.getDimensions();
        final String s3Key = formatKey(keys, dimensions);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ModelWriteOptions writeOptions = new ModelWriteOptions();

        writeOptions.setPrettyPrint(true);

        try {
            context.writeModel(out, model, writeOptions, SystemFormat.json.getFormatUri());
        }
        catch (final ModelWritingException e) {
            LOG.error(e.getMessage(), e);
            IOUtils.closeQuietly(out);
            throw new ServiceException("Failed to write model to S3 - error: " + e.toString() + " - message: "
                    + e.getMessage(), e, this);
        }

        final byte[] byteArray = out.toByteArray();
        final InputStream in = new ByteArrayInputStream(byteArray);
        final ObjectMetadata objectMetadata = new ObjectMetadata();
        final Map<String, String> userMetadata = new HashMap<String, String>();

        objectMetadata.setContentLength(byteArray.length);
        objectMetadata.setUserMetadata(userMetadata);

        try {
            final PutObjectRequest putObjectRequest = new PutObjectRequest(_BucketName, s3Key, in, objectMetadata);
            _S3.putObject(putObjectRequest);
        }
        catch (AmazonClientException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Failed to write model to S3 - error: " + e.toString() + " - message: "
                    + e.getMessage(), e, this);
        }
        finally {
            IOUtils.closeQuietly(in);
        }

        return model;
    }

    @Override
    public void delete(Keys keys, Dimensions dimensions) throws UnsupportedOperationException {
        final String s3Key = formatKey(keys, dimensions);

        try {
            _S3.deleteObject(_BucketName, s3Key);
        }
        catch (AmazonClientException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Failed to delete model from S3 - error: " + e.toString() + " - message: "
                    + e.getMessage(), e, this);
        }
    }

    @Override
    protected void initFromConfiguration(ServiceConfiguration config) {

        if (config == null) {
            final ServiceException e = new ServiceException("The config cannot be null.", null, this);
            LOG.error(e.getMessage(), e);
            throw e;
        }

        final Map<String, String> settings = config.getSettings();
        _BucketName = DEFAULT_BUCKET_NAME;
        _RootFolderPath = DEFAULT_ROOT_FOLDER_PATH;

        if (settings != null) {
            if (settings.containsKey(BUCKET_NAME_SETTING_NAME)) {
                _BucketName = settings.get(BUCKET_NAME_SETTING_NAME);
            }

            if (settings.containsKey(ROOT_FOLDER_PATH_SETTING_NAME)) {
                _RootFolderPath = settings.get(ROOT_FOLDER_PATH_SETTING_NAME);
            }
        }

        if (!_RootFolderPath.endsWith("/")) {
            _RootFolderPath = _RootFolderPath + "/";
        }

        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTP);
        clientConfig.setMaxConnections(1024);
        AWSCredentials awsCredentials = new DefaultAWSCredentialsProviderChain().getCredentials();
        _S3 = new AmazonS3Client(awsCredentials, clientConfig);

    }


    private String formatKey(final Keys keys, final Dimensions dimensions) {

        final URI schemaUri = dimensions.getSchemaUri();
        final Context context = getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        Prototype keyedPrototype = null;
        final Set<URI> keyedSchemaUris = keys.getKeyedSchemaUris();

        if (schemaUri != null && keyedSchemaUris.contains(schemaUri)) {
            keyedPrototype = schemaLoader.getPrototype(schemaUri);
        }
        else {

            for (final URI keyedSchemaUri : keyedSchemaUris) {

                if (keys.getCount() > 1 && keyedSchemaUri.equals(schemaLoader.getDocumentSchemaUri())) {
                    // To promote de-coupling of REST API Design from this back-end storage Service, skip Document's URI if we can.
                    continue;
                }

                keyedPrototype = schemaLoader.getPrototype(keyedSchemaUri);
                break;
            }
        }

        if (keyedPrototype == null) {
            // Should never happen
            LOG.error("S3 Service unable to format key for keys: {} and dimensions: {}", keys, dimensions);
            return null;
        }

        final UniqueName keyedSchemaUniqueName = keyedPrototype.getUniqueName();
        final URI keyedSchemaUri = keyedPrototype.getSchemaUri();
        final Object keyValue = keys.getValue(keyedSchemaUri);
        final String keyValueString;

        if (keyValue instanceof URI) {
            final URI uri = (URI) keyValue;
            final String host = uri.getHost();
            if (host == null || host.trim().isEmpty()) {
                return null;
            }

            String keyFolderPath = host + "/";

            final int port = (uri.getPort() == -1) ? 80 : uri.getPort();
            keyFolderPath += String.valueOf(port) + "/";
            keyFolderPath += StringUtils.stripStart(uri.getPath(), "/");

            if (keyFolderPath.trim().isEmpty() || keyFolderPath.endsWith("/")) {
                keyFolderPath += "index";
            }

            keyValueString = keyFolderPath;
        }
        else {
            keyValueString = context.getSyntaxLoader().formatSyntaxValue(keyValue);
        }

        if (keyValueString == null || keyValueString.equals("null")) {
            LOG.error("S3 Service unable to format null key for keys: {} and dimensions: {}", keys, dimensions);
            return null;
        }

        final String key = _RootFolderPath + keyedSchemaUniqueName + "/" + keyValueString + ".json";
        return key;
    }

    private Model getModel(String s3Key, Keys keys, Dimensions dimensions) {

        LOG.debug("S3 Service getModel looking for: {}", s3Key);

        final GetObjectRequest getObjectRequest = new GetObjectRequest(_BucketName, s3Key);
        final S3Object s3Object;
        try {
            s3Object = _S3.getObject(getObjectRequest);
        }
        catch (AmazonS3Exception e) {
            LOG.debug(e.getMessage());
            return null;
        }

        final InputStream in = s3Object.getObjectContent();

        final Context context = getContext();

        final Model model;
        try {
            model = context.readModel(in, keys, dimensions, SystemFormat.json.getFormatUri());
        }
        catch (final ModelFormattingException e) {
            throw new ServiceException("Failed to read model.", e, this);
        }
        finally {
            IOUtils.closeQuietly(in);
        }

        LOG.debug("S3 Service getModel returning: {}", model);

        return model;
    }


    /*
    @Override
    public Set<Model> search(SearchCriteria searchCriteria) throws UnsupportedOperationException {
        String typeId = type.getSimpleName();
        String keyPrefix = typeId + "/";

        List<T> allObjectsList = new LinkedList<T>();

        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                    .withBucketName(_BucketName)
                    .withPrefix(keyPrefix);

            ObjectListing objectListing;

            do {
                objectListing = _S3.listObjects(listObjectsRequest);

                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    String key = objectSummary.getKey();
                    if (key.endsWith("/")) {
                        // Skip root keys
                        continue;
                    }
                    T object = getModel(key, type);
                    allObjectsList.add(object);
                }

                listObjectsRequest.setMarker(objectListing.getNextMarker());

            } while (objectListing.isTruncated());

        } catch (AmazonServiceException ase) {

            System.out.println("Caught an AmazonServiceException, " +
                    "which means your request made it " +
                    "to Amazon S3, but was rejected with an error response " +
                    "for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());

        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, " +
                    "which means the client encountered " +
                    "an internal error while trying to communicate" +
                    " with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }

        return allObjectsList;

    }

*/








}
