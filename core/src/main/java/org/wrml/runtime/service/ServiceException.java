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
package org.wrml.runtime.service;

import org.wrml.model.rest.status.Status;

/**
 * <p>
 * The {@link Service}'s associated error type.
 * </p>
 *
 * @see Service
 */
public class ServiceException extends RuntimeException {

    private final Service _Service;

    private final Status _Status;

    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Create a new exception to communicate a problem about the specified {@link Service}.
     * </p>
     *
     * @param message The message to explain what is going on.
     * @param cause   The root cause of the {@link ServiceException} (or <code>null</code>, if this <i>is</i> the root
     *                {@link Throwable}).
     * @param service The impacted {@link Service}.
     */
    public ServiceException(final String message, final Throwable cause, final Service service) {

        this(message, cause, service, null);
    }

    /**
     * <p>
     * Create a new exception to communicate a problem about the specified {@link Service}.
     * </p>
     *
     * @param message The message to explain what is going on.
     * @param service The impacted {@link Service}.
     * @param status  The status to classify this exception as reflecting an HTTP
     *                rule enforcement.
     */
    public ServiceException(final String message, final Throwable cause, final Service service, final Status status) {

        super(message, cause);
        _Service = service;
        _Status = status;
    }

    public Service getService() {

        return _Service;
    }

    /**
     * <p>
     * The associated {@link Status} or null if this exception "type" has no REST status equivalent.
     * </p>
     *
     * @return The HTTP Status or null if there is no logical peer (in REST) for
     * this kind of problem in the WRML runtime.
     */
    public Status getStatus() {

        return _Status;
    }
}
