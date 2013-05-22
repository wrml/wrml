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

import org.wrml.werminal.Werminal;
import org.wrml.werminal.dialog.ListValueDialog;

import java.lang.reflect.Type;
import java.net.URI;

public class AddAction extends WerminalAction
{

    private ListValueDialog _ListValueDialog;

    public AddAction(final Werminal werminal)
    {

        super(werminal, "Add...");
    }

    @Override
    public void doAction()
    {

        final Werminal werminal = getWerminal();

        final ListValueDialog listValueDialog = getListValueDialog();
        Object initialValue = null;

        final Type listElementType = listValueDialog.getListElementType();
        if (String.class.equals(listElementType))
        {

        }
        else if (URI.class.equals(listElementType))
        {
            // TODO: Only do this for base schema uris slot
            initialValue = werminal.createSchemaUri("MyBaseModel");
        }

        listValueDialog.addFormField(initialValue);

    }

    public ListValueDialog getListValueDialog()
    {

        return _ListValueDialog;
    }

    public void setListValueDialog(final ListValueDialog listValueDialog)
    {

        _ListValueDialog = listValueDialog;
    }
}
