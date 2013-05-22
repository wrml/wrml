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
package org.wrml.werminal.dialog;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.component.Panel.Orientation;
import com.googlecode.lanterna.terminal.TerminalSize;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.component.HistoryCheckListBox;
import org.wrml.werminal.component.WerminalTextBox;
import org.wrml.werminal.terminal.TerminalAppButtonPanel;
import org.wrml.werminal.terminal.TerminalAppPanel;
import org.wrml.werminal.terminal.TerminalAppTextBoxPanel;
import org.wrml.werminal.terminal.TerminalAppToolBar;
import org.wrml.werminal.window.WerminalWindow;

import java.net.URI;

public class NewModelDialog extends WerminalWindow
{

    private final WerminalTextBox _SchemaUriTextBox;

    private final HistoryCheckListBox _SchemaUriHistoryCheckBoxList;

    public NewModelDialog(final Werminal werminal, final String title, final WerminalAction confirmAction,
                          final WerminalAction dismissAction)
    {

        super(werminal, title);

        setBorder(new Border.Standard());

        addEmptySpace();

        _SchemaUriTextBox = new WerminalTextBox(werminal, 70, URI.class, confirmAction);

        final TerminalAppTextBoxPanel schemaUriTextBoxPanel = new TerminalAppTextBoxPanel(werminal, " Schema (URI): ",
                _SchemaUriTextBox);
        addComponent(schemaUriTextBoxPanel);

        final TerminalAppPanel historyPanel = new TerminalAppPanel(werminal, " History: ", new Border.Standard(),
                Orientation.VERTICAL);
        _SchemaUriHistoryCheckBoxList = new HistoryCheckListBox(_SchemaUriTextBox, null, confirmAction);
        _SchemaUriHistoryCheckBoxList.setPreferredSize(new TerminalSize(70, 10));
        historyPanel.addComponent(_SchemaUriHistoryCheckBoxList);
        addComponent(historyPanel);

        final TerminalAppToolBar footerToolBar = new TerminalAppToolBar(werminal, new Component[]{
                new TerminalAppButtonPanel(confirmAction), new TerminalAppButtonPanel(dismissAction)});
        addComponent(footerToolBar);

    }

    public URI getSchemaUri()
    {

        return (URI) _SchemaUriTextBox.getValue();
    }

    public HistoryCheckListBox getSchemaUriHistoryCheckBoxList()
    {

        return _SchemaUriHistoryCheckBoxList;
    }

    public URI setSchemaUri(final URI schemaUri)
    {

        final URI oldSchemaUri = (URI) _SchemaUriTextBox.getValue();
        _SchemaUriTextBox.setValue(schemaUri);
        return oldSchemaUri;
    }

}
