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
package org.wrml.werminal.terminal;

import com.googlecode.lanterna.gui.Theme;

import java.util.HashMap;
import java.util.Map;

public abstract class TerminalAppTheme<E extends Enum<E>> extends Theme
{

    private final Map<E, Definition> _Definitions;

    private final Map<Category, Definition> _CategorizedDefinitions;

    private final Class<E> _DefinitionEnumClass;

    public TerminalAppTheme(final Class<E> definitionEnumClass)
    {

        _DefinitionEnumClass = definitionEnumClass;
        _Definitions = new HashMap<>();
        _CategorizedDefinitions = new HashMap<>();
    }

    protected Map<Category, Definition> getCategorizedDefinitions()
    {

        return _CategorizedDefinitions;
    }


    protected Map<E, Definition> getDefinitions()
    {

        return _Definitions;
    }

    @Override
    public Definition getDefinition(final Category category)
    {

        return _CategorizedDefinitions.get(category);
    }

}
