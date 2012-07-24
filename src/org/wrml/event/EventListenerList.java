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

import java.util.EventObject;
import java.util.List;

/**
 * A list of event listeners.
 * 
 * @param <L>
 *            The EventListener type to be contained within this List.
 */
public interface EventListenerList<L extends java.util.EventListener> extends List<L> {

    /**
     * Fire the named event to the list of registered listeners.
     * 
     * @param eventName
     *            The name of the event to fire.
     * @param event
     *            The event instance.
     */
    public void fireEvent(String eventName, EventObject event);
}
