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
import org.apache.commons.lang3.StringUtils;
import org.wrml.runtime.rest.RestUtils;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.action.WerminalAction;

import java.net.URI;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

public class HistoryCheckListBox extends WerminalCheckListBox {

    private final WerminalTextBox _TextBox;

    private final SortedSet<Object> _HistorySet;

    public HistoryCheckListBox(final WerminalTextBox forTextBox) {

        this(forTextBox, null, null);
    }

    public HistoryCheckListBox(final WerminalTextBox forTextBox, final WerminalAction enterAction,
                               final WerminalAction enterOnSelectionAction) {

        super((Werminal) forTextBox.getApp(), enterAction, enterOnSelectionAction);
        _TextBox = forTextBox;

        _HistorySet = new TreeSet<>();
    }

    @Override
    public void addItem(final Object item) {

        if (!addItemInternal(item)) {
            return;
        }

        syncView();
    }

    public void addItems(final Collection<?> items) {

        for (final Object item : items) {
            addItemInternal(item);
        }

        syncView();
    }

    @Override
    public void clearItems() {

        super.clearItems();
        _HistorySet.clear();
    }

    public WerminalTextBox getTextBox() {

        return _TextBox;
    }

    public SortedSet<Object> getItems() {

        return _HistorySet;
    }

    @Override
    public void setCheckedItem(final Object newCheckedItem) {

        super.setCheckedItem(newCheckedItem);
        _TextBox.setValue(newCheckedItem);

    }

    @Override
    public Result keyboardInteraction(final Key key) {

        if (!isVisible()) {
            return Result.EVENT_HANDLED;
        }

        final Key.Kind kind = key.getKind();
        switch (kind) {
            case NormalKey:

                final char character = key.getCharacter();
                if (setSelectedItemFromCharacter(character)) {
                    return Result.EVENT_HANDLED;
                }

                break;
        }

        return super.keyboardInteraction(key);
    }

    private boolean setSelectedItemFromCharacter(final char character) {
        // Support jumping to entry based on last path element in URI
        int indexOfBestMatch = -1;
        final int selectedIndex = getSelectedIndex();

        final String characterAsString = String.valueOf(character);

        if (_HistorySet.isEmpty() || !(_HistorySet.first() instanceof URI)) {
            return false;
        }

        final int size = _HistorySet.size();

        for (int i = selectedIndex + 1; i < size; i++) {
            if (itemLastSegementStartsWith(i, characterAsString)) {
                indexOfBestMatch = i;
                break;
            }
        }

        if (indexOfBestMatch < 0) {
            for (int i = 0; i < selectedIndex; i++) {
                if (itemLastSegementStartsWith(i, characterAsString)) {
                    indexOfBestMatch = i;
                    break;
                }
            }
        }

        if (indexOfBestMatch >= 0) {
            setSelectedItem(indexOfBestMatch);
            return true;
        }

        return false;
    }

    private boolean itemLastSegementStartsWith(int itemIndex, CharSequence charSequence) {

        final Object historyItem = getItemAt(itemIndex);

        if (!(historyItem instanceof URI)) {
            return false;
        }

        final URI uri = (URI) historyItem;
        final String lastPathElement = RestUtils.getLastPathElement(uri);
        return StringUtils.startsWithIgnoreCase(lastPathElement, charSequence);
    }

    private boolean addItemInternal(final Object item) {

        if (!(item instanceof Comparable) || _HistorySet.contains(item)) {
            return false;
        }

        return _HistorySet.add(item);
    }

    private void syncView() {

        super.clearItems();

        for (final Object historyItem : _HistorySet) {
            super.addItem(historyItem);
        }

    }
}
