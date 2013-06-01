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
package org.wrml.runtime.schema;

import org.wrml.runtime.DefaultFactoryConfiguration;

import java.io.File;
import java.net.URI;

/**
 * Configuration for the {@link SchemaLoader} component.
 */
public final class SchemaLoaderConfiguration extends DefaultFactoryConfiguration
{

    private File _SchemaClassRootDirectory;

    private URI[] _JsonSchemaIds;

    private File[] _JsonSchemaFiles;

    private File[] _JsonSchemaDirectories;

    public SchemaLoaderConfiguration()
    {

    }

    public File[] getJsonSchemaDirectories()
    {

        return _JsonSchemaDirectories;
    }

    public void setJsonSchemaDirectories(final File[] jsonSchemaDirectories)
    {

        _JsonSchemaDirectories = jsonSchemaDirectories;
    }

    public File[] getJsonSchemaFiles()
    {

        return _JsonSchemaFiles;
    }

    public void setJsonSchemaFiles(final File[] jsonSchemaFiles)
    {

        _JsonSchemaFiles = jsonSchemaFiles;
    }

    public URI[] getJsonSchemaIds()
    {

        return _JsonSchemaIds;
    }

    public void setJsonSchemaIds(final URI[] jsonSchemaIds)
    {

        _JsonSchemaIds = jsonSchemaIds;
    }

    public File getSchemaClassRootDirectory()
    {

        return _SchemaClassRootDirectory;
    }

    public void setSchemaClassRootDirectory(final File schemaClassRootDirectory)
    {

        _SchemaClassRootDirectory = schemaClassRootDirectory;
    }

}
