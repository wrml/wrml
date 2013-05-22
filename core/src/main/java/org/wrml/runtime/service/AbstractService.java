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

package org.wrml.runtime.service;

import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.search.SearchCriteria;

import java.util.Set;

/**
 * <p>
 * A very basic, partial implementation that provides an alternative to implementing the {@link Service} interface from scratch.
 * </p>
 * <p>
 * This abstract base class implements {@link Service#init(org.wrml.runtime.Context, ServiceConfiguration)} and provides field-based storage for the {@link Service}'s {@link Context} and {@link ServiceConfiguration}.
 * </p>
 * <p>
 * This abstract base class implements all of the <i>optional</i> {@link Service} methods by throwing an {@link UnsupportedOperationException} upon invocation.
 * </p>
 */
public abstract class AbstractService implements Service
{

    private Context _Context;

    private ServiceConfiguration _Config;

    @Override
    public final void init(final Context context, final ServiceConfiguration config)
    {

        if (context == null)
        {
            throw new ServiceException("The context cannot be null.", null, this);
        }

        _Context = context;
        _Config = config;
        initFromConfiguration(_Config);
    }

    @Override
    public final ServiceConfiguration getConfiguration()
    {

        return _Config;
    }

    @Override
    public final Context getContext()
    {

        return _Context;
    }

    @Override
    public void delete(final Keys keys, final Dimensions dimensions) throws UnsupportedOperationException
    {

        throwUnsupportedOperationException("delete");
    }

    @Override
    public Model save(final Model model) throws UnsupportedOperationException
    {

        throwUnsupportedOperationException("save");
        // Never reached
        return null;
    }

    @Override
    public Set<Model> search(final SearchCriteria searchCriteria) throws UnsupportedOperationException
    {

        throwUnsupportedOperationException("search");
        // Never reached
        return null;
    }

    @Override
    public Model invoke(final Model function, final Dimensions responseDimensions, final Model parameter) throws UnsupportedOperationException
    {

        throwUnsupportedOperationException("invoke");
        // Never reached
        return null;
    }

    /**
     * Hook for {@link Service} implementations to initialize from the specified {@link ServiceConfiguration}.
     *
     * @param config The {@link ServiceConfiguration} used to initialize this {@link Service} instance.
     */
    protected abstract void initFromConfiguration(final ServiceConfiguration config);

    /**
     * A simple utility function to throw an {@link UnsupportedOperationException} resulting from an invocation of the specified unsupported operation.
     *
     * @param operationName The name of the operation that is not supported.
     * @throws UnsupportedOperationException Always thrown as a result of calling this method.
     */
    protected final void throwUnsupportedOperationException(final String operationName) throws UnsupportedOperationException
    {

        String name = getClass().getSimpleName();

        throw new UnsupportedOperationException("The \"" + operationName + "\" operation is not supported by the \"" + name + "\" service.");
    }

}
