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

package org.wrml.event;

import java.util.EventListener;

/**
 * A source of events that are of interest to a specific type of listener.
 * 
 * @param <L>
 *            The type of listener that is interested in receiving events from
 *            this source.
 */
public interface EventSource<L extends java.util.EventListener> {

    /**
     * Adds the specified {@link EventListener} to receive events from this
     * source.
     * 
     * @param eventListener
     *            The {@link EventListener} to add.
     * 
     * @return True if the listener was successfully added.
     */
    public boolean addEventListener(L eventListener);

    /**
     * Removes the specified {@link EventListener} so that it no longer
     * receives events from this source.
     * 
     * @param eventListener
     *            The {@link EventListener} to remove.
     * 
     * @return True if the listener was successfully removed.
     */
    public boolean removeEventListener(L eventListener);
}