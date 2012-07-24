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

package org.wrml.util.observable;

import java.util.EventListener;

/**
 * Listener for the ObservableMap collection's events.
 * 
 * @param <K>
 *            The key type
 * @param <V>
 *            The value type
 */
public interface MapEventListener extends EventListener {

    public void onMapCleared(final MapEvent event);

    public void onMapClearing(final CancelableMapEvent event);

    public void onMapEntryRemoved(final MapEvent event);

    public void onMapEntryUpdated(final MapEvent event);

    public void onMapRemovingEntry(final CancelableMapEvent event);

    public void onMapUpdatingEntry(final CancelableMapEvent event);

    public enum EventType {
        mapCleared,
        mapClearing,
        mapEntryRemoved,
        mapEntryUpdated,
        mapRemovingEntry,
        mapUpdatingEntry;
    }
}
