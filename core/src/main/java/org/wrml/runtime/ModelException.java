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
 * Copyright (C) 2011 - 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
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

/**
 * The modeling framework's base exception class.
 */
public class ModelException extends RuntimeException
{

    private final Model _Model;

    private static final long serialVersionUID = 1L;

    /**
     * Create a new {@link ModelException}.
     * 
     * @param message
     *            The message to convey.
     * 
     * @param cause
     *            The (internal) cause of the issue.
     * 
     * @param model
     *            The model with issues.
     */
    public ModelException(final String message, final Throwable cause, final Model model)
    {
        super(message, cause);
        _Model = model;
    }

    /**
     * Get the troublemaker.
     * 
     * @return The model that all this fuss is about.
     */
    public Model getModel()
    {
        return _Model;
    }

}
