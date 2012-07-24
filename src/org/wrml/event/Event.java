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
import java.util.UUID;

import org.wrml.util.UniversallyUnique;

/**
 * A simple event with a strongly typed source and a universally unique id.
 * 
 * @param <S>
 *            The event source type.
 */
public class Event<S extends Object> extends EventObject implements UniversallyUnique {

    private static final long serialVersionUID = 1L;

    private final S _Source;
    private final UUID _Id;

    public Event(S source) {
        super(source);
        _Source = source;
        _Id = UniversallyUnique.IdFactory.createId();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final Event<S> other = (Event<S>) obj;

        if (!_Id.equals(other._Id)) {
            return false;
        }

        return true;
    }

    @Override
    public UUID getId() {
        return _Id;
    }

    @Override
    public S getSource() {
        return _Source;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + _Id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getClass().getName() + " [source = " + _Source + ", id = " + _Id + "]";
    }
}