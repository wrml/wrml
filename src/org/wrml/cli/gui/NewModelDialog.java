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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.component.CheckBoxList;
import com.googlecode.lanterna.gui.component.Panel.Orientation;
import com.googlecode.lanterna.gui.layout.SizePolicy;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;
import com.googlecode.lanterna.terminal.TerminalSize;

import org.wrml.cli.gui.base.BasePanel;
import org.wrml.cli.gui.base.ButtonPanel;
import org.wrml.cli.gui.base.ObjectTextBox;
import org.wrml.cli.gui.base.TextBoxPanel;
import org.wrml.cli.gui.base.ToolBar;
import org.wrml.runtime.Context;
import org.wrml.util.transformer.ToStringTransformer;

public class NewModelDialog extends GuiWindow {

    private final ObjectTextBox _SchemaIdTextBox;
    private final HistoryCheckBoxList _SchemaIdHistoryCheckBoxList;

    public NewModelDialog(final String title, Gui gui, GuiAction confirmAction, GuiAction dismissAction) {

        super(title, gui);

        setBorder(new Border.Standard());

        setBetweenComponentsPadding(0);

        addEmptyLine();

        final Context context = gui.getEngine().getDefaultContext();
        final ToStringTransformer<URI> uriToStringTransformer = context.getToStringTransformer(URI.class);

        _SchemaIdTextBox = new ObjectTextBox(70, uriToStringTransformer, confirmAction);

        final TextBoxPanel schemaIdTextBoxPanel = new TextBoxPanel(" Schema ID (URI): ", _SchemaIdTextBox);
        addComponent(schemaIdTextBoxPanel, SizePolicy.CONSTANT);

        final BasePanel historyPanel = new BasePanel(" History: ", new Border.Standard(), Orientation.VERTICAL);
        _SchemaIdHistoryCheckBoxList = new HistoryCheckBoxList(_SchemaIdTextBox);
        _SchemaIdHistoryCheckBoxList.setPreferredSize(new TerminalSize(70, 10));
        historyPanel.addComponent(_SchemaIdHistoryCheckBoxList, SizePolicy.CONSTANT);
        addComponent(historyPanel, SizePolicy.CONSTANT);

        final ToolBar footerToolBar = new ToolBar(new Component[] { new ButtonPanel(confirmAction),
                new ButtonPanel(dismissAction) });
        addComponent(footerToolBar);

    }

    public URI getSchemaId() {
        return (URI) _SchemaIdTextBox.getValue();
    }

    public CheckBoxList getSchemaIdHistoryCheckBoxList() {
        return _SchemaIdHistoryCheckBoxList;
    }

    public URI setSchemaId(URI schemaId) {
        final URI oldSchemaId = (URI) _SchemaIdTextBox.getValue();
        _SchemaIdTextBox.setValue(schemaId);
        return oldSchemaId;
    }

    private static class HistoryCheckBoxList extends CheckBoxList {

        private final ObjectTextBox _TextBox;
        private final LinkedList<Object> _HistoryList;
        private final Set<Object> _HistorySet;
        private Object _CheckedItem;

        public HistoryCheckBoxList(ObjectTextBox forTextBox) {
            _TextBox = forTextBox;
            _HistoryList = new LinkedList<Object>();
            _HistorySet = new HashSet<Object>();
        }

        @Override
        public void addItem(Object item) {
            if (!_HistoryList.isEmpty() && (_HistoryList.peek() == item)) {
                return;
            }

            if (_HistorySet.contains(item)) {
                _HistoryList.remove(item);
            }

            _HistoryList.push(item);
            _HistorySet.add(item);

            super.clearItems();

            for (final Object historyItem : _HistoryList) {
                super.addItem(historyItem);
            }
        }

        @Override
        public void clearItems() {
            super.clearItems();
            _HistoryList.clear();
            _HistorySet.clear();
        }

        @Override
        public Result keyboardInteraction(Key key) {

            final Kind kind = key.getKind();
            switch (kind) {
            case Enter:
            case NormalKey:

                if (_CheckedItem != null) {
                    setChecked(_CheckedItem, false);
                    _CheckedItem = null;
                }

                final Result result = super.keyboardInteraction(key);

                final Object newSelectedItem = getSelectedItem();
                if ((newSelectedItem != null) && isChecked(newSelectedItem)) {
                    _CheckedItem = newSelectedItem;
                    _TextBox.setValue(_CheckedItem);

                }

                return result;

            }

            return super.keyboardInteraction(key);
        }

    }
}
