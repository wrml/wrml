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

import java.lang.annotation.*;

/**
 * <p>
 * Used by the WRML runtime to annotate Schema "slots" (getter methods) with a default value that is available at
 * runtime.
 * </p>
 * <p>
 * Note that the runtime will convert this value from a {@link String} to the slot's value type as needed.
 * </p>
 *
 * @see org.wrml.runtime.syntax.SyntaxLoader#parseSyntacticText(String, java.lang.reflect.Type)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DefaultValue {
    /**
     * The default value (represented as a String) for the annotated slot.
     *
     * @return The default value (represented as a String) for the annotated slot.
     */
    String value();
}
