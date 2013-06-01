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
package org.wrml.model.schema;

import org.wrml.model.Described;
import org.wrml.model.UniquelyNamed;
import org.wrml.model.Versioned;
import org.wrml.model.rest.Document;
import org.wrml.runtime.schema.WRML;

import java.util.List;

/**
 * A menu of constant text-based choices. From a pure model perspective, a {@link Choices} is conceptually akin to an
 * enum's possible values.
 */
@WRML(keySlotNames = {"uniqueName"})
public interface Choices extends Described, Versioned, UniquelyNamed, Document
{

    /**
     * The list of {@link String} values/options associated with this {@link Choices}.
     */
    List<String> getList();

}
