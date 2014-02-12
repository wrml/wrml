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
package org.wrml.werminal.terminal;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.dialog.DialogResult;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.text.UnixTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.runtime.Context;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * <p>
 * {@link TerminalApp} is a terminal (command line) application.
 * </p>
 */
public class TerminalApp {

    private static final Logger LOG = LoggerFactory.getLogger(TerminalApp.class);

    private final String _AppTitle;

    private final Context _Context;

    private final GUIScreen _GuiScreen;

    private final LinkedList<Window> _WindowStack;

    public TerminalApp(final String appTitle, final Context context) throws Exception {

        this(appTitle, context, TerminalType.Swing);
    }

    public TerminalApp(final String appTitle, final Context context, final TerminalType terminalType) throws Exception {

        _AppTitle = appTitle;
        _Context = context;

        final Terminal terminal;
        switch (terminalType) {

            case Swing: {
            /*
             * This construction of the GUIScreen leads to the less cool looking
             * (but still pretty cool) Swing Terminal emulator.
             */
                // Setting to arbitrary large dimensions to scale the window up more.
                terminal = TerminalFacade.createSwingTerminal(110, 60);
                break;
            }
            case Unix: {
            /*
             * Using the UnixTerminal forces the use of the much cooler looking
             * Terminal shell (on Mac OS X at least).
             */

                terminal = new UnixTerminal(System.in, System.out, Charset.forName("UTF8"));
                break;
            }
            default:
                throw new TerminalAppException("Unknkown terminal type: " + terminalType, null, this);
        }

        _GuiScreen = TerminalFacade.createGUIScreen(terminal);

        _WindowStack = new LinkedList<>();

        if (_GuiScreen == null) {
            LOG.error("Couldn't allocate a terminal!");
            throw new TerminalAppException("Couldn't allocate a terminal!", null, this);
        }

    }

    public final Context getContext() {

        return _Context;
    }

    public final void closeTopWindow() {

        if (_WindowStack.isEmpty()) {
            return;
        }

        final Window topWindowToClose = _WindowStack.peek();

        // TODO: Check window edited state before closing.
        topWindowToClose.close();
        _WindowStack.pop();
        _GuiScreen.getScreen().refresh();

    }

    /**
     * Keeps line breaks in input string but also adds additional to wrap to fit screen.
     */
    public final String formatMessageBoxTextToWrap(final String input) {

        // TODO: Write a better algorithm for preservation of leading whitespace
        final String funkyText = "`Z!0";
        final String text = input.replace("    ", funkyText).replace("\t", funkyText);

        final int columns = _GuiScreen.getScreen().getTerminalSize().getColumns();
        final int maxWidth = columns - 10;
        final StringBuilder wrapped = new StringBuilder();

        final String[] lines = text.split("\n");

        for (final String line : lines) {
            int lineLen = 0;
            final String[] words = line.split("\\s+");

            for (final String word : words) {
                if (lineLen + word.length() > maxWidth) {
                    wrapped.append("\n");
                    lineLen = 0;
                }

                wrapped.append(word);
                wrapped.append(" ");
                lineLen += word.length() + 1;
            }
            wrapped.append("\n");
        }

        return wrapped.toString().replace(funkyText, "    ");
    }

    public final GUIScreen getGuiScreen() {

        return _GuiScreen;
    }

    public final Window getTopWindow() {

        return _WindowStack.peek();
    }

    public final void showError(final String errorMessage) {

        showError(errorMessage, null);
    }

    public final void showError(final String errorMessage, final Throwable t) {

        if (t != null) {
            showMessageBox("Error", "\n" + errorMessage + "\n\nError Details:\n\n" + t + "\n\nError Stack:\n\n" + Arrays.deepToString(t.getStackTrace()));
        }
        else {
            showMessageBox("Error", "\n" + errorMessage);
        }
    }

    public final DialogResult showMessageBox(final String title, final String message) {

        final StringBuilder titleBuilder = new StringBuilder("  ").append(getAppTitle()).append(" - ")
                .append(title).append("  ");

        return MessageBox.showMessageBox(_GuiScreen, titleBuilder.toString(), formatMessageBoxTextToWrap(message));
    }

    public final String getAppTitle() {

        return _AppTitle;
    }

    public final void showWindow(final Window window) {

        showWindow(window, GUIScreen.Position.CENTER);
    }

    public final void showWindow(final Window window, final GUIScreen.Position position) {

        _WindowStack.push(window);
        _GuiScreen.showWindow(window, position);
    }


}
