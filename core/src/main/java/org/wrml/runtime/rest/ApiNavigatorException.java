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
package org.wrml.runtime.rest;

/**
 * The {@link ApiNavigator}'s associated error type.
 */
public class ApiNavigatorException extends RuntimeException
{

    private final ApiNavigator _ApiNavigator;

    private final Status _Status;

    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception to communicate a problem about the specified {@link ApiNavigator}.
     * 
     * @param message
     *            The message to explain what is going on.
     * 
     * @param apiNavigator
     *            The impacted {@link ApiNavigator}.
     * 
     */
    ApiNavigatorException(final String message, final Throwable cause, final ApiNavigator apiNavigator)
    {
        this(message, cause, apiNavigator, null);
    }

    /**
     * Create a new exception to communicate a problem about the specified {@link ApiNavigator}.
     * 
     * @param message
     *            The message to explain what is going on.
     * 
     * @param apiNavigator
     *            The impacted {@link ApiNavigator}.
     * 
     * @param status
     *            The status to classify this exception as reflecting an HTTP
     *            rule enforcement.
     * 
     */
    ApiNavigatorException(final String message, final Throwable cause, final ApiNavigator apiNavigator,
            final Status status)
    {
        super(message, cause);
        _ApiNavigator = apiNavigator;
        _Status = status;
    }

    /**
     * Get the {@link ApiNavigator} associated with this error.
     * 
     * @return The {@link ApiNavigator} that raised this exception.
     */
    public ApiNavigator getApiNavigator()
    {
        return _ApiNavigator;
    }

    /**
     * The associated {@link Status} or null if this exception "type" has no
     * REST status equivalent.
     * 
     * @return The HTTP Status or null if there is no logical peer (in REST) for
     *         this kind of problem in the WRML runtime.
     */
    public Status getStatus()
    {
        return _Status;
    }
}
