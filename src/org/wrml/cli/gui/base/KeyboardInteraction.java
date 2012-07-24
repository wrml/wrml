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
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Interactable.Result;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;

public class KeyboardInteraction {

    private final GUIScreen _GUIScreen;
    private final Component _Component;
    //private final ComponentType _ComponentType;
    private final Action _EnterAction;
    private final boolean _CloseOnEscape;

    public KeyboardInteraction(GUIScreen guiScreen, Component component, Action enterAction, boolean closeOnEscape) {

        _GUIScreen = guiScreen;
        _Component = component;
        _EnterAction = enterAction;
        _CloseOnEscape = closeOnEscape;

        /*
         * if (_Component instanceof Button) {
         * _ComponentType = ComponentType.Button;
         * }
         * else if (_Component instanceof CheckBox) {
         * _ComponentType = ComponentType.Checkbox;
         * }
         * else if (_Component instanceof TextArea) {
         * _ComponentType = ComponentType.TextArea;
         * }
         * else if (_Component instanceof TextBox) {
         * _ComponentType = ComponentType.TextBox;
         * }
         * else {
         * _ComponentType = ComponentType.Unknown;
         * }
         */
    }

    public Component getComponent() {
        return _Component;
    }

    public Action getEnterAction() {
        return _EnterAction;
    }

    public GUIScreen getGUIScreen() {
        return _GUIScreen;
    }

    public Result keyboardInteraction(Key key) {

        final Kind kind = key.getKind();

        switch (kind) {

        case Enter:

            if (_EnterAction != null) {
                _EnterAction.doAction();
                return Result.DO_NOTHING;
            }

            break;

        case Escape:
            if (_CloseOnEscape) {
                getGUIScreen().closeWindow();
                return Result.DO_NOTHING;
            }

            break;

        }

        return null;
    }

    /*
     * private enum ComponentType {
     * Button,
     * Checkbox,
     * TextArea,
     * TextBox,
     * Unknown;
     * }
     */

}
