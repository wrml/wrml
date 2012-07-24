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

/**
 * An enum model representing WRML's "primitive" modeling types.
 */
public enum Type {

    /**
     * In Java, maps to: java.lang.String or T, where T is determined by a
     * configuration-based mapping of Text syntax constraint to native
     * (text-based) type.
     */
    Text,

    /**
     * In Java, maps to: java.lang.Object
     */
    //Native,

    /**
     * In Java, maps to Model or a subclass T determined by
     * schema constraint.
     */
    Model,

    /**
     * In Java, maps to boolean or Boolean
     */
    Boolean,

    /**
     * In Java, maps to: ObservableList<T>, where "primitive"
     * type is determined by type parameter constraint's type.
     * 
     * Note: If the Type parameter constraint's Type is "Model", then T is
     * either
     * org.wrml.Model or a subclass T determined by an added schema constraint.
     */
    List,

    /**
     * In Java, maps to: ObservableMap<K,V>, where the K and V "param"
     * types are determined by type constraints.
     */
    Map,

    /**
     * In Java, maps to: Enum<T>, where T is determined by
     * choice menu constraint's choice menu.
     */
    Choice,

    /**
     * In Java, maps to: either int or java.lang.Integer, dependent on Field's
     * isValueRequired flag's value.
     */
    Integer,

    /**
     * In Java, maps to java.util.Date
     */
    Date,

    /**
     * In Java, maps to either long or java.lang.Long, dependent on Field's
     * isRequired flag's value.
     */
    Long,

    /**
     * In Java, maps to: either float or java.lang.Float, dependent on Field's
     * isRequired flag's value.
     */
    Float;

}
