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

package org.wrml.cli.gui.base;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.EmptySpace;
import com.googlecode.lanterna.gui.layout.SizePolicy;

public class MenuBarWindow extends Window {

    private final BasePanel _MenuBar;
    private final Component _FooterComponent;

    public MenuBarWindow(final String title, final BasePanel menuBar, final Component footerComponent) {
        super(title);

        _MenuBar = menuBar;
        _FooterComponent = footerComponent;

        setBorder(new Border.Bevel(false));

        setBetweenComponentsPadding(0);

        addComponent(new EmptySpace(0, 1));

        addComponent(_MenuBar);

        // Refactoring note (may be deleted)
        //closeButtonPanel.getButton().setPreferredSize(new TerminalSize(10, 1));

        if (_FooterComponent != null) {
            addComponent(_FooterComponent, SizePolicy.CONSTANT);
        }
    }

    public BasePanel getMenuBar() {
        return _MenuBar;
    }

}
