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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility factory methods for creating Observable instances
 * 
 * @see java.util.Collections
 */
public final class Observables {

    private final static ObservableMap<?, ?> EMPTY_MAP = observableMap(Collections.emptyMap());
    private final static ObservableList<?> EMPTY_LIST = observableList(Collections.emptyList());

    @SuppressWarnings("unchecked")
    public static <E> ObservableList<E> emptyList() {
        return (ObservableList<E>) EMPTY_LIST;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> ObservableMap<K, V> emptyMap() {
        return (ObservableMap<K, V>) EMPTY_MAP;
    }

    /**
     * Convenience utility method for decorating a List as an ObservableList.
     * 
     * @param list
     *            The List to decorate
     * @param <E>
     *            The element type
     * @return An ObservableList backed by the given List
     */
    public static <E> ObservableList<E> observableList(List<E> list) {
        return new DelegatingObservableList<E>(list);
    }

    /**
     * Convenience utility method for decorating a Map as an ObservableMap.
     * 
     * @param map
     *            The Map to decorate
     * @param <K>
     *            The key type
     * @param <V>
     *            The value type
     * @return An ObservableMap backed by the given Map
     */
    public static <K, V> ObservableMap<K, V> observableMap(Map<K, V> map) {
        return new DelegatingObservableMap<K, V>(map);
    }

    private Observables() {
    }
}
