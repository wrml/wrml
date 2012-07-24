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

package org.wrml.cli.gui.base;

import com.googlecode.lanterna.gui.component.TextBox;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;
import com.googlecode.lanterna.terminal.TerminalSize;

public class BaseTextBox extends TextBox {

    private TerminalSize _PreferredSize;

    public BaseTextBox() {
        this(10, null);
    }

    public BaseTextBox(final int width) {
        this(width, null);
    }

    public BaseTextBox(final int width, final String title) {
        super(width, title);
    }

    public BaseTextBox(final String title) {
        this(10, title);
    }

    @Override
    public final TerminalSize getPreferredSize() {
        if (_PreferredSize == null) {
            return super.getPreferredSize();
        }

        return _PreferredSize;
    }

    @Override
    public Result keyboardInteraction(Key key) {

        if (!isVisible()) {
            return Result.DO_NOTHING;
        }

        final Kind kind = key.getKind();

        switch (kind) {

        case Escape:
            getGUIScreen().closeWindow();
            return Result.DO_NOTHING;
        }

        return super.keyboardInteraction(key);
    }

    public final void setPreferredSize(TerminalSize preferredSize) {
        _PreferredSize = preferredSize;
    }

}
