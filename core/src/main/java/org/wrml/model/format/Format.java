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
package org.wrml.model.format;

import org.wrml.model.Described;
import org.wrml.model.Titled;
import org.wrml.model.UniquelyNamed;
import org.wrml.model.Versioned;
import org.wrml.model.rest.Document;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.rest.MediaType;
import org.wrml.runtime.schema.Description;
import org.wrml.runtime.schema.WRML;

import java.net.URI;
import java.util.List;

/**
 * <a href="http://www.wrml.org/mediaType">WRML's media type design</a> allows
 * for formats to be pluggable via a {@link URI} reference to a {@link Document} conforming to this {@link Schema}.
 *
 * @see <a href="http://www.wrml.org/format">http://www.wrml.org/format</a>
 * @see <a href="http://blog.programmableweb.com/2011/11/18/rest-api-design-putting-the-type-in-content-type/">
 *      ProgrammableWeb.com blog post</a>
 */
@WRML(keySlotNames = {"uniqueName"}, comparableSlotNames = {"uniqueName"})
@Description("The WRML runtime allows for formats to be pluggable via a URI reference to a Document conforming to this Schema.")
public interface Format extends Titled, Versioned, Described, UniquelyNamed, Document
{

    /**
     * The WRML constant name for a Format's <i>homePageUri</i> slot.
     */
    public static final String SLOT_NAME_HOME_PAGE_URI_NAME = "homePageUri";

    /**
     * The format's optional aka media types.
     *
     * @return The media types associated with this same format.
     */
    @Description("The Format's optional aka media types.")
    List<MediaType> getAliasMediaTypes();

    /**
     * The {@link URI} of the format's home page (e.g. <a
     * href="http://www.json.org">http://www.json.org</a>)
     *
     * @return the format's home page URI.
     */
    @Description("The URI of the Format's home page.")
    URI getHomePageUri();

    /**
     * The format's media type (e.g. application/json).
     *
     * @return the media type associated with this format.
     * @see <a href="http://www.iana.org/assignments/media-types/index.html">IANA Media Type Registry</a>
     */
    @Description("The Format's media type (e.g. application/json).")
    MediaType getMediaType();

    /**
     * The format's RFC resource identifier (e.g. <a
     * href="http://www.ietf.org/rfc/rfc4627.txt?number=4627"
     * >http://www.ietf.org/rfc/rfc4627.txt?number=4627</a>)
     *
     * @return the format's RFC URI.
     */
    @Description("The Format's RFC resource identifier.")
    URI getRfcPageUri();

    /**
     * The format's associated file extension; without a '.' (or other) prefix.
     *
     * @return The format's associated file extension; without a '.' (or other) prefix.
     */
    @Description("The Format's associated file extension; without a '.' (or other) prefix.")
    String getFileExtension();

    /**
     * @see #getHomePageUri()
     */
    URI setHomePageUri(URI homePageUri);

    /**
     * @see #getMediaType()
     */
    MediaType setMediaType(MediaType mediaType);

    /**
     * @see #getRfcPageUri()
     */
    URI setRfcPageUri(URI rfcPageUri);


    String setFileExtension(String fileExtension);

}
