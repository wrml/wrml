/**
 * WRML - Web Resource Modeling Language
 *  __     __   ______   __    __   __
 * /\ \  _ \ \ /\  == \ /\ "-./  \ /\ \
 * \ \ \/ ".\ \\ \  __< \ \ \-./\ \\ \ \____
 *  \ \__/".~\_\\ \_\ \_\\ \_\ \ \_\\ \_____\
 *   \/_/   \/_/ \/_/ /_/ \/_/  \/_/ \/_____/
 *
 * http://www.wrml.org
 *
 * Copyright (C) 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wrml.runtime;

import org.wrml.model.Model;
import org.wrml.model.rest.Api;

import javax.xml.validation.Schema;

/**
 * <p>
 * The {@link Engine} is the WRML runtime's root component. The Engine uses a {@link org.wrml.runtime.syntax.SyntaxLoader} to manage the more
 * static/permanent data and the {@link Context} to load App and Api specific data.
 * </p>
 *
 * @see EngineConfiguration
 * @see Context
 * @see org.wrml.runtime.syntax.SyntaxLoader
 */
public interface Engine
{

    /**
     * Initialize the Engine, loading the initial Context
     *
     * @param config the Engine's configuration.
     */
    void init(EngineConfiguration config) throws EngineException;

    /**
     * The {@link Engine}'s configuration, supplied during initialization.
     */
    EngineConfiguration getConfig();

    /**
     * The currently loaded {@link Context}.
     */
    Context getContext();

    /**
     * Call to "burst" the current {@link Context}'s bubble and reform a new/empty one. Or in code lingo, do a pointer
     * swap and leave the current {@link Context} behind (along with any loaded {@link Schema}, {@link Api}s, and
     * {@link Model}s).
     * <p/>
     * {@link Engine} implementations should initialize a {@link Context} to begin with, meaning that calling this
     * method is only necessary if the caller wishes to do a "soft reboot" of the runtime.
     */
    Context reloadContext() throws EngineException;
}
