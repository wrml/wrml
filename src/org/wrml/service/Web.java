/**
 * Copyright (C) 2012 WRML.org <mark@wrml.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wrml.service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.wrml.model.Collection;
import org.wrml.model.Document;
import org.wrml.model.DocumentSearchCriteria;
import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.ModelReader;
import org.wrml.runtime.ModelReaderException;

/**
 * TODO: Note that this is "PoC quality" code.
 */
public final class Web implements CollectionService {

    private ModelReader _ModelReader;

    @Override
    public boolean addEventListener(ServiceEventListener eventListener) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <D extends Document> D create(final Collection<?> collection, final D document) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(final URI key) {

    }

    @Override
    public <M extends Model> M get(URI key, Dimensions dimensions) {

        M model = null;
        final HttpClient httpClient = new DefaultHttpClient();
        final HttpGet httpGet = new HttpGet(key);
        HttpResponse response;
        try {
            response = httpClient.execute(httpGet);
        }
        catch (final Exception e) {
            throw new ServiceException("Failed to execute HTTP GET request.", e, this);
        }

        final HttpEntity entity = response.getEntity();
        if (entity != null) {

            InputStream in;
            try {
                in = entity.getContent();
            }
            catch (final Exception e) {
                throw new ServiceException("Failed to read HTTP response content.", e, this);
            }

            try {

                model = _ModelReader.readModel(in, dimensions);
            }
            catch (final ModelReaderException e) {
                throw new ServiceException("Failed to read model graph from HTTP response input stream (URI = " + key
                        + ").", e, this);
            }
            finally {
                try {
                    in.close();
                }
                catch (final IOException e) {
                    throw new ServiceException("Failed to read close model graph input stream.", e, this);
                }
            }
        }
        return model;
    }

    @Override
    public <M extends Model> List<M> getMultiple(List<URI> keys, Dimensions dimensions) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object invoke(Object model, Method method, Object[] args) throws Throwable {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAvailable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeEventListener(ServiceEventListener eventListener) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void restart(Context context) {
        // TODO Auto-generated method stub

    }

    @Override
    public <D extends Document> Set<D> search(Collection<?> collection, DocumentSearchCriteria criteria,
            Dimensions dimensions) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void start(final Context context) {
        _ModelReader = new ModelReader(context);
    }

    @Override
    public void stop(final Context context) {
        _ModelReader = null;
    }

    @Override
    public <M extends Model> M update(final M model) {
        // TODO Auto-generated method stub
        return null;
    }

}
