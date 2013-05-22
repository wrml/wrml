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
package org.wrml.model.schema;

import org.wrml.model.rest.*;
import org.wrml.model.rest.Method;

import java.net.URI;
import java.util.List;

/**
 * <p>
 * A {@link LinkValue} provides the schematics for a {@link Schema}'s {@link Link} {@link Slot} value.
 * </p>
 *
 * @see Link
 * @see LinkRelation
 * @see Schema
 * @see Slot
 * @see Api
 * @see Document
 * @see org.wrml.runtime.schema.LinkSlot
 */
public interface LinkValue extends Primitive, Inextensible, Value
{

    /**
     * <p>
     * The {@link URI} id of the {@link LinkRelation} associated with this {@link LinkValue}.
     * </p>
     *
     * @see LinkRelation#getUri()
     * @see Document
     * @see Link
     * @see LinkTemplate
     */
    URI getLinkRelationUri();

    /**
     * <p>
     * An <i>optional</i> {@link URI} that identifies the {@link Schema} of models that are permitted as a (request
     * body) argument when "interacting" with the {@link Link} stored in this value.
     * </p>
     * <p/>
     * <p>
     * NOTE: The {@link Schema} id indicated here takes precedence over the {@link Schema} id returned from a call to
     * this {@link LinkValue}'s associated {@link LinkRelation#getRequestSchemaUri()}.
     * </p>
     *
     * @see Schema#getUri()
     * @see LinkRelation#getRequestSchemaUri()
     * @see LinkTemplate#getRequestSchemaUri()
     */
    URI getRequestSchemaUri();

    /**
     * <p>
     * An <i>optional</i> {@link URI} that identifies the {@link Schema} of models that may be found in a
     * response associated with interactions that use {@link Link}s <i>held</i> in this {@link LinkValue}.
     * </p>
     * <p/>
     * <p>
     * NOTE: The {@link Schema} id indicated here takes precedence over the {@link Schema} id returned from a call to
     * this {@link LinkValue}'s associated {@link LinkRelation#getResponseSchemaUri()}.
     * </p>
     *
     * @see Schema#getUri()
     * @see LinkRelation#getResponseSchemaUri()
     * @see LinkTemplate#getResponseSchemaUri()
     */
    URI getResponseSchemaUri();

    /**
     * <p>
     * Schematically, embedded link values are used to represent an embedded Document relationship.
     * </p>
     * <p/>
     * <p>
     * The runtime attempts to get the linked Document when getting the Document with an embedded link. If successful,
     * the embedded Document model is placed in the {@link Link#getDoc()} slot. On failure, either a (non-Document)
     * Model representing an error or <code>null</code> will be placed in this {@link Link} slot.
     * </p>
     * <p/>
     * <p>
     * <b>NOTE:</b> Embedded links are only supported for {@link LinkRelation}'s utilizing {@link Method#Get}
     * </p>
     */
    boolean isEmbedded();

    /**
     * The optional list of {@link LinkValueBinding}s that convey how this {@link LinkValue}'s instance {@link Link}s will
     * form their "href" values.
     *
     * @return The optional list of {@link LinkValueBinding}s that convey how this {@link LinkValue}'s instance {@link Link}s will
     *         form their "href" values.
     * @see org.wrml.model.rest.Link#getHref()
     */
    List<LinkValueBinding> getBindings();

    /**
     * @see #getLinkRelationUri()
     */
    URI setLinkRelationUri(URI linkRelationUri);

    /**
     * @see #getRequestSchemaUri()
     */
    URI setRequestSchemaUri(URI requestSchemaUri);

    /**
     * @see #getResponseSchemaUri()
     */
    URI setResponseSchemaUri(URI responseSchemaUri);

    /**
     * @see #isEmbedded()
     */
    boolean setEmbedded(boolean isEmbedded);


}
