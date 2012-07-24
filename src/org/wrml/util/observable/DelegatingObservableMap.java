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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.wrml.util.DelegatingMap;

public class DelegatingObservableMap<K, V> extends AbstractObservableMap<K, V> implements DelegatingMap<K, V> {

    private final Map<K, V> _Delegate;

    public DelegatingObservableMap(Class<MapEventListener> listenerClass, Map<K, V> delegate) {
        super(listenerClass);
        _Delegate = delegate;
    }

    public DelegatingObservableMap(Map<K, V> delegate) {
        this(MapEventListener.class, delegate);
    }

    @Override
    public void clear() {

        if (isEventHearable() && !fireMapClearing(new CancelableMapEvent(this))) {
            return;
        }

        _Delegate.clear();

        if (isEventHearable()) {
            fireMapCleared(new MapEvent(this));
        }

    }

    @Override
    public boolean containsKey(Object key) {
        return _Delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return _Delegate.containsValue(value);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return _Delegate.entrySet();
    }

    @Override
    public boolean equals(Object otherMap) {
        return _Delegate.equals(otherMap);
    }

    @Override
    public V get(Object key) {
        return _Delegate.get(key);
    }

    @Override
    public Map<K, V> getDelegate() {
        return _Delegate;
    }

    @Override
    public int hashCode() {
        return _Delegate.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return _Delegate.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return _Delegate.keySet();
    }

    @Override
    public V put(K key, V newValue) {

        V oldValue = null;
        if (isEventHearable()) {
            oldValue = get(key);
            if (!fireMapUpdatingEntry(new CancelableMapEvent(this, key, newValue, oldValue))) {
                return null;
            }
        }

        oldValue = _Delegate.put(key, newValue);

        if (isEventHearable()) {
            fireMapEntryUpdated(new MapEvent(this, key, newValue, oldValue));
        }

        return oldValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {

        Map<? extends K, ? extends V> oldValues = null;
        if (isEventHearable()) {
            oldValues = new HashMap<K, V>(this);
            for (final K key : map.keySet()) {
                if (!fireMapUpdatingEntry(new CancelableMapEvent(this, key, map.get(key), oldValues.get(key)))) {
                    // TODO: Filter out the unwanted elements and build a list of acceptable ones instead?
                    return;
                }
            }
        }

        _Delegate.putAll(map);

        if (isEventHearable() && (oldValues != null)) {
            for (final K key : map.keySet()) {
                fireMapEntryUpdated(new MapEvent(this, key, map.get(key), oldValues.get(key)));
            }
        }
    }

    @Override
    public V remove(Object key) {

        V oldValue = null;
        if (isEventHearable()) {
            oldValue = get(key);
            if (!fireMapRemovingEntry(new CancelableMapEvent(this, key, null, oldValue))) {
                return null;
            }
        }

        oldValue = _Delegate.remove(key);

        if (isEventHearable()) {
            fireMapEntryRemoved(new MapEvent(this, key, null, oldValue));
        }

        return oldValue;
    }

    @Override
    public int size() {
        return _Delegate.size();
    }

    @Override
    public String toString() {
        return getClass().getName() + " [" + (_Delegate != null ? "Delegate=" + _Delegate : "") + "]";
    }

    @Override
    public Collection<V> values() {
        return _Delegate.values();
    }
}
