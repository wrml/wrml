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

import java.util.EventListener;

/**
 * The {@link EventListener} interface may be implemented in order to receive
 * {@link ModelReaderEvent}s.
 */
public interface ModelReaderEventListener extends EventListener {

    public void onModelRead(final ModelReaderEvent event);

    public enum EventType {
        modelRead
    }
}
