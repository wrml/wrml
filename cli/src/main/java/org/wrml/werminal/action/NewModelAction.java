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
package org.wrml.werminal.action;

import com.googlecode.lanterna.gui.GUIScreen.Position;
import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.schema.Prototype;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.component.FormField;
import org.wrml.werminal.component.WerminalTextBox;
import org.wrml.werminal.dialog.NewModelDialog;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.SortedSet;

public class NewModelAction extends CloseBeforeAction
{

    private final FormField _FormField;

    public NewModelAction(final Werminal werminal, final FormField formField)
    {

        super(werminal, "New...");
        _FormField = formField;
    }

    public FormField getFormField()
    {

        return _FormField;
    }

    @Override
    protected boolean doIt()
    {

        final Werminal werminal = getWerminal();
        final FormField formField = getFormField();
        final Context context = werminal.getContext();

        final ConfirmAction confirmAction = new ConfirmAction(werminal);

        final WerminalTextBox valueTextBox = formField.getFieldValueTextBox();
        final Type heapValueType = valueTextBox.getHeapValueType();
        final URI schemaUri = context.getSchemaLoader().getTypeUri(heapValueType);
        final String dialogTitle = "New \"" + schemaUri + "\"";
        final NewModelDialog newModelDialog = new NewModelDialog(werminal, dialogTitle, confirmAction,
                werminal.getCancelAction());

        confirmAction.setNewModelDialog(newModelDialog);

        final SortedSet<Object> historyItems = werminal.getNewModelDialog().getSchemaUriHistoryCheckBoxList().getItems();
        newModelDialog.getSchemaUriHistoryCheckBoxList().addItems(historyItems);

        newModelDialog.setSchemaUri(schemaUri);
        werminal.showWindow(newModelDialog, Position.CENTER);

        return true;
    }

    private class ConfirmAction extends CloseBeforeAction
    {

        private NewModelDialog _NewModelDialog;

        public ConfirmAction(final Werminal werminal)
        {

            super(werminal, "OK");
        }

        public NewModelDialog getNewModelDialog()
        {

            return _NewModelDialog;
        }

        public void setNewModelDialog(final NewModelDialog newModelDialog)
        {

            _NewModelDialog = newModelDialog;

        }

        @Override
        protected boolean doIt()
        {

            final Werminal werminal = getWerminal();
            final FormField formField = getFormField();

            final NewModelDialog newModelDialog = getNewModelDialog();
            final URI schemaUri = newModelDialog.getSchemaUri();

            final Context context = werminal.getContext();

            final Prototype prototype = context.getSchemaLoader().getPrototype(schemaUri);
            if (prototype.isAbstract())
            {
                werminal.showError("\""
                        + schemaUri
                        + "\" is *Abstract*, meaning that models cannot be created based on this type directly. Try a subschema?");
                return false;
            }

            try
            {
                final Model newModel = context.newModel(schemaUri);
                formField.getFieldValueTextBox().setValue(newModel, false);
                werminal.openModelWindow(newModel);
            }
            catch (final Exception t)
            {
                final String errorMessage = "An unexpected error has occurred.";
                werminal.showError(errorMessage, t);
                Werminal.LOG.error(errorMessage + " (" + t.getMessage() + ")", t);
                return false;
            }

            return true;
        }
    }

}
