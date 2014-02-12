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
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.service.AbstractService;
import org.wrml.runtime.service.ServiceConfiguration;
import org.wrml.runtime.service.ServiceException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

public class GroovyTemplateService extends AbstractService {

    public static final String TEMPLATE_ROOT_LOCATION = "templateRoots";

    public static final String ROOTS_SEP = ",";

    private static final Logger LOG = LoggerFactory.getLogger(GroovyTemplateService.class);

    private List<String> _Roots;

    private GroovyClassLoader _Loader;

    private Map<URI, GroovyTemplate> _Templates;

    public Map<URI, GroovyTemplate> getTemplates() {

        return _Templates;
    }

    public void clearTemplates() {

        _Templates.clear();
    }

    public List<String> getRoots() {

        return _Roots;
    }

    @Override
    public Model get(Keys keys, Dimensions dimensions) {
        // Create an instance of the model
        Model model = getContext().newModel(dimensions);

        // Pass arguments to the engine, cross fingers
        GroovyTemplate template = _Templates.get(dimensions.getSchemaUri());
        if (template == null) {
            template = loadGroovyTemplate(dimensions.getSchemaUri());
            _Templates.put(dimensions.getSchemaUri(), template);
        }

        template.fill(getContext(), model, keys, dimensions);

        // Return the model
        return model;
    }

    private GroovyTemplate loadGroovyTemplate(URI schemaUri) {

        GroovyTemplate template = null;
        File scriptLocation;
        for (String rootPath : _Roots) {
            String scriptAttempt = rootPath + schemaUri;
            scriptLocation = new File(scriptAttempt);
            if (scriptLocation.exists() && scriptLocation.isFile()) {
                try {
                    template = getGroovyTemplate(scriptLocation);
                    break;
                }
                catch (Exception e) {
                    LOG.warn("Failed to load template from location {}, proceeding to next root.", new Object[]{rootPath});
                }
            }
            else {
                scriptLocation = null;
            }
        }

        if (template == null) {
            String message = "No template found to match the requested resource, " + schemaUri;
            throw new ServiceException(message, null, this);
        }

        return template;
    }

    private Class getGroovyClass(File location) {

        Class groovyTemplateClass;
        try {
            groovyTemplateClass = _Loader.parseClass(location);
        }
        catch (CompilationFailedException cfe) {
            String message = "Unable to compile target file [" + location + "]";
            LOG.error(message, cfe);
            throw new ServiceException(message, cfe, this);
        }
        catch (IOException ioe) {
            String message = "Unable to read/locate file [" + location + "]";
            LOG.error(message, ioe);
            throw new ServiceException(message, ioe, this);
        }

        return groovyTemplateClass;
    }

    private GroovyTemplate getGroovyTemplate(File scriptLocation) {

        Class groovyTemplateClass = getGroovyClass(scriptLocation);

        GroovyTemplate template;
        try {
            template = (GroovyTemplate) groovyTemplateClass.newInstance();
        }
        catch (InstantiationException ie) {
            String message = "Unable to instantiate instance of groovy template.";
            LOG.error(message, ie);
            throw new ServiceException(message, ie, this);
        }
        catch (IllegalAccessException iae) {
            String message = "Unable to access class for instantiation.";
            LOG.error(message, iae);
            throw new ServiceException(message, iae, this);
        }

        return template;
    }

    @Override
    protected void initFromConfiguration(final ServiceConfiguration config) {

        if (config == null) {
            final ServiceException e = new ServiceException("The config cannot be null.", null, this);
            LOG.error(e.getMessage(), e);
            throw e;
        }

        final Map<String, String> settings = config.getSettings();
        if (settings == null) {
            final ServiceException e = new ServiceException("The config settings cannot be null.", null, this);
            LOG.error(e.getMessage(), e);
            throw e;
        }

        String rootsString = settings.get(TEMPLATE_ROOT_LOCATION);
        if (rootsString == null || rootsString.equals("")) {
            LOG.error("No root path param passed to GroovyTemplateService.");
            throw new ServiceException("Unable to instantiate GroovyTemplateService, the template roots are null or empty.", null, this);
        }

        String[] rootStrings = rootsString.split(ROOTS_SEP);
        List<String> tempRoots = Arrays.asList(rootStrings);
        _Roots = new ArrayList<String>();

        for (String rootPath : tempRoots) {
            File root = new File(rootPath);
            if (root.exists() && root.isDirectory()) {
                _Roots.add(root.getAbsolutePath() + File.separator);
            }
            else {
                LOG.error("Removing {} from the list of roots, unable to locate.", new Object[]{rootPath});
            }
        }

        if (_Roots.isEmpty()) {
            throw new ServiceException("No viable roots configured.", null, this);
        }

        ClassLoader parent = getClass().getClassLoader();
        _Loader = new GroovyClassLoader(parent);
        _Templates = new HashMap<URI, GroovyTemplate>();
    }
}
