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
package org.wrml.runtime.service.cache;

import org.wrml.model.Model;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.service.AbstractService;
import org.wrml.runtime.service.ServiceConfiguration;

import java.net.URI;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * A {@link ModelCache} implementation that is sharded by {@link Schema}. Each shard represents a "domain" or keyspace
 * identified using the Schema that declared the mapped key.
 * </p>
 * <p>
 * <b>DefaultConfiguration:</b>
 * </p>
 * <p>
 * <code>
 * <p/>
 * "context" :
 * {
 * <p/>
 * "modelCache" :
 * {
 * "name" : "Cache",
 * "implementation" : "org.wrml.runtime.service.cache.ShardedModelCache"
 * }
 * <p/>
 * }
 * </code>
 * </p>
 */
public class ShardedModelCache extends AbstractService implements ModelCache {

    /**
     * Mapping of Model heap id to cached Models.
     */
    private final ConcurrentHashMap<UUID, Model> _Models;

    /**
     * Mapping of Schema URI to Shard (Mapping of Schema-declared Key value to Model heap id.
     */
    private final ConcurrentHashMap<URI, ConcurrentHashMap<Object, UUID>> _Shards;

    public ShardedModelCache() {

        _Models = new ConcurrentHashMap<UUID, Model>();
        _Shards = new ConcurrentHashMap<URI, ConcurrentHashMap<Object, UUID>>();
    }

    @Override
    public void clear() {

        _Shards.clear();
        _Models.clear();
    }

    @Override
    public boolean contains(final Keys keys, final Dimensions requestedDimensions) {

        final UUID heapId = getCachedHeapId(keys);
        return heapId != null;
    }

    @Override
    public void delete(final Keys keys, final Dimensions dimensions) {

        final UUID heapId = getCachedHeapId(keys);
        if (heapId == null) {
            return;
        }

        _Models.remove(heapId);
        final Set<URI> keyedSchemaUris = keys.getKeyedSchemaUris();
        for (final URI keyedSchemaUri : keyedSchemaUris) {
            if (keyedSchemaUri != null && _Shards.containsKey(keyedSchemaUri)) {
                final ConcurrentHashMap<Object, UUID> shard = _Shards.get(keyedSchemaUri);
                final Object key = keys.getValue(keyedSchemaUri);
                if (key != null && shard.containsKey(key)) {
                    shard.remove(key);
                }

                if (shard.isEmpty()) {
                    _Shards.remove(keyedSchemaUri);
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public Model get(final Keys keys, final Dimensions dimensions) {

        final UUID heapId = getCachedHeapId(keys);
        if (heapId == null) {
            return null;
        }

        return _Models.get(heapId);
    }

    @Override
    public Model save(final Model model) {

        final Keys keys = model.getKeys();
        final UUID heapId = model.getHeapId();
        _Models.put(heapId, model);

        final Set<URI> keyedSchemaUris = keys.getKeyedSchemaUris();
        for (final URI keyedSchemaUri : keyedSchemaUris) {
            if (keyedSchemaUri == null) {
                continue;
            }

            if (!_Shards.containsKey(keyedSchemaUri)) {
                _Shards.put(keyedSchemaUri, new ConcurrentHashMap<Object, UUID>());
            }

            final ConcurrentHashMap<Object, UUID> shard = _Shards.get(keyedSchemaUri);

            final Object key = keys.getValue(keyedSchemaUri);
            if (key != null) {
                shard.put(key, heapId);
            }
        }

        return model;
    }

    @Override
    protected void initFromConfiguration(final ServiceConfiguration config) {

    }

    private UUID getCachedHeapId(final Keys keys) {

        final Set<URI> keyedSchemaUris = keys.getKeyedSchemaUris();
        for (final URI keyedSchemaUri : keyedSchemaUris) {
            if (keyedSchemaUri != null && _Shards.containsKey(keyedSchemaUri)) {
                final ConcurrentHashMap<Object, UUID> shard = _Shards.get(keyedSchemaUri);
                final Object key = keys.getValue(keyedSchemaUri);
                if (key != null && shard.containsKey(key)) {
                    final UUID heapId = shard.get(key);
                    return heapId;
                }
            }
        }

        return null;
    }
}
