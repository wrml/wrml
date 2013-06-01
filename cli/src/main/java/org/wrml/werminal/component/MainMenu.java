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
import com.googlecode.lanterna.terminal.TerminalSize;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.terminal.TerminalAppButtonPanel;
import org.wrml.werminal.terminal.TerminalAppMenu;
import org.wrml.werminal.terminal.TerminalAppPanel;

/**
 * Moved from inner class of {@link org.wrml.werminal.Werminal}.
 *
 * @author JJ Zabkar
 */
public class MainMenu extends TerminalAppPanel
{

    public MainMenu(final Werminal werminal)
    {

        super(werminal, "", new Border.Invisible(), Orientation.HORISONTAL, true, false);

        final TerminalAppMenu modelMenu = new TerminalAppMenu(getApp(), "  Model  ");
        modelMenu.setPreferredSize(new TerminalSize(30, 8));
        addComponent(modelMenu);

        final TerminalAppButtonPanel modelNewMenuItem = new TerminalAppButtonPanel(werminal.getNewAction());
        modelMenu.addComponent(modelNewMenuItem);
        final TerminalAppButtonPanel modelOpenMenuItem = new TerminalAppButtonPanel(werminal.getOpenAction());
        modelMenu.addComponent(modelOpenMenuItem);

        final TerminalAppMenu helpMenu = new TerminalAppMenu(getApp(), "  Help  ");
        helpMenu.setPreferredSize(new TerminalSize(30, 8));
        addComponent(helpMenu);

        final TerminalAppButtonPanel helpGuideMenuItem = new TerminalAppButtonPanel(werminal.getHelpGuideAction());
        helpMenu.addComponent(helpGuideMenuItem);

        final TerminalAppButtonPanel wrmlOrgMenuItem = new TerminalAppButtonPanel(werminal.getWrmlOrgAction());
        helpMenu.addComponent(wrmlOrgMenuItem);

    }
}