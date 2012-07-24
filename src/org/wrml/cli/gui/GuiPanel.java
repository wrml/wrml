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
import com.googlecode.lanterna.gui.component.EmptySpace;
import com.googlecode.lanterna.gui.component.Separator;

import org.wrml.cli.gui.base.BasePanel;
import org.wrml.cli.gui.base.ToolBar;

public class GuiPanel extends BasePanel {

    private final Gui _Gui;
    private final ToolBar _ToolBar;

    public GuiPanel(final String title, final boolean horizontallyMaximized, final boolean verticallyMaximized,
            final Gui gui, Component[] toolBarComponents) {
        this(title, new Border.Standard(), Orientation.VERTICAL, horizontallyMaximized, verticallyMaximized, gui,
                toolBarComponents);
    }

    public GuiPanel(final String title, final Border border, final Orientation orientation,
            final boolean horizontallyMaximized, final boolean verticallyMaximized, final Gui gui,
            Component[] toolBarComponents) {
        super(title, border, orientation, horizontallyMaximized, verticallyMaximized);

        _Gui = gui;

        if (toolBarComponents != null) {
            _ToolBar = new ToolBar(toolBarComponents);
            addComponent(_ToolBar);
            addComponent(new Separator());
        }
        else {
            _ToolBar = null;
            addComponent(new EmptySpace(0, 1));
        }
    }

    public GuiPanel(final String title, final Border border, final Orientation orientation, final Gui gui,
            Component[] toolBarComponents) {
        this(title, border, orientation, true, true, gui, toolBarComponents);
    }

    public GuiPanel(final String title, final Gui gui, Component[] toolBarComponents) {
        this(title, new Border.Standard(), Orientation.VERTICAL, true, true, gui, toolBarComponents);
    }

    public Gui getGui() {
        return _Gui;
    }

    public ToolBar getToolBar() {
        return _ToolBar;
    }

}