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
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Label.Alignment;
import com.googlecode.lanterna.gui.layout.SizePolicy;
import com.googlecode.lanterna.terminal.Terminal;

public class NameValueLabel extends BasePanel {

    private final Label _NameLabel;
    private final Label _ValueLabel;

    public NameValueLabel(final String name, int nameWidth, Terminal.Color nameColor, boolean nameBold,
            Alignment nameAlignment, final String value, int valueWidth, Terminal.Color valueColor, boolean valueBold,
            Alignment valueAlignment) {

        super("", new Border.Invisible(), Orientation.HORISONTAL, false, false);

        setBetweenComponentsPadding(0);

        _NameLabel = new Label(name, nameWidth, nameColor, nameBold, nameAlignment);
        addComponent(_NameLabel, SizePolicy.CONSTANT);

        _ValueLabel = new Label(value, valueWidth, valueColor, valueBold, valueAlignment);
        addComponent(_ValueLabel, SizePolicy.CONSTANT);

    }

    public Label getNameLabel() {
        return _NameLabel;
    }

    public Label getValueLabel() {
        return _ValueLabel;
    }

}
