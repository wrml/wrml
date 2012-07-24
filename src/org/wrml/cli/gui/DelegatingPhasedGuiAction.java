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

import org.wrml.util.Delegating;

public class DelegatingPhasedGuiAction extends PhasedGuiAction implements Delegating<GuiAction> {

    private final GuiAction _Delegate;

    public DelegatingPhasedGuiAction(final Gui gui, final GuiAction delegate) {
        this(null, gui, delegate);
    }

    public DelegatingPhasedGuiAction(final String title, final Gui gui) {
        this(title, gui, null);
    }

    public DelegatingPhasedGuiAction(final String title, final Gui gui, final GuiAction delegate) {
        super((title != null) ? title : delegate.getTitle(), gui);
        _Delegate = delegate;
    }

    @Override
    public final GuiAction getDelegate() {
        return _Delegate;
    }

    @Override
    protected boolean doIt() {
        final GuiAction delegate = getDelegate();
        if (delegate != null) {
            delegate.doAction();
        }
        return true;
    }

    @Override
    protected void postDoIt() {
    }

    @Override
    protected boolean preDoIt() {
        return true;
    }

}
