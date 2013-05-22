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
package org.wrml.model.rest;

import org.wrml.model.Model;
import org.wrml.model.schema.LinkValue;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.rest.UriTemplate;

import java.net.URI;
import java.util.UUID;

/**
 * <p>
 * A {@link LinkTemplate} exists within a specific {@link Api}'s design metadata. An {@link Api}'s
 * {@link ResourceTemplate}s are the nodes in a web-like graph and the {@link LinkTemplate}s hold the API's hyperlinked
 * resource graph together.
 * </p>
 * <p/>
 * <p>
 * A {@link LinkTemplate} captures the metadata associated with the design-time linking of two {@link ResourceTemplate}
 * s. Visually, two {@link ResourceTemplate}s, <code>/R</code> and <code>/E</code>, are the two ends of the
 * {@link LinkTemplate}'s ASCII arrow representation <code>/R ----> /E</code>, where <code>/R</code> is the referrer and
 * <code>/E</code> is the end-point ("pointy-end" of the link).
 * </p>
 * <p/>
 * <p>
 * As a webbed graph, each resource node has links pointing to it (with its Document.id in a link's href) and also may
 * be capable of generating representational {@link Document}s which may contain references, using {@link Link}s to
 * other nodes. From a {@link LinkTemplate}'s point of view, it is connecting a referrer to its end point.
 * </p>
 * <p/>
 * <p>
 * Conceptually, a "link" starts from a resource because it is embedded in some specific document model that is
 * associated/connected with that resource. This connection is often because the link's associated document model is a
 * representation of the resource's state. This starting model is the link's referrer.
 * </p>
 * <p/>
 * <p>
 * A link ends with its pointy-end pointing to the end point. This end point is the resource that the referrer resource
 * was referencing with its link.
 * </p>
 *
 * @see Api
 * @see ResourceTemplate
 * @see UriTemplate
 * @see URI
 * @see Link
 * @see LinkRelation
 * @see org.wrml.runtime.schema.LinkSlot
 * @see LinkValue
 * @see Schema
 * @see Document
 * @see <a href="http://en.wikipedia.org/wiki/HATEOAS">http://en.wikipedia.org/wiki/HATEOAS</a>
 */
public interface LinkTemplate extends Model
{

    /**
     * If this {@link LinkTemplate} describes <code>/R ----> /E</code>, then this method returns <code>/E</code>'s
     * id.
     *
     * @return {@link ResourceTemplate} <code>/E</code>'s id
     */
    UUID getEndPointId();

    /**
     * Get the URI associated with the {@link LinkRelation}, which describes the
     * relationship between our two {@link ResourceTemplate}s.
     *
     * @return the URI of the LinkRelation.
     */
    URI getLinkRelationUri();

    /**
     * If this {@link LinkTemplate} describes <code>/R ----> /E</code>, then this method returns <code>/R</code>'s
     * id.
     *
     * @return {@link ResourceTemplate} <code>/R</code>'s id
     */
    UUID getReferrerId();

    /**
     * An <i>optional</i> {@link URI} that identifies the {@link Schema} that may be passed in a request
     * associated with this {@link LinkTemplate}. If no values is specified, the
     * {@link LinkRelation#getRequestSchemaUri()} value is assumed to apply to this {@link LinkTemplate}.
     *
     * @see LinkRelation#getResponseSchemaUri()
     */
    URI getRequestSchemaUri();

    /**
     * An <i>optional</i> {@link URI} that identifies the {@link Schema} that may be found in a response
     * associated with this {@link LinkTemplate}. If no value is specified, the
     * {@link LinkRelation#getResponseSchemaUri()} value is assumed to apply to this {@link LinkTemplate}.
     *
     * @see LinkRelation#getResponseSchemaUri()
     */
    URI getResponseSchemaUri();

    /**
     * If this {@link LinkTemplate} describes <code>/R ----> /E</code>, then this method sets <code>/E</code>'s
     * id.
     *
     * @param endPointId The id of our end point {@link ResourceTemplate}, <code>/E</code>.
     * @return <code>/E</code>'s previous id
     */
    UUID setEndPointId(UUID endPointId);

    /**
     * Set the id associated with the {@link LinkRelation}, which describes the
     * relationship between our two {@link ResourceTemplate}s.
     *
     * @return The uri of the {@link LinkRelation}.
     */
    URI setLinkRelationUri(URI linkRelationUri);

    /**
     * If this {@link LinkTemplate} describes <code>/R ----> /E</code>, then this method sets <code>/R</code>'s
     * id.
     *
     * @param referrerId The id of our referrer (or starting point) {@link ResourceTemplate}, <code>/R</code>.
     * @return <code>/R</code>'s previous id
     */
    UUID setReferrerId(UUID referrerId);

    /**
     * @see #getRequestSchemaUri()
     */
    URI setRequestSchemaUri(URI requestSchemaUri);

    /**
     * @see #getResponseSchemaUri()
     */
    URI setResponseSchemaUri(URI responseSchemaUri);

}
