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

package org.wrml.service;

import java.lang.reflect.Method;
import java.util.List;

import org.wrml.event.EventManager;
import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;

public abstract class ProxyService<K, S extends Service<K>> implements DelegatingService<K, S> {

    private S _DelegateService;
    private final EventManager<ServiceEventListener> _EventManager;
    private ServiceInvocationHandler _ServiceInvocationHandler;

    /**
     * Creates a new {@link ProxyService}.
     */
    public ProxyService() {
        _EventManager = new EventManager<ServiceEventListener>(ServiceEventListener.class);
    }

    @Override
    public boolean addEventListener(ServiceEventListener eventListener) {
        return _EventManager.addEventListener(eventListener);
    }

    @Override
    public void delete(K key) {
        getDelegate().delete(key);
    }

    @Override
    public <M extends Model> M get(K key, Dimensions dimensions) {
        return getDelegate().get(key, dimensions);
    }

    @Override
    public S getDelegate() {
        return _DelegateService;
    }

    @Override
    public <M extends Model> List<M> getMultiple(List<K> keys, Dimensions dimensions) {
        return getDelegate().getMultiple(keys, dimensions);
    }

    @Override
    public Object invoke(Object model, Method method, Object[] args) throws Throwable {

        if (_ServiceInvocationHandler == null) {
            _ServiceInvocationHandler = new ServiceInvocationHandler(this);
        }

        return _ServiceInvocationHandler.invoke(model, method, args);
    }

    @Override
    public boolean isAvailable() {
        return getDelegate().isAvailable();
    }

    @Override
    public boolean removeEventListener(ServiceEventListener eventListener) {
        return _EventManager.removeEventListener(eventListener);
    }

    @Override
    public void restart(Context context) {
        getDelegate().start(context);
        getDelegate().stop(context);
    }

    @Override
    public void start(Context context) {
        initDelegateService(context);
        getDelegate().start(context);
    }

    @Override
    public void stop(Context context) {
        getDelegate().stop(context);
    }

    @Override
    public <M extends Model> M update(M model) {
        return getDelegate().update(model);
    }

    protected abstract void initDelegateService(Context context);

    protected void setDelegateService(S delegateService) {
        if (_DelegateService != null) {
            throw new ServiceException("The delegate service has already been initialized.", null, this);
        }
        _DelegateService = delegateService;
    }
}
