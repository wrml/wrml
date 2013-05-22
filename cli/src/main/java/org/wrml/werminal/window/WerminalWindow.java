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
package org.wrml.werminal.window;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.component.EmptySpace;
import com.googlecode.lanterna.gui.component.Separator;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.terminal.TerminalAppToolBar;
import org.wrml.werminal.terminal.TerminalAppWindow;

public class WerminalWindow extends TerminalAppWindow
{

    private TerminalAppToolBar _HeaderToolBar;

    public WerminalWindow(final Werminal werminal, final String title)
    {

        this(werminal, title, null);
    }

    public WerminalWindow(final Werminal werminal, final String title, final Component[] toolBarComponents)
    {

        super(werminal, getTitle(werminal, title));

        setBorder(new Border.Standard());

        if (toolBarComponents != null)
        {
            _HeaderToolBar = new TerminalAppToolBar(werminal, toolBarComponents);
        }

        render();
    }

    private static String getTitle(final Werminal werminal, final String title)
    {

        // NOTE: This check is needed to support mocking tests
        if (werminal == null)
        {
            return title;
        }

        String werminalTitle = werminal.getAppTitle();
        return String.format(" %s - %s ", werminalTitle, title);
    }

    public void addEmptySpace()
    {

        addComponent(new EmptySpace(0, 1));
    }

    public TerminalAppToolBar getHeaderToolBar()
    {

        return _HeaderToolBar;
    }

    public Werminal getWerminal()
    {

        return getApp();
    }

    public void render()
    {

        removeAllComponents();
        renderHeaderToolBar();
    }

    protected void renderHeaderToolBar()
    {

        if (_HeaderToolBar != null)
        {
            addComponent(_HeaderToolBar);
            addComponent(new Separator());
        }
    }

}
