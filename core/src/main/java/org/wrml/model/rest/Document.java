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

import org.wrml.model.Abstract;
import org.wrml.model.Model;
import org.wrml.runtime.Keys;
import org.wrml.runtime.schema.Description;
import org.wrml.runtime.schema.LinkSlot;
import org.wrml.runtime.schema.Title;
import org.wrml.runtime.schema.WRML;
import org.wrml.runtime.service.rest.RestService;

import java.net.URI;

/**
 * <p>
 * A {@link Document} is a {@link Model} that has an {@link URI} key slot ({@link Document#getUri()}) used to identify the model on the World Wide Web (or other URI-based
 * keyspace).
 * </p>
 * <p>
 * <b>NOTE:</b> <code>Document.id</code> is a key slot for all {@link Document}s. The WRML runtime uses {@link Api} metadata to decipher a {@link Document} model's
 * surrogate/internal keys and includes them all in the model's associated {@link Keys}.
 * </p>
 *
 * @see Api
 * @see RestService
 * @see Keys
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616.html">Hypertext Transfer Protocol -- HTTP/1.1</a>
 * @see <a href="http://www.w3.org/DesignIssues/Axioms.html#uri">Universal Resource Identifiers -- Axioms of Web Architecture</a>
 */
@WRML(keySlotNames = "uri")
@Description("A Document is a model that has a URI key slot used to identify the model on the World Wide Web (or other URI-based keyspace).")
public interface Document extends Abstract
{

    static final long serialVersionUID = 1L;

    /**
     * The WRML constant name for a Document's <i>uri</i> slot.
     */
    public static final String SLOT_NAME_URI = "uri";

    @LinkSlot(linkRelationUri = "http://relation.api.wrml.org/org/wrml/relation/delete", method = Method.Delete)
    @Description("Deletes this Document.")
    void delete();

    /**
     * <p>
     * Gets an opaque string representation of the {@link Document}'s current state.
     * </p>
     * <p>
     * HTTP/1.1 uses <b><a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">Entity Tags</a></b> to compare two or more "entities"; or {@link Document} model
     * instances with the same URI ( <code>Document.id</code> ). Entity tag values are used in the <code>ETag</code>, <code>If-Match</code>, <code>If-None-Match</code>, and
     * <code>If-Range</code> header fields.
     * </p>
     *
     * @return The "entity tag" string for this instance.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP/1.1 Entity Tags</a>
     */
    @Description("Gets an opaque string representation of the Document's current state.")
    String getCacheTag();

    /**
     * <p>
     * As a representation of some server-owned resource's state, a document's slot values may change at any time (within the system of record). This method returns the number of
     * seconds that it is safe to cache this document's representation without concern for changes taking place at its resource origin.
     * </p>
     *
     * @return The number of seconds that this document's representation may be considered "fresh".
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21">HTTP/1.1 Expires Header</a>
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13">Caching in HTTP/1.1</a>
     */
    @Description("As a representation of some server-owned resource's state, a Document's slot values may change at any time (within the system of record). This method returns the number of seconds that it is safe to cache this Document's representation without concern for changes taking place at its resource origin.")
    Long getSecondsToLive();

    @LinkSlot(linkRelationUri = "http://relation.api.wrml.org/org/wrml/relation/self", method = Method.Get)
    @Description("Get this Document (refresh).")
    Document getSelf();

    /**
     * <p>
     * Get's this {@link Document}'s "universal document identifier" as a {@link URI}.
     * </p>
     * <p>
     * <div style="font-family: Times New Roman, Times, serif; font-size: 1.5em;" > "Well, the most important thing that was new was the idea of URI or URL [it was UDI back then,
     * universal document identifier]. The idea that any piece of information anywhere should have an identifier, which will not only identify it, but allow you to get hold of it.
     * That idea was the basic clue to the universality of the Web. That was the only thing I insisted upon." </div>
     * </p>
     * <p>
     * <b><i>--&nbsp;<a href="http://www.wired.com/science/discoveries/news/1999/10/31830?currentPage=all">Tim Berners-Lee</a></b></i>
     * </p>
     */
    @Title("URI")
    @Description("Get's this Document's \"universal document identifier\" as a URI.")
    URI getUri();

    @LinkSlot(linkRelationUri = "http://relation.api.wrml.org/org/wrml/relation/save", method = Method.Save)
    @Description("Saves this Document.")
    Document save();

    /**
     * @see #getCacheTag()
     */
    String setCacheTag(String tag);

    /**
     * @see #getSecondsToLive()
     */
    Long setSecondsToLive(Long secondsToLive);

    /**
     * @see #getUri()
     */
    URI setUri(URI uri);

}
