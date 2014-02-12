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

import org.wrml.model.Model;
import org.wrml.model.rest.Link;
import org.wrml.runtime.schema.LinkProtoSlot;
import org.wrml.runtime.schema.Prototype;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.CancelAction;
import org.wrml.werminal.action.FormFieldOpenAction;
import org.wrml.werminal.action.SubmitAction;
import org.wrml.werminal.component.FormField;
import org.wrml.werminal.component.FormPanel;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public class
        InvocationDialog extends WerminalDialog {

    private final FormField _FunctionLinkFormField;

    private final FormField _ParameterFormField;

    public InvocationDialog(final Werminal werminal, final String title, final Model function, final FormField functionLinkFormField) throws ClassNotFoundException {

        super(werminal, title, new SubmitAction(werminal, function, functionLinkFormField), new CancelAction(werminal));

        _FunctionLinkFormField = functionLinkFormField;

        final Prototype prototype = function.getPrototype();
        final String linkSlotName = functionLinkFormField.getFieldName();
        final LinkProtoSlot linkProtoSlot = prototype.getProtoSlot(linkSlotName);

        final URI requestSchemaUri = linkProtoSlot.getRequestSchemaUri();
        if (requestSchemaUri != null) {


            final SchemaLoader schemaLoader = werminal.getContext().getSchemaLoader();
            final Type parameterType = schemaLoader.getSchemaInterface(requestSchemaUri);

            final FormFieldOpenAction parameterFormFieldOpenAction = new FormFieldOpenAction(werminal);
            _ParameterFormField = new FormField("Parameter", parameterType, parameterFormFieldOpenAction);
            parameterFormFieldOpenAction.setFormField(_ParameterFormField);
        }
        else {
            _ParameterFormField = null;
        }

        render();
    }

    public Model getParameter() {

        if (_ParameterFormField != null) {
            return _ParameterFormField.getFieldValueTextBox().getValue();
        }

        return null;
    }

    @Override
    public void render() {

        removeAllComponents();

        if (_FunctionLinkFormField == null) {
            return;
        }

        addEmptySpace();

        final Werminal werminal = getWerminal();

        final int fieldCount = (_ParameterFormField != null) ? 2 : 1;
        final Map<String, FormField> formFieldMap = new LinkedHashMap<>(fieldCount);

        final FormFieldOpenAction functionFormFieldOpenAction = new FormFieldOpenAction(werminal);
        final FormField functionUriFormField = new FormField("Function", URI.class, functionFormFieldOpenAction);
        functionFormFieldOpenAction.setFormField(functionUriFormField);

        final Link functionLink = _FunctionLinkFormField.getFieldValueTextBox().getValue();
        final URI functionHref = functionLink.getHref();
        functionUriFormField.getFieldValueTextBox().setValue(functionHref);

        formFieldMap.put(functionUriFormField.getFieldName(), functionUriFormField);

        if (_ParameterFormField != null) {
            formFieldMap.put(_ParameterFormField.getFieldName(), _ParameterFormField);
        }

        final String linkSlotName = _FunctionLinkFormField.getFieldName();
        final String panelTitle = "  " + linkSlotName + "  ";
        final FormPanel formPanel = new FormPanel(werminal, panelTitle, formFieldMap);

        addComponent(formPanel);

        addEmptySpace();

        super.renderFooterToolBar();

    }


}