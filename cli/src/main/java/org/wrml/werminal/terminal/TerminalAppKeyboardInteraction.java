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

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;

public class TerminalAppKeyboardInteraction {

    private final TerminalApp _App;

    private final Action _EnterAction;

    private final boolean _CloseOnEscape;

    public TerminalAppKeyboardInteraction(final TerminalApp app, final Action enterAction, final boolean closeOnEscape) {

        _App = app;
        _EnterAction = enterAction;
        _CloseOnEscape = closeOnEscape;
    }

    public TerminalApp getApp() {

        return _App;
    }

    public Action getEnterAction() {

        return _EnterAction;
    }

    public boolean handleKeyboardInteraction(final Key key) {

        final Kind kind = key.getKind();

        boolean handled = false;
        switch (kind) {

            case Enter: {

                if (_EnterAction != null) {
                    _EnterAction.doAction();
                    handled = true;
                }

                break;
            }
            case Escape: {
                if (_CloseOnEscape) {
                    _App.closeTopWindow();
                    handled = true;
                }

                break;
            }
            default: {
                break;
            }
        }

        return handled;
    }

}
