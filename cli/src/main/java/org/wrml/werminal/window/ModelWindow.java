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
import com.googlecode.lanterna.terminal.Terminal;
import org.wrml.model.Model;
import org.wrml.model.Titled;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.Document;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.schema.Prototype;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.LoadAction;
import org.wrml.werminal.component.FormField;
import org.wrml.werminal.component.FormPanel;
import org.wrml.werminal.component.WerminalPanel;
import org.wrml.werminal.component.WerminalTextBox;
import org.wrml.werminal.terminal.TerminalAppButtonPanel;
import org.wrml.werminal.terminal.TerminalAppNameValueLabel;
import org.wrml.werminal.terminal.TerminalAppToolBar;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;

/**
 * <p>
 * {@link org.wrml.werminal.Werminal}'s primary {@link Model} view/edit {@link WerminalWindow}.
 * </p>
 */
public class ModelWindow extends FormPanelWindow
{

    private final TerminalAppNameValueLabel _HeapIdLabel;

    private final TerminalAppNameValueLabel _SchemaUriLabel;

    private final TerminalAppNameValueLabel _OriginServiceLabel;

    private final TerminalAppButtonPanel _LoadButton;

    private Model _Model;

    public ModelWindow(final Werminal werminal, final String title, final Component[] toolBarComponents)
    {

        super(werminal, title, toolBarComponents);

        _SchemaUriLabel = new TerminalAppNameValueLabel(werminal, "Schema: ",
                FormPanelWindow.HEADER_FIELD_NAME_LABEL_WIDTH, Terminal.Color.BLACK, true, "",
                FormPanelWindow.HEADER_FIELD_VALUE_LABEL_WIDTH, Terminal.Color.BLACK, false);

        _HeapIdLabel = new TerminalAppNameValueLabel(werminal, "Heap ID: ",
                FormPanelWindow.HEADER_FIELD_NAME_LABEL_WIDTH, Terminal.Color.BLACK, true, "",
                FormPanelWindow.HEADER_FIELD_VALUE_LABEL_WIDTH, Terminal.Color.BLACK, false);

        _OriginServiceLabel = new TerminalAppNameValueLabel(werminal, "Origin: ",
                FormPanelWindow.HEADER_FIELD_NAME_LABEL_WIDTH, Terminal.Color.BLACK, true, "",
                FormPanelWindow.HEADER_FIELD_VALUE_LABEL_WIDTH, Terminal.Color.BLACK, false);

        _LoadButton = new TerminalAppButtonPanel(werminal.getLoadAction());

        render();
    }

    public final static String getModelWindowTitle(final Model model)
    {

        final StringBuilder modelWindowTitleBuilder = new StringBuilder("Model - ").append(model.getPrototype()
                .getTitle());
        if (model instanceof Titled)
        {
            final String modelTitle = ((Titled) model).getTitle();
            if (modelTitle != null)
            {
                final String trimmedTitle = modelTitle.trim();
                if (!trimmedTitle.isEmpty())
                {
                    modelWindowTitleBuilder.append(" - ").append(trimmedTitle);
                }
            }

        }

        return modelWindowTitleBuilder.toString();

    }

    public final Model getModel()
    {

        return _Model;
    }

    public final void setModel(final Model model)
    {

        _Model = model;

        final Werminal werminal = getWerminal();

        final URI schemaUri = _Model.getSchemaUri();
        _SchemaUriLabel.getValueLabel().setText(schemaUri.toString());

        final UUID heapId = _Model.getHeapId();
        _HeapIdLabel.getValueLabel().setText(String.valueOf(heapId));


        final Prototype prototype = _Model.getPrototype();

        werminal.addToSchemaUriHistory(schemaUri);

        if (_Model instanceof Schema)
        {
            werminal.addToSchemaUriHistory(((Schema) _Model).getUri());
        }

        final Map<String, Type> fieldMap = new LinkedHashMap<>();

        final Set<String> keySlotNameSet = prototype.getAllKeySlotNames();
        final List<String> allKeySlotNames = new ArrayList<String>(keySlotNameSet);
        for (final String keySlotName : allKeySlotNames)
        {
            fieldMap.put(keySlotName, prototype.getProtoSlot(keySlotName).getHeapValueType());
        }

        final List<String> allSlotNames = new ArrayList<String>(prototype.getAllSlotNames());

        for (final String slotName : allSlotNames)
        {
            if (!keySlotNameSet.contains(slotName))
            {
                fieldMap.put(slotName, prototype.getProtoSlot(slotName).getHeapValueType());
            }
        }

        initPanels("Slots", fieldMap);

        for (final String slotName : allSlotNames)
        {

            // Note: the _Model field reference is used because of the getModel side-effect (funny smell)
            final Object value = _Model.getSlotValue(slotName);
            final FormField formField = getFormField(slotName);
            if (formField == null)
            {
                werminal.showError("The slot named \"" + slotName + "\" has no associated form field.");
                break;
            }
            final WerminalTextBox valueTextBox = formField.getFieldValueTextBox();
            valueTextBox.setValue(value);

        }

        updateLoadButton();

    }

    public final Model syncModel()
    {

        final Model model = getModel();
        final List<WerminalPanel> panels = getPanels();
        for (final WerminalPanel panel : panels)
        {

            if (panel instanceof FormPanel)
            {
                final FormPanel formPanel = (FormPanel) panel;
                for (final String slotName : formPanel.getFieldNames())
                {

                    final FormField field = formPanel.getFormField(slotName);
                    final WerminalTextBox valueTextBox = field.getFieldValueTextBox();
                    if (valueTextBox.isValueChanged())
                    {

                        final Object slotValue = valueTextBox.getValue();
                        model.setSlotValue(slotName, slotValue);

                    }
                }
            }
        }
        return model;
    }

    @Override
    public void render()
    {

        removeAllComponents();
        renderHeaderToolBar();
        renderDimensionsPanel();
        renderCurrentPanel();
    }

    public void updateLoadButton()
    {

        final TerminalAppToolBar headerToolBar = getHeaderToolBar();
        if (headerToolBar == null)
        {
            return;
        }

        final Model model = getModel();
        final boolean isLoadButtonAlreadyVisible = headerToolBar.containsComponent(_LoadButton);
        final boolean shouldLoadButtonBeVisible = LoadAction.appliesTo(model);

        String loadButtonTitleText = "Load";
        if (model instanceof Api)
        {
            if (shouldLoadButtonBeVisible)
            {
                final ApiLoader apiLoader = model.getContext().getApiLoader();
                if (apiLoader.getLoadedApi(model.getKeys()) != null)
                {
                    loadButtonTitleText = "Reload";
                }
            }
        }

        _LoadButton.getButton().setText(loadButtonTitleText);

        if (isLoadButtonAlreadyVisible != shouldLoadButtonBeVisible)
        {
            if (shouldLoadButtonBeVisible)
            {
                headerToolBar.addComponent(_LoadButton);
            }
            else
            {
                headerToolBar.removeComponent(_LoadButton);
            }
        }
    }

    private void renderDimensionsPanel()
    {


        if (_Model != null)
        {
            final String originServiceName = _Model.getOriginServiceName();
            final String labelText = (originServiceName != null) ? originServiceName : "";
            _OriginServiceLabel.getValueLabel().setText(labelText);
        }


        addEmptySpace();
        addComponent(_SchemaUriLabel);
        addComponent(_HeapIdLabel);
        addComponent(_OriginServiceLabel);
        addEmptySpace();

    }

}
