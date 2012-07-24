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

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;

import org.wrml.util.transformer.ToStringTransformer;

public class ObjectTextBox extends BaseTextBox {

    private final ToStringTransformer<?> _ValueTransformer;
    private final Action _EnterAction;
    private Object _Value;

    public ObjectTextBox(final int width, final ToStringTransformer<?> valueTransformer, final Action enterAction) {
        super(width);
        _ValueTransformer = valueTransformer;
        _EnterAction = enterAction;
    }

    public Action getEnterAction() {
        return _EnterAction;
    }

    public Object getValue() {

        final String stringValue = getText();

        if (_Value == null) {
            if (_ValueTransformer != null) {
                _Value = _ValueTransformer.bToA(stringValue);
            }
            else {
                _Value = stringValue;
            }

        }
        return _Value;
    }

    public ToStringTransformer<?> getValueTransformer() {
        return _ValueTransformer;
    }

    @Override
    public Result keyboardInteraction(Key key) {

        if (!isVisible()) {
            return Result.DO_NOTHING;
        }

        final Kind kind = key.getKind();

        switch (kind) {
        case Enter:

            if (_EnterAction != null) {
                _EnterAction.doAction();
                return Result.DO_NOTHING;
            }

            break;

        case NormalKey:
        case Backspace:
        case Delete:
            _Value = null;
        }

        return super.keyboardInteraction(key);
    }

    @SuppressWarnings("unchecked")
    public Object setValue(Object value) {

        final Object oldValue = _Value;
        _Value = value;

        final String stringValue;
        if (_Value == null) {
            stringValue = "";
        }
        else if (_ValueTransformer != null) {

            @SuppressWarnings("rawtypes")
            final ToStringTransformer transformer = _ValueTransformer;
            stringValue = (String) transformer.aToB(_Value);
        }
        else {
            stringValue = String.valueOf(_Value);
        }

        setText(stringValue);

        return oldValue;
    }

}
