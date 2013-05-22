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
 * A value that is named for identification while passing.
 */
public final class Parameter implements Comparable<Parameter>
{

    private String _Name;

    private String _Value;

    public Parameter(final String name, final String value)
    {

        _Name = name;
        _Value = value;
    }

    @Override
    public int compareTo(final Parameter p)
    {

        if (p.getName() == null && _Name == null)
        {
            return 0;
        }

        return _Name.compareTo(p.getName());
    }

    public String getName()
    {

        return _Name;
    }

    public void setName(final String name)
    {

        _Name = name;
    }

    public String getValue()
    {

        return _Value;
    }

    public void setValue(final String value)
    {

        _Value = value;
    }

    @Override
    public String toString()
    {

        return "{ \"name\" : " + _Name + ", \"value\" : " + _Value + "}";
    }
}
