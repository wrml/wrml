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

import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.TerminalSize;

public class TerminalAppButton extends Button
{

    private final TerminalAppKeyboardInteraction _KeyboardInteraction;

    private TerminalAppAction _Action;

    private TerminalSize _PreferredSize;

    public TerminalAppButton(final TerminalAppAction action)
    {

        super(action.getTitle());
        setAction(action);
        _KeyboardInteraction = new TerminalAppKeyboardInteraction(action.getApp(), action, true);
    }

    public TerminalAppAction getAction()
    {

        return _Action;
    }

    @SuppressWarnings("unchecked")
    public <T extends TerminalApp> T getApp()
    {

        return (T) getAction().getApp();
    }

    @Override
    public final TerminalSize getPreferredSize()
    {

        if (_PreferredSize == null)
        {
            return super.getPreferredSize();
        }

        return _PreferredSize;
    }

    @Override
    public Result keyboardInteraction(final Key key)
    {

        if (!isVisible())
        {
            return Result.EVENT_HANDLED;
        }

        if (_KeyboardInteraction.handleKeyboardInteraction(key))
        {
            return Result.EVENT_HANDLED;
        }

        return super.keyboardInteraction(key);

    }

    public void setAction(final TerminalAppAction action)
    {

        _Action = action;
        setText(_Action.getTitle());
    }

    @Override
    public final void setPreferredSize(final TerminalSize preferredSize)
    {

        _PreferredSize = preferredSize;
    }

    @Override
    public String toString()
    {

        return "TerminalButton (" + super.toString() + ")";
    }

}
