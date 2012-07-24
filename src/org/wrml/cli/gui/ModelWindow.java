/**
 * Copyright (C) 2012 WRML.org <mark@wrml.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wrml.cli.gui;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.component.EmptySpace;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Label.Alignment;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.layout.SizePolicy;
import com.googlecode.lanterna.terminal.Terminal;

import org.wrml.cli.gui.base.BasePanel;
import org.wrml.cli.gui.base.ButtonPanel;
import org.wrml.cli.gui.base.FormField;
import org.wrml.cli.gui.base.NameValueLabel;
import org.wrml.cli.gui.base.ToolBar;
import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.Prototype;
import org.wrml.runtime.SchemaLoader;
import org.wrml.util.transformer.ToStringTransformer;

public class ModelWindow extends GuiWindow {

    public final static int HEADER_FIELD_NAME_LABEL_WIDTH = 12;

    public final static int HEADER_FIELD_VALUE_LABEL_WIDTH = 60;
    public final static int FIELDS_PER_PANEL = 8;

    private final static int PREVIOUS_BUTTON_INDEX = 0;
    private final static int NEXT_BUTTON_INDEX = 2;
    private Model _Model;

    private ModelPanel _CurrentPanel;
    private final NameValueLabel _HeapIdLabel;

    private final NameValueLabel _SchemaIdLabel;
    private final NameValueLabel _KeyLabel;
    private final List<ModelPanel> _Panels;

    private final GuiAction _NextAction;

    private final GuiAction _PreviousAction;

    public ModelWindow(final String title, final Gui gui, final Component[] toolBarComponents) {
        super(title, gui, toolBarComponents);

        _SchemaIdLabel = new NameValueLabel("Schema ID: ", HEADER_FIELD_NAME_LABEL_WIDTH, Terminal.Color.BLACK, true,
                Alignment.END, "", HEADER_FIELD_VALUE_LABEL_WIDTH, Terminal.Color.BLACK, false, Alignment.START);
        addComponent(_SchemaIdLabel, SizePolicy.CONSTANT);

        _KeyLabel = new NameValueLabel("Key: ", HEADER_FIELD_NAME_LABEL_WIDTH, Terminal.Color.BLACK, true,
                Alignment.END, "", HEADER_FIELD_VALUE_LABEL_WIDTH, Terminal.Color.BLACK, false, Alignment.START);
        addComponent(_KeyLabel, SizePolicy.CONSTANT);

        _HeapIdLabel = new NameValueLabel("Heap ID: ", HEADER_FIELD_NAME_LABEL_WIDTH, Terminal.Color.BLACK, true,
                Alignment.END, "", HEADER_FIELD_VALUE_LABEL_WIDTH, Terminal.Color.BLACK, false, Alignment.START);

        addComponent(_HeapIdLabel, SizePolicy.CONSTANT);

        _Panels = new ArrayList<ModelPanel>();

        _NextAction = new NextAction(gui);
        _PreviousAction = new PreviousAction(gui);
    }

    public ModelPanel getCurrentPanel() {
        return _CurrentPanel;
    }

    public Model getModel() {
        return _Model;
    }

    public GuiAction getNextAction() {
        return _NextAction;
    }

    public GuiAction getPreviousAction() {
        return _PreviousAction;
    }

    public void nextPanel() {
        directPanel(false);
    }

    public void previousPanel() {
        directPanel(true);
    }

    public void setCurrentPanel(ModelPanel currentPanel) {
        unsetCurrentPanel();
        _CurrentPanel = currentPanel;
        addComponent(_CurrentPanel);
    }

    public void setModel(Model model) {
        _Model = model;
        initPanels();
    }

    private void directPanel(boolean reverse) {

        final int panelIndexIncrement = (reverse) ? -1 : 1;

        setCurrentPanel(_Panels.get(getCurrentPanel().getPanelIndex() + panelIndexIncrement));

        final ToolBar toolBar = getCurrentPanel().getToolBar();
        if (toolBar != null) {
            final int toolBarComponentCount = toolBar.getComponents().length;
            int toolBarButtonIndex = (reverse) ? PREVIOUS_BUTTON_INDEX : NEXT_BUTTON_INDEX;
            toolBarButtonIndex = (toolBarButtonIndex < toolBarComponentCount) ? toolBarButtonIndex : 0;
            final Component focusComponent = toolBar.getComponents()[toolBarButtonIndex];
            if (focusComponent instanceof ButtonPanel) {
                setFocus(((ButtonPanel) focusComponent).getButton());
            }
        }
    }

    private void initPanels() {

        final URI schemaId = _Model.getSchemaId();
        _SchemaIdLabel.getValueLabel().setText("..." + schemaId.getPath());
        final UUID heapId = _Model.getHeapId();
        _HeapIdLabel.getValueLabel().setText(String.valueOf(heapId));

        // TODO: final Object key = _Model.getKey();

        final Gui gui = getGui();
        final SchemaLoader schemalLoader = gui.getEngine().getDefaultContext().getSchemaLoader();
        final Prototype prototype = schemalLoader.getPrototype(schemaId);

        final List<String> allFieldNames = new ArrayList<String>(prototype.getAllFieldNames());
        final int totalFieldCount = allFieldNames.size();
        final int fullFieldPanelCount = totalFieldCount / FIELDS_PER_PANEL;
        final int leftoverFieldCount = totalFieldCount % FIELDS_PER_PANEL;
        final boolean needOddLastPanel = (leftoverFieldCount != 0);
        final int fieldPanelCount = (fullFieldPanelCount) + (needOddLastPanel ? 1 : 0);

        for (int i = 0; i < fieldPanelCount; i++) {

            final boolean isFirstPanel = (i == 0);
            final boolean isLastPanel = (i == (fieldPanelCount - 1));

            final int panelFieldStartIndex = i * FIELDS_PER_PANEL;
            final int panelFieldCount = (isLastPanel) ? leftoverFieldCount : FIELDS_PER_PANEL;
            final int panelFieldEndIndex = panelFieldStartIndex + panelFieldCount;
            final Set<String> panelFieldNames = new TreeSet<String>();
            for (int j = panelFieldStartIndex; j < panelFieldEndIndex; j++) {
                panelFieldNames.add(allFieldNames.get(j));
            }

            final String panelNumberSpacePaddingPrefix;

            if ((i < 10) & (fieldPanelCount < 10)) {
                panelNumberSpacePaddingPrefix = "";
            }
            else if ((i < 10) && (fieldPanelCount < 100)) {
                panelNumberSpacePaddingPrefix = " ";
            }
            else if ((i < 100) && (fieldPanelCount < 1000)) {
                if (i < 10) {
                    panelNumberSpacePaddingPrefix = "  ";
                }
                else {
                    panelNumberSpacePaddingPrefix = " ";
                }
            }
            else {
                panelNumberSpacePaddingPrefix = "";
            }

            final int panelDisplayNumber = i + 1;

            final BasePanel panelNumberPanel = new BasePanel(new Border.Invisible(), Panel.Orientation.VERTICAL);
            panelNumberPanel.addComponent(new EmptySpace(0, 1));
            panelNumberPanel.addComponent(new Label(panelNumberSpacePaddingPrefix + panelDisplayNumber + " of "
                    + fieldPanelCount, 8, Terminal.Color.BLACK, false, Alignment.MIDDLE), SizePolicy.CONSTANT);
            panelNumberPanel.addComponent(new EmptySpace(0, 1));

            final Component[] toolBarComponents;
            if (isFirstPanel && isLastPanel) {
                toolBarComponents = null;
            }
            else if (isFirstPanel) {
                toolBarComponents = new Component[] { new ButtonPanel(getNextAction()), panelNumberPanel };
            }
            else if (isLastPanel) {
                toolBarComponents = new Component[] { new ButtonPanel(getPreviousAction()), panelNumberPanel };
            }
            else {
                toolBarComponents = new Component[] { new ButtonPanel(getPreviousAction()), panelNumberPanel,
                        new ButtonPanel(getNextAction()) };
            }

            _Panels.add(new FieldsModelPanel(this, i, fieldPanelCount, panelFieldNames, toolBarComponents));
        }

        if (_Panels.size() > 0) {
            setCurrentPanel(_Panels.get(0));
        }
    }

    private void unsetCurrentPanel() {
        if (_CurrentPanel == null) {
            return;
        }

        removeComponent(_CurrentPanel);
    }

    public static class FormFieldOpenAction extends GuiAction {

        private final Model _Model;
        private final Prototype.Field _ProtoField;
        private final ToStringTransformer<?> _ValueTransformer;

        public FormFieldOpenAction(final Gui gui, final Model model, final Prototype.Field protoField,
                ToStringTransformer<?> valueTransformer) {
            super("...", gui);
            _Model = model;
            _ProtoField = protoField;
            _ValueTransformer = valueTransformer;
        }

        @Override
        public void doAction() {
            getGui().showMessageBox("Details", "Opening the detials on a form field has not been implemented (yet).");
        }

        public Model getModel() {
            return _Model;
        }

        public Prototype.Field getProtoField() {
            return _ProtoField;
        }

        public ToStringTransformer<?> getValueTransformer() {
            return _ValueTransformer;
        }

    }

    static class FieldsModelPanel extends ModelPanel {

        public final static int FORM_FIELD_WIDTH = 60;

        private final Map<String, FormField> _FormFields;

        public FieldsModelPanel(final ModelWindow modelWindow, final int panelIndex, final int panelCount,
                final Set<String> fieldNames, Component[] toolBarComponents) {

            super("  Fields:  ", modelWindow, panelIndex, panelCount, toolBarComponents);

            _FormFields = new TreeMap<String, FormField>();

            final Model model = getModel();
            final URI schemaId = model.getSchemaId();
            final Context context = model.getContext();
            final Prototype prototype = context.getSchemaLoader().getPrototype(schemaId);

            for (final String fieldName : fieldNames) {

                final Prototype.Field protoField = prototype.getPrototypeField(fieldName);
                final Type fieldType = protoField.getProperty().getType();

                ToStringTransformer<?> transformer = null;
                if (fieldType instanceof Class<?>) {
                    transformer = context.getToStringTransformer((Class<?>) fieldType);
                }

                final FormField formField = new FormField("  " + fieldName + " : ", FORM_FIELD_WIDTH, transformer,
                        new FormFieldOpenAction(modelWindow.getGui(), model, protoField, transformer));

                _FormFields.put(fieldName, formField);

                final Object value = model.getFieldValue(fieldName);
                if (value != null) {
                    formField.getFieldValueTextBox().setValue(value);
                }

                addComponent(formField);
            }

            final int invisibleFieldCount = FIELDS_PER_PANEL - fieldNames.size();
            for (int i = 0; i < invisibleFieldCount; i++) {
                addComponent(new EmptySpace(0, 3));
            }

            addComponent(new EmptySpace(0, 1));
        }

        public final int getFieldCount() {
            return _FormFields.size();
        }

        public final Set<String> getFieldNames() {
            return _FormFields.keySet();
        }

        public final Object getFieldRawValue(String fieldName) {
            final String stringValue = getFormField(fieldName).getFieldValueTextBox().getText();
            return stringValue;
        }

        public final FormField getFormField(String fieldName) {
            return _FormFields.get(fieldName);
        }

        public final void setFieldRawValue(String fieldName, Object rawValue) {
            final String stringValue = (rawValue == null) ? "" : String.valueOf(rawValue);
            getFormField(fieldName).getFieldValueTextBox().setText(stringValue);
        }
    }

    static class ModelPanel extends GuiPanel {

        private final ModelWindow _ModelWindow;
        private final int _PanelIndex;
        private final int _PanelCount;

        public ModelPanel(final String title, final ModelWindow modelWindow, final int panelIndex,
                final int panelCount, Component[] toolBarComponents) {

            super(title, modelWindow.getGui(), toolBarComponents);
            _ModelWindow = modelWindow;
            _PanelIndex = panelIndex;
            _PanelCount = panelCount;
        }

        public Model getModel() {
            return _ModelWindow.getModel();
        }

        public ModelWindow getModelWindow() {
            return _ModelWindow;
        }

        public final int getPanelCount() {
            return _PanelCount;
        }

        public final int getPanelIndex() {
            return _PanelIndex;
        }
    }

    private class NextAction extends GuiAction {

        public NextAction(final Gui gui) {
            super("Next", gui);
        }

        @Override
        public void doAction() {
            nextPanel();
        }

    }

    private class PreviousAction extends GuiAction {

        public PreviousAction(final Gui gui) {
            super("Previous", gui);
        }

        @Override
        public void doAction() {
            previousPanel();
        }
    }

}
