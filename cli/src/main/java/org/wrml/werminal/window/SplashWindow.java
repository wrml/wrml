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
package org.wrml.werminal.window;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.component.EmptySpace;
import com.googlecode.lanterna.gui.component.TextArea;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.TerminalSize;
import org.wrml.util.AsciiArt;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.terminal.TerminalAppMenu;
import org.wrml.werminal.terminal.TerminalAppMenuWindow;
import org.wrml.werminal.terminal.TerminalAppPanel;
import org.wrml.werminal.terminal.TerminalAppTextArea;

import java.util.Calendar;

public class SplashWindow extends TerminalAppMenuWindow
{

    public final static String WERMINAL_SPLASH_SCREEN;

    static
    {

        final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        final String attribution = "Mark Masse <mark@wrml.org> (WRML.org)";

        final StringBuilder splash = new StringBuilder();
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append("                     Web Resource Modeling Language");
        splash.append('\n');
        splash.append(AsciiArt.LOGO);
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append("                          Welcome to Werminal");
        splash.append('\n');
        splash.append('\n');
        splash.append("                  A keyboard-only terminal/console app");
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append("               < Please press any key to start modeling >");
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append('\n');
        splash.append("        Copyright (C) " + currentYear + " " + attribution);
        splash.append('\n');

        WERMINAL_SPLASH_SCREEN = splash.toString();

    }

    private final static TerminalSize PREFERRED_MENU_SIZE = new TerminalSize(78, 40);

    private final static TerminalSize PREFERRED_TEXT_SIZE = new TerminalSize(70, 38);

    public SplashWindow(final Werminal werminal)
    {

        super(werminal, "", new SplashPanel(werminal), null);

        setBorder(new Border.Invisible());
        setSoloWindow(true);
    }

    public static class SplashPanel extends TerminalAppPanel
    {

        public SplashPanel(final Werminal werminal)
        {

            super(werminal, "", new Border.Invisible(), Orientation.HORISONTAL, true, false);

            addComponent(new EmptySpace(2, 0));

            final TerminalAppMenu textMenu = new TerminalAppMenu(werminal);
            textMenu.setPreferredSize(SplashWindow.PREFERRED_MENU_SIZE);
            addComponent(textMenu);

            final TextArea textArea = new AsciiTextArea(werminal, SplashWindow.PREFERRED_TEXT_SIZE,
                    WERMINAL_SPLASH_SCREEN);
            textMenu.addComponent(textArea);

            addComponent(new EmptySpace(2, 0));
        }

        @Override
        public boolean isScrollable()
        {

            return false;
        }
    }

    private static class AsciiTextArea extends TerminalAppTextArea
    {

        public AsciiTextArea(final Werminal werminal, final TerminalSize preferredSize, final String text)
        {

            super(werminal, preferredSize, text);
        }

        @Override
        public boolean isScrollable()
        {

            return false;
        }

        @Override
        public Result keyboardInteraction(final Key key)
        {

            getApp().closeTopWindow();
            return Result.EVENT_HANDLED;
        }

    }

}
