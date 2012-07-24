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

package org.wrml.util.observable;

import org.wrml.event.Event;

/**
 * Fired from an ObservableList whenever its contents are altered.
 */
public class ListEvent extends Event<ObservableList<?>> {

    private static final long serialVersionUID = 1L;

    private final int _Index;
    private final Object _InsertionElement;
    private final Object _RemovalElement;

    public ListEvent(final ObservableList<?> list) {
        this(list, null, null);
    }

    public ListEvent(final ObservableList<?> list, final Object insertionElement, final Object removalElement) {
        this(list, insertionElement, removalElement, -1);
    }

    public ListEvent(final ObservableList<?> list, final Object insertionElement, final Object removalElement,
            final int index) {

        super(list);
        _InsertionElement = insertionElement;
        _RemovalElement = removalElement;
        _Index = index;
    }

    public int getIndex() {
        return _Index;
    }

    public Object getInsertionElement() {
        return _InsertionElement;
    }

    public ObservableList<?> getList() {
        return getSource();
    }

    public Object getRemovalElement() {
        return _RemovalElement;
    }

}
