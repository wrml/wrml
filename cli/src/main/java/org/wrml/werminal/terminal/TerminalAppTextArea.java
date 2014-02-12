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

import com.googlecode.lanterna.gui.component.TextArea;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.TerminalSize;

public class TerminalAppTextArea extends TextArea {

    private final TerminalApp _App;

    private final TerminalAppKeyboardInteraction _KeyboardInteraction;

    public TerminalAppTextArea(final TerminalApp app, final TerminalSize preferredSize, final String text) {

        super(preferredSize, text);
        _App = app;
        _KeyboardInteraction = createTerminalAppKeyboardInteraction();
    }

    @SuppressWarnings("unchecked")
    public <T extends TerminalApp> T getApp() {

        return (T) _App;
    }

    @Override
    public Result keyboardInteraction(final Key key) {

        if (!isVisible()) {
            return Result.EVENT_HANDLED;
        }

        if (_KeyboardInteraction.handleKeyboardInteraction(key)) {
            return Result.EVENT_HANDLED;
        }

        return super.keyboardInteraction(key);
    }

    protected TerminalAppKeyboardInteraction createTerminalAppKeyboardInteraction() {

        return new TerminalAppKeyboardInteraction(getApp(), null, true);
    }
}
