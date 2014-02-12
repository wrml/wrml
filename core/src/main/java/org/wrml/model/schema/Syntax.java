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

import org.wrml.model.UniquelyNamed;
import org.wrml.model.rest.Document;
import org.wrml.runtime.schema.WRML;
import org.wrml.runtime.syntax.SyntaxHandler;

/**
 * <p>
 * A document that defines a (text) syntax. A syntax is some domain-specific text ({@link String}) format that means something special to the person or machine interested in parsing some conforming text.
 * </p>
 * <p/>
 * <p>
 * From: http://en.wikipedia.org/wiki/Syntax
 * </p>
 * <p/>
 * <p>
 * In linguistics, syntax is "the study of the principles and processes by which sentences are constructed in particular languages".
 * </p>
 *
 * @see SyntaxHandler
 * @see TextValue#getSyntaxUri()
 * @see Slot
 * @see Schema
 */
@WRML(keySlotNames = {"uniqueName"})
public interface Syntax extends UniquelyNamed, Document {

}
