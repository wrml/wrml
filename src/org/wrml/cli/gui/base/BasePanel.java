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

public class BasePanel extends Panel {

    private TerminalSize _PreferredSize;
    private boolean _HorizontallyMaximized;

    private boolean _VerticallyMaximized;

    public BasePanel() {
        this("", new Border.Standard(), Orientation.VERTICAL, false, false);
    }

    public BasePanel(final Border border, final Orientation orientation) {
        this("", border, orientation, false, false);
    }

    public BasePanel(final String title, final Border border, final Orientation orientation) {
        this(title, border, orientation, false, false);
    }

    public BasePanel(final String title, final Border border, final Orientation orientation,
            final boolean horizontallyMaximized, final boolean verticallyMaximized) {

        super(title, border, orientation);

        setBetweenComponentsPadding(0);

        _HorizontallyMaximized = horizontallyMaximized;
        _VerticallyMaximized = verticallyMaximized;
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

    public void setHorizontallyMaximized(boolean horizontallyMaximized) {
        _HorizontallyMaximized = horizontallyMaximized;
    }

    public final void setPreferredSize(TerminalSize preferredSize) {
        _PreferredSize = preferredSize;
    }

    public void setVerticallyMaximized(boolean verticallyMaximized) {
        _VerticallyMaximized = verticallyMaximized;
    }

    @Override
    protected final boolean maximisesHorisontally() {
        return _HorizontallyMaximized;
    }

    @Override
    protected final boolean maximisesVertically() {
        return _VerticallyMaximized;
    }
}
