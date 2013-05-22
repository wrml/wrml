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
package org.wrml.werminal.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class History<E>
{

    private final HistoryMap<E> _Map;

    public History()
    {

        _Map = new HistoryMap<>();
    }

    public History(final Collection<E> initialState)
    {

        _Map = new HistoryMap<E>(initialState);
    }

    public History(final int initialCapcity)
    {

        _Map = new HistoryMap<E>(initialCapcity);
    }

    public void add(final E element)
    {

        if (_Map.containsKey(element))
        {
            _Map.get(element);
        }
        else
        {
            _Map.put(element, element);
        }
    }

    public void addAll(final Collection<E> elements)
    {

        _Map.addAll(elements);
    }

    public void clear()
    {

        _Map.clear();
    }

    public Set<E> getElementSet()
    {

        return _Map.keySet();
    }

    public void remove(final E element)
    {

        _Map.remove(element);
    }

    private class HistoryMap<T> extends LinkedHashMap<T, T>
    {

        private static final int DEFAULT_EXTRA_INITIAL_CAPACITY = 64;

        private static final float DEFAULT_LOAD_FACTOR = 0.75f;

        private static final int DEFAULT_MAX_ENTRIES = 250;

        private static final long serialVersionUID = 1L;

        public HistoryMap()
        {

            this(DEFAULT_EXTRA_INITIAL_CAPACITY);
        }

        public HistoryMap(final Collection<T> initialElements)
        {

            this(initialElements.size() + DEFAULT_EXTRA_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
            addAll(initialElements);
        }

        public HistoryMap(final int initialCapacity)
        {

            this(initialCapacity, DEFAULT_LOAD_FACTOR);
        }

        public HistoryMap(final int initialCapacity, final float loadFactor)
        {

            super(initialCapacity, loadFactor, true);
        }

        public void addAll(final Collection<T> elements)
        {

            for (final T element : elements)
            {
                put(element, element);
            }

        }

        @Override
        protected boolean removeEldestEntry(@SuppressWarnings("rawtypes") final Map.Entry eldest)
        {

            return size() > DEFAULT_MAX_ENTRIES;
        }

    }
}
