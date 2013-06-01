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
package org.wrml.werminal.component;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.component.CheckBox;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.terminal.TerminalAppPanel;

import java.lang.reflect.Type;

public class FormField extends TerminalAppPanel
{

    private final CheckBox _FieldCheckBox;

    private final WerminalTextBox _FieldValueTextBox;

    private final String _FieldName;

    public FormField(final String fieldName, final Type valueType, final WerminalAction enterAction)
    {

        super(enterAction.getApp(),
                "  " + fieldName + " (" + enterAction.getWerminal().getTypeTitle(valueType) + "): ",
                new Border.Standard(), Orientation.HORISONTAL, false, false);

        _FieldName = fieldName;

        _FieldCheckBox = new CheckBox("", false);

        _FieldValueTextBox = new WerminalTextBox((Werminal) enterAction.getApp(), valueType, enterAction);

        render();
    }

    public CheckBox getFieldCheckBox()
    {

        return _FieldCheckBox;
    }

    public String getFieldName()
    {

        return _FieldName;
    }

    public WerminalTextBox getFieldValueTextBox()
    {

        return _FieldValueTextBox;
    }

    protected void render()
    {
        // addComponent(_FieldCheckBox);
        addComponent(_FieldValueTextBox);
    }

}
