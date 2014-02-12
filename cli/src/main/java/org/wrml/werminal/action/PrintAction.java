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
package org.wrml.werminal.action;

import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import org.wrml.model.Model;
import org.wrml.model.Named;
import org.wrml.model.Titled;
import org.wrml.model.UniquelyNamed;
import org.wrml.model.format.Format;
import org.wrml.model.rest.Document;
import org.wrml.util.UniqueName;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.dialog.PrintDialog;
import org.wrml.werminal.dialog.PrintPreviewDialog;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class PrintAction extends CloseAfterAction {
    public PrintAction(final Werminal werminal) {

        super(werminal, "Print...");
    }

    @Override
    protected boolean doIt() {

        final Werminal werminal = getWerminal();

        final Window topWindow = werminal.getTopWindow();
        if (!(topWindow instanceof PrintPreviewDialog)) {
            werminal.showError("The " + getTitle() + " action requires a top level " + PrintPreviewDialog.class.getSimpleName());
            return false;
        }

        final PrintPreviewDialog printPreviewDialog = (PrintPreviewDialog) topWindow;

        final URI formatUri = printPreviewDialog.getFormatUri();
        if (formatUri == null) {
            werminal.showError("The " + getTitle() + " action requires a Format URI.");
            return false;
        }

        final PrintDialog printDialog = werminal.getPrintDialog();

        File baseDir = new File(".");
        final File previousFile = printDialog.getPrintToFile();
        if (previousFile != null) {
            baseDir = (previousFile.isDirectory()) ? previousFile : previousFile.getParentFile();
        }

        try {
            baseDir = baseDir.getCanonicalFile();
        }
        catch (IOException e) {
        }

        final Model modelToPrint = printPreviewDialog.getModel();
        String fileName = null;

        if (modelToPrint instanceof Titled) {
            fileName = ((Titled) modelToPrint).getTitle();
        }
        else if (modelToPrint instanceof Named) {
            fileName = ((Named) modelToPrint).getName();
        }
        else if (modelToPrint instanceof UniquelyNamed) {
            final UniqueName uniqueName = ((UniquelyNamed) modelToPrint).getUniqueName();
            fileName = uniqueName.getLocalName();
        }
        else if (modelToPrint instanceof Document) {
            final URI uri = ((Document) modelToPrint).getUri();
            final String uriPath = uri.getPath();
            final int lastSlashIndex = uriPath.lastIndexOf('/');
            if (lastSlashIndex < uriPath.length() - 1) {
                fileName = uriPath.substring(lastSlashIndex + 1);
            }
        }

        if (fileName == null) {

            fileName = String.valueOf(modelToPrint.getHeapId());
        }


        String fileExtension = "txt";

        final Format format = getContext().getFormatLoader().loadFormat(formatUri);
        if (format != null) {
            final String formatFileExtension = format.getFileExtension();
            fileExtension = formatFileExtension;
        }

        fileName = fileName + "." + fileExtension;

        final File printToFile = new File(baseDir, fileName);
        printDialog.setModel(modelToPrint);
        printDialog.setFormatUri(formatUri);
        printDialog.setPrintToFile(printToFile);

        werminal.showWindow(printDialog, GUIScreen.Position.CENTER);
        return true;
    }

}
