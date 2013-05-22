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

import com.googlecode.lanterna.gui.Window;
import org.wrml.model.Model;
import org.wrml.runtime.DimensionsBuilder;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.component.FormField;
import org.wrml.werminal.dialog.InvocationDialog;

public class SubmitAction extends CloseAfterAction
{

    private final Model _Function;

    private final FormField _FormField;

    public SubmitAction(final Werminal werminal, final Model function, final FormField formField)
    {

        super(werminal, "Submit");
        _Function = function;
        _FormField = formField;
    }

    @Override
    protected boolean doIt()
    {

        final Werminal werminal = getWerminal();
        final Window topWindow = werminal.getTopWindow();

        if (!(topWindow instanceof InvocationDialog))
        {
            werminal.showError("The " + getTitle() + " action requires a top level "
                    + InvocationDialog.class.getSimpleName());
            return false;
        }

        final InvocationDialog invocationDialog = (InvocationDialog) topWindow;

        final String formFieldName = _FormField.getFieldName();
        final Model parameter = invocationDialog.getParameter();

        final Model returnValue;
        try
        {
            returnValue = _Function.reference(formFieldName, new DimensionsBuilder(), parameter);
        }
        catch (Exception t)
        {
            werminal.showError("Invocation Failed.", t);
            return false;
        }

        if (returnValue != null)
        {
            werminal.openModelWindow(returnValue);
        }

        return true;
    }
}
