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
 * An event triggered by a {@link ModelGraph}.
 */
public class ModelGraphEvent extends Event<ModelGraph> {

    private static final long serialVersionUID = 1L;

    private final Object _RawValue;
    private final Object _RuntimeValue;

    ModelGraphEvent(ModelGraph source) {
        this(source, null, null);
    }

    ModelGraphEvent(ModelGraph source, Object rawValue, Object runtimeValue) {
        super(source);
        _RawValue = rawValue;
        _RuntimeValue = runtimeValue;
    }

    public Object getRawValue() {
        return _RawValue;
    }

    public Object getRuntimeValue() {
        return _RuntimeValue;
    }

}
