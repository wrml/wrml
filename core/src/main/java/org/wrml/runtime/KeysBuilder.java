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
package org.wrml.runtime;

import org.wrml.util.AsciiArt;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * A simple builder for immutable {@link Keys}.
 */
public final class KeysBuilder {

    private final MutableKeys _Keys;

    /**
     * Default constructor creates initially "empty" Keys.
     */
    public KeysBuilder() {

        _Keys = new MutableKeys();
    }

    /**
     * Creates the {@link KeysBuilder} with the initial keyed schema.
     */
    public KeysBuilder(final URI schemaUri, final Object keyValue) {

        this();
        addKey(schemaUri, keyValue);
    }

    public KeysBuilder addKey(final URI schemaUri, final Object keyValue) {

        _Keys.addKey(schemaUri, keyValue);
        return this;
    }

    public Keys toKeys() {

        return _Keys;
    }

    @Override
    public String toString() {

        return getClass().getSimpleName() + " { keys : " + _Keys + "}";
    }

    /**
     * Include all of the other Keys in the built Keys
     *
     * @param otherKeys some other Keys to include in the Keys being built.
     */
    public void addAll(final Keys otherKeys) {

        if (otherKeys instanceof MutableKeys) {
            _Keys._KeyedSchemaUriToKeyValue.putAll(((MutableKeys) otherKeys).getInternalMap());
        }
        else if (otherKeys != null) {
            final Set<URI> keyedSchemaUris = otherKeys.getKeyedSchemaUris();
            for (URI keyedSchemaUri : keyedSchemaUris) {
                _Keys.addKey(keyedSchemaUri, otherKeys.getValue(keyedSchemaUri));
            }
        }
    }

    /**
     * Internal, mutable implementation of {@link Keys}.
     */
    private static final class MutableKeys implements Keys {

        private static final long serialVersionUID = 1L;

        private final LinkedHashMap<URI, Object> _KeyedSchemaUriToKeyValue;

        /**
         * Create a set of keys with the specified pair as the first/primary identity.
         */
        MutableKeys() {

            _KeyedSchemaUriToKeyValue = new LinkedHashMap<URI, Object>();
        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) {
                return true;
            }
            if (!(o instanceof MutableKeys)) {
                return false;
            }

            final MutableKeys that = (MutableKeys) o;

            if (_KeyedSchemaUriToKeyValue != null ? !_KeyedSchemaUriToKeyValue.equals(that._KeyedSchemaUriToKeyValue) : that._KeyedSchemaUriToKeyValue != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {

            return _KeyedSchemaUriToKeyValue != null ? _KeyedSchemaUriToKeyValue.hashCode() : 0;
        }

        @Override
        public int getCount() {

            return _KeyedSchemaUriToKeyValue.size();
        }

        /**
         * Returns a set of the schemas that pertain to this Keys instance.
         *
         * @return an unordered Set of key URI's
         */
        @Override
        public Set<URI> getKeyedSchemaUris() {

            return _KeyedSchemaUriToKeyValue.keySet();
        }

        /**
         * Returns the key object that correlates to the schema passed in.
         * Useful when a model (or request) has more than one schema.
         *
         * @param keyedSchemaUri
         * @return the value of the requested key
         */
        @Override
        @SuppressWarnings("unchecked")
        public <V> V getValue(final URI keyedSchemaUri) {

            if (!_KeyedSchemaUriToKeyValue.containsKey(keyedSchemaUri)) {
                return null;
            }

            return (V) _KeyedSchemaUriToKeyValue.get(keyedSchemaUri);
        }

        @Override
        public String toString() {

            return AsciiArt.express(_KeyedSchemaUriToKeyValue);
        }

        /**
         * Add or replace a key to signify a secondary identity (scoped within an alternate/base schema).
         *
         * @param schemaUri - the schema to associate the key with
         * @param keyValue
         */
        void addKey(final URI schemaUri, final Object keyValue) {

            if (schemaUri == null) {
                throw new NullPointerException("The schema uri is required.");
            }

            if (keyValue == null) {
                throw new NullPointerException("The key value is required.");
            }

            _KeyedSchemaUriToKeyValue.put(schemaUri, keyValue);

        }

        Map<URI, Object> getInternalMap() {

            return _KeyedSchemaUriToKeyValue;
        }

    }

}
