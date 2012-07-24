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

/**
 * See HTTP's <a
 * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html">method</a>.
 */
public enum Method {

    GetContent(true, true),
    GetMetadataOnly(true, true),
    GetOptionsOnly(false, true),
    Create(false, false),
    Update(false, true),
    Delete(false, true),
    Invoke(false, false);

    private final boolean _Safe;
    private final boolean _Idempotent;

    private Method(final boolean safe, final boolean idempotent) {
        _Safe = safe;
        _Idempotent = idempotent;
    }

    public boolean isIdempotent() {
        return _Idempotent;
    }

    public boolean isSafe() {
        return _Safe;
    }

}
