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
package org.wrml.runtime.format;

import org.wrml.runtime.rest.MediaType;
import org.wrml.runtime.rest.SystemApi;
import org.wrml.util.UniqueName;

import java.net.URI;

public enum SystemFormat {

    json(new UniqueName("application/json"),
            "The WRML System Format representing the \"application/json\" media type.",
            URI.create("http://www.json.org"),
            URI.create("http://www.ietf.org/rfc/rfc4627.txt?number=4627"),
            "json"),

    xml(new UniqueName("application/xml"),
            "The WRML System Format representing the \"application/xml\" media type.",
            URI.create("http://www.xml.com"),
            URI.create(""),
            "xml"),

    html(new UniqueName("text/html"),
            "The WRML System Format representing the \"text/html\" media type.",
            URI.create("http://www.w3.org/html"),
            URI.create("http://www.ietf.org/rfc/rfc2854.txt"),
            "html"),

    json_schema(new UniqueName("application/schema+json"),
            "The WRML System Format representing the \"application/schema+json\" media type.",
            URI.create("http://json-schema.org"),
            URI.create("http://tools.ietf.org/html/draft-zyp-json-schema-03"),
            "json"),

    java(new UniqueName("text/java"),
            "The WRML System Format representing the \"text/java\" media type (for Java source files).",
            URI.create(""),
            URI.create(""),
            "java"),

    vnd_wrml_ascii_api(new UniqueName("text/vnd.wrml.ascii.api"),
            "The WRML System Format representing the \"application/vnd.wrml.ascii.api\" media type.",
            URI.create("http://wrml.org"),
            URI.create(""), "txt"),

    vnd_wrml_complete_schema(new UniqueName("application/vnd.wrml.complete.schema+json"),
            "The WRML System Format representing the \"application/vnd.wrml.complete.schema\" media type.",
            URI.create("http://www.w3.org/xml"),
            URI.create("http://www.ietf.org/rfc/rfc3023.txt"),
            "json"),

    vnd_wrml_complete_api(new UniqueName("application/vnd.wrml.complete.api+json"),
            "The WRML System Format representing the \"application/vnd.wrml.complete.api\" media type.",
            URI.create("http://www.w3.org/xml"),
            URI.create("http://www.ietf.org/rfc/rfc3023.txt"),
            "json"),

    vnd_wrml_swagger_api(new UniqueName("application/vnd.wrml.swagger.api+json"),
    "The WRML System Format representing the \"application/vnd.wrml.swagger.api\" media type.",
            URI.create("http://wrml.org"),
            URI.create(""), "json"),

    vnd_wrml_wrmldoc(new UniqueName("application/vnd.wrml.wrmldoc+json"),
    "The WRML System Format representing the \"application/vnd.wrml.wrmldoc\" media type.",
            URI.create("http://www.w3.org/xml"),
            URI.create("http://www.ietf.org/rfc/rfc3023.txt"),
            "json");

    private final URI _FormatUri;

    private final String _Description;

    private final UniqueName _UniqueName;

    private final URI _HomePageUri;

    private final URI _RfcPageUri;

    private final MediaType _MediaType;

    private final String _FileExtension;

    private SystemFormat(final UniqueName uniqueName, final String description, final URI homePageUri, final URI rfcPageUri, final String fileExtension) {

        _UniqueName = uniqueName;

        _Description = description;
        _HomePageUri = homePageUri;
        _RfcPageUri = rfcPageUri;
        _FileExtension = fileExtension;

        _MediaType = new MediaType(uniqueName.getNamespace(), uniqueName.getLocalName());
        final URI relativeUri = URI.create("/" + _UniqueName.toString());
        _FormatUri = SystemApi.Format.getUri().resolve(relativeUri);

    }

    public static SystemFormat fromUniqueName(final UniqueName name) {

        for (final SystemFormat format : SystemFormat.values()) {
            if (format._UniqueName.equals(name)) {
                return format;
            }
        }

        return null;
    }

    public String getDescription() {

        return _Description;
    }

    public URI getFormatUri() {

        return _FormatUri;
    }

    public URI getHomePageUri() {

        return _HomePageUri;
    }

    public MediaType getMediaType() {

        return _MediaType;
    }

    public URI getRfcPageUri() {

        return _RfcPageUri;
    }

    public UniqueName getUniqueName() {

        return _UniqueName;
    }

    public String getFileExtension() {

        return _FileExtension;
    }
}
