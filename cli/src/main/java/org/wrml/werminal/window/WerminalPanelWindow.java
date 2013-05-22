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
 * Copyright (C) 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
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

import com.googlecode.lanterna.gui.Component;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.component.WerminalPanel;
import org.wrml.werminal.terminal.TerminalAppButtonPanel;
import org.wrml.werminal.terminal.TerminalAppToolBar;

import java.util.ArrayList;
import java.util.List;

public class WerminalPanelWindow extends WerminalWindow
{

    private final static int PREVIOUS_BUTTON_INDEX = 0;

    private final static int NEXT_BUTTON_INDEX = 2;

    private WerminalPanel _CurrentPanel;

    private final List<WerminalPanel> _Panels;

    private final WerminalAction _NextAction;

    private final WerminalAction _PreviousAction;

    public WerminalPanelWindow(final Werminal werminal, final String title, final Component[] toolBarComponents)
    {

        super(werminal, title, toolBarComponents);

        _Panels = new ArrayList<WerminalPanel>();

        _NextAction = new NextAction(werminal);
        _PreviousAction = new PreviousAction(werminal);
    }

    public final WerminalPanel getCurrentPanel()
    {

        return _CurrentPanel;
    }

    public final WerminalAction getNextAction()
    {

        return _NextAction;
    }

    public final int getPanelCount()
    {

        return _Panels.size();
    }

    @SuppressWarnings("unchecked")
    public final <W extends WerminalPanel> List<W> getPanels()
    {

        return (List<W>) _Panels;
    }

    public final WerminalAction getPreviousAction()
    {

        return _PreviousAction;
    }

    public final void goToFirstPanel()
    {

        if (_Panels.isEmpty())
        {
            return;
        }
        setCurrentPanel(_Panels.get(0));
    }

    public final void goToLastPanel()
    {

        if (_Panels.isEmpty())
        {
            return;
        }
        setCurrentPanel(_Panels.get(_Panels.size() - 1));
    }

    public final void goToNextPanel()
    {

        if (_Panels.isEmpty())
        {
            return;
        }
        directPanel(false);
    }

    public final void goToPreviousPanel()
    {

        if (_Panels.isEmpty())
        {
            return;
        }
        directPanel(true);
    }

    public final void setCurrentPanel(final WerminalPanel currentPanel)
    {

        _CurrentPanel = currentPanel;
        render();
    }

    protected final void directPanel(final boolean reverse)
    {

        final int panelIndexIncrement = (reverse) ? -1 : 1;

        setCurrentPanel(_Panels.get(getCurrentPanel().getPanelIndex() + panelIndexIncrement));

        final TerminalAppToolBar toolBar = getCurrentPanel().getToolBar();
        if (toolBar != null)
        {
            final int toolBarComponentCount = toolBar.getComponents().length;
            int toolBarButtonIndex = (reverse) ? WerminalPanelWindow.PREVIOUS_BUTTON_INDEX
                    : WerminalPanelWindow.NEXT_BUTTON_INDEX;
            toolBarButtonIndex = (toolBarButtonIndex < toolBarComponentCount) ? toolBarButtonIndex : 0;
            final Component focusComponent = toolBar.getComponents()[toolBarButtonIndex];
            if (focusComponent instanceof TerminalAppButtonPanel)
            {
                setFocus(((TerminalAppButtonPanel) focusComponent).getButton());
            }
        }
    }

    @Override
    public void render()
    {

        super.render();
        renderCurrentPanel();
    }

    protected void renderCurrentPanel()
    {

        if (_CurrentPanel != null)
        {
            addComponent(_CurrentPanel);
        }
    }

    private class NextAction extends WerminalAction
    {

        protected NextAction(final Werminal werminal)
        {

            super(werminal, "Next");
        }

        @Override
        public void doAction()
        {

            goToNextPanel();
        }

    }

    private class PreviousAction extends WerminalAction
    {

        protected PreviousAction(final Werminal werminal)
        {

            super(werminal, "Previous");
        }

        @Override
        public void doAction()
        {

            goToPreviousPanel();
        }
    }

}
