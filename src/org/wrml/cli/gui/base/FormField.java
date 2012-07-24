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

package org.wrml.cli.gui.base;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.CheckBox;
import com.googlecode.lanterna.gui.layout.SizePolicy;

import org.wrml.cli.gui.GuiAction;
import org.wrml.util.transformer.ToStringTransformer;

public class FormField extends BasePanel {

    private final CheckBox _FieldCheckBox;
    private final ObjectTextBox _FieldValueTextBox;
    private final BaseButton _FieldDetailsButton;

    public FormField(String title, int textBoxWidth, ToStringTransformer<?> valueTransformer, GuiAction enterAction) {
        super(title, new Border.Standard(), Orientation.HORISONTAL, false, false);

        _FieldCheckBox = new CheckBox("", false);
        addComponent(_FieldCheckBox, SizePolicy.CONSTANT);

        _FieldValueTextBox = new ObjectTextBox(textBoxWidth, valueTransformer, enterAction);
        addComponent(_FieldValueTextBox, SizePolicy.CONSTANT);

        _FieldDetailsButton = new BaseButton(enterAction);
        addComponent(_FieldDetailsButton, SizePolicy.CONSTANT);

    }

    public CheckBox getFieldCheckBox() {
        return _FieldCheckBox;
    }

    public Button getFieldDetailsButton() {
        return _FieldDetailsButton;
    }

    public ObjectTextBox getFieldValueTextBox() {
        return _FieldValueTextBox;
    }

}
