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

import org.wrml.cli.gui.LightTheme.LightThemeDefinition;
import org.wrml.cli.gui.base.BaseTheme;

public class LightTheme extends BaseTheme<LightThemeDefinition> {

    public LightTheme() {
        super(LightThemeDefinition.class);

        final Map<LightThemeDefinition, Definition> definitions = getDefinitions();
        final Map<Category, Definition> categorizedDefinitions = getCategorizedDefinitions();

        final LightThemeDefinition[] wrmlThemeDefinitions = LightThemeDefinition.values();

        for (final LightThemeDefinition wrmlThemeDefinition : wrmlThemeDefinitions) {

            final Definition definition = new Theme.Definition(wrmlThemeDefinition.getForegroundColor(),
                    wrmlThemeDefinition.getBackgroundColor(), wrmlThemeDefinition.isHighlighted(),
                    wrmlThemeDefinition.isUnderlined());

            definitions.put(wrmlThemeDefinition, definition);
            categorizedDefinitions.put(wrmlThemeDefinition.toCategory(), definition);
        }
    }

    static enum LightThemeDefinition {

        Border(Terminal.Color.WHITE, Terminal.Color.BLACK, false, false),
        ButtonActive(Terminal.Color.WHITE, Terminal.Color.RED, false, true),
        ButtonInactive(Terminal.Color.WHITE, Terminal.Color.BLACK, false, true),
        ButtonLabelActive(Terminal.Color.WHITE, Terminal.Color.RED, true, true),
        ButtonLabelInactive(Terminal.Color.WHITE, Terminal.Color.BLACK, false, false),
        CheckBox(Terminal.Color.WHITE, Terminal.Color.BLACK, false, false),
        CheckBoxSelected(Terminal.Color.WHITE, Terminal.Color.RED, false, false),
        DialogArea(Terminal.Color.WHITE, Terminal.Color.BLACK, false, false),
        ListItem(Terminal.Color.WHITE, Terminal.Color.BLACK, false, false),
        ListItemSelected(Terminal.Color.WHITE, Terminal.Color.RED, false, false),
        RaisedBorder(Terminal.Color.WHITE, Terminal.Color.BLACK, false, false),
        ScreenBackground(Terminal.Color.BLACK, Terminal.Color.RED, false, false),
        Shadow(Terminal.Color.RED, Terminal.Color.RED, false, false),
        TextBox(Terminal.Color.WHITE, Terminal.Color.BLACK, false, false),
        TextBoxFocused(Terminal.Color.WHITE, Terminal.Color.RED, false, false);

        private final Terminal.Color _BackgroundColor;
        private final Terminal.Color _ForegroundColor;
        private final boolean _Highlighted;
        private final boolean _Underlined;

        private LightThemeDefinition(final Terminal.Color backgroundColor, final Terminal.Color foregroundColor,
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