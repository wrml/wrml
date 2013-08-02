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

import org.wrml.runtime.Context;
import org.wrml.runtime.schema.Prototype;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.dialog.NewModelDialog;

import java.net.URI;

public class NewConfirmationAction extends CloseBeforeAction
{

    public NewConfirmationAction(final Werminal werminal)
    {

        super(werminal, "OK");
    }

    @Override
    protected boolean doIt()
    {

        final Werminal werminal = getWerminal();

        final NewModelDialog newModelDialog = werminal.getNewModelDialog();
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

        werminal.newModelWindow(schemaUri, null);

        return true;
    }

}
