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

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel.Orientation;
import com.googlecode.lanterna.terminal.TerminalSize;
import org.wrml.model.Model;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.format.FormatLoader;
import org.wrml.runtime.format.Formatter;
import org.wrml.runtime.format.ModelWritingException;
import org.wrml.runtime.format.SystemFormat;
import org.wrml.runtime.format.application.schema.json.JsonSchema;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.util.AsciiArt;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.component.HistoryCheckListBox;
import org.wrml.werminal.component.WerminalActionListBox;
import org.wrml.werminal.component.WerminalTextBox;
import org.wrml.werminal.terminal.TerminalAppButtonPanel;
import org.wrml.werminal.terminal.TerminalAppPanel;
import org.wrml.werminal.terminal.TerminalAppTextBoxPanel;
import org.wrml.werminal.terminal.TerminalAppToolBar;
import org.wrml.werminal.window.WerminalWindow;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.SortedSet;

public class PrintPreviewDialog extends WerminalWindow
{

    private static final int COMPONENT_COLUMNS = 80;

    private final Label _SchemaUriLabel;

    private final HistoryCheckListBox _FormatUriHistoryCheckListBox;

    private final WerminalTextBox _FormatUriTextBox;

    private final WerminalActionListBox _PrintedModelLineListBox;

    private Model _Model;

    public PrintPreviewDialog(final Werminal werminal, final String title, final WerminalAction dismissAction)
    {

        super(werminal, title);

        setBorder(new Border.Standard());

        addEmptySpace();

        _SchemaUriLabel = new Label("", COMPONENT_COLUMNS);
        final TerminalAppPanel schemaPanel = new TerminalAppPanel(werminal, " Schema: ");
        schemaPanel.addComponent(_SchemaUriLabel);

        addEmptySpace();
        addComponent(schemaPanel);

        final WerminalAction formatConfirmAction = new WerminalAction(werminal, "Preview Format")
        {

            @Override
            public void doAction()
            {

                updatePreview();

            }
        };

        _FormatUriTextBox = new WerminalTextBox(werminal, COMPONENT_COLUMNS, URI.class, formatConfirmAction);
        final TerminalAppTextBoxPanel formatUriTextBoxPanel = new TerminalAppTextBoxPanel(werminal, " Format [URI]: ",
                _FormatUriTextBox);
        addEmptySpace();
        addComponent(formatUriTextBoxPanel);

        final TerminalAppPanel formatsPanel = new TerminalAppPanel(werminal, " Formats: ", new Border.Standard(),
                Orientation.VERTICAL);
        _FormatUriHistoryCheckListBox = new HistoryCheckListBox(_FormatUriTextBox, formatConfirmAction, null);
        _FormatUriHistoryCheckListBox.setPreferredSize(new TerminalSize(70, 10));
        formatsPanel.addEmptySpace();
        formatsPanel.addComponent(_FormatUriHistoryCheckListBox);
        formatsPanel.addEmptySpace();

        addComponent(formatsPanel);

        final TerminalAppPanel previewPanel = new TerminalAppPanel(werminal, " Preview: ", new Border.Standard(),
                Orientation.VERTICAL);
        _PrintedModelLineListBox = new WerminalActionListBox(werminal);
        _PrintedModelLineListBox.setPreferredSize(new TerminalSize(COMPONENT_COLUMNS, 30));
        previewPanel.addEmptySpace();
        previewPanel.addComponent(_PrintedModelLineListBox);
        previewPanel.addEmptySpace();

        addEmptySpace();
        addComponent(previewPanel);

        final TerminalAppToolBar footerToolBar = new TerminalAppToolBar(werminal,
                new Component[]{new TerminalAppButtonPanel(werminal.getPrintAction()), new TerminalAppButtonPanel(dismissAction)});
        addEmptySpace();
        addComponent(footerToolBar);

    }

    public URI getFormatUri()
    {

        return (URI) _FormatUriTextBox.getValue();
    }

    public Model getModel()
    {

        return _Model;
    }

    public void setModel(final Model model)
    {

        _Model = model;
        final URI schemaUri = _Model.getSchemaUri();
        _SchemaUriLabel.setText(String.valueOf(schemaUri));

        _FormatUriHistoryCheckListBox.clearItems();

        final Context context = model.getContext();
        final FormatLoader formatLoader = context.getFormatLoader();
        final SortedSet<URI> formatUris = formatLoader.getLoadedFormatUris();
        final URI defaultFormatUri = formatLoader.getDefaultFormatUri();

        for (final URI formatUri : formatUris)
        {
            final Formatter formatter = formatLoader.getFormatter(formatUri);
            if (formatter.isApplicableTo(model.getSchemaUri()))
            {
                _FormatUriHistoryCheckListBox.addItem(formatUri);

                if (formatUri.equals(defaultFormatUri))
                {
                    _FormatUriHistoryCheckListBox.setCheckedItem(formatUri);
                }

            }
        }

        updatePreview();
    }

    private void updatePreview()
    {

        final Werminal werminal = getWerminal();
        final Model model = getModel();

        final Context context = model.getContext();
        final FormatLoader formatLoader = context.getFormatLoader();

        URI formatUri = getFormatUri();
        if (formatUri == null)
        {

            formatUri = formatLoader.getDefaultFormatUri();
        }

        _FormatUriHistoryCheckListBox.setCheckedItem(formatUri);

        final String printOut;
        if (model instanceof Schema && formatUri.equals(SystemFormat.json_schema.getFormatUri()))
        {
            final Schema wrmlSchema = (Schema) model;
            final SchemaLoader schemaLoader = context.getSchemaLoader();
            final JsonSchema jsonSchema = schemaLoader.getJsonSchemaLoader().load(wrmlSchema);
            printOut = AsciiArt.express(jsonSchema.getRootNode());
        }
        else
        {

            final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

            try
            {
                context.writeModel(byteOut, model, formatUri);
            }
            catch (final ModelWritingException e)
            {
                werminal.showError("Unable to express the model " + model.getHeapId() + ", returning null.", e);
                return;
            }

            final byte[] modelBytes = byteOut.toByteArray();
            printOut = new String(modelBytes);
        }

        final String[] unwrappedLines = printOut.split("\n");

        _PrintedModelLineListBox.clearItems();

        for (final String line : unwrappedLines)
        {
            _PrintedModelLineListBox.addAction(new PrintedLineAction(werminal, line));
        }

    }

    private class PrintedLineAction extends WerminalAction
    {

        private final String _Line;

        public PrintedLineAction(final Werminal werminal, final String line)
        {

            super(werminal, "");
            _Line = line;
        }

        @Override
        public void doAction()
        {

            String line = getLine();
            if (line == null)
            {
                return;
            }

            line = line.trim();

            if (line.isEmpty())
            {
                return;
            }

            final Werminal werminal = getWerminal();

            werminal.showMessageBox("Selected Line", "\n\n" + _Line.trim() + "\n\n");
        }

        public String getLine()
        {

            return _Line;
        }

        @Override
        public String toString()
        {

            return getLine();
        }

    }

}
