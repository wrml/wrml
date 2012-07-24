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

import org.wrml.util.UniversallyUniqueObject;

public class Engine extends UniversallyUniqueObject {

    private final EngineConfiguration _Config;
    private final Context _DefaultContext;

    public Engine(EngineConfiguration config) {
        _Config = config;
        _DefaultContext = new Context(this);

        // TODO: Implement logging
        // Note from Greg:
        // Logback > Log4j, so if you are writing new code prefer Logback. Logback + SLF4J = problem solved.
        // SLF4J: http://www.slf4j.org/index.html
        // Logback: http://logback.qos.ch/index.html
    }

    public EngineConfiguration getConfig() {
        return _Config;
    }

    public Context getDefaultContext() {
        return _DefaultContext;
    }

}
