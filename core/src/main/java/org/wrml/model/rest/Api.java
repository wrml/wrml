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
import org.wrml.model.Titled;
import org.wrml.model.Versioned;
import org.wrml.runtime.rest.UriTemplate;
import org.wrml.runtime.schema.Title;
import org.wrml.runtime.schema.WRML;

import java.net.URI;
import java.util.List;

/**
 * <p>
 * The metadata associated with a Web/REST API modeled as a WRML {@link Document}.
 * </p>
 * <p/>
 * <p>
 * An {@link Api} identifies the root level {@link ResourceTemplate} of a tree of {@link ResourceTemplate} nodes. This
 * tree of {@link ResourceTemplate} nodes describes all of the available URI (or URI Template) paths provided by this
 * Api.
 * </p>
 * <p/>
 * <p>
 * Like arrows (----->) links have two ends, the starting point (left end) is embedded within a referring document and
 * the end point is a resource that is identified by a URI-based id (aka the endpoint document). The Api's list of
 * {@link LinkTemplate}s captures both of these ends for each link that is made available by an Api.
 * </p>
 * <p/>
 * <p>
 * When layered on top of the Api's {@link ResourceTemplate} tree, the Api's {@link LinkTemplate}s form a hypermedia
 * graph of interrelated ResourceTemplates.
 * </p>
 * <p/>
 * <p>
 * <i>(Hopefully that will make more sense in the GUI).</i>
 * </p>
 *
 * @see URI
 * @see URI#getPath()
 * @see UriTemplate
 */
@Title("API")
@WRML(comparableSlotNames = {"title", "version"})
public interface Api extends Titled, Versioned, Described, Document
{

    /**
     * <p>
     * The Api's root {@link ResourceTemplate}, which corresponds to the "forward slash" (/) at the root of the
     * {@link Api}'s {@link UriTemplate}-based tree structure.
     * </p>
     *
     * @return The docroot {@link ResourceTemplate}.
     * @see URI
     * @see UriTemplate
     */
    ResourceTemplate getDocroot();

    /**
     * <p>
     * The list of {@link LinkTemplate}s that describe all of the relationships between the {@link Api}'s
     * {@link ResourceTemplate}s.
     * </p>
     *
     * @return The list that holds the {@link Api}'s resource linkage.
     */
    List<LinkTemplate> getLinkTemplates();

    /**
     * @see Api#getDocroot()
     */
    ResourceTemplate setDocroot(ResourceTemplate docroot);

}
