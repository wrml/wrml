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

import org.wrml.event.Event;

/**
 * Fired from an ObservableMap whenever its contents are altered.
 */
public class MapEvent extends Event<ObservableMap<?, ?>> {

    private static final long serialVersionUID = 1L;

    private final Object _Key;
    private final Object _NewValue;
    private final Object _OldValue;

    public MapEvent(final ObservableMap<?, ?> map) {
        this(map, null, null, null);
    }

    public MapEvent(final ObservableMap<?, ?> map, final Object key) {
        this(map, key, null, null);
    }

    public MapEvent(final ObservableMap<?, ?> map, final Object key, final Object newValue, final Object oldValue) {
        super(map);
        _Key = key;
        _NewValue = newValue;
        _OldValue = oldValue;
    }

    public Object getKey() {
        return _Key;
    }

    public ObservableMap<?, ?> getMap() {
        return getSource();
    }

    public Object getNewValue() {
        return _NewValue;
    }

    public Object getOldValue() {
        return _OldValue;
    }

}
