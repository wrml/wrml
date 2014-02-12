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
package org.wrml.runtime.schema;

import org.wrml.model.schema.Slot;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * <p>
 * Indicates that the annotated accessor {@link Method} "slot" has one or more aliases, which are {@link Slot}s named by
 * this annotation's value.  A slot with aliases still has only one single value "bucket" but it can have other slot names associated with this same value.
 * </p>
 * <p/>
 * <p>
 * The WRML runtime will map/route access to all alias slots to the annotated "real" identity of the slot.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Aliases {

    /**
     * The set of aliases, or alternative names, for the annotated slot.
     *
     * @return The set of aliases, or alternative names, for the annotated slot.
     */
    String[] value();
}
