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

import org.wrml.model.Model;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.component.WerminalTextBox;
import org.wrml.werminal.terminal.TerminalAppPanel;

import java.io.File;
import java.net.URI;

public class PrintDialog extends WerminalDialog
{

    private final WerminalTextBox _PrintToFilePathTextBox;

    private URI _FormatUri;

    private Model _Model;

    public PrintDialog(final Werminal werminal, final String title)
    {

        super(werminal, title, werminal.getPrintConfirmationAction(), werminal.getCancelAction());

        _PrintToFilePathTextBox = new WerminalTextBox(werminal, File.class, werminal.getPrintConfirmationAction());
        render();
    }

    public File getPrintToFile()
    {

        return _PrintToFilePathTextBox.getValue();
    }

    public void setPrintToFile(final File file)
    {

        _PrintToFilePathTextBox.setValue(file);
    }

    @Override
    public void render()
    {

        removeAllComponents();

        final Werminal werminal = getWerminal();

        final TerminalAppPanel printToFilePanel = new TerminalAppPanel(werminal, " Print to File: ");
        printToFilePanel.addComponent(_PrintToFilePathTextBox);

        addEmptySpace();
        addComponent(printToFilePanel);
        addEmptySpace();

        super.renderFooterToolBar();

    }

    public URI getFormatUri()
    {

        return _FormatUri;
    }

    public void setFormatUri(final URI formatUri)
    {

        _FormatUri = formatUri;
    }

    public Model getModel()
    {

        return _Model;
    }

    public void setModel(final Model model)
    {

        _Model = model;
    }
}