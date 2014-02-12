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
package org.wrml.werminal.window;

import com.googlecode.lanterna.gui.Component;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.FormFieldOpenAction;
import org.wrml.werminal.component.FormField;
import org.wrml.werminal.component.FormPanel;
import org.wrml.werminal.component.WerminalPanel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FormPanelWindow extends WerminalPanelWindow {

    public final static int FIELDS_PER_PANEL = 10;

    public final static int HEADER_FIELD_NAME_LABEL_WIDTH = 12;

    public final static int HEADER_FIELD_VALUE_LABEL_WIDTH = 100;

    public FormPanelWindow(final Werminal werminal, final String title, final Component[] toolBarComponents) {

        super(werminal, title, toolBarComponents);
    }

    public void addFormField(final String panelTitle, final Type fieldType, final Object value) {

        goToLastPanel();

        FormPanel form = (FormPanel) getCurrentPanel();
        final FormField formField;
        if (form == null) {
            final Map<String, Type> fieldMap = new LinkedHashMap<>();
            final String fieldZeroSlotName = "0";
            fieldMap.put(fieldZeroSlotName, fieldType);
            initPanels(panelTitle, fieldMap);
            formField = getFormField(fieldZeroSlotName);
        }
        else {
            final FormFieldOpenAction formFieldOpenAction = new FormFieldOpenAction(getWerminal());
            formField = new FormField(String.valueOf((FormPanelWindow.FIELDS_PER_PANEL * form.getPanelIndex())
                    + form.getFieldCount()), fieldType, formFieldOpenAction);
            formFieldOpenAction.setFormField(formField);

            if (form.getFieldCount() == FormPanelWindow.FIELDS_PER_PANEL) {
                final Map<String, FormField> formFields = new LinkedHashMap<>();
                formFields.put(formField.getFieldName(), formField);
                form = addFormPanel(panelTitle, formFields);
                setCurrentPanel(form);
            }

            form.addFormField(formField);
        }

        formField.getFieldValueTextBox().setValue(value);
    }

    public FormPanel addFormPanel(final String panelTitle, final Map<String, FormField> formFields) {

        final List<WerminalPanel> panels = getPanels();
        final int oldPanelCount = panels.size();
        final int newPanelCount = panels.size() + 1;

        for (final WerminalPanel panel : panels) {
            panel.setPanelCount(newPanelCount);
        }

        final FormPanel formPanel = createFormPanel(panelTitle, formFields, oldPanelCount, newPanelCount, FormPanelWindow.FIELDS_PER_PANEL);
        panels.add(formPanel);
        return formPanel;
    }

    public final FormField getFormField(final String fieldName) {

        for (final WerminalPanel panel : getPanels()) {

            if (panel instanceof FormPanel) {
                final FormPanel formPanel = (FormPanel) panel;
                final FormField field = formPanel.getFormField(fieldName);
                if (field != null) {
                    return field;
                }
            }

        }

        return null;
    }

    public final void initPanels(final String panelTitle, final Map<String, Type> fieldMap) {

        final List<WerminalPanel> panels = getPanels();
        panels.clear();

        final List<String> allFieldNames = new ArrayList<>(fieldMap.keySet());

        final int totalFieldCount = allFieldNames.size();
        final int fullFieldPanelCount = totalFieldCount / FormPanelWindow.FIELDS_PER_PANEL;
        final int leftoverFieldCount = totalFieldCount % FormPanelWindow.FIELDS_PER_PANEL;
        final boolean needOddLastPanel = (leftoverFieldCount != 0);
        final int panelCount = (fullFieldPanelCount) + (needOddLastPanel ? 1 : 0);

        final int lastPanelFieldCount = (panelCount == 1) ? totalFieldCount
                : (leftoverFieldCount == 0) ? FormPanelWindow.FIELDS_PER_PANEL : leftoverFieldCount;

        for (int panelIndex = 0; panelIndex < panelCount; panelIndex++) {

            final boolean isLastPanel = (panelIndex == (panelCount - 1));

            final int panelFieldStartIndex = panelIndex * FormPanelWindow.FIELDS_PER_PANEL;
            final int panelFieldCount = (isLastPanel) ? lastPanelFieldCount : FormPanelWindow.FIELDS_PER_PANEL;
            final int panelFieldEndIndex = panelFieldStartIndex + panelFieldCount;
            final Map<String, FormField> formFields = new LinkedHashMap<>();
            for (int fieldIndex = panelFieldStartIndex; fieldIndex < panelFieldEndIndex; fieldIndex++) {
                final String fieldName = allFieldNames.get(fieldIndex);
                final Type fieldType = fieldMap.get(fieldName);
                final FormFieldOpenAction formFieldOpenAction = new FormFieldOpenAction(getWerminal());
                final FormField formField = new FormField(fieldName, fieldType, formFieldOpenAction);
                formFieldOpenAction.setFormField(formField);
                formFields.put(fieldName, formField);
            }

            final FormPanel formPanel = createFormPanel(panelTitle, formFields, panelIndex, panelCount, FormPanelWindow.FIELDS_PER_PANEL);
            panels.add(formPanel);
        }

        if (panels.size() > 0) {
            setCurrentPanel(panels.get(0));
        }
    }

    protected FormPanel createFormPanel(final String panelTitle, final Map<String, FormField> formFields,
                                        final int panelIndex, final int panelCount, final int fieldsPerPanel) {

        final FormPanel formPanel = new FormPanel(getWerminal(), panelTitle, formFields, getNextAction(), getPreviousAction(), fieldsPerPanel);
        formPanel.setPanelIndex(panelIndex);
        formPanel.setPanelCount(panelCount);
        return formPanel;

    }
}
