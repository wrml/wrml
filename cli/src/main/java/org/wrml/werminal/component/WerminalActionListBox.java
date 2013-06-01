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
package org.wrml.werminal.component;

import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.terminal.TerminalAppKeyboardInteraction;

public class WerminalActionListBox extends ActionListBox
{

    private final TerminalAppKeyboardInteraction _KeyboardInteraction;

    private final WerminalAction _EnterOnSelectionAction;

    private final Werminal _Werminal;

    public WerminalActionListBox(final Werminal werminal)
    {

        this(werminal, null, null);
    }

    public WerminalActionListBox(final Werminal werminal, final WerminalAction enterAction,
                                 final WerminalAction enterOnSelectionAction)
    {

        _Werminal = werminal;
        _KeyboardInteraction = new TerminalAppKeyboardInteraction(_Werminal, enterAction, true);
        _EnterOnSelectionAction = enterOnSelectionAction;
    }

    @Override
    public Result keyboardInteraction(final Key key)
    {

        if (!isVisible())
        {
            return Result.EVENT_HANDLED;
        }

        final Kind kind = key.getKind();
        switch (kind)
        {

            case Enter:
            {

                final Object oldSelectedItem = getSelectedItem();

                Result result = super.keyboardInteraction(key);

                final Object newSelectedItem = getSelectedItem();

                if (_KeyboardInteraction.handleKeyboardInteraction(key))
                {
                    result = Result.EVENT_HANDLED;
                }

                if (_EnterOnSelectionAction != null && oldSelectedItem != null && newSelectedItem == oldSelectedItem)
                {
                    _EnterOnSelectionAction.doAction();
                }

                return result;
            }

            default:
            {

                if (_KeyboardInteraction.handleKeyboardInteraction(key))
                {
                    return Result.EVENT_HANDLED;
                }

                return super.keyboardInteraction(key);
            }
        }

    }

}
