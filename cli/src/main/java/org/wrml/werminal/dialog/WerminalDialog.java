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
package org.wrml.werminal.dialog;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.terminal.TerminalAppButtonPanel;
import org.wrml.werminal.terminal.TerminalAppToolBar;
import org.wrml.werminal.window.WerminalWindow;

public class WerminalDialog extends WerminalWindow {

    private final TerminalAppToolBar _FooterToolBar;

    public WerminalDialog(final Werminal werminal, final String title, final WerminalAction confirmAction,
                          final WerminalAction dismissAction) {

        super(werminal, title);

        setBorder(new Border.Standard());

        final Component[] footerToolBarComponents;
        if (confirmAction != null && dismissAction != null) {
            footerToolBarComponents = new Component[]{new TerminalAppButtonPanel(confirmAction),
                    new TerminalAppButtonPanel(dismissAction)};
        }
        else if (confirmAction != null) {
            footerToolBarComponents = new Component[]{new TerminalAppButtonPanel(confirmAction)};
        }

        else if (dismissAction != null) {
            footerToolBarComponents = new Component[]{new TerminalAppButtonPanel(dismissAction)};
        }
        else {
            footerToolBarComponents = null;
        }

        if (footerToolBarComponents != null) {
            _FooterToolBar = new TerminalAppToolBar(werminal, footerToolBarComponents);
        }
        else {
            _FooterToolBar = null;
        }
        render();
    }

    @Override
    public void render() {

        super.render();
        renderFooterToolBar();
    }

    protected final void renderFooterToolBar() {

        if (_FooterToolBar != null) {
            addComponent(_FooterToolBar);
        }
    }

}
