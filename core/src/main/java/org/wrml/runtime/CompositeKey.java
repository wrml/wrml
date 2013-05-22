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
 * Copyright (C) 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
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
package org.wrml.runtime;

import org.wrml.util.UniqueComposition;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

/**
 * A {@link CompositeKey} is a key value that is composed of two or more components.
 * When a WRML schema declares more than one {@link org.wrml.runtime.schema.Key} slot, the WRML runtime uses a CompositeKey instance to represent the combined value of the key slot values.
 */
public final class CompositeKey
{

    private static final String TO_STRING_SEPARATOR_BETWEEN_KEY_VALUE = "_";

    private static final String TO_STRING_SEPARATOR_BETWEEN_KEYS = "__";

    private final Map<String, Object> _KeySlots;

    private final UniqueComposition _UniqueComposition;

    /**
     * Create a new CompositeKey from the specified name value pairs representing the individual key components,
     * with each key slot name mapped to the corresponding key slot value.
     */
    public CompositeKey(final SortedMap<String, Object> keySlots)
    {

        _KeySlots = Collections.unmodifiableMap(keySlots);

        final Object[] components = new Object[_KeySlots.size()];
        int i = 0;

        final Set<Entry<String, Object>> keySlotEntrySet = _KeySlots.entrySet();
        for (final Entry<String, Object> keySlot : keySlotEntrySet)
        {
            components[i++] = keySlot;
        }

        _UniqueComposition = new UniqueComposition(components);
    }

    @Override
    public boolean equals(final Object obj)
    {

        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final CompositeKey other = (CompositeKey) obj;
        if (!_UniqueComposition.equals(other._UniqueComposition))
        {
            return false;
        }
        return true;
    }

    /**
     * The name value pairs representing the individual key components,
     * with each key slot name mapped to the corresponding key slot value.
     */
    public Map<String, Object> getKeySlots()
    {

        return _KeySlots;
    }

    @Override
    public int hashCode()
    {

        return _UniqueComposition.hashCode();
    }

    @Override
    public String toString()
    {

        final StringBuilder sb = new StringBuilder();

        for (final Entry<String, Object> entry : _KeySlots.entrySet())
        {
            sb.append(entry.getKey()).append(TO_STRING_SEPARATOR_BETWEEN_KEY_VALUE)
                    .append(String.valueOf(entry.getValue()));
            sb.append(TO_STRING_SEPARATOR_BETWEEN_KEYS);
        }

        sb.setLength(sb.length() - TO_STRING_SEPARATOR_BETWEEN_KEYS.length());

        return sb.toString();
    }

}
