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
package org.wrml.model.rest;

import java.util.HashMap;
import java.util.Map;

/**
 * See HTTP's <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html">method</a>.
 * 
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html">HTTP/1.1 Request Method</a>
 */
public enum Method
{

    Get("GET", true, true, false, true),
    Metadata("HEAD", true, true, false, false),
    Options("OPTIONS", true, true, false, true),
    Save("PUT", false, true, true, true),
    Invoke("POST", false, false, true, true),
    Delete("DELETE", false, true, false, false);

    private static final Map<String, Method> PROTOCOL_GIVEN_NAME_MAP = new HashMap<>();

    static
    {
        final Method[] methods = Method.values();
        for (final Method method : methods)
        {
            PROTOCOL_GIVEN_NAME_MAP.put(method.getProtocolGivenName(), method);
        }
    }

    public static Method fromProtocolGivenName(final String protocolGivenName)
    {

        if (!PROTOCOL_GIVEN_NAME_MAP.containsKey(protocolGivenName))
        {
            throw new IllegalArgumentException("\"" + protocolGivenName
                    + "\" is not a valid (HTTP/1.1) protocol method name.");
        }

        return PROTOCOL_GIVEN_NAME_MAP.get(protocolGivenName.toUpperCase());
    }

    /**
     * <p>
     * Is an entity (e.g. Model) allowed to be sent along in a request message using this {@link Method}.
     * </p>
     */
    private final boolean _EntityAllowedInRequestMessage;

    /**
     * <p>
     * Is an entity (e.g. Model) allowed to be return in response to a request message that use this {@link Method}.
     * </p>
     */
    private final boolean _EntityAllowedInResponseMessage;

    private final boolean _Idempotent;

    private final String _ProtocolGivenName;

    private final boolean _Safe;

    private Method(final String protocolGivenName, final boolean safe, final boolean idempotent,
            final boolean entityAllowedInRequestMessage, final boolean entityAllowedInResponseMessage)
    {
        _ProtocolGivenName = protocolGivenName;
        _Safe = safe;
        _Idempotent = idempotent;
        _EntityAllowedInRequestMessage = entityAllowedInRequestMessage;
        _EntityAllowedInResponseMessage = entityAllowedInResponseMessage;
    }

    public String getProtocolGivenName()
    {
        return _ProtocolGivenName;
    }

    public boolean isEntityAllowedInRequestMessage()
    {
        return _EntityAllowedInRequestMessage;
    }

    public boolean isEntityAllowedInResponseMessage()
    {
        return _EntityAllowedInResponseMessage;
    }

    public boolean isIdempotent()
    {
        return _Idempotent;
    }

    public boolean isSafe()
    {
        return _Safe;
    }
}
