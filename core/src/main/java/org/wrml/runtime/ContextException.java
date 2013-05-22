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

/**
 * The {@link Context}'s associated error type.
 */
public class ContextException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    private final Context _Context;

    /**
     * A new {@link ContextException} without a {@link Throwable Throwable (cause)}.
     * 
     * @see Preferred if {@link Throwable} exists: {@link #ContextException(String, Throwable, Context)}
     * @param message
     * @param context
     */
    ContextException(final String message, final Context context)
    {
        this(message, null, context);
    }

    ContextException(final String message, final Throwable cause, final Context context)
    {
        super(message, cause);
        _Context = context;
    }

    public Context getContext()
    {
        return _Context;
    }
}
