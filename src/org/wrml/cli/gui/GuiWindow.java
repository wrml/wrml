/**
 * Copyright (C) 2012 WRML.org <mark@wrml.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wrml.cli.gui;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.EmptySpace;
import com.googlecode.lanterna.gui.component.Separator;

import org.wrml.cli.gui.base.ToolBar;

public class GuiWindow extends Window {

    private final Gui _Gui;
    private final ToolBar _ToolBar;

    public GuiWindow(final String title, final Gui gui) {
        this(title, gui, null);
    }

    public GuiWindow(final String title, final Gui gui, Component[] toolBarComponents) {

        super(title);

        _Gui = gui;

        setBorder(new Border.Standard());

        setBetweenComponentsPadding(0);

        if (toolBarComponents != null) {
            _ToolBar = new ToolBar(toolBarComponents);
            addComponent(_ToolBar);
            addComponent(new Separator());
        }
        else {
            _ToolBar = null;
        }

        addComponent(new EmptySpace(76, 0));
    }

    public Gui getGui() {
        return _Gui;
    }

    public ToolBar getToolBar() {
        return _ToolBar;
    }

}
