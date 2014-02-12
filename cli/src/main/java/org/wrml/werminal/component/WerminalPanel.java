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
package org.wrml.werminal.component;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.component.EmptySpace;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.Separator;
import com.googlecode.lanterna.terminal.Terminal;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.terminal.TerminalAppButtonPanel;
import org.wrml.werminal.terminal.TerminalAppPanel;
import org.wrml.werminal.terminal.TerminalAppToolBar;

public class WerminalPanel extends TerminalAppPanel {

    private final WerminalAction _NextAction;

    private final WerminalAction _PreviousAction;

    private TerminalAppToolBar _ToolBar;

    private int _PanelIndex;

    private int _PanelCount;

    public WerminalPanel(final Werminal werminal, final String title) {

        this(werminal, title, true, true);
    }

    public WerminalPanel(final Werminal werminal, final String title, final boolean horizontallyMaximized, final boolean verticallyMaximized) {

        this(werminal, title, horizontallyMaximized, verticallyMaximized, null, null);
    }

    public WerminalPanel(final Werminal werminal, final String title, final WerminalAction nextAction, final WerminalAction previousAction) {

        this(werminal, title, new Border.Standard(), Orientation.VERTICAL, nextAction, previousAction);
    }

    public WerminalPanel(final Werminal werminal, final String title, final boolean horizontallyMaximized,
                         final boolean verticallyMaximized, final WerminalAction nextAction, final WerminalAction previousAction) {

        this(werminal, title, new Border.Standard(), Orientation.VERTICAL, horizontallyMaximized, verticallyMaximized, nextAction, previousAction);
    }


    public WerminalPanel(final Werminal werminal, final String title, final Border border,
                         final Orientation orientation, final WerminalAction nextAction, final WerminalAction previousAction) {

        this(werminal, title, border, orientation, true, true, nextAction, previousAction);
    }

    public WerminalPanel(final Werminal werminal, final String title, final Border border,
                         final Orientation orientation, final boolean horizontallyMaximized, final boolean verticallyMaximized,
                         final WerminalAction nextAction, final WerminalAction previousAction) {

        super(werminal, title, border, orientation, horizontallyMaximized, verticallyMaximized);

        _NextAction = nextAction;
        _PreviousAction = previousAction;

        render();
    }


    public WerminalAction getNextAction() {

        return _NextAction;
    }

    public final int getPanelCount() {

        return _PanelCount;
    }

    public final int getPanelIndex() {

        return _PanelIndex;
    }

    public WerminalAction getPreviousAction() {

        return _PreviousAction;
    }

    public final TerminalAppToolBar getToolBar() {

        return _ToolBar;
    }

    public final Werminal getWerminal() {

        return getApp();
    }

    public final void setPanelCount(final int panelCount) {

        _PanelCount = panelCount;
        render();
    }

    public final void setPanelIndex(final int panelIndex) {

        _PanelIndex = panelIndex;
        render();
    }

    protected final Component[] getNavigationToolBarComponents() {

        final String panelNumberSpacePaddingPrefix;

        final int panelIndex = getPanelIndex();
        final int panelCount = getPanelCount();

        final boolean isFirstPanel = (panelIndex == 0);
        final boolean isLastPanel = (panelIndex == (panelCount - 1));

        if (isFirstPanel && isLastPanel) {
            return null;
        }

        if ((panelIndex < 10) & (panelCount < 10)) {
            panelNumberSpacePaddingPrefix = "";
        }
        else if ((panelIndex < 10) && (panelCount < 100)) {
            panelNumberSpacePaddingPrefix = " ";
        }
        else if ((panelIndex < 100) && (panelCount < 1000)) {
            if (panelIndex < 10) {
                panelNumberSpacePaddingPrefix = "  ";
            }
            else {
                panelNumberSpacePaddingPrefix = " ";
            }
        }
        else {
            panelNumberSpacePaddingPrefix = "";
        }

        final int panelDisplayNumber = panelIndex + 1;

        final TerminalAppPanel panelNumberPanel = new TerminalAppPanel(getWerminal(), new Border.Invisible(),
                Panel.Orientation.VERTICAL);
        panelNumberPanel.addComponent(new EmptySpace(0, 1));
        final Label pageLabel = new Label(panelNumberSpacePaddingPrefix + panelDisplayNumber + " of " + panelCount, 8,
                Terminal.Color.BLACK, false);
        pageLabel.setAlignment(Alignment.CENTER);
        panelNumberPanel.addComponent(pageLabel);

        panelNumberPanel.addComponent(new EmptySpace(0, 1));

        final WerminalAction nextAction = getNextAction();
        final WerminalAction previousAction = getPreviousAction();
        final Component[] toolBarComponents;

        if (nextAction != null && isFirstPanel) {
            toolBarComponents = new Component[]{new TerminalAppButtonPanel(nextAction), panelNumberPanel};
        }
        else if (previousAction != null && isLastPanel) {
            toolBarComponents = new Component[]{new TerminalAppButtonPanel(previousAction), panelNumberPanel};
        }
        else if (nextAction != null && previousAction != null) {
            toolBarComponents = new Component[]{new TerminalAppButtonPanel(getPreviousAction()), panelNumberPanel,
                    new TerminalAppButtonPanel(getNextAction())};
        }
        else {
            toolBarComponents = null;
        }

        return toolBarComponents;

    }

    protected void render() {

        removeAllComponents();
        renderToolBar();
    }

    protected final void renderToolBar() {

        final Component[] toolBarComponents = getNavigationToolBarComponents();
        if (toolBarComponents != null) {
            _ToolBar = new TerminalAppToolBar(getWerminal(), toolBarComponents);
        }
        else {
            _ToolBar = null;
        }

        if (_ToolBar != null) {
            addComponent(_ToolBar);
            addComponent(new Separator());
        }
        else {
            addComponent(new EmptySpace(0, 1));
        }

    }

}
