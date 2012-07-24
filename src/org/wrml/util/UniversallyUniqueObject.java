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

import java.util.UUID;

/**
 * A universally unique object.
 */
public class UniversallyUniqueObject implements UniversallyUnique {

    public final UUID _Id;

    public UniversallyUniqueObject() {
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

        final UniversallyUniqueObject other = (UniversallyUniqueObject) obj;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + _Id.hashCode();
        return result;
    }

}
