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

import com.googlecode.lanterna.gui.component.TextArea;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;
import com.googlecode.lanterna.terminal.TerminalSize;

public class BaseTextArea extends TextArea {

    public BaseTextArea(final TerminalSize preferredSize, final String text) {
        super(preferredSize, text);
    }

    @Override
    public Result keyboardInteraction(Key key) {

        final Kind kind = key.getKind();

        switch (kind) {

        case Escape:
            getGUIScreen().closeWindow();
            return Result.DO_NOTHING;

        default:
            return super.keyboardInteraction(key);
        }
    }

}
