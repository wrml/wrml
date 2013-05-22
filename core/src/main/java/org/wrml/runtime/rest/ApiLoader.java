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
 * Copyright 2012 Mark Masse (OSS project WRML.org)
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

import org.wrml.model.format.Format;
import org.wrml.model.rest.*;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.DimensionsBuilder;
import org.wrml.runtime.Keys;
import org.wrml.runtime.Loader;

import java.net.URI;
import java.util.Set;
import java.util.SortedSet;

/**
 * Runtime component responsible for loading and "introspecting" REST APIs.
 *
 * @see ApiNavigator
 * @see Resource
 * @see Document
 * @see Api
 * @see ResourceTemplate
 * @see LinkTemplate
 * @see Format
 * @see LinkRelation
 */
public interface ApiLoader extends Loader
{

    /**
     * Uses the specified {@link DimensionsBuilder} to build the {@link Dimensions} associated with the identified document's default (response) representation.
     */
    Dimensions buildDocumentDimensions(final Method requestMethod, final URI uri, DimensionsBuilder dimensionsBuilder);

    /**
     * Builds {@link Keys} starting from the specified {@link Document} key value. Uses available REST API metadata
     * to determine additional, surrogate key values (if possible).
     *
     * @param uri       A {@link Document} key identifier value.
     * @param schemaUri The schemaUri associated with one of the identified resource's possible response {@link Schema}.
     */
    Keys buildDocumentKeys(final URI uri, final URI schemaUri);

    /**
     * Returns the {@link ApiLoaderConfiguration} used to initialize this {@link ApiLoader}.
     */
    ApiLoaderConfiguration getConfig();

    /**
     * Returns the {@link URI} associated with the identified resource's default {@link Schema} (response)
     * representation.
     */
    URI getDefaultResponseSchemaUri(final Method requestMethod, final URI uri);

    /**
     * Returns the already loaded {@link Api} associated with the specified {@link Keys}.
     */
    Api getLoadedApi(final Keys keys);

    /**
     * Returns the already loaded {@link ApiNavigator} associated with the specified {@link Api}.
     */
    ApiNavigator getLoadedApiNavigator(final URI apiUri);

    /**
     * Returns the {@link Set} of already loaded {@link Api}s.
     */
    Set<Api> getLoadedApis();

    /**
     * Returns the {@link Set} of already loaded API {@link URI}s.
     */
    SortedSet<URI> getLoadedApiUris();

    /**
     * Returns the already loaded {@link LinkRelation} associated with the specified {@link Keys}.
     */
    LinkRelation getLoadedLinkRelation(final Keys keys);

    /**
     * Returns the {@link Set} of already loaded {@link LinkRelation}s.
     */
    Set<LinkRelation> getLoadedLinkRelations();

    /**
     * Returns the {@link Set} of already loaded LinkRelation {@link URI}s.
     */
    SortedSet<URI> getLoadedLinkRelationUris();

    /**
     * Returns the {@link ApiNavigator} associated with the {@link Api} that "parents" the specified resource
     * identifier.
     */
    ApiNavigator getParentApiNavigator(final URI uri) throws ApiLoaderException;

    /**
     * Loads/reloads the specified {@link Api}, returning the newly created {@link ApiNavigator}.
     */
    ApiNavigator loadApi(final Api api) throws ApiLoaderException;

    /**
     * Loads/reloads the specified {@link Api}, by it's {@link Document} key ({@link URI}) value; returning the newly
     * created {@link ApiNavigator}.
     */
    ApiNavigator loadApi(final URI apiUri) throws ApiLoaderException;

    /**
     * Loads/reloads the specified {@link LinkRelation}.
     */
    void loadLinkRelation(final LinkRelation linkRelation) throws ApiLoaderException;

    /**
     * Loads/reloads the specified {@link LinkRelation}, by it's {@link Document} key ({@link URI}) value.
     */
    LinkRelation loadLinkRelation(final URI linkRelationUri) throws ApiLoaderException;


}
