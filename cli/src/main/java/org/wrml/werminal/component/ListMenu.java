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

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.component.EmptySpace;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.terminal.TerminalAppButtonPanel;
import org.wrml.werminal.terminal.TerminalAppMenu;
import org.wrml.werminal.terminal.TerminalAppPanel;

/**
 * Moved from inner class of {@link org.wrml.werminal.Werminal}.
 *
 * @author JJ Zabkar
 */
public class ListMenu extends TerminalAppPanel
{

    public ListMenu(final Werminal werminal)
    {

        super(werminal, "", new Border.Invisible(), Orientation.HORISONTAL, true, false);

        final TerminalAppMenu editMenu = new TerminalAppMenu(werminal, "  Edit Selected Elements  ");
        addComponent(editMenu);

        final String[] editMenuItems = {"Copy", "Cut", "Paste", "Delete"};
        for (String editMenuItem : editMenuItems)
        {
            editMenu.addComponent(new TerminalAppButtonPanel(werminal.getUnimplementedAction(editMenuItem)));
        }

        final TerminalAppMenu helpMenu = new TerminalAppMenu(werminal, "  Help  ");
        addComponent(helpMenu);

        final TerminalAppButtonPanel wrmlOrgMenuItem = new TerminalAppButtonPanel(werminal.getClosingWrmlOrgAction());
        helpMenu.addComponent(wrmlOrgMenuItem);

        addComponent(new EmptySpace(0, 4));
    }
}