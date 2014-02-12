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

import com.googlecode.lanterna.gui.Window;
import org.wrml.model.Filed;
import org.wrml.model.Model;
import org.wrml.runtime.format.ModelWriteOptions;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.runtime.service.file.FileSystemService;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.dialog.PrintDialog;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class PrintConfirmationAction extends CloseAfterAction {
    public PrintConfirmationAction(final Werminal werminal) {

        super(werminal, "Print");
    }

    @Override
    protected boolean doIt() {

        final Werminal werminal = getWerminal();

        final Window topWindow = werminal.getTopWindow();

        if (!(topWindow instanceof PrintDialog)) {
            werminal.showError("The " + getTitle() + " action requires a top level " + PrintDialog.class.getSimpleName());
            return false;
        }

        final PrintDialog printDialog = (PrintDialog) topWindow;

        final Model model = printDialog.getModel();
        final File printToFile = printDialog.getPrintToFile();
        final Path filePath = printToFile.toPath();
        final URI formatUri = printDialog.getFormatUri();

        final ModelWriteOptions writeOptions = new ModelWriteOptions();
        writeOptions.setPrettyPrint(true);
        final Set<URI> excludedSchemaUris = new HashSet<>(1);
        final SchemaLoader schemaLoader = model.getContext().getSchemaLoader();

        excludedSchemaUris.add(schemaLoader.getTypeUri(Filed.class));
        excludedSchemaUris.add(schemaLoader.getDocumentSchemaUri());

        writeOptions.setExcludedSchemaUris(excludedSchemaUris);

        try {
            FileSystemService.writeModelFile(model, filePath, formatUri, writeOptions);
        }
        catch (final Exception e) {
            werminal.showError("Print Failed.", e);
            return false;
        }


        return true;
    }

}
