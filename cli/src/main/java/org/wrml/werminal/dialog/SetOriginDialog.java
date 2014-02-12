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
import com.googlecode.lanterna.gui.component.RadioCheckBoxList;
import com.googlecode.lanterna.input.Key;
import org.wrml.runtime.Context;
import org.wrml.runtime.service.ServiceLoader;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.terminal.TerminalAppButtonPanel;
import org.wrml.werminal.terminal.TerminalAppKeyboardInteraction;
import org.wrml.werminal.terminal.TerminalAppToolBar;
import org.wrml.werminal.window.WerminalWindow;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SetOriginDialog extends WerminalWindow {

    private final OriginValuesCheckBoxList _OriginValuesCheckBoxList;

    public SetOriginDialog(final Werminal werminal, final String title, final WerminalAction confirmAction, final WerminalAction dismissAction) {

        super(werminal, title);

        setBorder(new Border.Standard());

        _OriginValuesCheckBoxList = new OriginValuesCheckBoxList(werminal, confirmAction);

        addEmptySpace();

        addComponent(_OriginValuesCheckBoxList);

        final TerminalAppToolBar footerToolBar = new TerminalAppToolBar(werminal, new Component[]{
                new TerminalAppButtonPanel(confirmAction), new TerminalAppButtonPanel(dismissAction)});
        addComponent(footerToolBar);
    }

    public final String getSelectedValue() {

        return (String) _OriginValuesCheckBoxList.getItemAt(_OriginValuesCheckBoxList.getCheckedItemIndex());
    }

    public final void setSelectedValue(final String selectedValue) {

        _OriginValuesCheckBoxList.clearItems();


        final Context context = getWerminal().getContext();
        final ServiceLoader serviceLoader = context.getServiceLoader();

        final Set<String> serviceNames = serviceLoader.getServiceNames();

        final SortedSet<String> sortedServiceNames = new TreeSet<>(serviceNames);

        int index = 0;
        for (final String serviceName : sortedServiceNames) {

            _OriginValuesCheckBoxList.addItem(serviceName);

            if (serviceName.equals(selectedValue)) {
                _OriginValuesCheckBoxList.setCheckedItemIndex(index);
            }

            index++;
        }


    }

    public static class OriginValuesCheckBoxList extends RadioCheckBoxList {

        private final TerminalAppKeyboardInteraction _KeyboardInteraction;

        public OriginValuesCheckBoxList(final Werminal werminal, final WerminalAction confirmAction) {

            _KeyboardInteraction = new TerminalAppKeyboardInteraction(werminal, confirmAction, true);
        }

        @Override
        public Result keyboardInteraction(final Key key) {

            if (!isVisible()) {
                return Result.EVENT_HANDLED;
            }

            if (key.getKind() == Key.Kind.Enter) {
                super.keyboardInteraction(key);
            }

            if (_KeyboardInteraction.handleKeyboardInteraction(key)) {
                return Result.EVENT_HANDLED;
            }

            return super.keyboardInteraction(key);
        }

    }
}
