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

import java.net.URI;

import org.wrml.model.schema.Constrainable;
import org.wrml.model.schema.Constraint;
import org.wrml.model.schema.ConstraintResult;
import org.wrml.runtime.Context;

public class ConstraintService extends ProxyService<URI, CollectionService> {

    @Override
    protected void initDelegateService(Context context) {
        // TODO:  setDelegateService(delegateService);

    }

    ConstraintResult enforce(final Constraint constraint, final Constrainable onConstrainable) {
        /*
         * TODO: Use the CodeOnDemand execution engine (design one of those
         * first) to execute the constraint's code (in the execution engine
         * context's secure sandbox).
         */
        return null;
    }
}
