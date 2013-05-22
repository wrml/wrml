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
package org.wrml.werminal.action;

import com.googlecode.lanterna.gui.GUIScreen.Position;
import com.googlecode.lanterna.gui.Window;
import org.wrml.model.Model;
import org.wrml.model.rest.Link;
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.rest.LinkTemplate;
import org.wrml.model.rest.Method;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.DimensionsBuilder;
import org.wrml.runtime.Keys;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.rest.ApiNavigator;
import org.wrml.runtime.rest.Resource;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.runtime.schema.ValueType;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.component.FormField;
import org.wrml.werminal.component.WerminalTextBox;
import org.wrml.werminal.dialog.EnumValueDialog;
import org.wrml.werminal.dialog.InvocationDialog;
import org.wrml.werminal.dialog.ListValueDialog;
import org.wrml.werminal.dialog.NewOrOpenModelDialog;
import org.wrml.werminal.window.ModelWindow;

import java.net.URI;
import java.util.UUID;

public class FormFieldOpenAction extends WerminalAction
{

    private FormField _FormField;

    public FormFieldOpenAction(final Werminal werminal)
    {

        super(werminal, "...");
    }

    @Override
    public void doAction()
    {

        final Werminal werminal = getWerminal();
        final Context context = werminal.getContext();
        final SchemaLoader schemaLoader = context.getSchemaLoader();
        final ApiLoader apiLoader = context.getApiLoader();
        final Window topWindow = werminal.getTopWindow();

        final FormField formField = getFormField();
        final String formFieldName = formField.getFieldName();
        final WerminalTextBox valueTextBox = formField.getFieldValueTextBox();
        final ValueType valueType = valueTextBox.getValueType();
        final Object currentValue = valueTextBox.getValue();

        switch (valueType)
        {
            case Boolean:
            {
                Boolean currentBooleanValue = (Boolean) currentValue;
                if (currentBooleanValue == null)
                {
                    currentBooleanValue = Boolean.FALSE;
                }
                valueTextBox.setValue(!(currentBooleanValue), false);

                break;
            }
            case Link:
            {
                final Link link = (Link) currentValue;
                if (link == null)
                {
                    if (!(topWindow instanceof ModelWindow))
                    {

                        final String dialogTitle = "How would you like to initialize \"" + formFieldName + "\"?";

                        final NewOrOpenModelDialog newOrOpenModelDialog =
                                new NewOrOpenModelDialog(werminal, dialogTitle, new CancelAction(werminal), formField);

                        werminal.showWindow(newOrOpenModelDialog, Position.CENTER);
                    }
                    else
                    {

                        werminal.showMessageBox("Empty Link Slot",
                                "This Link slot value is empty; it is not \"clickable\".");
                    }

                    break;
                }

                final URI linkRelationUri = link.getRel();
                final LinkRelation linkRelation = apiLoader.loadLinkRelation(linkRelationUri);
                final Method method = linkRelation.getMethod();

                if (topWindow instanceof ListValueDialog)
                {
                    if (method != Method.Get)
                    {
                        werminal.showMessageBox("Not Implemented", "Support for referencing links with the method: "
                                + method + " (HTTP " + method.getProtocolGivenName() + ") has not been implemented for Links within a List.");

                        break;
                    }

                    final URI documentSchemaUri = schemaLoader.getDocumentSchemaUri();
                    final URI href = link.getHref();
                    if (href != null)
                    {
                        final ApiNavigator apiNavigator = apiLoader.getParentApiNavigator(href);
                        final Resource endpoint = apiNavigator.getResource(href);
                        final LinkTemplate referenceTemplate = endpoint.getReferenceTemplates().get(linkRelationUri);
                        final URI responseSchemaUri = referenceTemplate.getResponseSchemaUri();

                        final URI schemaUri;
                        if (responseSchemaUri != null)
                        {
                            schemaUri = responseSchemaUri;
                        }
                        else
                        {
                            schemaUri = documentSchemaUri;
                        }

                        final Keys keys = apiLoader.buildDocumentKeys(href, schemaUri);
                        final Dimensions dimensions = new DimensionsBuilder(schemaUri).toDimensions();
                        final Model referencedModel = context.getModel(keys, dimensions);
                        if (referencedModel == null)
                        {
                            werminal.showMessageBox("404", "Failed to open linked document.");
                            break;
                        }

                        werminal.openModelWindow(referencedModel);
                    }
                    break;
                }
                else if (topWindow instanceof ModelWindow)
                {
                    final ModelWindow modelWindow = (ModelWindow) werminal.getTopWindow();
                    final Model model = modelWindow.syncModel();


                    switch (method)
                    {
                        case Delete:
                        {
                            // TODO: Prompt to delete
                            werminal.showMessageBox("Not Implemented", "Support for referencing links with the method: "
                                    + method + " (HTTP " + method.getProtocolGivenName()
                                    + ") has not been implemented in Werminal (yet).");
                            break;

                        }
                        case Get:
                        {
                            final Model referencedModel = model.reference(formFieldName);
                            werminal.openModelWindow(referencedModel);
                            break;
                        }
                        case Invoke:
                        {

                            final String dialogTitle = Method.Invoke.name() + " (" + Method.Invoke.getProtocolGivenName() + ") ";


                            try
                            {
                                final InvocationDialog invocationDialog = new InvocationDialog(werminal, dialogTitle, model, formField);
                                werminal.showWindow(invocationDialog, Position.CENTER);
                            }
                            catch (ClassNotFoundException e)
                            {
                                werminal.showError("Could not load Schema Java interface.", e);
                                return;
                            }


                            break;
                        }
                        case Metadata:
                        {
                            werminal.showMessageBox("Not Implemented", "Support for referencing links with the method: "
                                    + method + " (HTTP " + method.getProtocolGivenName()
                                    + ") has not been implemented in Werminal (yet).");
                            break;

                        }
                        case Options:
                        {
                            werminal.showMessageBox("Not Implemented", "Support for referencing links with the method: "
                                    + method + " (HTTP " + method.getProtocolGivenName()
                                    + ") has not been implemented in Werminal (yet).");
                            break;

                        }
                        case Save:
                        {
                            // TODO: Prompt to save
                            werminal.showMessageBox("Not Implemented", "Support for referencing links with the method: "
                                    + method + " (HTTP " + method.getProtocolGivenName()
                                    + ") has not been implemented in Werminal (yet).");
                            break;

                        }
                        default:
                        {
                            werminal.showMessageBox("Not Implemented", "Support for referencing links with the method: "
                                    + method + " (HTTP " + method.getProtocolGivenName()
                                    + ") has not been implemented in Werminal (yet).");
                            break;
                        }

                    }

                    break;

                }
                else
                {
                    werminal.showMessageBox("Not Implemented", "Support for referencing Link slots has not been implemented (yet).");
                }

                break;
            }

            case List:
            {
                // werminal.showMessageBox("Not Implemented",
                // "Support for opening List slots has not been implemented (yet).");
                werminal.openListDialog(formField);
                break;
            }
            case Model:
            {

                if (currentValue == null)
                {

                    final String dialogTitle = "How would you like to initialize \"" + formFieldName + "\"?";

                    final NewOrOpenModelDialog newOrOpenModelDialog =
                            new NewOrOpenModelDialog(werminal, dialogTitle, new CancelAction(werminal), formField);

                    werminal.showWindow(newOrOpenModelDialog, Position.CENTER);

                }
                else if (currentValue instanceof Model)
                {
                    final Model nestedModel = (Model) currentValue;
                    werminal.openModelWindow(nestedModel);
                }
                break;
            }
            case SingleSelect:
            {
                final Enum<?> selectedValue = (Enum<?>) currentValue;

                final EnumValueSelectionConfirmationAction enumValueSelectionConfirmationAction = new EnumValueSelectionConfirmationAction(
                        werminal, valueTextBox);

                final EnumValueDialog enumValueDialog = new EnumValueDialog(werminal, "Select One Value",
                        enumValueSelectionConfirmationAction, new CancelAction(werminal));
                enumValueSelectionConfirmationAction.setEnumValueDialog(enumValueDialog);

                enumValueDialog.setSelectedValue(selectedValue);

                werminal.showWindow(enumValueDialog, Position.CENTER);

                break;
            }
            case Text:


                if (currentValue == null && valueTextBox.getHeapValueType().equals(UUID.class))
                {
                    valueTextBox.setValue(UUID.randomUUID(), false);
                }
                else
                {
                    werminal.showMessageBox("Text (Not Implemented)",
                            "Support for opening Text slots has not been implemented (yet).");
                }
                break;

            default:
                werminal.showMessageBox("Not Implemented",
                        "Support for opening this slot type has not been implemented (yet).");
                break;
        }
    }

    public FormField getFormField()
    {

        return _FormField;
    }

    public void setFormField(final FormField formField)
    {

        _FormField = formField;
    }

}
