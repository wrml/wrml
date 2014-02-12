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
import org.wrml.model.UniquelyNamed;
import org.wrml.model.Versioned;
import org.wrml.runtime.schema.DefaultValue;
import org.wrml.runtime.schema.Title;
import org.wrml.runtime.schema.WRML;

import java.net.URI;

/**
 * <p>
 * A unique, yet highly generic LinkRelation may be shared by many different instances of an <code>/R ----> /E</code>
 * relationship. For example a "Person" schema and a "Node" schema may both want to link to what they conceive of as
 * their "parent".
 * </p>
 * <p/>
 * <p>
 * The re-use of the same LinkRelation is common within the same domain, like building a Web site about movies offering
 * many different APIs that share the same domain-specific language (DSL). Common, domain-specific-relationships like
 * "genre" and "cast" might appear in the application-specific APIs that directly power a set of Web apps.
 * </p>
 * <p/>
 * <p>
 * Reusing a link relation has the powerful side-effect of furthering the establishment of application and
 * domain-specific "keywords" that may be shared by countless schema. The benefit of this uniformity will be felt most
 * directly by the programmers tasked with system integrations.
 * </p>
 *
 * @see <a href="http://martinfowler.com/articles/richardsonMaturityModel.html">Richardson Maturity Model (article by
 * Martin Fowler)</a>
 * @see <a href="http://www.iana.org/assignments/link-relations/link-relations.xml">IANA Link Relations Registry</a>
 */
@Title("Link Relation")
@WRML(keySlotNames = {"uniqueName"}, comparableSlotNames = {"title", "uniqueName"})
public interface LinkRelation extends Titled, Versioned, Described, UniquelyNamed, Document {

    /**
     * The {@link Method} to invoke on the HREF associated with this
     * LinkRelation.
     *
     * @return the Method used to "click through" this LinkRelation.
     */
    @DefaultValue("Get")
    Method getMethod();

    /**
     * The schema id that identifies the type of models that are allowed as
     * (request body) arguments in {@link Link}s referencing this link relation.
     *
     * @return The schema id that identifies the type of models that can
     * typically be passed (as "function" arguments) when navigating
     * with this link relation.
     */
    URI getRequestSchemaUri();

    /**
     * The schema id that identifies the type of models that are allowed as
     * (response body) "return values" from {@link Link}s referencing this link
     * relation.
     *
     * @return The schema id that identifies the type of models that can
     * be returned (as "function" return types) when
     * navigating with this link relation.
     */
    URI getResponseSchemaUri();

    /**
     * Sets the {@link Method} slot, which is described in {@link LinkRelation#getMethod()}
     *
     * @param method the {@link Method} to associate with this LinkRelation.
     * @return the {@link Method} that used to be associated, before the caller
     * called.
     */
    Method setMethod(Method method);

    /**
     * @see #getRequestSchemaUri()
     */
    URI setRequestSchemaUri(URI requestSchemaUri);

    /**
     * @see #getResponseSchemaUri()
     */
    URI setResponseSchemaUri(URI responseSchemaUri);
}
