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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import org.wrml.runtime.JavaBean;
import org.wrml.runtime.JavaMethod;
import org.wrml.runtime.JavaMethod.Signature;

/**
 * Handles the execution of any "non-Field-accessor" methods associated with
 * Schemas serviced by this invocation handler's delegate. In other words, calls
 * to any model (Proxy) methods that don't start with either "get", "set", or
 * "is" may be routed to this class where the method call is "augmented" by
 * adding the model as the first parameter and then "proxied" to our Service
 * delegate.
 */
public class ServiceInvocationHandler implements InvocationHandler {

    private final Service<?> _Service;
    private final JavaBean _ServiceBean;

    public ServiceInvocationHandler(Service<?> service) {
        _Service = service;
        _ServiceBean = new JavaBean(service.getClass(), Object.class);
    }

    public Service<?> getService() {
        return _Service;
    }

    public JavaBean getServiceBean() {
        return _ServiceBean;
    }

    @Override
    public Object invoke(final Object model, final Method modelMethod, final Object[] args) throws Throwable {

        final String sharedMethodName = modelMethod.getName();
        final int modelMethodArgCount = (args != null) ? args.length : 0;
        final int serviceMethodArgCount = modelMethodArgCount + 1;
        final Signature signature = new Signature(modelMethod);

        final Map<String, Set<JavaMethod>> serviceMethods = _ServiceBean.getOtherMethods();
        if (!serviceMethods.containsKey(sharedMethodName)) {
            throwMethodNotFound(model, signature);
        }

        final Set<JavaMethod> serviceMethodSet = serviceMethods.get(sharedMethodName);
        if (serviceMethodSet == null) {
            throwMethodNotFound(model, signature);
        }

        JavaMethod mappedServiceMethod = null;

        Object[] serviceMethodArgs = new Object[] { model };
        if (serviceMethodArgCount > 1) {
            serviceMethodArgs = ArrayUtils.addAll(serviceMethodArgs, args);
        }

        outer:
        for (final JavaMethod serviceMethod : serviceMethodSet) {

            if (serviceMethod.getParameterCount() == serviceMethodArgCount) {
                // The # of slots matches the # of objects
                final Signature serviceMethodSignature = serviceMethod.getSignature();
                final Type[] parameterTypes = serviceMethodSignature.getParameterTypes();
                if ((parameterTypes != null) && (parameterTypes.length > 0)) {
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (!TypeUtils.isInstance(serviceMethodArgs[i], parameterTypes[i])) {
                            continue outer;
                        }
                    }
                }

                mappedServiceMethod = serviceMethod;
                break outer;
            }
        }

        if (mappedServiceMethod == null) {
            throwMethodNotFound(model, signature);
        }

        final Service<?> service = getService();
        return mappedServiceMethod.getMethod().invoke(service, serviceMethodArgs);
    }

    private void throwMethodNotFound(Object model, Signature signature) throws ServiceException {
        throw new ServiceException("The model (" + model + ") wants a method  (" + signature
                + ") to be implemented by this service (" + getService() + ").", null, getService());

    }

}