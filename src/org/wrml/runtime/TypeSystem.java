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

package org.wrml.runtime;

import java.lang.reflect.ParameterizedType;

/*
 * NOTE: This is PoC code (not production quality).
 */

public final class TypeSystem {

    /** "is" is the standard *read* access method name prefix for Booleans. */
    public static final String IS = "is";

    /** "get" is the standard *read* access method name prefix non-Booleans. */
    public static final String GET = "get";

    /**
     * "set" is the standard *write* access method name prefix for all field
     * types.
     */
    public static final String SET = "set";

    public static Class<?> typeToClass(java.lang.reflect.Type type) {

        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {

            final ParameterizedType parameterizedType = (ParameterizedType) type;

            final java.lang.reflect.Type rawType = parameterizedType.getRawType();

            if (rawType instanceof Class<?>) {
                return (Class<?>) rawType;
            }
        }

        return Object.class;
    }

}
