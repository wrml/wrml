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

package org.wrml.event;

import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.wrml.util.Cancelable;
import org.wrml.util.observable.DelegatingObservableList;

/**
 * A handy utility or base class that manages event registration,
 * deregistration, and dispatching for a specific class of listeners.
 * 
 * @param <L>
 *            The class of EventListener to work with.
 */
public class EventManager<L extends java.util.EventListener> implements EventSource<L> {

    private final transient Class<L> _EventListenerClass;
    private transient EventListenerList<L> _EventListeners;
    private transient Set<L> _EventListenerSet;

    public EventManager(Class<L> eventListenerClass) {
        _EventListenerClass = eventListenerClass;
    }

    @Override
    public boolean addEventListener(L eventListener) {
        if (_EventListeners == null) {
            init();
        }

        if (_EventListenerSet.contains(eventListener)) {
            return false;
        }

        _EventListenerSet.add(eventListener);

        return _EventListeners.add(eventListener);
    }

    public void fireEvent(Enum<?> eventEnum, EventObject event) {
        fireEvent(String.valueOf(eventEnum), event);
    }

    public void fireEvent(String eventName, EventObject event) {
        if (_EventListeners == null) {
            return;
        }

        _EventListeners.fireEvent(eventName, event);
    }

    public void free() {

        if (_EventListeners != null) {
            _EventListeners.clear();
            _EventListeners = null;
        }

        if (_EventListenerSet != null) {
            _EventListenerSet.clear();
            _EventListenerSet = null;
        }

    }

    public Class<L> getEventListenerClass() {
        return _EventListenerClass;
    }

    public int getEventListenerCount() {
        return (_EventListenerSet != null) ? _EventListenerSet.size() : 0;
    }

    public EventListenerList<L> getEventListeners() {
        if (_EventListeners == null) {
            init();
        }
        return _EventListeners;
    }

    public Set<L> getEventListenerSet() {
        if (_EventListenerSet == null) {
            init();
        }
        return _EventListenerSet;
    }

    /**
     * Returns true if an event is worth creating and firing (because it will be
     * heard by listeners).
     * 
     * @return true if there are listeners interested in our events.
     */
    public boolean isEventHearable() {
        return getEventListenerCount() > 0;
    }

    @Override
    public boolean removeEventListener(L eventListener) {
        if (_EventListeners == null) {
            return false;
        }
        return _EventListeners.remove(eventListener);
    }

    private void init() {
        _EventListeners = new DelegatingEventListenerList<L>(getEventListenerClass(), new CopyOnWriteArrayList<L>());
        _EventListenerSet = new HashSet<L>();
    }

    /**
     * An exception that fires back from an event's recipient.
     */
    public static class EventBackfiredException extends RuntimeException {

        private static final long serialVersionUID = 1L;
        private final EventObject _Event;

        public EventBackfiredException(String yourProblem, Throwable toBlame, EventObject event) {
            super(yourProblem, toBlame);
            _Event = event;
        }

        public EventObject getEvent() {
            return _Event;
        }

    }

    /**
     * A helpful List that makes it easy to fire named events to multiple
     * registered listeners.
     * 
     * @param <L>
     *            The type of EventListener to be contained within the list.
     */
    static class DelegatingEventListenerList<L extends java.util.EventListener> extends DelegatingObservableList<L>
            implements EventListenerList<L> {

        private final Class<L> _EventListenerElementType;
        private GenericEventListener<L> _GenericEventListener;

        public DelegatingEventListenerList(Class<L> eventListenerElementType, List<L> delegate) {
            super(delegate);
            _EventListenerElementType = eventListenerElementType;
        }

        @Override
        public void fireEvent(String eventName, EventObject event) {
            if (_GenericEventListener == null) {
                _GenericEventListener = GenericEventListener.forEventListenerClass(getEventListenerElementType());
            }

            _GenericEventListener.fireEvent(eventName, event, getDelegate());
        }

        public Class<L> getEventListenerElementType() {
            return _EventListenerElementType;
        }

        /**
         * An internal class that uses reflection to "proxy" for a specific type
         * of
         * event listener.
         * 
         * @param <L>
         *            The EventListener interface to mimic.
         */
        static class GenericEventListener<L extends EventListener> implements EventListener {

            public final static String EVENT_METHOD_NAME_PREFIX = "on";

            private final static int EVENT_NAME_FIRST_LETTER_INDEX = EVENT_METHOD_NAME_PREFIX.length();
            private final static int EVENT_NAME_SECOND_LETTER_INDEX = EVENT_NAME_FIRST_LETTER_INDEX + 1;

            private final static Map<Class<?>, GenericEventListener<?>> _Cache;

            static {
                _Cache = new WeakHashMap<Class<?>, GenericEventListener<?>>();
            }

            @SuppressWarnings("unchecked")
            final static <L extends EventListener> GenericEventListener<L> forEventListenerClass(
                    Class<L> eventListenerClass) {

                GenericEventListener<L> genericEventListener = null;
                if (_Cache.containsKey(eventListenerClass)) {
                    genericEventListener = (GenericEventListener<L>) _Cache.get(eventListenerClass);
                }
                else {
                    genericEventListener = new GenericEventListener<L>(eventListenerClass);
                    _Cache.put(eventListenerClass, genericEventListener);
                }

                return genericEventListener;
            }

            private final Class<L> _EventListenerClass;
            private Map<String, Method> _EventMethods;

            GenericEventListener(Class<L> eventListenerClass) {
                _EventListenerClass = eventListenerClass;
            }

            public void fireEvent(String eventName, EventObject event, List<L> listeners) {

                final Method onEventMethod = getEventMethod(eventName);

                if (onEventMethod == null) {
                    throw new NullPointerException(getEventListenerClass() + " does not have an event named \""
                            + eventName + "\"");
                }

                Cancelable cancelable = null;
                if (event instanceof Cancelable) {
                    cancelable = (Cancelable) event;
                }

                final Object[] args = new Object[] { event };

                for (final L listener : listeners) {

                    if ((cancelable != null) && cancelable.isCancelled()) {
                        break;
                    }

                    try {
                        onEventMethod.invoke(listener, args);
                    }
                    catch (final Throwable bang) {
                        throw new EventBackfiredException("Invocation of (" + listener + ") listener method: ("
                                + onEventMethod + "), with args: (" + args + ") has backfired.", bang, event);
                    }

                }
            }

            public Class<L> getEventListenerClass() {
                return _EventListenerClass;
            }

            public Method getEventMethod(String eventName) {
                return getEventMethods().get(eventName);
            }

            public Map<String, Method> getEventMethods() {

                if (_EventMethods == null) {

                    _EventMethods = new HashMap<String, Method>();

                    final Method[] methods = _EventListenerClass.getMethods();
                    for (final Method method : methods) {
                        final String eventName = getEventName(method);
                        if (eventName != null) {
                            _EventMethods.put(eventName, method);
                        }
                    }
                }

                return _EventMethods;
            }

            private String getEventName(Method method) {
                final String methodName = method.getName();

                if (!methodName.startsWith(EVENT_METHOD_NAME_PREFIX)) {
                    //System.err.println("\"" + methodName + "\" does not start with: " + EVENT_METHOD_NAME_PREFIX);
                    return null;
                }

                final String eventName = Character.toLowerCase(methodName.charAt(EVENT_NAME_FIRST_LETTER_INDEX))
                        + methodName.substring(EVENT_NAME_SECOND_LETTER_INDEX);

                //System.err.println("\"" + eventName + "\" is the event name for method: " + method);

                return eventName;
            }

        }

    }

}
