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

import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.dialog.DialogButtons;
import com.googlecode.lanterna.gui.dialog.DialogResult;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import org.wrml.runtime.Context;
import org.wrml.runtime.Keys;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.rest.ApiNavigator;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.dialog.LoadApiDialog;
import org.wrml.werminal.dialog.OpenModelDialog;

import java.net.URI;

public class OpenConfirmationAction extends CloseBeforeAction
{

    public OpenConfirmationAction(final Werminal werminal)
    {

        super(werminal, "OK");
    }

    @Override
    protected boolean doIt()
    {

        final Werminal werminal = getWerminal();
        final Context context = werminal.getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();

        final OpenModelDialog openModelDialog = werminal.getOpenModelDialog();
        final URI schemaUri = openModelDialog.getSchemaUri();
        final Keys keys = openModelDialog.getKeys();

        URI uri = null;
        ApiNavigator apiNavigator = null;

        if (keys != null)
        {
            final ApiLoader apiLoader = context.getApiLoader();
            uri = keys.getValue(schemaLoader.getDocumentSchemaUri());
            if (uri != null)
            {
                apiNavigator = apiLoader.getParentApiNavigator(uri);
            }
        }

        Window window = null;
        boolean cancelled = false;

        if (schemaUri == null)
        {
            werminal.showError("\nPlease indicate the type of data that you would like to open by entering a Schema URI value.\n\n ");
            cancelled = true;
        }
        else if (keys == null || keys.getKeyedSchemaUris().isEmpty())
        {
            werminal.showError("\nPlease enter one or more key values to identify the data that you would like to open.\n\n ");
            cancelled = true;
        }
        else if (uri != null && !schemaLoader.getApiSchemaUri().equals(schemaUri) && apiNavigator == null)
        {

            final GUIScreen owner = werminal.getGuiScreen();
            final String title = "Load Parent REST API Metadata?";
            final String message = werminal
                    .formatMessageBoxTextToWrap("\nThe URI:\n\n\t"
                            + uri
                            + "\n\nIs not parented by any loaded REST API metadata.\n\nDo you wish to load this URI's parent REST API metadata now?\n\n  ");
            final DialogButtons buttons = DialogButtons.YES_NO_CANCEL;
            final DialogResult result = MessageBox.showMessageBox(owner, title, message, buttons);

            if (result == DialogResult.YES)
            {
                final LoadApiDialog loadApiDialog = werminal.getLoadApiDialog();
                loadApiDialog.setApiUri(uri);
                werminal.showWindow(loadApiDialog);
                if (loadApiDialog.isCancelled())
                {
                    cancelled = true;
                }

            }
            else if (result == DialogResult.CANCEL)
            {
                cancelled = true;
            }

        }

        if (!cancelled)
        {
            window = werminal.openModelWindow(schemaUri, keys, openModelDialog.getHeapId());
        }

        if (window == null)
        {
            // Kind of a hack, re-open the open dialog.
            werminal.getOpenAction().doAction();
            return false;
        }

        return true;
    }

}
