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

import org.wrml.event.Event;

/**
 * An event triggered by a {@link SchemaLoader}.
 */
public class SchemaLoaderEvent extends Event<SchemaLoader> {

    private static final long serialVersionUID = 1L;

    private final Prototype _Prototype;
    private final Class<?> _SchemaInterfaceClass;

    public SchemaLoaderEvent(final SchemaLoader source, final Prototype prototype) {
        this(source, null, prototype);
    }

    SchemaLoaderEvent(final SchemaLoader source) {
        this(source, null, null);
    }

    SchemaLoaderEvent(final SchemaLoader source, final Class<?> schemaInterfaceClass) {
        this(source, schemaInterfaceClass, null);
    }

    SchemaLoaderEvent(final SchemaLoader source, final Class<?> schemaInterfaceClass, final Prototype prototype) {
        super(source);
        _SchemaInterfaceClass = schemaInterfaceClass;
        _Prototype = prototype;

    }

    public Prototype getPrototype() {
        return _Prototype;
    }

    public Class<?> getSchemaInterfaceClass() {
        return _SchemaInterfaceClass;
    }

}
