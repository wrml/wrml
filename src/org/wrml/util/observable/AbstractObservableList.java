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

import org.wrml.event.EventManager;
import org.wrml.util.observable.ListEventListener.EventType;

public abstract class AbstractObservableList<E> extends EventManager<ListEventListener> implements ObservableList<E> {

    public AbstractObservableList() {
        this(ListEventListener.class);
    }

    public AbstractObservableList(Class<ListEventListener> listenerClass) {
        super(listenerClass);
    }

    protected void fireListCleared(ListEvent event) {
        fireEvent(EventType.listCleared, event);
    }

    protected boolean fireListClearing(CancelableListEvent event) {
        fireEvent(EventType.listClearing, event);
        return !event.isCancelled();
    }

    protected void fireListElementInserted(ListEvent event) {
        fireEvent(EventType.listElementInserted, event);
    }

    protected void fireListElementRemoved(ListEvent event) {
        fireEvent(EventType.listElementRemoved, event);
    }

    protected void fireListElementUpdated(ListEvent event) {
        fireEvent(EventType.listElementUpdated, event);
    }

    protected boolean fireListInsertingElement(CancelableListEvent event) {
        fireEvent(EventType.listInsertingElement, event);
        return !event.isCancelled();
    }

    protected boolean fireListRemovingElement(CancelableListEvent event) {
        fireEvent(EventType.listRemovingElement, event);
        return !event.isCancelled();
    }

    protected boolean fireListUpdatingElement(CancelableListEvent event) {
        fireEvent(EventType.listUpdatingElement, event);
        return !event.isCancelled();
    }

}
