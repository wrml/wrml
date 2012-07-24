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

package org.wrml.cli.gui;

import java.util.Map;

import com.googlecode.lanterna.gui.Theme;
import com.googlecode.lanterna.terminal.Terminal;

import org.wrml.cli.gui.DarkTheme.DarkThemeDefinition;
import org.wrml.cli.gui.base.BaseTheme;

public class DarkTheme extends BaseTheme<DarkThemeDefinition> {

    public DarkTheme() {
        super(DarkThemeDefinition.class);

        final Map<DarkThemeDefinition, Definition> definitions = getDefinitions();
        final Map<Category, Definition> categorizedDefinitions = getCategorizedDefinitions();

        final DarkThemeDefinition[] wrmlThemeDefinitions = DarkThemeDefinition.values();

        for (final DarkThemeDefinition wrmlThemeDefinition : wrmlThemeDefinitions) {

            final Definition definition = new Theme.Definition(wrmlThemeDefinition.getForegroundColor(),
                    wrmlThemeDefinition.getBackgroundColor(), wrmlThemeDefinition.isHighlighted(),
                    wrmlThemeDefinition.isUnderlined());

            definitions.put(wrmlThemeDefinition, definition);
            categorizedDefinitions.put(wrmlThemeDefinition.toCategory(), definition);
        }

    }

    static enum DarkThemeDefinition {

        Border(Terminal.Color.BLACK, Terminal.Color.RED, false, false),
        ButtonActive(Terminal.Color.BLACK, Terminal.Color.BLACK, false, true),
        ButtonInactive(Terminal.Color.BLACK, Terminal.Color.BLACK, false, true),
        ButtonLabelActive(Terminal.Color.BLACK, Terminal.Color.RED, true, true),
        ButtonLabelInactive(Terminal.Color.BLACK, Terminal.Color.RED, false, false),
        CheckBox(Terminal.Color.BLACK, Terminal.Color.WHITE, false, false),
        CheckBoxSelected(Terminal.Color.BLACK, Terminal.Color.RED, true, false),
        DialogArea(Terminal.Color.BLACK, Terminal.Color.RED, false, false),
        ListItem(Terminal.Color.BLACK, Terminal.Color.WHITE, false, false),
        ListItemSelected(Terminal.Color.BLACK, Terminal.Color.RED, true, false),
        RaisedBorder(Terminal.Color.BLACK, Terminal.Color.RED, false, false),
        ScreenBackground(Terminal.Color.BLACK, Terminal.Color.RED, false, false),
        Shadow(Terminal.Color.BLACK, Terminal.Color.BLACK, false, false),
        TextBox(Terminal.Color.BLACK, Terminal.Color.RED, false, false),
        TextBoxFocused(Terminal.Color.BLACK, Terminal.Color.RED, false, false);

        private final Terminal.Color _BackgroundColor;
        private final Terminal.Color _ForegroundColor;
        private final boolean _Highlighted;
        private final boolean _Underlined;

        private DarkThemeDefinition(final Terminal.Color backgroundColor, final Terminal.Color foregroundColor,
                final boolean highlighted, final boolean underlined) {

            _BackgroundColor = backgroundColor;
            _ForegroundColor = foregroundColor;
            _Highlighted = highlighted;
            _Underlined = underlined;
        }

        public Terminal.Color getBackgroundColor() {
            return _BackgroundColor;
        }

        public Terminal.Color getForegroundColor() {
            return _ForegroundColor;
        }

        public boolean isHighlighted() {
            return _Highlighted;
        }

        public boolean isUnderlined() {
            return _Underlined;
        }

        public Category toCategory() {
            return Category.valueOf(String.valueOf(this));
        }
    }
}