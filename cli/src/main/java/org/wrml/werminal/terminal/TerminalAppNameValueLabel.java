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
package org.wrml.werminal.terminal;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.layout.LinearLayout;
import com.googlecode.lanterna.terminal.Terminal;

public class TerminalAppNameValueLabel extends TerminalAppPanel {

    private final Label _NameLabel;

    private final Label _ValueLabel;

    public TerminalAppNameValueLabel(final TerminalApp app, final String name, final int nameWidth,
                                     final Terminal.Color nameColor, final boolean nameBold, final Alignment nameAlignment, final String value,
                                     final int valueWidth, final Terminal.Color valueColor, final boolean valueBold,
                                     final Alignment valueAlignment) {

        super(app, "", new Border.Invisible(), Orientation.HORISONTAL, false, false);

        // setBetweenComponentsPadding(0);

        _NameLabel = new Label(name, nameWidth, nameColor, nameBold);
        _NameLabel.setAlignment(nameAlignment);
        addComponent(_NameLabel);

        _ValueLabel = new Label(value, valueWidth, valueColor, valueBold);
        _ValueLabel.setAlignment(valueAlignment);
        addComponent(_ValueLabel, LinearLayout.GROWS_HORIZONTALLY);

    }

    public TerminalAppNameValueLabel(final TerminalApp app, final String name, final int nameWidth,
                                     final Terminal.Color nameColor, final boolean nameBold, final String value, final int valueWidth,
                                     final Terminal.Color valueColor, final boolean valueBold) {

        this(app, name, nameWidth, nameColor, nameBold, Alignment.RIGHT_CENTER, value, valueWidth, valueColor,
                valueBold, Alignment.LEFT_CENTER);
    }

    public Label getNameLabel() {

        return _NameLabel;
    }

    public Label getValueLabel() {

        return _ValueLabel;
    }

}
