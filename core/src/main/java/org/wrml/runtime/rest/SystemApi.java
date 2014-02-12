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
package org.wrml.runtime.rest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The system's internal REST APIs, adapted from the WRML architecture originally depicted on page 91 of O'Reilly's
 * "REST API Design Rulebook".
 */
public enum SystemApi {

    Format(URI.create("http://format.api.wrml.org"), org.wrml.model.format.Format.class, "Format API",
            "The WRML System Format REST API.", UUID.fromString("8B45CDE2-C846-449C-9DF4-5772CFA77926"), UUID
            .fromString("1A32BFDD-E57D-4FA6-852A-939329793052")),
    LinkRelation(URI.create("http://relation.api.wrml.org"), org.wrml.model.rest.LinkRelation.class,
            "Link Relation API", "The WRML System Link Relation REST API.", UUID
            .fromString("5056F231-0100-448C-8E56-BB1981AAAF4F"), UUID
            .fromString("2F6BD54A-32D1-4704-86B5-0E0D05E2581B")),
    Schema(URI.create("http://schema.api.wrml.org"), org.wrml.model.schema.Schema.class, "Schema API",
            "The WRML System Schema REST API.", UUID.fromString("4796A61C-4C1B-4295-BFBC-4701B372C7CB"), UUID
            .fromString("840FA671-7766-4598-B956-37FD9ABBFD81")),
    Syntax(URI.create("http://syntax.api.wrml.org"), org.wrml.model.schema.Syntax.class, "Syntax API",
            "The WRML System Syntax REST API.", UUID.fromString("D70E6130-6563-434D-A048-FCB4A3F18DC3"), UUID
            .fromString("EC3488E4-BBEF-413B-8F82-080BA4708D42")),
    Choices(URI.create("http://choices.api.wrml.org"), org.wrml.model.schema.Choices.class, "Choices API",
            "The WRML System Choices REST API.", UUID.fromString("A2D3C31F-D45A-4920-8505-765202A1D4E8"), UUID
            .fromString("8AC96AA9-B439-4227-A0AB-6C2EEF5909DA"));

    private static final Map<URI, SystemApi> SYSTEM_API_URIS = new HashMap<>();

    static {
        final SystemApi[] systemApis = SystemApi.values();
        for (final SystemApi systemApi : systemApis) {
            SYSTEM_API_URIS.put(systemApi.getUri(), systemApi);
        }
    }

    private final URI _Uri;

    private final Class<?> _DefaultSchemaInterface;

    private final String _Title;

    private final String _Description;

    private final UUID _DocrootId;

    private final UUID _PrimaryEndpointId;

    private SystemApi(final URI apiUri, final Class<?> defaultSchemaInterface, final String title,
                      final String description, final UUID docrootId, final UUID primaryEndpointId) {

        _Uri = apiUri;
        _DefaultSchemaInterface = defaultSchemaInterface;
        _Title = title;
        _Description = description;
        _DocrootId = docrootId;
        _PrimaryEndpointId = primaryEndpointId;
    }

    public Class<?> getDefaultSchemaInterface() {

        return _DefaultSchemaInterface;
    }

    public String getDescription() {

        return _Description;
    }

    public UUID getDocrootId() {

        return _DocrootId;
    }

    public UUID getPrimaryEndpointId() {

        return _PrimaryEndpointId;
    }

    public String getTitle() {

        return _Title;
    }

    public URI getUri() {

        return _Uri;
    }

    public static boolean isSystemApiUri(final URI apiUri) {

        return SYSTEM_API_URIS.containsKey(apiUri);
    }
}
