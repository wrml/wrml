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
package org.wrml.runtime.rest;

import java.net.URI;

import org.wrml.model.rest.Method;
import org.wrml.util.UniqueName;

/**
 * The WRML built-in link relations.
 */
public enum SystemLinkRelation
{

    create(Method.Invoke),     // C.
    self(Method.Get),          // R.
    save(Method.Save),         // U.
    delete(Method.Delete),     // D.
    metadata(Method.Metadata), // HEAD
    options(Method.Options),   // OPTIONS
    element(Method.Get),
    child(Method.Get),

    // TODO: This (autoLink) is Api Designer tool/app specific. Move this to a non-system LinkRelation (resource file?)
    autoLink(Method.Get),
    home(Method.Get),
    invoke(Method.Invoke);

    private final Method _Method;

    private final UniqueName _UniqueName;

    private final URI _Uri;

    private SystemLinkRelation(final Method method)
    {
        _Method = method;
        final String localName = toString();
        _UniqueName = new UniqueName(Constants.NAMESPACE, localName);
        final java.net.URI relativeUri = java.net.URI.create("/" + _UniqueName.toString());
        _Uri = SystemApi.LinkRelation.getUri().resolve(relativeUri);
    }

    public Method getMethod()
    {
        return _Method;
    }

    public UniqueName getUniqueName()
    {
        return _UniqueName;
    }

    public URI getUri()
    {
        return _Uri;
    }

    public static interface Constants
    {

        public static final UniqueName NAMESPACE = new UniqueName("org/wrml/relation");

    }

}
