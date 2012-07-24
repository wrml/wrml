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

import java.util.HashMap;
import java.util.Map;

import com.googlecode.lanterna.gui.Theme;

public abstract class BaseTheme<E extends Enum<E>> extends Theme {

    private final Map<E, Definition> _Definitions;
    private final Map<Category, Definition> _CategorizedDefinitions;

    private final Class<E> _DefinitionEnumClass;

    public BaseTheme(Class<E> definitionEnumClass) {
        _DefinitionEnumClass = definitionEnumClass;
        _Definitions = new HashMap<E, Definition>();
        _CategorizedDefinitions = new HashMap<Category, Definition>();
    }

    @Override
    public Definition getDefinition(Category category) {
        return _CategorizedDefinitions.get(category);
    }

    @Override
    protected Definition getBorder() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "Border"));
    }

    @Override
    protected Definition getButtonActive() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "ButtonActive"));
    }

    @Override
    protected Definition getButtonInactive() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "ButtonInactive"));
    }

    @Override
    protected Definition getButtonLabelActive() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "ButtonLabelActive"));
    }

    @Override
    protected Definition getButtonLabelInactive() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "ButtonLabelInactive"));
    }

    protected Map<Category, Definition> getCategorizedDefinitions() {
        return _CategorizedDefinitions;
    }

    @Override
    protected Definition getCheckBox() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "CheckBox"));
    }

    @Override
    protected Definition getCheckBoxSelected() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "CheckBoxSelected"));
    }

    @Override
    protected Definition getDefault() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "DialogArea"));
    }

    protected Map<E, Definition> getDefinitions() {
        return _Definitions;
    }

    @Override
    protected Definition getDialogEmptyArea() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "DialogArea"));
    }

    @Override
    protected Definition getItem() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "ListItem"));
    }

    @Override
    protected Definition getItemSelected() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "ListItemSelected"));
    }

    @Override
    protected Definition getRaisedBorder() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "RaisedBorder"));
    }

    @Override
    protected Definition getScreenBackground() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "ScreenBackground"));
    }

    @Override
    protected Definition getShadow() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "Shadow"));
    }

    @Override
    protected Definition getTextBox() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "TextBox"));
    }

    @Override
    protected Definition getTextBoxFocused() {
        return _Definitions.get(Enum.valueOf(_DefinitionEnumClass, "TextBoxFocused"));
    }

}
