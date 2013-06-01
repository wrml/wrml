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
package org.wrml.model.rest;

import org.wrml.model.Described;
import org.wrml.model.Model;
import org.wrml.runtime.rest.CommonHeader;
import org.wrml.runtime.rest.Status;
import org.wrml.runtime.rest.UriTemplate;

import java.util.List;

/**
 * OPTIONS model for a {@code Resource} node or {@link Api} root.
 * <p>
 * In the context of a {@code Resource} node, the OPTIONS discoverable by consumers of the node.
 * <p>
 * In the context of a root {@code Api}, all {@code Resource} nodes may be traversed and all child OPTIONS will be collected.
 * <p>
 * Extends {@link Model} <i>(as opposed to {@code Document})</i> since an OPTIONS request should not be a uniquely identifiable location.
 * <p>
 * 
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html">RFC 2626 [w3.org]</a>
 * @see <a href="http://ws-rest.org/2011/proc/a3-steiner.pdf">HTTP OPTIONS Hypermedia Constraint white paper [ws-rest.org]</a>
 * @see <a href="http://zacstewart.com/2012/04/14/http-options-method.html">HTTP OPTIONS method, self-describing RESTful APIs [zacstewart.com]</a>
 */
public interface ResourceOptions extends Model, Described
{

    /**
     * @param status
     *            - a RESTful {@link Status}.
     */
    void setStatus(Status status);

    /** @return- the RESTful {@link Status}. */
    Status getStatus();

    /**
     * @param allow
     *            - the {@link List} of {@link CommonHeader#ALLOW ALLOW}-able {@link Method}s
     */
    void setAllow(List<Method> allow);

    /** @return {@link CommonHeader#ALLOW ALLOW}-able {@link Method}s. */
    List<Method> getAllow();

    // TODO: change to return a header with content-type and other info? TBD.
    void setContentType(String contentType);

    String getContentType();

    void setUriTemplate(UriTemplate uriTemplate);

    UriTemplate getUriTemplate();

}
