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

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.component.EmptySpace;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.terminal.TerminalSize;

public class TerminalAppPanel extends Panel {

    private final TerminalApp _App;

    private TerminalSize _PreferredSize;

    private boolean _HorizontallyMaximized;

    private boolean _VerticallyMaximized;

    public TerminalAppPanel(final TerminalApp app) {

        this(app, "", new Border.Standard(), Orientation.VERTICAL, false, false);
    }

    public TerminalAppPanel(final TerminalApp app, final Border border, final Orientation orientation) {

        this(app, "", border, orientation, false, false);
    }

    public TerminalAppPanel(final TerminalApp app, final String title) {

        this(app, title, new Border.Standard(), Orientation.VERTICAL, false, false);
    }

    public TerminalAppPanel(final TerminalApp app, final String title, final Border border,
                            final Orientation orientation) {

        this(app, title, border, orientation, false, false);
    }

    public TerminalAppPanel(final TerminalApp app, final String title, final Border border,
                            final Orientation orientation, final boolean horizontallyMaximized, final boolean verticallyMaximized) {

        super(title, border, orientation);
        _App = app;
        // setBetweenComponentsPadding(0);

        _HorizontallyMaximized = horizontallyMaximized;
        _VerticallyMaximized = verticallyMaximized;
    }

    public void addEmptySpace() {
        // addComponent(new Label());
        addComponent(new EmptySpace(0, 1));
    }

    @SuppressWarnings("unchecked")
    public <T extends TerminalApp> T getApp() {

        return (T) _App;
    }

    @Override
    public final TerminalSize getPreferredSize() {

        if (_PreferredSize == null) {
            return super.getPreferredSize();
        }

        return _PreferredSize;
    }

    public boolean isHorizontallyMaximized() {

        return _HorizontallyMaximized;
    }

    public boolean isVerticallyMaximized() {

        return _VerticallyMaximized;
    }

    @Override
    public final boolean maximisesHorisontally() {

        return _HorizontallyMaximized;
    }

    @Override
    public final boolean maximisesVertically() {

        return _VerticallyMaximized;
    }

    public void setHorizontallyMaximized(final boolean horizontallyMaximized) {

        _HorizontallyMaximized = horizontallyMaximized;
    }

    @Override
    public final void setPreferredSize(final TerminalSize preferredSize) {

        _PreferredSize = preferredSize;
    }

    public void setVerticallyMaximized(final boolean verticallyMaximized) {

        _VerticallyMaximized = verticallyMaximized;
    }
}
