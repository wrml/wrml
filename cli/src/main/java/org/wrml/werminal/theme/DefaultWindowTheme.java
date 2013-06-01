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
package org.wrml.werminal.theme;

import com.googlecode.lanterna.gui.Theme;
import com.googlecode.lanterna.terminal.Terminal;
import org.wrml.werminal.terminal.TerminalAppTheme;
import org.wrml.werminal.theme.DefaultWindowTheme.DefaultWindowThemeDefinition;

import java.util.Map;

//TODO: Redesign this class to enable greater reuse
public class DefaultWindowTheme extends TerminalAppTheme<DefaultWindowThemeDefinition>
{

    public DefaultWindowTheme()
    {

        super(DefaultWindowThemeDefinition.class);

        final Map<DefaultWindowThemeDefinition, Definition> definitions = getDefinitions();
        final Map<Category, Definition> categorizedDefinitions = getCategorizedDefinitions();

        final DefaultWindowThemeDefinition[] wrmlThemeDefinitions = DefaultWindowThemeDefinition.values();

        for (final DefaultWindowThemeDefinition wrmlThemeDefinition : wrmlThemeDefinitions)
        {

            final Definition definition = new Theme.Definition(wrmlThemeDefinition.getForegroundColor(),
                    wrmlThemeDefinition.getBackgroundColor(), wrmlThemeDefinition.isHighlighted(),
                    wrmlThemeDefinition.isUnderlined());

            definitions.put(wrmlThemeDefinition, definition);
            categorizedDefinitions.put(wrmlThemeDefinition.toCategory(), definition);
        }
    }

    static enum DefaultWindowThemeDefinition
    {

        BORDER(Terminal.Color.WHITE, Terminal.Color.BLACK, false, false),
        BUTTON_ACTIVE(Terminal.Color.WHITE, Terminal.Color.RED, false, true),
        BUTTON_INACTIVE(Terminal.Color.WHITE, Terminal.Color.BLACK, false, true),
        BUTTON_LABEL_ACTIVE(Terminal.Color.WHITE, Terminal.Color.RED, true, true),
        BUTTON_LABEL_INACTIVE(Terminal.Color.WHITE, Terminal.Color.BLACK, false, false),
        CHECKBOX(Terminal.Color.WHITE, Terminal.Color.BLACK, false, false),
        CHECKBOX_SELECTED(Terminal.Color.WHITE, Terminal.Color.RED, false, false),
        DIALOG_AREA(Terminal.Color.WHITE, Terminal.Color.BLACK, false, false),
        LIST_ITEM(Terminal.Color.WHITE, Terminal.Color.BLACK, false, false),
        LIST_ITEM_SELECTED(Terminal.Color.WHITE, Terminal.Color.RED, false, false),
        RAISED_BORDER(Terminal.Color.WHITE, Terminal.Color.RED, false, false),
        SCREEN_BACKGROUND(Terminal.Color.BLACK, Terminal.Color.RED, false, false),
        SHADOW(Terminal.Color.RED, Terminal.Color.RED, false, false),
        TEXTBOX(Terminal.Color.WHITE, Terminal.Color.BLACK, false, false),
        TEXTBOX_FOCUSED(Terminal.Color.WHITE, Terminal.Color.RED, false, false);

        private final Terminal.Color _BackgroundColor;

        private final Terminal.Color _ForegroundColor;

        private final boolean _Highlighted;

        private final boolean _Underlined;

        private DefaultWindowThemeDefinition(final Terminal.Color backgroundColor,
                                             final Terminal.Color foregroundColor, final boolean highlighted, final boolean underlined)
        {

            _BackgroundColor = backgroundColor;
            _ForegroundColor = foregroundColor;
            _Highlighted = highlighted;
            _Underlined = underlined;
        }

        public Terminal.Color getBackgroundColor()
        {

            return _BackgroundColor;
        }

        public Terminal.Color getForegroundColor()
        {

            return _ForegroundColor;
        }

        public boolean isHighlighted()
        {

            return _Highlighted;
        }

        public boolean isUnderlined()
        {

            return _Underlined;
        }

        public Category toCategory()
        {

            return Category.valueOf(String.valueOf(this));
        }
    }
}
