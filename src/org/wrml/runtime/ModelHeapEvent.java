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

import java.net.URI;
import java.util.UUID;

import org.wrml.event.Event;

/**
 * An event triggered by a {@link ModelHeap}.
 * 
 * Most applications using WRML will not need to listen to the heap directly. It
 * shouldn't be necessary since the {@link ModelReference} already does so and
 * relays the events to application code.
 */
public class ModelHeapEvent extends Event<ModelHeap> {

    private static final long serialVersionUID = 1L;

    private final UUID _HeapId;
    private final URI _SchemaId;
    private final String _FieldName;

    ModelHeapEvent(ModelHeap source, URI schemaId) {
        this(source, null, schemaId, null);
    }

    ModelHeapEvent(ModelHeap source, UUID heapId, URI schemaId) {
        this(source, heapId, schemaId, null);
    }

    ModelHeapEvent(ModelHeap source, UUID heapId, URI schemaId, String fieldName) {
        super(source);
        _HeapId = heapId;
        _SchemaId = schemaId;
        _FieldName = fieldName;
    }

    public String getFieldName() {
        return _FieldName;
    }

    public UUID getHeapId() {
        return _HeapId;
    }

    public URI getSchemaId() {
        return _SchemaId;
    }

}
