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
package org.wrml.contrib.runtime.service.groovy;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.service.AbstractService;
import org.wrml.runtime.service.ServiceConfiguration;
import org.wrml.runtime.service.ServiceException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class EmbeddedGroovyService extends AbstractService
{
    public static final String SERVICE_ABS_LOCATION_KEY = "absGroovyService";

    public static final String SERVICE_RES_LOCATION_KEY = "resGroovyService";

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedGroovyService.class);

    private GroovyClassLoader _Loader;

    private GroovyServiceInterface _GroovyService;

    public GroovyServiceInterface getGroovyService()
    {

        return _GroovyService;
    }

    @Override
    public void initFromConfiguration(final ServiceConfiguration serviceConfig)
    {

        Map<String, String> config = serviceConfig.getSettings();
        File scriptLocation = null;
        String givenLocation;
        if (config.containsKey(SERVICE_ABS_LOCATION_KEY))
        {
            givenLocation = config.get(SERVICE_ABS_LOCATION_KEY);
            scriptLocation = new File(givenLocation);
        }
        else if (config.containsKey(SERVICE_RES_LOCATION_KEY))
        {
            givenLocation = config.get(SERVICE_RES_LOCATION_KEY);
            URL resourceLocation = getClass().getResource(givenLocation);
            if (resourceLocation == null)
            {
                throw new ServiceException("Service script specified not found. " + givenLocation, null, this);
            }
            scriptLocation = new File(resourceLocation.getFile());
        }
        else
        {
            throw new ServiceException("No service script specified to start EmbeddedGroovy.", null, this);
        }

        ClassLoader parent = getClass().getClassLoader();
        _Loader = new GroovyClassLoader(parent);

        Class groovyServiceClass;
        try
        {
            groovyServiceClass = _Loader.parseClass(scriptLocation);
        }
        catch (CompilationFailedException | IOException ex)
        {
            String message = "Unable to parse given script into class. " + scriptLocation;
            LOG.error(message, ex);
            throw new ServiceException(message, ex, this);
        }

        try
        {
            _GroovyService = (GroovyServiceInterface) groovyServiceClass.newInstance();
        }
        catch (InstantiationException | IllegalAccessException ex)
        {
            String message = "Unable to instantiate Groovy Service class " + scriptLocation + ".";
            LOG.error(message, ex);
            throw new ServiceException(message, ex, this);
        }

        // TODO, comment out
        if (_GroovyService == null)
        {
            throw new ServiceException("Unable to find given script name to load. " + scriptLocation, null, this);
        }
    }

    @Override
    public void delete(Keys keys, final Dimensions dimensions)
    {

        _GroovyService.delete(getContext(), keys);
    }

    @Override
    public Model get(Keys keys, Dimensions dimensions)
    {

        final Context context = getContext();
        // Create an instance of the model
        Model model = context.newModel(dimensions);

        _GroovyService.get(context, model, keys, dimensions);

        return model;
    }

    @Override
    public Model save(Model model)
    {

        model = _GroovyService.save(model);

        return model;
    }
}
