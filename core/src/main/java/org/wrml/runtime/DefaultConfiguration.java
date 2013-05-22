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
 * Copyright 2012 Mark Masse (OSS project WRML.org)
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

import java.util.Map;

public abstract class DefaultConfiguration implements Configuration
{

    private Map<String, String> _Settings;

    @SuppressWarnings("unchecked")
    public static final <T> T newInstance(final String className) throws ConfigurationException
    {

        if (className == null)
        {
            throw new ConfigurationException("The class name cannot be null", null, null);
        }

        Class<?> clazz;
        try
        {
            clazz = Class.forName(className);
        }
        catch (final ClassNotFoundException e)
        {
            throw new ConfigurationException("Failed to load class: " + className, e, null);
        }

        Object instance = null;
        try
        {
            instance = clazz.newInstance();
        }
        catch (final Exception e)
        {
            throw new ConfigurationException("Failed to create new instance of class: " + className, e, null);
        }

        return (T) instance;
    }

    @Override
    public final Map<String, String> getSettings()
    {

        return _Settings;
    }

    public final void setSettings(final Map<String, String> settings)
    {

        _Settings = settings;
    }


}
