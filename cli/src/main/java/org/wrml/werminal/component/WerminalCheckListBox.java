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
 * Copyright 2012 Mark Masse (OSS project WRML.org)
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

import com.googlecode.lanterna.gui.component.CheckBoxList;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;
import org.wrml.werminal.terminal.TerminalAppKeyboardInteraction;

public class WerminalCheckListBox extends CheckBoxList
{

    private final Werminal _Werminal;

    private final TerminalAppKeyboardInteraction _KeyboardInteraction;

    private final WerminalAction _EnterOnSelectionAction;

    private Object _CheckedItem;

    public WerminalCheckListBox(final Werminal werminal)
    {

        this(werminal, null, null);
    }

    public WerminalCheckListBox(final Werminal werminal, final WerminalAction enterAction,
                                final WerminalAction enterOnSelectionAction)
    {

        _Werminal = werminal;
        _KeyboardInteraction = new TerminalAppKeyboardInteraction(_Werminal, enterAction, true);
        _EnterOnSelectionAction = enterOnSelectionAction;
    }

    public Object getCheckedItem()
    {

        return _CheckedItem;
    }

    @Override
    public Result keyboardInteraction(final Key key)
    {

        if (!isVisible())
        {
            return Result.EVENT_HANDLED;
        }

        final Kind kind = key.getKind();
        switch (kind)
        {

            case Enter:
            {

                final Object oldSelectedItem = getSelectedItem();
                final Object oldCheckedItem = getCheckedItem();

                // Intentional fall through here...
                if (_CheckedItem != null)
                {
                    setChecked(_CheckedItem, false);
                    _CheckedItem = null;
                }

                Result result = super.keyboardInteraction(key);

                final Object newSelectedItem = getSelectedItem();
                if ((newSelectedItem != null) && isChecked(newSelectedItem))
                {

                    setCheckedItem(newSelectedItem);

                }

                if (_KeyboardInteraction.handleKeyboardInteraction(key))
                {
                    result = Result.EVENT_HANDLED;
                }

                if (_EnterOnSelectionAction != null && oldCheckedItem != null && oldSelectedItem == oldCheckedItem)
                {
                    _EnterOnSelectionAction.doAction();
                }

                return result;
            }

            default:
            {

                if (_KeyboardInteraction.handleKeyboardInteraction(key))
                {
                    return Result.EVENT_HANDLED;
                }

                return super.keyboardInteraction(key);
            }
        }

    }

    protected void setCheckedItem(final Object newCheckedItem)
    {

        _CheckedItem = newCheckedItem;
        if (_CheckedItem != null)
        {
            setChecked(_CheckedItem, true);
        }
    }

}
