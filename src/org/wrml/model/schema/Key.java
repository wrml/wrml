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

package org.wrml.model.schema;

import org.wrml.model.Embedded;
import org.wrml.model.Model;
import org.wrml.util.observable.ObservableList;

/**
 * A Key names one (or more) of a Schema's fields, which (in combination) may be
 * used to determine uniqueness among models representing the same Schema.
 * 
 * Keys are crucial to implementing Model singularity, which is the
 * collapsing/merging of two distinct models that share the same key value(s).
 * After the merge, there are still two separate model references, but they will
 * both share the same internal heap id, which results in them sharing the same
 * field value storage.
 */
public interface Key extends Embedded, Model {

    /**
     * Returns the list ({@link Type#List}) of field names that make up this
     * Key. The value(s) of the named fields are used to compute a unique key
     * for model instances.
     * 
     * @return the {@link ObservableList} of {@link String} field names.
     */
    public ObservableList<String> getKeyFieldNames();

}
