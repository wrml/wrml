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
package org.wrml.werminal.action;

import com.googlecode.lanterna.gui.GUIScreen.Position;
import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.Keys;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.component.FormField;
import org.wrml.werminal.component.WerminalTextBox;
import org.wrml.werminal.dialog.OpenModelDialog;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.SortedSet;
import java.util.UUID;

public class OpenModelAction extends CloseBeforeAction {

    private final FormField _FormField;

    public OpenModelAction(final Werminal werminal, final FormField formField) {

        super(werminal, "Open...");
        _FormField = formField;
    }

    public FormField getFormField() {

        return _FormField;
    }

    @Override
    protected boolean doIt() {

        final Werminal werminal = getWerminal();
        final FormField formField = getFormField();
        final Context context = werminal.getContext();

        final ConfirmAction confirmAction = new ConfirmAction(werminal);

        final WerminalTextBox valueTextBox = formField.getFieldValueTextBox();
        final Type heapValueType = valueTextBox.getHeapValueType();
        final URI schemaUri = context.getSchemaLoader().getTypeUri(heapValueType);

        final String dialogTitle = " Open \"" + schemaUri + "\"  ";
        final OpenModelDialog openModelDialog = new OpenModelDialog(werminal, dialogTitle, confirmAction,
                werminal.getCancelAction());

        confirmAction.setOpenModelDialog(openModelDialog);

        final SortedSet<Object> historyItems = werminal.getOpenModelDialog().getSchemaUriHistoryCheckBoxList().getItems();
        openModelDialog.getSchemaUriHistoryCheckBoxList().addItems(historyItems);

        openModelDialog.setSchemaUri(schemaUri);
        werminal.showWindow(openModelDialog, Position.CENTER);

        return true;
    }

    private class ConfirmAction extends CloseBeforeAction {

        private OpenModelDialog _OpenModelDialog;

        public ConfirmAction(final Werminal werminal) {

            super(werminal, "OK");
        }

        public OpenModelDialog getOpenModelDialog() {

            return _OpenModelDialog;
        }

        public void setOpenModelDialog(final OpenModelDialog openModelDialog) {

            _OpenModelDialog = openModelDialog;

        }

        @Override
        protected boolean doIt() {

            final OpenModelDialog openModelDialog = getOpenModelDialog();

            final URI schemaUri = openModelDialog.getSchemaUri();
            final Keys keys = openModelDialog.getKeys();
            final UUID heapId = openModelDialog.getHeapId();

            final Werminal werminal = getWerminal();

            if (schemaUri == null) {
                werminal.showError("\nPlease indicate the type of data that you would like to open by entering a Schema URI value.\n\n ");
                return false;
            }
            else if (keys == null || keys.getKeyedSchemaUris().isEmpty()) {
                werminal.showError("\nPlease enter one or more key values to identify the data that you would like to open.\n\n ");
                return false;
            }

            final Model openedModel = werminal.openModel(schemaUri, keys, heapId);

            final FormField formField = getFormField();
            formField.getFieldValueTextBox().setValue(openedModel, false);

            return true;
        }
    }

}
