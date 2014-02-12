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
package org.wrml.runtime.service;

import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.search.SearchCriteria;

import java.util.Set;

/**
 * <p>
 * WRML's generic back-end connection interface. Implementations of this interface provide access to data and controls
 * add/or add some custom "application logic" to support an app (client) or API (server).
 * </p>
 * <p>
 * The set of methods declared in the {@link Service} interface is intended to mirror the "uniform interface" of REST, with an interface method corollary for each of the HTTP/1.1 document-centric interaction methods.
 * </p>
 *
 * @see Context
 * @see ServiceLoader
 * @see ServiceConfiguration
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9">HTTP/1.1 Method Definitions</a>
 */
public interface Service {

    /**
     * <p>
     * The {@link Context} invokes this method to delegate the deletion/removal ("404ification") of the {@link Model}
     * identified by the specified {@link Keys}.
     * </p>
     * <p/>
     * <p>
     * This method may be considered <i>optional</i>.
     * Implementations that cannot support this operation may throw {@link UnsupportedOperationException} upon invocation.
     * </p>
     *
     * @param keys       The {@link Keys} of the {@link Model} that is about to be derezzed/404'ed.
     * @param dimensions The {@link Dimensions} that may be used to inform/scope the requested operation.
     * @throws UnsupportedOperationException Thrown if the delete operation is not supported by this service.
     * @see Context#deleteModel(org.wrml.runtime.Keys, org.wrml.runtime.Dimensions)
     */
    void delete(final Keys keys, final Dimensions dimensions) throws UnsupportedOperationException;

    /**
     * Get the ServiceConfiguration used to initialize this {@link Service}.
     *
     * @return the ServiceConfiguration that was passed into init.
     */
    ServiceConfiguration getConfiguration();

    /**
     * <p>
     * The {@link Context} invokes this method to delegate the retrieval/fetching (GET'ing) of the {@link Model} keyed
     * with the specified {@link Keys} and dimensioned by the requested {@link Dimensions}.
     * </p>
     * <p>
     * As a REST-oriented framework WRML's "retrieve" interface method is designed to abstract HTTP GET without losing
     * its core semantics.
     * </p>
     *
     * @param keys       The {@link Keys} to the sought-after {@link Model}.
     * @param dimensions The {@link Dimensions} of the desired {@link Model}.
     * @see Context#getModel(Keys, Dimensions)
     * @see Dimensions#getSchemaUri()
     * @see Model#getKeys()
     * @see Model#getDimensions()
     */
    Model get(final Keys keys, final Dimensions dimensions);

    /**
     * Get the Context that loaded this {@link Service}.
     *
     * @return the Context that was passed into init.
     */
    Context getContext();

    /**
     * <p>
     * Initializes the {@link Service} for the given context with the given configuration.
     * </p>
     *
     * @param context The {@link Context} for this {@link Service}.
     * @param config  The {@link Service}'s "custom" configuration.
     */
    void init(final Context context, final ServiceConfiguration config);

    /**
     * <p>
     * The {@link Context} invokes this method to delegate the saving/upserting (update or insert) of the specified
     * {@link Model}. This method's semantics reflects WRML's interpretation of REST's PUT method.
     * </p>
     * <p>
     * This method may be considered <i>optional</i>.
     * Implementations that cannot support this operation may throw {@link UnsupportedOperationException} upon invocation.
     * </p>
     * <p>
     * The framework calls this method for "inserts" in cases when the app code knows <i>all</i> of the {@link Model}'s
     * {@link Keys} before the model has ever been saved. This method is <i>not</i> appropriate to <i><b>insert</b></i>
     * {@link Model}s that have {@link Service}-generated key slot values (e.g. server/db populated IDs). This method
     * <i>is</i> appropriate to <i><b>update</b></i> any/all {@link Model}s (once they have been appropriately
     * "inserted").
     * </p>
     *
     * @param model The {@link Model} state to be persisted.
     * @throws UnsupportedOperationException Thrown if the save operation is not supported by this {@link Service}.
     * @see Context#saveModel(Model)
     */
    Model save(final Model model) throws UnsupportedOperationException;

    /**
     * <p>
     * This method may be considered <i>optional</i>.
     * Implementations that cannot support this operation may throw {@link UnsupportedOperationException} upon invocation.
     * </p>
     *
     * @param searchCriteria The {@link org.wrml.runtime.search.SearchCriteria}.
     * @return The {@link Set} of {@link org.wrml.model.Model} representing the outcome of a search that was performed with a given {@link org.wrml.runtime.search.SearchCriteria} input.
     * @throws UnsupportedOperationException Thrown if the search operation is not supported by this {@link Service}.
     */
    Set<Model> search(final SearchCriteria searchCriteria) throws UnsupportedOperationException;

    /**
     * <p>
     * Invokes the functional model with the (optional) parameter model.
     * </p>
     * <p>
     * This method may be considered <i>optional</i>.
     * Implementations that cannot support this operation may throw {@link UnsupportedOperationException} upon invocation.
     * </p>
     * <p>
     * The type/schema of both the functional model and the parameter model may vary, so long as the {@link Service} understands their semantics.
     * </p>
     *
     * @param function  The {@link Model} representing the function to be invoked.
     * @param parameter The {@link Model} representing the function's parameter (may be <code>null</code> if the function invocation requires no parameters).
     * @return The <i>function's return value</i> as a {@link Model} or <code>null</code> if the function conceptually returns {@link Void}.
     * @throws UnsupportedOperationException Thrown if the invoke operation is not supported by this {@link Service}.
     */
    Model invoke(final Model function, final Dimensions responseDimensions, final Model parameter) throws UnsupportedOperationException;


}
