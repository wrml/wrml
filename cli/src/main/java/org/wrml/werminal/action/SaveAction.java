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

import org.wrml.model.Model;
import org.wrml.model.rest.Embedded;
import org.wrml.runtime.Context;
import org.wrml.runtime.Keys;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.window.ModelWindow;

public class SaveAction extends WerminalAction
{

    public SaveAction(final Werminal werminal)
    {

        super(werminal, "Save");
    }

    @Override
    public void doAction()
    {

        final Werminal werminal = getWerminal();
        final ModelWindow modelWindow = (ModelWindow) werminal.getTopWindow();
        final Model model;

        try
        {
            model = modelWindow.syncModel();
        }
        catch (final Exception e)
        {
            getWerminal().showError(e.getMessage());
            return;
        }

        if (model instanceof Embedded || model.getPrototype().getAllKeySlotNames().isEmpty())
        {
            werminal.showMessageBox("Form Edits Applied", "The model's state was successfully committed (locally).");
            return;
        }

        final Keys keys = model.getKeys();
        if (keys == null)
        {
            werminal.showMessageBox("Error - Save Failed", "Cannot save model; all of the key slot values are blank.");
            return;
        }

        final Model savedModel;

        try
        {

            final Context context = getContext();

            savedModel = context.saveModel(model);

            if (savedModel == null)
            {
                werminal.showError("An unexpected error has occurred and the model could not be saved.");
                return;
            }

        }
        catch (final Exception t)
        {
            werminal.showError("An unexpected error has occurred and the model could not be saved.", t);
            return;
        }

        modelWindow.setModel(savedModel);
        werminal.showMessageBox("Save", "The model was successfully saved.");

    }

}
