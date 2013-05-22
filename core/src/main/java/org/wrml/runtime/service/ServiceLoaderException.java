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
package org.wrml.runtime.service;

/**
 * <p>
 * The {@link ServiceLoader}'s associated error type.
 * </p>
 * 
 * @see ServiceLoader
 */
public class ServiceLoaderException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    private final ServiceLoader _ServiceLoader;

    /**
     * <p>
     * Create a new exception to communicate a problem about the specified {@link ServiceLoader}.
     * </p>
     * 
     * @param message
     *            The message to explain what is going on.
     * 
     * @param cause
     *            The root cause of the {@link ServiceLoaderException} (or <code>null</code>, if this
     *            <i>is</i> the root {@link Throwable}).
     * 
     * @param serviceLoader
     *            The impacted {@link ServiceLoader}.
     * 
     */
    public ServiceLoaderException(final String message, final Throwable cause, final ServiceLoader serviceLoader)
    {
        super(message, cause);
        _ServiceLoader = serviceLoader;
    }

    public ServiceLoader getServiceLoader()
    {
        return _ServiceLoader;
    }
}
