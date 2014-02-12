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
package org.wrml.runtime;

import org.wrml.model.Model;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.Link;
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.rest.LinkTemplate;
import org.wrml.model.schema.Schema;
import org.wrml.model.schema.Slot;
import org.wrml.runtime.service.Service;
import org.wrml.runtime.service.cache.ShardedModelCache;
import org.wrml.runtime.syntax.LocaleSyntaxHandler;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;

/**
 * <p>
 * Dimensions are the request parameters/options associated with a {@link Model}'s retrieval. The design of this POJO
 * class is intended to reflect and abstract the role of HTTP/1.1 headers in REST interactions designed to respond with
 * some representational state.
 * </p>
 * <p/>
 * <p>
 * WRML core methods used to retrieve a {@link Model} require a {@link Dimensions} parameter in order to
 * specify/customize some aspects of the sought-after {@link Model}. This design is roughly analogous to the HTTP/1.1
 * GET request message headers that are used to "dimension" a response representation.
 * </p>
 * <p/>
 * <p>
 * {@link Dimensions} role in a {@link Model}'s life-cycle, as a predecessor for the Model's first retrieval and as a
 * "request closures" enables the WRML runtime to "chain" request metadata when automating hypermedia-based interactions
 * between {@link Model}s.
 * </p>
 *
 * @see Context#getModel(Keys, Dimensions)
 * @see Model#getDimensions()
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">HTTP/1.1 Header Field Definitions</a>
 */
public interface Dimensions extends Serializable {

    /**
     * An <i>optional</i> map of metadata "headers" associated with these {@link Dimensions}.
     */
    SortedMap<String, String> getMetadata();

    /**
     * An <i>optional</i> map of request "query" associated with these {@link Dimensions}.
     *
     * @see java.net.URI#getQuery()
     */
    SortedMap<String, String> getQueryParameters();

    /**
     * An <i>optional</i> list of the {@link Link} {@link Slot} names that should be "referenced" by the {@link Context}
     * or
     * origin {@link Service} when retrieving the {@link Model} associated with these {@link Dimensions}.
     *
     * @see Link
     * @see LinkRelation
     * @see Slot
     * @see org.wrml.runtime.schema.LinkSlot
     */
    List<String> getEmbeddedLinkSlotNames();

    /**
     * <p>
     * An <i>optional</i> list of {@link Slot} names that should be excluded from the requested {@link Model} associated
     * with these {@link Dimensions}. If this list is not empty, it contains the list of slot names that should not have
     * values defined in the {@link Model}.
     * </p>
     * <p/>
     * <p>
     * NOTE: This feature is not supported yet. Need to figure out what it means to have <i>partial</i> {@link Model}s
     * in the heap.
     * </p>
     *
     * @see ShardedModelCache
     */
    List<String> getExcludedSlotNames();

    /**
     * <p>
     * An <i>optional</i> list of {@link Slot} names that should be included in the requested {@link Model} associated
     * with these {@link Dimensions}. If this list is not empty, it contains the listing of all slot names that should
     * have values defined in the {@link Model}.
     * </p>
     * <p/>
     * <p>
     * NOTE: This feature is not supported yet. Need to figure out what it means to have <i>partial</i> {@link Model}s
     * in the heap.
     * </p>
     */
    List<String> getIncludedSlotNames();

    /**
     * The <i>optional</i> {@link Locale} "view" of the {@link Model} associated with these {@link Dimensions}.
     *
     * @see LocaleSyntaxHandler
     */
    Locale getLocale();

    /**
     * The <i>optional</i> id of the {@link Document} that referenced the {@link Model} associated with these
     * {@link Dimensions}.
     *
     * @see Model#reference(String, DimensionsBuilder)
     * @see org.wrml.model.rest.Document#getUri()
     * @see LinkRelation
     * @see LinkTemplate
     */
    URI getReferrerUri();

    /**
     * <p>
     * The <b>required</b> {@link URI} id ({@link org.wrml.model.rest.Document#getUri()} ()}) associated with the {@link Schema} that describes
     * the structure of the {@link Model} to be retrieved with these {@link Dimensions}.
     * </p>
     *
     * @see Context#getModel(org.wrml.runtime.Keys, Dimensions)
     * @see Model
     * @see Schema
     * @see org.wrml.model.rest.Document#getUri()
     * @see <a href="http://www.wrml.org/schema">http://www.wrml.org/schema</a>
     * @see <a href="http://blog.programmableweb.com/2011/11/18/rest-api-design-putting-the-type-in-content-type/">REST
     * API Design: Put the "Type" in "Content-Type"</a>
     */
    URI getSchemaUri();

}
