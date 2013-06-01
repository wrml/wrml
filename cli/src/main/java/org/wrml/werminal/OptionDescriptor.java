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
package org.wrml.werminal;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public enum OptionDescriptor
{

    config("config", "config", true, "The WRML configuration file."),
    unix("unix", "unix", false, "Runs the Werminal GUI app within a UNIX terminal."),
    swing("swing", "swing", false, "Runs the Werminal GUI app within a Swing terminal emulator.");

    public static final Options OPTIONS = new Options();

    static
    {

        final OptionDescriptor[] optionDescriptors = OptionDescriptor.values();

        for (final OptionDescriptor optionDescriptor : optionDescriptors)
        {
            OPTIONS.addOption(optionDescriptor.toOption());
        }

    }

    private final String _Name;

    private final String _LongName;

    private final boolean _Parameterized;

    private final String _Description;

    private Option _Option;

    private OptionDescriptor(final String name, final String longName, final boolean paramenterized,
                             final String description)
    {

        _Name = name;
        _LongName = longName;
        _Parameterized = paramenterized;
        _Description = description;
    }

    public String getDescription()
    {

        return _Description;
    }

    public String getLongName()
    {

        return _LongName;
    }

    public String getName()
    {

        return _Name;
    }

    public boolean isParameterized()
    {

        return _Parameterized;
    }

    public Option toOption()
    {

        if (_Option == null)
        {
            _Option = new Option(getName(), getLongName(), isParameterized(), getDescription());
        }
        return _Option;
    }

    @Override
    public String toString()
    {

        return _Name;
    }
}