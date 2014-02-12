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

import org.wrml.model.Model;
import org.wrml.runtime.schema.Prototype;

import java.net.URI;
import java.util.Comparator;
import java.util.Set;

public final class ModelComparator implements Comparator<Model> {

    private final Context _Context;

    ModelComparator(final Context context) {

        _Context = context;
    }

    @Override
    public int compare(final Model model1, final Model model2) {

        return compare(model1, model2, null);
    }

    public int compare(final Model model1, final Model model2, Set<String> slotNames) {

        if (model1 == model2) {
            return 0;
        }

        if (model1 == null) {
            return -1;
        }

        if (model2 == null) {
            return 1;
        }

        final URI schemaUri1 = model1.getSchemaUri();
        final URI schemaUri2 = model2.getSchemaUri();

        if (schemaUri1 == null && schemaUri2 == null) {
            return compareBasics(model1, model2, slotNames);
        }
        else if (schemaUri1 == null) {
            // An undefined 1st model is less than a defined 2nd one
            return -1;
        }
        else if (schemaUri2 == null) {
            // An defined 1st model is greater than an undefined 2nd one
            return 1;
        }

        if (slotNames == null) {
            if (schemaUri1.equals(schemaUri2)) {
                final Prototype prototype = model1.getPrototype();
                slotNames = prototype.getComparableSlotNames();
            }
            else {
                return schemaUri1.compareTo(schemaUri2);
            }
        }

        if (slotNames == null || slotNames.size() == 0) {
            return compareBasics(model1, model2, slotNames);
        }

        /*
         * for (final String slotName : slotNames)
         * {
         * // TODO: Finish this
         * }
         */

        return 0;
    }

    public Context getContext() {

        return _Context;
    }

    private int compareBasics(final Model model1, final Model model2, final Set<String> slotNames) {

        // TODO: Default sort to heap id + create time (long value)
        // TODO: Need an invoke-based implementation to handle the
        // schema-specific sorting hints
        return 0;
    }
}
