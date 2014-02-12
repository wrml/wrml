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
package org.wrml.werminal.dialog;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import org.wrml.runtime.Context;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.rest.ApiLoaderException;
import org.wrml.runtime.rest.ApiNavigator;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.CancelAction;
import org.wrml.werminal.action.CloseAfterAction;
import org.wrml.werminal.component.WerminalTextBox;
import org.wrml.werminal.terminal.TerminalAppButtonPanel;
import org.wrml.werminal.terminal.TerminalAppTextBoxPanel;
import org.wrml.werminal.terminal.TerminalAppToolBar;
import org.wrml.werminal.window.WerminalWindow;

import java.net.URI;

public class LoadApiDialog extends WerminalWindow {

    final LoadApiDialogConfirmationAction _ConfirmAction;

    final LoadApiDialogCancelAction _CancelAction;

    private final WerminalTextBox _ApiUriTextBox;

    private boolean _Cancelled;

    public LoadApiDialog(final Werminal werminal, final String title) {

        super(werminal, title);

        _ConfirmAction = new LoadApiDialogConfirmationAction(werminal);
        _CancelAction = new LoadApiDialogCancelAction(werminal);

        setBorder(new Border.Standard());

        addEmptySpace();

        _ApiUriTextBox = new WerminalTextBox(werminal, 70, URI.class, _ConfirmAction);
        final TerminalAppTextBoxPanel schemaUriTextBoxPanel = new TerminalAppTextBoxPanel(werminal, " REST API (URI): ", _ApiUriTextBox);
        addComponent(schemaUriTextBoxPanel);

        addEmptySpace();

        final TerminalAppToolBar footerToolBar = new TerminalAppToolBar(werminal, new Component[]{
                new TerminalAppButtonPanel(_ConfirmAction), new TerminalAppButtonPanel(_CancelAction)});

        addComponent(footerToolBar);

    }

    public URI getApiUri() {

        return _ApiUriTextBox.getValue();
    }

    public void setApiUri(final URI apiUri) {

        _ApiUriTextBox.setValue(apiUri);
    }

    public boolean isCancelled() {

        return _Cancelled;
    }

    class LoadApiDialogCancelAction extends CancelAction {

        public LoadApiDialogCancelAction(final Werminal werminal) {

            super(werminal);
        }

        @Override
        protected boolean doIt() {

            _Cancelled = true;
            return super.doIt();
        }

    }

    class LoadApiDialogConfirmationAction extends CloseAfterAction {


        public LoadApiDialogConfirmationAction(final Werminal werminal) {

            super(werminal, "OK");
        }

        @Override
        protected boolean doIt() {

            _Cancelled = false;

            final Werminal werminal = getWerminal();
            final Context context = werminal.getContext();
            final ApiLoader apiLoader = context.getApiLoader();

            final LoadApiDialog loadApiDialog = werminal.getLoadApiDialog();

            final URI apiUri = loadApiDialog.getApiUri();
            if (apiUri == null) {
                werminal.showError("\nPlease enter the (root) URI value to identify the REST API.\n\n ");
                return false;
            }

            try {
                final ApiNavigator apiNavigator = apiLoader.loadApi(apiUri);
                return (apiNavigator != null);
            }
            catch (ApiLoaderException e) {
                werminal.showError("\nFailed to load REST API metadata from URI:\n\n\t" + apiUri + "\n\nFailure detail message:\n\n" + e.getMessage() + "\n\nPlease enter the (root) URI value to identify the REST API.\n\n ");
                return false;
            }


        }

    }


}
