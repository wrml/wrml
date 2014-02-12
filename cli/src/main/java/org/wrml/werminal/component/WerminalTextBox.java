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
package org.wrml.werminal.component;

import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.wrml.model.Model;
import org.wrml.model.rest.Link;
import org.wrml.model.schema.ValueType;
import org.wrml.runtime.Context;
import org.wrml.runtime.Keys;
import org.wrml.runtime.syntax.SyntaxLoader;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.terminal.TerminalAppTextBox;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"unchecked"})
public class WerminalTextBox extends TerminalAppTextBox {

    private static final String OPEN_BUTTON_STRING = "< Open... >";

    private static final String CLICK_BUTTON_STRING = "< Click... >";

    private final WerminalAction _EnterAction;

    private final Type _ValueType;

    private Object _Value;

    private String _TextValue;

    private boolean _ListValueChanged;

    private boolean _ModelValueChanged;

    public WerminalTextBox(final Werminal werminal, final int width, final Type valueType,
                           final WerminalAction enterAction) {

        super(werminal, width);
        _ValueType = valueType;
        _EnterAction = enterAction;

    }

    public WerminalTextBox(final Werminal werminal, final Type valueType, final WerminalAction enterAction) {

        this(werminal, 255, valueType, enterAction);
    }

    public WerminalAction getEnterAction() {

        return _EnterAction;
    }

    public Type getHeapValueType() {

        return _ValueType;
    }

    public <V> V getValue() {

        final Context context = getWerminal().getContext();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();
        final Type heapValueType = getHeapValueType();
        final ValueType valueType = getValueType();

        if (_Value == null && valueType != ValueType.Model && valueType != ValueType.List
                && valueType != ValueType.Link) {
            final String stringValue = getText();
            _Value = syntaxLoader.parseSyntacticText(stringValue, heapValueType);
        }

        return (V) _Value;
    }

    public ValueType getValueType() {

        final Context context = getWerminal().getContext();
        return context.getSchemaLoader().getValueType(getHeapValueType());
    }

    public Werminal getWerminal() {

        return getApp();
    }

    public boolean isValueChanged() {

        final ValueType valueType = getValueType();
        switch (valueType) {
            case Link:
            case Model:
                return isModelValueChanged();

            case List:
                return isListValueChanged();

            case Boolean:
            case Date:
            case Double:
            case Integer:
            case Long:
            case Native:
            case SingleSelect:
            case Text:
            default:

                final String currentText = getText();
                if (_TextValue != null) {
                    return !(_TextValue.equals(currentText));
                }
                else {
                    return currentText != null && !currentText.isEmpty();
                }

        }

    }

    @Override
    public Result keyboardInteraction(final Key key) {

        if (!isVisible()) {
            return Result.EVENT_HANDLED;
        }

        final Result result;
        final Kind kind = key.getKind();
        final Type valueType = getHeapValueType();

        switch (kind) {
            case Enter: {

                if (_EnterAction != null) {
                    try {
                        _EnterAction.doAction();
                    }
                    catch (Exception t) {
                        getWerminal().showError("Error - An unhandled exception has arisen.", t);

                    }
                }

                result = Result.EVENT_HANDLED;
                break;
            }
            case NormalKey: {
                if (ValueType.isModelType(valueType) || TypeUtils.isAssignable(valueType, List.class)) {
                    result = Result.EVENT_HANDLED;
                }
                else {
                    _Value = null;

                    result = super.keyboardInteraction(key);
                }
                break;
            }
            case Backspace:
            case Delete: {

                // TODO: Check the value type and determine if editable

                if (ValueType.isModelType(valueType)) {
                    setValue(null, false);
                    result = Result.EVENT_HANDLED;
                }
                else if (TypeUtils.isAssignable(valueType, List.class)) {
                    result = Result.EVENT_HANDLED;
                }
                else {
                    _Value = null;
                    result = super.keyboardInteraction(key);
                }
                break;
            }

            default: {
                result = super.keyboardInteraction(key);
                break;
            }
        }

        return result;
    }

    @Override
    public void setText(final String text) {

        setText(text, true);

    }

    public Object setValue(final Object value) {

        return setValue(value, true);
    }

    public Object setValue(final Object value, final boolean silentChange) {

        final Object oldValue = _Value;

        final Werminal werminal = getWerminal();
        final ValueType valueType = getValueType();
        _Value = value;

        final Context context = getWerminal().getContext();
        final SyntaxLoader syntaxLoader = context.getSyntaxLoader();
        String stringValue = "";
        switch (valueType) {

            case Link: {

                final String disabledLinkString = "href=\"\"";
                if (value instanceof Link) {
                    final Link linkValue = (Link) value;
                    final URI href = linkValue.getHref();
                    if (href != null) {
                        // TODO: Display the HTTP method and the relation URI too

                        stringValue = "href=\"" + href.toString() + "\"   " + CLICK_BUTTON_STRING;
                    }
                    else {
                        stringValue = disabledLinkString;
                    }

                    _ModelValueChanged = !silentChange;

                    // TODO: Track Model changes
                }
                else {
                    stringValue = disabledLinkString;
                }

                break;
            }
            case Model: {

                if (value instanceof Model) {
                    final Model modelValue = (Model) value;
                    final Keys keys = modelValue.getKeys();
                    if (keys != null) {
                        final Set<URI> keyedSchemaUris = keys.getKeyedSchemaUris();
                        for (final URI keyedSchemaUri : keyedSchemaUris) {
                            final Object initialKeyValue = keys.getValue(keyedSchemaUri);
                            stringValue = String.valueOf(initialKeyValue);
                            break;
                        }
                    }
                    else {
                        stringValue = modelValue.getHeapId().toString();
                    }

                    _ModelValueChanged = !silentChange;

                    // TODO: Track Model changes
                }

                if (stringValue == null || stringValue.isEmpty()) {
                    stringValue = OPEN_BUTTON_STRING;
                }
                else {
                    stringValue = stringValue + " " + OPEN_BUTTON_STRING;
                }
                break;
            }
            case List: {

                if (value instanceof List) {
                    final List<?> listValue = (List<?>) value;

                    stringValue = werminal.listToString(listValue, getHeapValueType());

                    _ListValueChanged = !silentChange;
                    // TODO: Track List changes
                }

                stringValue = stringValue + " " + OPEN_BUTTON_STRING;
                break;
            }
            case Boolean:
            case Date:
            case Double:
            case Integer:
            case Long:
            case Native:
            case SingleSelect:
            case Text:
            default:
                stringValue = syntaxLoader.formatSyntaxValue(value);
                break;

        }

        if (stringValue == null) {
            stringValue = "";
        }

        setText(stringValue, false);
        if (silentChange) {
            _TextValue = stringValue;
        }

        return oldValue;
    }

    protected void setText(final String stringValue, final boolean clearValue) {

        if (clearValue) {
            _Value = null;
        }

        super.setText(stringValue);

    }

    private boolean isListValueChanged() {

        return _ListValueChanged;
    }

    private boolean isModelValueChanged() {

        return _ModelValueChanged;
    }

}
