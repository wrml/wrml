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
package org.wrml.runtime.service.rest;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.model.rest.Document;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.ModelWriteOptions;
import org.wrml.runtime.format.ModelWriterException;
import org.wrml.runtime.rest.RestUtils;
import org.wrml.runtime.service.AbstractService;
import org.wrml.runtime.service.Service;
import org.wrml.runtime.service.ServiceConfiguration;
import org.wrml.runtime.service.ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Set;

/**
 * The Web's uniform interface as a WRML {@link Service}.
 */
public class RestService extends AbstractService
{

    private static final Logger LOG = LoggerFactory.getLogger(RestService.class);

    private HttpClient _HttpClient;

    @Override
    public void delete(final Keys keys, final Dimensions dimensions)
    {

        final Context context = getContext();
        final URI uri = keys.getValue(context.getSchemaLoader().getDocumentSchemaUri());
        final HttpDelete httpDelete = new HttpDelete(uri);
        executeRequest(httpDelete);
    }

    @Override
    public Model get(final Keys keys, final Dimensions dimensions)
    {

        final Context context = getContext();
        final URI uri = keys.getValue(context.getSchemaLoader().getDocumentSchemaUri());

        final Set<Header> requestHeaders = RestUtils.extractRequestHeaders(context, dimensions);

        final HttpGet httpGet = new HttpGet(uri);

        final Header[] requestHeaderArray = new Header[requestHeaders.size()];
        httpGet.setHeaders(requestHeaders.toArray(requestHeaderArray));
        final HttpResponse response = executeRequest(httpGet);

        final Dimensions responseDimensions = RestUtils.extractResponseDimensions(context, response,
                dimensions);

        return readResponseModel(response, context, keys, responseDimensions);
    }

    @Override
    public Model save(final Model model)
    {

        final Document document = (Document) model;
        final URI uri = document.getUri();

        final HttpPut httpPut = new HttpPut(uri);

        final Context context = model.getContext();

        final ModelContentProducer httpWriter = new ModelContentProducer(context, null, model);
        httpPut.setEntity(new EntityTemplate(httpWriter));

        final HttpResponse response = executeRequest(httpPut);
        final Dimensions responseDimensions = RestUtils.extractResponseDimensions(context, response,
                model.getDimensions());

        return readResponseModel(response, model.getContext(), model.getKeys(), responseDimensions);
    }

    @Override
    protected void initFromConfiguration(final ServiceConfiguration config)
    {

        final SchemeRegistry schemeRegistry = new SchemeRegistry();

        // Tips on HttpClient performance: http://hc.apache.org/httpclient-3.x/performance.html

        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

        // TODO: Make configurable
        final PoolingClientConnectionManager _ConnectionManager = new PoolingClientConnectionManager(schemeRegistry);
        _ConnectionManager.setMaxTotal(200);
        _ConnectionManager.setDefaultMaxPerRoute(20);

        _HttpClient = new DefaultHttpClient(_ConnectionManager);
    }

    private HttpResponse executeRequest(final HttpRequestBase request)
    {

        LOG.debug("Making outgoing request {}", request);

        final HttpResponse response;
        try
        {
            response = _HttpClient.execute(request);
        }
        catch (final IOException e)
        {
            LOG.error("Failed to execute HTTP request: " + request.getClass().toString() + " to " + request.getURI(), e);
            throw new ServiceException("Failed to execute HTTP PUT request.", e, this);
        }

        LOG.debug("Received status code: {} in response to update request.",
                response.getStatusLine() != null ? response.getStatusLine().getStatusCode() : "NULL");

        if (response.getStatusLine().getStatusCode() / 100 != 2)
        {
            // Anything in the 300, 400, or 500 ranges will go here

            final String errorMessage = "Error: (" + response.getStatusLine().getStatusCode() + ") - "
                    + request.getURI() + "\n" + response.getStatusLine().getReasonPhrase();
            LOG.error(errorMessage);

            throw new ServiceException(errorMessage, null, this);
        }

        return response;

    }

    private <M extends Model> M readResponseModel(final HttpResponse response, final Context context, final Keys keys,
                                                  final Dimensions dimensions)
    {

        if (response == null)
        {
            return null;
        }

        final HttpEntity entity = response.getEntity();
        if (entity == null)
        {
            return null;
        }

        // TODO: Initialize this from the response content-type header? As is, null will default to the default format.
        final URI formatUri = null;

        try
        {
            final InputStream in = entity.getContent();
            return context.readModel(in, keys, dimensions, formatUri);
        }
        catch (final IllegalStateException | IOException e)
        {
            throw new ServiceException("Failed to read HTTP response content.", e, this);
        }
        finally
        {
            EntityUtils.consumeQuietly(entity);
        }
    }

    class ModelContentProducer implements ContentProducer
    {

        private final Context _Context;

        private final ModelWriteOptions _WriteOptions;

        private final Model _Model;

        /**
         * Initialize this with the model writer and model that it will use
         */
        public ModelContentProducer(final Context context, final ModelWriteOptions writeOptions, final Model model)
        {

            _Context = context;
            _Model = model;
            _WriteOptions = writeOptions;
        }

        @Override
        public void writeTo(final OutputStream stream) throws IOException
        {

            try
            {
                // TODO: How best to determine the best Format URI to pass here?
                // Use API metadata?
                // Remember/store the Format URI that was used when the Model was read from the origin?
                _Context.writeModel(stream, _Model, _WriteOptions, null);
            }
            catch (final ModelWriterException e)
            {
                e.printStackTrace();
                throw new IOException("Error writing the given model out to the stream provided.", e);
            }
        }

    }

}
