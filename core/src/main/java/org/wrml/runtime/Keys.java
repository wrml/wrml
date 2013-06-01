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
import org.wrml.model.rest.Api;
import org.wrml.model.rest.Document;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.schema.Prototype;

import java.io.Serializable;
import java.net.URI;
import java.util.Set;

/**
 * <p>
 * {@link Keys} encapsulate the idea that a given model may have a few distinct identities, with respect to a few
 * distinct domains (identity spaces). When connecting or intersecting domains, it is often necessary to translate or
 * transform between two (or more) identity spaces. A common example of this problem occurs when an organization exposes
 * an "internal" system to a globally/universally <i>visible</i> "external" domain, such as the Web.
 * </p>
 * <p/>
 * <p>
 * "External" domains require universally unique identifiers, such as a Document model's <i>"URI on the Web"</i>. The
 * Web's URIs commonly mask some other, "internal" (surrogate) identifier, such as a storage system-generated identifier
 * (e.g. DB primary key). A URI "facade" has the side-benefit of granting the storage system's entities unique
 * identities within the <i>Universe of Documents</i> domain.
 * </p>
 * <p/>
 * <p>
 * A {@link Keys} object contains one or more key values that identify a single model instance within the domain of an
 * associated {@link Schema}. The WRML runtime uses {@link Keys} to communicate the multiple identity values that
 * scope/qualify models within some multitude of domains.
 * </p>
 * <p/>
 * <p>
 * A {@link Keys} instance organizes a model's multiple identities via a mapping from {@link Schema} URI (a key value
 * itself from {@link Document#getUri()}) to the model's key slot value (represented via {@link Object} so that it fits
 * in any slot). This approach enables the WRML runtime itself to be agnostic with regard to the nature of keys, while
 * allowing clients and servers to identify models using the key names and value types that feel natural.
 * </p>
 * <p/>
 * <p>
 * As a REST-oriented framework, WRML's core engine handles the translation between a model's <i>Document.id</i> and
 * it's "internal", surrogate key(s). The {@link org.wrml.runtime.rest.ApiLoader#buildDocumentKeys(URI, URI)} method is a convenient
 * way to create {@link Keys} containing the <i>Document.uri</i> and any surrogate key that could be derived from the
 * available {@link Api} metadata.
 * </p>
 * <p/>
 * <p>
 * From a REST perspective, the specific problem that {@link Keys} helps WRML address is the enforcement (or at least
 * encouragement) of "The Opacity Axiom", which applies to the use of URIs. The beginning of this axiom is quoted below.
 * Please refer to the source document, <a href="http://www.w3.org/DesignIssues/Axioms.html#opaque">Tim Berners-Lee's
 * original WWW design notes</a>, for the complete description.
 * </p>
 * <p/>
 * <h4>The Opacity Axiom</h4>
 * <p/>
 * <p>
 * The concept of an identifier referring to a resource is very fundamental in the World Wide Web. Identifiers will
 * refer to resources all different sorts. Any addressable thing will have an identifier. There are mechanisms we have
 * just discussed for extending the spaces of identifiers into name spaces which have different properties. Different
 * spaces may address different sorts of objects, and the relationship between the identifier and the object, such as
 * the uniqueness of the object and the concept of identity, may vary. A very important axiom of the Web is that in
 * general:
 * </p>
 * <p/>
 * <h5>Axiom: Opacity of URIs</h5>
 * <p/>
 * <p>
 * The only thing you can use an identifier for is to refer to an object. When you are not dereferencing, you should not
 * look at the contents of the URI string to gain other information.
 * </p>
 * <p/>
 * <p>
 * For the bulk of Web use URIs are passed around without anyone looking at their internal contents, the content of the
 * string itself. This is known as the opacity. Software should be made to treat URIs as generally as possible, to allow
 * the most reuse of existing or future schemes.
 * </p>
 *
 * @see Model#getKeys()
 * @see Prototype#getAllKeySlotNames()
 * @see org.wrml.runtime.schema.WRML
 * @see Schema#getKeySlotNames()
 * @see <a href="http://www.w3.org/DesignIssues/Axioms.html#opaque">The Opacity Axiom</a>
 */
public interface Keys extends Serializable
{

    /**
     * Returns the number (N >= 1) of individual keys in this Keys
     */
    int getCount();

    /**
     * Returns a set of the schemas that pertain to this Keys instance.
     *
     * @return a Set of key URI's
     */
    Set<URI> getKeyedSchemaUris();

    /**
     * Returns the key object that correlates to the schema passed in.
     * Useful when a model (or request) has more than one schema.
     *
     * @param keyedSchemaUri
     * @return the value of the requested key
     */
    <V> V getValue(final URI keyedSchemaUri);

}
