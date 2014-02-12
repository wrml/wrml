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

import org.wrml.model.schema.Schema;

/**
 * <p>
 * {@link Model} instances cannot be formed from a {@link Schema} that claims {@link Abstract} as a direct basis. Other
 * {@link Schema} may claim {@link Abstract} as their basis and they may formed into {@link Model}s as usual; unless
 * they choose to also directly state {@link Abstract} as a basis.
 * </p>
 * <p/>
 * <p>
 * Claim {@link Abstract} as a basis for any {@link Schema} that you do not wish to be modeled/composed without some
 * intermediate extension.
 * </p>
 *
 * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-8.html#jls-8.1.1.1">Abstract classes</a>
 */
public interface Abstract extends Model {

}
