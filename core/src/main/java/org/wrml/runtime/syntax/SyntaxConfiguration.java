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
package org.wrml.runtime.syntax;

import java.net.URI;

import org.wrml.runtime.DefaultConfiguration;

public class SyntaxConfiguration extends DefaultConfiguration
{

    private URI _SyntaxUri;

    private String _HandlerClassName;

    public SyntaxConfiguration()
    {
    }

    public SyntaxConfiguration(final String syntaxUriString, final String syntaxHandlerClassName)
    {
        setSyntaxUri(URI.create(syntaxUriString));
        setHandler(syntaxHandlerClassName);
    }

    public String getHandler()
    {
        return _HandlerClassName;
    }

    public URI getSyntaxUri()
    {
        return _SyntaxUri;
    }

    public void setHandler(final String syntaxHandlerClassName)
    {
        _HandlerClassName = syntaxHandlerClassName;
    }

    public void setSyntaxUri(final URI syntaxUri)
    {
        _SyntaxUri = syntaxUri;
    }
}
