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
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.terminal.TerminalSize;

public class Menu extends BasePanel {

    private final static TerminalSize DEFAULT_PREFERRED_SIZE = new TerminalSize(26, 27);

    public Menu() {
        this(null);
    }

    public Menu(final String title) {
        super(title, new Border.Bevel(true), Panel.Orientation.VERTICAL);
        setPreferredSize(DEFAULT_PREFERRED_SIZE);
    }
}
