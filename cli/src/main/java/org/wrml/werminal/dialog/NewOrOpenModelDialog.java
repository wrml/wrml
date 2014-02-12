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
import com.googlecode.lanterna.gui.component.EmptySpace;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.terminal.Terminal.Color;
import org.wrml.runtime.Context;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.NewModelAction;
import org.wrml.werminal.action.OpenModelAction;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.component.FormField;
import org.wrml.werminal.component.WerminalTextBox;
import org.wrml.werminal.terminal.TerminalAppButtonPanel;
import org.wrml.werminal.terminal.TerminalAppMenu;
import org.wrml.werminal.terminal.TerminalAppPanel;

import java.lang.reflect.Type;
import java.net.URI;

public class NewOrOpenModelDialog extends WerminalDialog {

    private final FormField _FormField;

    private final OptionsMenu _OptionsMenu;

    public NewOrOpenModelDialog(final Werminal werminal, final String title, final WerminalAction dismissAction, final FormField formField) {

        super(werminal, title, null, dismissAction);
        _FormField = formField;
        _OptionsMenu = new OptionsMenu(werminal, title);
        render();
    }

    public final FormField getFormField() {

        return _FormField;
    }

    @Override
    public void render() {

        removeAllComponents();
        super.renderHeaderToolBar();

        addEmptySpace();

        addComponent(_OptionsMenu);

        addEmptySpace();

        super.renderFooterToolBar();

    }

    private class OptionsMenu extends TerminalAppPanel {

        public OptionsMenu(final Werminal werminal, final String title) {

            super(werminal, title, new Border.Invisible(), Orientation.VERTICAL, false, false);

            final Context context = werminal.getContext();
            final FormField formField = getFormField();
            final WerminalTextBox valueTextBox = formField.getFieldValueTextBox();
            final Type heapValueType = valueTextBox.getHeapValueType();
            final URI schemaUri = context.getSchemaLoader().getTypeUri(heapValueType);
            final String schemaUriString = String.valueOf(schemaUri);

            final TerminalAppMenu newMenu = new TerminalAppMenu(werminal, "  New  ");
            addComponent(newMenu);

            newMenu.addComponent(new EmptySpace(0, 2));
            newMenu.addComponent(new Label("With a new: "));
            final Label newSchemaLabel = new Label(schemaUriString);
            newSchemaLabel.setTextColor(Color.RED);
            newMenu.addComponent(newSchemaLabel);

            newMenu.addComponent(new EmptySpace(0, 2));

            final TerminalAppButtonPanel newMenuItem = new TerminalAppButtonPanel(new NewModelAction(werminal, formField));
            newMenu.addComponent(newMenuItem);

            addComponent(new EmptySpace(2, 0));

            final TerminalAppMenu openMenu = new TerminalAppMenu(werminal, "  Open  ");
            addComponent(openMenu);
            openMenu.addComponent(new EmptySpace(0, 2));
            openMenu.addComponent(new Label("With an existing: "));
            final Label openSchemaLabel = new Label(schemaUriString);
            openSchemaLabel.setTextColor(Color.RED);
            openMenu.addComponent(openSchemaLabel);

            openMenu.addComponent(new EmptySpace(0, 2));

            final TerminalAppButtonPanel openMenuItem = new TerminalAppButtonPanel(new OpenModelAction(werminal, formField));
            openMenu.addComponent(openMenuItem);
        }
    }

}
