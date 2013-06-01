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

import org.wrml.runtime.rest.SystemApi;
import org.wrml.util.UniqueName;

public enum SystemSyntax
{

    Boolean(new BooleanSyntaxHandler()),
    Date(new DateSyntaxHandler()),
    Double(new DoubleSyntaxHandler()),
    File(new FileSyntaxHandler()),
    Integer(new IntegerSyntaxHandler()),
    Locale(new LocaleSyntaxHandler()),
    Long(new LongSyntaxHandler()),
    MediaType(new MediaTypeSyntaxHandler()),
    UniqueName(new UniqueNameSyntaxHandler()),
    URI(new UriSyntaxHandler()),
    URL(new UrlSyntaxHandler()),
    UUID(new UuidSyntaxHandler());

    private final SyntaxHandler<?> _SyntaxHandler;

    private final UniqueName _UniqueName;

    private final java.net.URI _SyntaxUri;

    private SystemSyntax(final SyntaxHandler<?> syntaxHandler)
    {
        _SyntaxHandler = syntaxHandler;
        final String localName = toString();
        _UniqueName = new UniqueName(Constants.NAMESPACE, localName);
        final java.net.URI relativeUri = java.net.URI.create("/" + _UniqueName.toString());
        _SyntaxUri = SystemApi.Syntax.getUri().resolve(relativeUri);
    }

    public SyntaxHandler<?> getSyntaxHandler()
    {
        return _SyntaxHandler;
    }

    public java.net.URI getSyntaxUri()
    {
        return _SyntaxUri;
    }

    public UniqueName getUniqueName()
    {
        return _UniqueName;
    }

    /**
     * It seems crazy ironic that java requires this kind of hackery to allow the Enum constrtor to access static
     * constants...
     */
    public static interface Constants
    {

        public static final UniqueName NAMESPACE = new UniqueName("org/wrml/syntax");

    }
}
