/**
 * Copyright (C) 2012 WRML.org <mark@wrml.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wrml.cli;

import org.apache.commons.cli.Option;

public enum OptionDescriptor {

    help("h", "help", false, "Prints this help message."),
    usage("u", "usage", false, "Prints the command line interface's usage information.");

    private final String _Name;
    private final String _LongName;
    private final boolean _Parameterized;
    private final String _Description;
    private Option _Option;

    private OptionDescriptor(final String name, final String longName, final boolean paramenterized,
            final String description) {

        _Name = name;
        _LongName = longName;
        _Parameterized = paramenterized;
        _Description = description;
    }

    public String getDescription() {
        return _Description;
    }

    public String getLongName() {
        return _LongName;
    }

    public String getName() {
        return _Name;
    }

    public boolean isParameterized() {
        return _Parameterized;
    }

    public Option toOption() {
        if (_Option == null) {
            _Option = new Option(_Name, _LongName, _Parameterized, _Description);
        }
        return _Option;
    }

    @Override
    public String toString() {
        return _Name;
    }
}
