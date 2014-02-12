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
 * Copyright (C) 2011 - 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
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
package org.wrml.util;

import java.util.Arrays;

/**
 * A component-based unique. Hash code is computed *ONCE* upon first request as
 * this is intended to be used only for immutable components.
 */
public class UniqueComposition {
    private final Object[] _Components;

    private Integer _HashCode;

    /**
     * Creates a new UniqueComposition with the specified components.
     *
     * @param components the components to compose.
     */
    public UniqueComposition(final Object... components) {

        _Components = components;
        if ((_Components == null) || (_Components.length == 0)) {
            throw new NullPointerException();
        }
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final UniqueComposition other = (UniqueComposition) obj;

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

        if (_HashCode == null) {
            _HashCode = 31 + Arrays.deepHashCode(_Components);
        }

        return _HashCode;
    }

    @Override
    public String toString() {

        return getClass().getCanonicalName() + " :: { \"components\" : [" + Arrays.toString(_Components) + "] }";
    }

}
