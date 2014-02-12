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
package org.wrml.model.rest;

import org.wrml.model.Described;
import org.wrml.model.Model;
import org.wrml.model.UniquelyIdentified;
import org.wrml.model.Versioned;
import org.wrml.runtime.rest.UriTemplate;

import java.net.URI;
import java.util.List;

/**
 * <p>
 * A WRML-based REST API, as expressed via {@link Api}, is a tree of resource templates. Thus a {@link ResourceTemplate}
 * is a single "node" in a REST API's path-based resource hierarchy.
 * </p>
 * <p/>
 * <p>
 * A resource template tree can be visually represented in a UI as a "tree view" of (possibly templated) URI path
 * segments. Like this:
 * </p>
 * <p/>
 * <p>
 * <p/>
 * <pre>
 *
 *  /A
 *   |
 *   |
 *   +---- /{B}
 *           |
 *           |
 *           +---- /C
 *                  |
 *                  |
 *                  +---- /{D}
 *
 * </pre>
 * <p/>
 * </p>
 * <p/>
 * <p>
 * Which maps one-to-one with this "flatter" one-line representation:
 * </p>
 * <p/>
 * <p>
 * <p/>
 * <pre>
 * /A/{B}/C/{D}
 * </pre>
 * <p/>
 * </p>
 * <p/>
 * <p>
 * Which starts to look a lot like a four segment long templated URI path.
 * </p>
 * <p/>
 * <p>
 * The {@link ResourceTemplate} class models this path hierarchy by having each {@link ResourceTemplate} contribute its
 * own <i>path segment</i> value. The value of the segment may be templated with the popular <a
 * href="http://tools.ietf.org/html/rfc6570">UriTemplate</a> syntax for '{' variable '}' substitution. It is the
 * possibility of "templated" URI path segments applies to this class's path segment, which is where the "template" part
 * of its name comes from.
 * </p>
 *
 * @see URI
 * @see UriTemplate
 * @see UniquelyIdentified
 */
public interface ResourceTemplate extends UniquelyIdentified, Versioned, Described, Model {

    /**
     * <p>
     * The resource templates that are hierarchically beneath this resource template; this resource template's
     * "subresource templates".
     * </p>
     *
     * @return The subresource templates.
     */
    List<ResourceTemplate> getChildren();

    URI getDefaultSchemaUri();

    /**
     * <p>
     * The <i>segment</i> (i.e. part/portion) of the {@link URI}/{@link UriTemplate} path associated with this
     * {@link ResourceTemplate}. The segment of <code>/A</code> is "A". The segment of <code>/{B}</code> is "{B}".
     * </p>
     */
    String getPathSegment();

    URI setDefaultSchemaUri(URI defaultSchemaUri);

    /**
     * @see #getPathSegment()
     */
    String setPathSegment(String pathSegment);
}
