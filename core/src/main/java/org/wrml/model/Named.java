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
package org.wrml.model;

import org.wrml.runtime.schema.Description;
import org.wrml.runtime.schema.WRML;

/**
 * Something with a name. A Named <i>mixin</i> model manages the state of a "name" text slot.
 */
@WRML(comparableSlotNames = {"name"})
@Description("Something with a name.")
public interface Named extends Abstract
{

    /**
     * The WRML constant name for a Named's <i>name</i> slot.
     */
    public static final String SLOT_NAME_NAME = "name";

    /**
     * The name associated with this {@link Named} model.
     */
    @Description("The name associated with this model.")
    String getName();

    String setName(String name);
}
