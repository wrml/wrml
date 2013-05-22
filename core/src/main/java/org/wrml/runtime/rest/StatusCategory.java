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

public enum StatusCategory
{

    /**
     * Informational 1xx - Request received, continuing process
     */
    INFORMATIONAL("Informational", "1xx", 100, 199),

    /**
     * Success 2xx - The action was successfully received, understood, and
     * accepted
     */
    SUCCESSFUL("Successful", "2xx", 200, 299),

    /**
     * Redirection 3xx - Further action must be taken in order to complete the
     * request
     */
    REDIRECTION("Redirection", "3xx", 300, 399),

    /**
     * Client Error 4xx - The request contains bad syntax or cannot be fulfilled
     */
    CLIENT_ERROR("Client Error", "4xx", 400, 499),

    /**
     * Server Error 5xx - The server failed to fulfill an apparently valid
     * request
     */
    SERVER_ERROR("Server Error", "5xx", 500, 599);

    public static StatusCategory getStatusCategory(final int code)
    {
        final StatusCategory[] values = StatusCategory.values();
        for (final StatusCategory statusCategory : values)
        {
            if (statusCategory.inRange(code))
            {
                return statusCategory;
            }
        }
        return null;
    }

    private final String _Name;

    private final String _Nickname;

    private final int _MinValue;

    private final int _MaxValue;

    private StatusCategory(final String name, final String nickname, final int minValue, final int maxValue)
    {
        _Name = name;
        _Nickname = nickname;
        _MinValue = minValue;
        _MaxValue = maxValue;

    }

    public int getMaxValue()
    {
        return _MaxValue;
    }

    public int getMinValue()
    {
        return _MinValue;
    }

    public String getName()
    {
        return _Name;
    }

    public String getNickname()
    {
        return _Nickname;
    }

    public boolean inRange(final int code)
    {
        return ((code >= getMinValue()) && (code <= getMaxValue()));
    }

    @Override
    public String toString()
    {
        return new StringBuilder().append(getName()).append(' ').append(getNickname()).toString();
    }

}
