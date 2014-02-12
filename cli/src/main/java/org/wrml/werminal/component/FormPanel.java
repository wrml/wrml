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

import com.googlecode.lanterna.gui.component.EmptySpace;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.window.FormPanelWindow;

import java.util.Map;
import java.util.Set;

public class FormPanel extends WerminalPanel {


    private final int _FieldsPerPanel;

    private final Map<String, FormField> _FormFields;


    public FormPanel(final Werminal werminal, final String title, final Map<String, FormField> formFields) {

        this(werminal, title, formFields, null, null, formFields.size());
    }

    public FormPanel(final Werminal werminal, final String title, final Map<String, FormField> formFields, final int fieldsPerPanel) {

        this(werminal, title, formFields, null, null, fieldsPerPanel);
    }

    public FormPanel(final Werminal werminal, final String title, final Map<String, FormField> formFields, final WerminalAction nextAction, final WerminalAction previousAction) {

        this(werminal, title, formFields, nextAction, previousAction, FormPanelWindow.FIELDS_PER_PANEL);
    }

    public FormPanel(final Werminal werminal, final String title, final Map<String, FormField> formFields, final WerminalAction nextAction, final WerminalAction previousAction, final int fieldsPerPanel) {

        super(werminal, title, nextAction, previousAction);

        _FormFields = formFields;
        _FieldsPerPanel = fieldsPerPanel;
        render();
    }

    public void addFormField(final FormField formField) {

        _FormFields.put(formField.getFieldName(), formField);
        render();
    }

    public final int getFieldCount() {

        return _FormFields.size();
    }

    public int getFieldsPerPanel() {

        return _FieldsPerPanel;
    }

    public final Set<String> getFieldNames() {

        return _FormFields.keySet();
    }

    public final FormField getFormField(final String fieldName) {

        return _FormFields.get(fieldName);
    }

    @Override
    protected void render() {

        super.render();
        renderForm();
        addComponent(new EmptySpace(0, 1));
    }

    protected final void renderForm() {

        if (_FormFields != null && !_FormFields.isEmpty()) {

            for (final String fieldName : _FormFields.keySet()) {
                final FormField formField = _FormFields.get(fieldName);
                addComponent(formField);
            }

            final int invisibleFieldCount = getFieldsPerPanel() - _FormFields.size();
            for (int i = 0; i < invisibleFieldCount; i++) {
                addComponent(new EmptySpace(0, 3));
            }

        }

    }

}
