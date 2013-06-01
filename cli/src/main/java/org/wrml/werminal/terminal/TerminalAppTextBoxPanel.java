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

import com.googlecode.lanterna.gui.Border;

public class TerminalAppTextBoxPanel extends TerminalAppPanel
{

    private final TerminalAppTextBox _TextBox;

    public TerminalAppTextBoxPanel(final TerminalApp app, final String title, final int textBoxWidth)
    {

        this(app, title, new TerminalAppTextBox(app, textBoxWidth));
    }

    public TerminalAppTextBoxPanel(final TerminalApp app, final String title, final TerminalAppTextBox terminalTextBox)
    {

        super(app, title, new Border.Standard(), Orientation.HORISONTAL, false, false);
        _TextBox = terminalTextBox;
        addComponent(_TextBox);
    }

    public TerminalAppTextBox getTextBox()
    {

        return _TextBox;
    }

}
