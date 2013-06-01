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
package org.wrml.werminal.dialog;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.terminal.TerminalSize;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.component.HistoryCheckListBox;
import org.wrml.werminal.component.WerminalTextBox;
import org.wrml.werminal.window.WerminalWindow;

public class HistoryPopup extends WerminalWindow
{

    private final HistoryCheckListBox _HistoryCheckListBox;

    public HistoryPopup(final Werminal werminal, final String title, final WerminalTextBox keyTextBox)
    {

        super(werminal, title);
        setBorder(new Border.Standard());

        addEmptySpace();

        _HistoryCheckListBox = new HistoryCheckListBox(keyTextBox, werminal.getCloseAction(), null);
        _HistoryCheckListBox.setPreferredSize(new TerminalSize(70, 10));
        addComponent(_HistoryCheckListBox);

    }

    public HistoryCheckListBox getHistoryCheckListBox()
    {

        return _HistoryCheckListBox;
    }

}
