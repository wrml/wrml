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
import org.wrml.runtime.Context;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.window.ModelWindow;

public class DeleteAction extends WerminalAction
{

    public DeleteAction(final Werminal werminal)
    {

        super(werminal, "Delete");
    }

    @Override
    public void doAction()
    {

        final Werminal werminal = getWerminal();
        final ModelWindow modelWindow = (ModelWindow) werminal.getTopWindow();
        final Model model = modelWindow.syncModel();
        try
        {

            final Context context = getContext();
            context.deleteModel(model.getKeys(), model.getDimensions());

        }
        catch (final Exception t)
        {
            werminal.showError("An unexpected error occurred while deleting the model.", t);
        }

    }
}