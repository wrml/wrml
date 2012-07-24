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
import com.googlecode.lanterna.gui.component.EmptySpace;
import com.googlecode.lanterna.gui.component.TextArea;
import com.googlecode.lanterna.gui.layout.SizePolicy;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.TerminalSize;

import org.wrml.cli.gui.base.BasePanel;
import org.wrml.cli.gui.base.BaseTextArea;
import org.wrml.cli.gui.base.Menu;
import org.wrml.cli.gui.base.MenuBarWindow;

public class SplashWindow extends MenuBarWindow {

    private final static String ASCII_ART_LOGO;

    static {
        final StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append("                    Web Resource Modeling Language     ");
        sb.append('\n');
        sb.append('\n');
        sb.append("             __     __     ______     __    __     __             ");
        sb.append('\n');
        sb.append("            /\\ \\  _ \\ \\   /\\  == \\   /\\ \"-./  \\   /\\ \\            ");
        sb.append('\n');
        sb.append("            \\ \\ \\/ \".\\ \\  \\ \\  __<   \\ \\ \\-./\\ \\  \\ \\ \\____       ");
        sb.append('\n');
        sb.append("             \\ \\__/\".~\\_\\  \\ \\_\\ \\_\\  \\ \\_\\ \\ \\_\\  \\ \\_____\\      ");
        sb.append('\n');
        sb.append("              \\/_/   \\/_/   \\/_/ /_/   \\/_/  \\/_/   \\/_____/      ");

        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append("                          Press any to begin.     ");
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append("               Copyright WRML.org.  All Rights Reserved.   ");
        sb.append('\n');

        ASCII_ART_LOGO = sb.toString();

    }

    private final static TerminalSize PREFERRED_MENU_SIZE = new TerminalSize(75, 35);
    private final static TerminalSize PREFERRED_TEXT_SIZE = new TerminalSize(70, 30);

    public SplashWindow() {
        super("", new SplashPanel(), null);

        setBorder(new Border.Invisible());
        setSoloWindow(true);
    }

    public static class SplashPanel extends BasePanel {

        public SplashPanel() {
            super("", new Border.Invisible(), Orientation.HORISONTAL, true, false);

            addComponent(new EmptySpace(2, 0));

            final Menu textMenu = new Menu();
            textMenu.setPreferredSize(PREFERRED_MENU_SIZE);
            addComponent(textMenu, SizePolicy.CONSTANT);

            final TextArea textArea = new AsciiTextArea(PREFERRED_TEXT_SIZE, ASCII_ART_LOGO);
            textMenu.addComponent(textArea, SizePolicy.CONSTANT);

            addComponent(new EmptySpace(2, 0));
        }

        @Override
        public boolean isScrollable() {
            return false;
        }
    }

    private static class AsciiTextArea extends BaseTextArea {

        public AsciiTextArea(TerminalSize preferredSize, String text) {
            super(preferredSize, text);
        }

        @Override
        public boolean isScrollable() {
            return false;
        }

        @Override
        public Result keyboardInteraction(Key key) {

            getGUIScreen().closeWindow();
            return Result.DO_NOTHING;
        }

    }

}
