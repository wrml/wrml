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

import org.wrml.runtime.Loader;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

/**
 * The WRML runtime's loader of {@link Service}s.
 *
 * @see org.wrml.runtime.Context
 * @see Service
 * @see org.wrml.model.schema.Schema
 */
public interface ServiceLoader extends Loader {

    /**
     * The names of the currently loaded {@link Service}s.
     *
     * @return The names of the currently loaded {@link Service}s.
     */
    public Set<String> getServiceNames();

    /**
     * Get a loaded {@link Service} by its configured name.
     *
     * @param serviceName The configured name of the loaded {@link Service} to get.
     * @return The {@link Service} with the specified name or <code>null</code> if there isn't a loaded {@link Service} with that name.
     */
    Service getService(final String serviceName);

    /**
     * Get the loaded {@link Service} that is configured to support interactions with {@link org.wrml.model.Model}s with the specified {@link org.wrml.model.schema.Schema}.
     *
     * @param schemaUri The {@link URI} of the {@link org.wrml.model.schema.Schema} of a {@link org.wrml.model.Model} instance that requires support from a loaded {@link Service}.
     * @return The {@link Service} for the specified {@link org.wrml.model.schema.Schema} or <code>null</code> if there isn't a loaded {@link Service} mapped to support the {@link org.wrml.model.schema.Schema}.
     */
    Service getServiceForSchema(final URI schemaUri);

    /**
     * Get all of the loaded {@link Service}s.
     *
     * @return All of the loaded {@link Service}s.
     */
    Collection<Service> getServices();

    /**
     * Load a new {@link Service} instance from the specified configuration.
     *
     * @param serviceConfiguration The configuration data used to create and initialize the {@link Service} to be loaded.
     */
    void loadConfiguredService(final ServiceConfiguration serviceConfiguration);

    /**
     * Load the specified {@link Service} instance and assigns it the specified name.
     *
     * @param service     The {@link Service} instance to load.
     * @param serviceName The configured name of the {@link Service} to load.
     */
    void loadService(final Service service, final String serviceName);

    /**
     * <p>
     * Maps a {@link org.wrml.model.schema.Schema} {@link URI} <i>pattern</i> to a loaded {@link Service}. The <code>schemaUriPattern</code> may be a
     * fully qualified URI for a specific schema or a {@link URI} "prefix" that ends with a wildcard "*" to enable mapping a <i>namespace</i>
     * of {@link org.wrml.model.schema.Schema}s
     * </p>
     *
     * @param schemaUriPattern The {@link org.wrml.model.schema.Schema} {@link URI} <i>pattern</i> to map.
     * @param serviceName      The name of the {@link Service} instance that should support support interactions
     *                         with {@link org.wrml.model.Model}s with the specified {@link org.wrml.model.schema.Schema}.
     */
    void mapSchemaPatternToService(final String schemaUriPattern, final String serviceName);

}
