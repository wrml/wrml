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

import java.net.URI;
import java.util.UUID;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Label.Alignment;
import com.googlecode.lanterna.gui.component.Panel.Orientation;
import com.googlecode.lanterna.gui.layout.SizePolicy;

import org.wrml.cli.gui.base.BasePanel;
import org.wrml.cli.gui.base.ButtonPanel;
import org.wrml.cli.gui.base.ObjectTextBox;
import org.wrml.cli.gui.base.TextBoxPanel;
import org.wrml.cli.gui.base.ToolBar;
import org.wrml.runtime.Context;
import org.wrml.util.transformer.ToStringTransformer;

public class OpenModelDialog extends GuiWindow {

    private final ObjectTextBox _HeapIdTextBox;
    private final ObjectTextBox _SchemaIdTextBox;
    private final ObjectTextBox _KeyTextBox;

    public OpenModelDialog(final String title, Gui gui, GuiAction confirmAction, GuiAction dismissAction) {

        super(title, gui);

        setBorder(new Border.Standard());

        setBetweenComponentsPadding(0);

        addEmptyLine();

        final Context context = gui.getEngine().getDefaultContext();

        //
        // Schema ID
        //
        final ToStringTransformer<URI> uriToStringTransformer = context.getToStringTransformer(URI.class);
        _SchemaIdTextBox = new ObjectTextBox(60, uriToStringTransformer, confirmAction);
        final TextBoxPanel schemaIdTextBoxPanel = new TextBoxPanel(" Schema ID (URI): ", _SchemaIdTextBox);
        addComponent(schemaIdTextBoxPanel, SizePolicy.CONSTANT);

        //
        // and
        //
        addComponent(new Label("and", 70, Alignment.MIDDLE), SizePolicy.CONSTANT);
        final BasePanel instancePanel = new BasePanel(new Border.Bevel(true), Orientation.VERTICAL);
        addComponent(instancePanel, SizePolicy.CONSTANT);

        //
        // Key
        //

        // TODO: Get the transformer for the key type (don't hardcode URI here)
        _KeyTextBox = new ObjectTextBox(60, uriToStringTransformer, confirmAction);
        final TextBoxPanel keyTextBoxPanel = new TextBoxPanel(" Key: ", _KeyTextBox);
        instancePanel.addComponent(keyTextBoxPanel, SizePolicy.CONSTANT);

        //
        // or
        //
        instancePanel.addComponent(new Label("or", 70, Alignment.MIDDLE), SizePolicy.CONSTANT);

        // Heap ID
        final ToStringTransformer<UUID> uuidToStringTransformer = context.getToStringTransformer(UUID.class);
        _HeapIdTextBox = new ObjectTextBox(60, uuidToStringTransformer, confirmAction);
        final TextBoxPanel heapIdTextBoxPanel = new TextBoxPanel(" Heap ID (UUID): ", _HeapIdTextBox);
        instancePanel.addComponent(heapIdTextBoxPanel, SizePolicy.CONSTANT);

        //
        // Dialog buttons
        //
        final ToolBar footerToolBar = new ToolBar(new Component[] { new ButtonPanel(confirmAction),
                new ButtonPanel(dismissAction) });
        addComponent(footerToolBar);

    }

    public UUID getHeapId() {
        return (UUID) _HeapIdTextBox.getValue();
    }

    public Object getKey() {
        return _KeyTextBox.getValue();
    }

    public URI getSchemaId() {
        return (URI) _SchemaIdTextBox.getValue();
    }

    public UUID setHeapId(UUID heapId) {
        final UUID oldHeapId = (UUID) _HeapIdTextBox.getValue();
        _HeapIdTextBox.setValue(heapId);
        return oldHeapId;
    }

    public Object setKey(Object key) {
        final Object oldKey = _KeyTextBox.getValue();
        _KeyTextBox.setValue(key);
        return oldKey;
    }

    public URI setSchemaId(URI schemaId) {
        final URI oldSchemaId = (URI) _SchemaIdTextBox.getValue();
        _SchemaIdTextBox.setValue(schemaId);
        return oldSchemaId;
    }

}
