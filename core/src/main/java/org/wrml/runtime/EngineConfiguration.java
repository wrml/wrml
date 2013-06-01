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
package org.wrml.runtime;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.util.AsciiArt;
import org.wrml.util.PropertyUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * WRML engine configuration. By default, looks for a {@link #DEFAULT_WRML_CONFIGURATION_FILE_NAME} on the classpath, but can be overridden by specifying
 * <code>-D{@link #WRML_CONFIGURATION_FILE_PATH_PROPERTY_NAME wrmlConfiguration}=<i>file.json</i></code>.
 */
public final class EngineConfiguration extends DefaultConfiguration
{

    public static final String WRML_CONFIGURATION_FILE_PATH_PROPERTY_NAME = "wrmlConfiguration";

    public final static String DEFAULT_WRML_CONFIGURATION_FILE_NAME = "wrml.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineConfiguration.class);

    private ContextConfiguration _ContextConfiguration;

    // Privatize constructor
    private EngineConfiguration()
    {

    }

    public final static EngineConfiguration load() throws IOException
    {

        String fileName = PropertyUtil.getSystemProperty(WRML_CONFIGURATION_FILE_PATH_PROPERTY_NAME);
        if (fileName != null)
        {
            return EngineConfiguration.load(fileName);
        }

        return EngineConfiguration.load((File) null);
    }

    public final static EngineConfiguration load(final File fileOrDirectory) throws IOException
    {

        File configFile = fileOrDirectory;

        if (fileOrDirectory == null)
        {
            // Check the system property
            String fileName = PropertyUtil.getSystemProperty(WRML_CONFIGURATION_FILE_PATH_PROPERTY_NAME);
            if (fileName != null)
            {
                configFile = FileUtils.getFile(fileName);
            }
            else
            {
                // Look in the local directory for the configuration file with the default name
                configFile = FileUtils.getFile(new File("."), DEFAULT_WRML_CONFIGURATION_FILE_NAME);

                if (!configFile.exists())
                {
                    configFile = FileUtils.getFile(FileUtils.getUserDirectory(), DEFAULT_WRML_CONFIGURATION_FILE_NAME);
                }
            }
        }
        else if (fileOrDirectory.isDirectory())
        {
            // Named a directory (assume default file name)
            configFile = FileUtils.getFile(fileOrDirectory, DEFAULT_WRML_CONFIGURATION_FILE_NAME);
        }

        if (!configFile.exists())
        {
            throw new FileNotFoundException("The path \"" + configFile.getAbsolutePath() + "\" does not exist.");
        }

        LOGGER.trace("loading EngineConfiguration from  '{}'...", configFile);
        final InputStream in = FileUtils.openInputStream(configFile);
        final EngineConfiguration config = EngineConfiguration.load(in);
        IOUtils.closeQuietly(in);
        LOGGER.debug("loaded EngineConfiguration from  '{}'", configFile);
        return config;
    }

    /**
     * Load the EngineConfiguration from a named resource owned by the identified class.
     */
    public final static EngineConfiguration load(final Class<?> resourceOwner, final String resourceName) throws IOException
    {
        final URL resource = resourceOwner.getResource(resourceName);
        LOGGER.trace("loading EngineConfiguration from  '{}' [{}]...", resourceName, resource);
        final EngineConfiguration result = EngineConfiguration.load(resourceOwner.getResourceAsStream(resourceName));
        LOGGER.debug("loaded EngineConfiguration from  '{}'", resource);
        return result;
    }

    public final static EngineConfiguration load(final File directory, final String fileName) throws IOException
    {

        return EngineConfiguration.load(FileUtils.getFile(directory, fileName));
    }

    public final static EngineConfiguration load(final InputStream in) throws IOException
    {

        return new ObjectMapper().readValue(in, EngineConfiguration.class);
    }

    public final static EngineConfiguration load(final String filePath) throws IOException
    {

        final File fileOrDirectory = (filePath != null) ? FileUtils.getFile(filePath) : null;
        return EngineConfiguration.load(fileOrDirectory);
    }

    public final static EngineConfiguration load(final URI configLocation) throws IOException
    {

        return EngineConfiguration.load(configLocation.toURL());
    }

    public final static EngineConfiguration load(final URL configLocation) throws IOException
    {
        // http://www.unitconversion.org/time/seconds-to-milliseconds-conversion.html

        final int connectionTimeoutMilliseconds = 10000;
        final int readTimeoutMilliseconds = 10000;
        final File tempFile = FileUtils.getFile(FileUtils.getTempDirectory(), EngineConfiguration.DEFAULT_WRML_CONFIGURATION_FILE_NAME);
        FileUtils.copyURLToFile(configLocation, tempFile, connectionTimeoutMilliseconds, readTimeoutMilliseconds);
        return EngineConfiguration.load(tempFile);
    }

    public ContextConfiguration getContext()
    {

        return _ContextConfiguration;
    }

    public void setContext(final ContextConfiguration contextConfiguration)
    {

        _ContextConfiguration = contextConfiguration;
    }

    @Override
    public String toString()
    {

        return AsciiArt.express(this);
    }

}
