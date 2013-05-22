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
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.rest.UriTemplate;
import org.wrml.runtime.schema.WRML;

import java.net.URI;

/**
 * <p>
 * A runtime hyperlink that slots the information necessary to treat REST-based interactions as function calls.
 * </p>
 * <p/>
 * <h4>href - {@link #getHref()}</h4>
 * <p/>
 * <p>
 * The URI of the endpoint resource referenced by this {@link Link}. The WRML runtime uses a link's href value to
 * navigate APIs; to link Document-based models together with their own link slots (member functions).
 * </p>
 * <p/>
 * <h4>rel - {@link #getRel()}</h4>
 * <p/>
 * <p>
 * The URI of the link relation that adds some semantics to classify this interaction. A {@link LinkRelation} is a
 * reusable function description which is referenced by a {@link Link} for reuse-sake; to leverage the default values
 * with {@link LinkRelation#getRequestSchemaUri()} and {@link LinkRelation#getResponseSchemaUri()}.
 * </p>
 * <p/>
 * <h4>doc - {@link #getDoc()}</h4>
 * <p/>
 * <p>
 * For embedded links, this slot will (on successful retrieval) hold the {@link Document} found at the end point.
 * However, if the runtime encountered a problem embedding the Document, then this slot may contain either
 * <code>null</code> or a {@link Model} (perhaps non-{@link Document}) representing an error (or other status message).
 * </p>
 */
@WRML(comparableSlotNames = {"rel", "href"})
public interface Link extends Model
{

    /**
     * <p>
     * The <i>optional</i> embedded {@link Model} which resulted from a <i>reference</i> using this {@link Link}.
     * </p>
     *
     * @see Model#reference(String)
     * @see Context#visitLink(org.wrml.model.Model, String)
     * @see Dimensions#getEmbeddedLinkSlotNames()
     */
    // TODO: Should this be a Document (instead of Model). Current design allows for non-Document errors to be returned. Is this a good design choice?
    Model getDoc();

    /**
     * <p>
     * The URI of the endpoint resource referenced by this {@link Link}. The WRML runtime uses a {@link Link}'s href
     * value to navigate {@link Api}s; to link {@link Document}-based models together.
     * </p>
     *
     * @see org.wrml.model.rest.Document#getUri() ()
     * @see UriTemplate#evaluate(java.util.Map)
     * @see LinkTemplate
     * @see Api
     */
    URI getHref();

    /**
     * <p>
     * The URI of the link relation that adds some semantics to <i>classify</i> this interaction. A {@link LinkRelation}
     * is a reusable function description which is referenced by a {@link Link} for reuse-sake; to leverage the default
     * values with {@link LinkRelation#getRequestSchemaUri()} and {@link LinkRelation#getResponseSchemaUri()}.
     * </p>
     *
     * @see org.wrml.model.rest.LinkRelation#getUri()
     */
    URI getRel();

    /**
     * @see #getDoc()
     */
    Model setDoc(Model embedded);

    /**
     * @see #getHref()
     */
    URI setHref(URI href);

    /**
     * @see #getRel()
     */
    URI setRel(URI linkRelationUri);
}
