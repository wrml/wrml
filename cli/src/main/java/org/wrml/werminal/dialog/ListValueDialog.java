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
package org.wrml.werminal.dialog;

import com.googlecode.lanterna.gui.Component;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.component.FormField;
import org.wrml.werminal.component.FormPanel;
import org.wrml.werminal.component.WerminalPanel;
import org.wrml.werminal.terminal.TerminalAppButtonPanel;
import org.wrml.werminal.terminal.TerminalAppToolBar;
import org.wrml.werminal.window.FormPanelWindow;

import java.lang.reflect.Type;
import java.util.*;

public class ListValueDialog extends FormPanelWindow
{

    private Type _ListElementType;

    private final TerminalAppToolBar _FooterToolBar;

    public ListValueDialog(final Werminal werminal, final String title, final Component[] toolBarComponents,
                           final WerminalAction confirmAction, final WerminalAction dismissAction)
    {

        super(werminal, title, toolBarComponents);

        _FooterToolBar = new TerminalAppToolBar(werminal, new Component[]{new TerminalAppButtonPanel(confirmAction),
                new TerminalAppButtonPanel(dismissAction)});

        render();
    }

    public void addFormField(final Object value)
    {

        addFormField("Elements", getListElementType(), value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<?> getList()
    {

        final List list = new ArrayList<>();

        final List<WerminalPanel> panels = getPanels();
        for (final WerminalPanel panel : panels)
        {

            if (panel instanceof FormPanel)
            {

                final FormPanel formPanel = (FormPanel) panel;

                final Set<String> fieldNames = formPanel.getFieldNames();

                for (final String fieldName : fieldNames)
                {
                    final FormField field = formPanel.getFormField(fieldName);
                    list.add(field.getFieldValueTextBox().getValue());
                }
            }
        }

        return (List<?>) list;
    }

    public Type getListElementType()
    {

        return _ListElementType;
    }

    public void setList(final List<?> list, final Type elementType)
    {

        _ListElementType = elementType;

        final List<WerminalPanel> panels = getPanels();
        panels.clear();

        // _ElementLabel.getValueLabel().setText("List Element Type: " + _ElementType);

        final Map<String, Type> fieldMap = new LinkedHashMap<>();

        for (int i = 0; i < list.size(); i++)
        {
            final String fieldName = String.valueOf(i);
            fieldMap.put(fieldName, _ListElementType);
        }

        initPanels("Elements", fieldMap);

        for (int i = 0; i < list.size(); i++)
        {
            final String fieldName = String.valueOf(i);
            final Object value = list.get(i);
            final FormField formField = getFormField(fieldName);
            formField.getFieldValueTextBox().setValue(value);
        }

    }

    @Override
    public void render()
    {

        super.render();
        renderFooterToolBar();
    }

    protected void renderFooterToolBar()
    {

        addEmptySpace();
        if (_FooterToolBar != null)
        {
            addComponent(_FooterToolBar);
        }
    }

}
