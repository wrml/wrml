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
 * Copyright (C) 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
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

import com.googlecode.lanterna.gui.component.TextBox;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.TerminalSize;
import org.wrml.werminal.util.ClipboardUtils;

public class TerminalAppTextBox extends TextBox
{

    private final TerminalApp _App;

    private final TerminalAppKeyboardInteraction _KeyboardInteraction;

    private TerminalSize _PreferredSize;

    public TerminalAppTextBox(final TerminalApp app)
    {

        this(app, 10, null);
    }

    public TerminalAppTextBox(final TerminalApp app, final int width)
    {

        this(app, width, null);
    }

    public TerminalAppTextBox(final TerminalApp app, final int width, final String title)
    {

        super(title, width);
        _App = app;
        _KeyboardInteraction = createTerminalAppKeyboardInteraction();

    }

    public TerminalAppTextBox(final TerminalApp app, final String title)
    {

        this(app, 10, title);
    }

    @SuppressWarnings("unchecked")
    public <T extends TerminalApp> T getApp()
    {

        return (T) _App;
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
    public final void setPreferredSize(final TerminalSize preferredSize)
    {

        _PreferredSize = preferredSize;
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

        if (key.getKind() == Key.Kind.NormalKey)
        {
            if (key.isCtrlPressed())
            {

                final char keyCharacter = key.getCharacter();
                if (keyCharacter == 'C' || keyCharacter == 'c')
                {
                    // COPY
                    ClipboardUtils.setClipboardText(getText());
                    return Result.EVENT_HANDLED;
                }
                else if (keyCharacter == 'V' || keyCharacter == 'v')
                {
                    // PASTE
                    final String text = ClipboardUtils.getClipboardText();
                    if (text != null && !text.isEmpty())
                    {
                        setText(text);
                        return Result.EVENT_HANDLED;
                    }

                }
                else if (keyCharacter == 'X' || keyCharacter == 'x')
                {
                    // CUT
                    ClipboardUtils.setClipboardText(getText());
                    setText("");
                    return Result.EVENT_HANDLED;
                }

            }
        }


        return super.keyboardInteraction(key);
    }

    protected TerminalAppKeyboardInteraction createTerminalAppKeyboardInteraction()
    {

        return new TerminalAppKeyboardInteraction(getApp(), null, true);
    }

}
