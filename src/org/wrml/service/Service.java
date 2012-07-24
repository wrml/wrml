/**
 * Copyright (C) 2012 WRML.org <mark@wrml.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Copyright (C) 2012 WRML.org <mark@wrml.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package org.wrml.service;

import java.lang.reflect.InvocationHandler;
import java.util.List;

import org.wrml.event.EventSource;
import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;

/**
 * WRML's generic “back-end connection” interface.
 */
public interface Service<K> extends EventSource<ServiceEventListener>, InvocationHandler {

    /**
     * Deletes the identified model.
     * 
     * @param key
     */
    public void delete(final K key);

    /**
     * Gets the identified model by it's key.
     * 
     * @param key
     * @param dimensions
     * @return
     */
    public <M extends Model> M get(final K key, final Dimensions dimensions);

    /**
     * Gets the identified models.
     * 
     * @param keys
     * @param dimensions
     * @return
     */
    public <M extends Model> List<M> getMultiple(final List<K> keys, final Dimensions dimensions);

    public boolean isAvailable();

    public void restart(final Context context);

    public void start(final Context context);

    public void stop(final Context context);

    /**
     * Updates the specified model.
     */
    public <M extends Model> M update(final M model);
}
