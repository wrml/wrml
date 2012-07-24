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

package org.wrml.util;

import java.util.Arrays;

/**
 * A component-based unique.
 */
public class Composition {

    private final Object[] _Components;

    public Composition(final Object... components) {
        _Components = components;
        if ((_Components == null) || (_Components.length == 0)) {
            throw new NullPointerException();
        }
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

        final Composition other = (Composition) obj;

        if (!Arrays.deepEquals(_Components, other._Components)) {
            return false;
        }

        return true;
    }

    public Object[] getComponents() {
        return _Components;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Arrays.deepHashCode(_Components);
        return result;
    }

    @Override
    public String toString() {
        return getClass().getCanonicalName() + " :: { \"components\" : [" + Arrays.toString(_Components) + "] }";
    }

}
