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
 * Copyright (C) 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org) 
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
import org.wrml.model.Virtual;
import org.wrml.model.rest.Document;
import org.wrml.runtime.schema.CollectionSlot;
import org.wrml.runtime.schema.CollectionSlotCriterion;
import org.wrml.runtime.schema.WRML;

import java.util.List;

/**
 * <p>
 * A hierarchical <i>container</i> of {@link Schema}s and sub-{@link SchemaNamespace}s.
 * </p>
 * <p>
 * Conceptually, a {@link SchemaNamespace} is to a {@link Schema} as a {@link Package} is to a {@link Class}.
 * </p>
 *
 * @see org.wrml.util.UniqueName
 */
@WRML(keySlotNames = {"uniqueName"})
public interface SchemaNamespace extends UniquelyNamed, Virtual, Document
{

    /**
     * The {@link SchemaNamespace}s contained (hierarchically) directly within this {@link SchemaNamespace}.
     *
     * @return The {@link SchemaNamespace}s contained (hierarchically) directly within this {@link SchemaNamespace}.
     */
    @CollectionSlot(
            linkRelationUri = "http://relation.api.wrml.org/org/wrml/relation/child",
            and = {
                    @CollectionSlotCriterion(
                            referenceSlot = "uniqueName",
                            operator = ComparisonOperator.regex,
                            valueSource = "uniqueName",

                            // TODO: Need a regex that includes the valueSource as a dynamic variable within a regex pattern
                            // that matches uniqueNames that start with this SchemaNamespace's uniqueName and add a "/Something"
                            // (only one more / followed by an alpha numeric that starts with alpha).
                            regex = "TODO"

                    )
            }
    )
    List<SchemaNamespace> getSchemaNamespaces();

    /**
     * The {@link Schema}s contained (hierarchically) directly within this {@link SchemaNamespace}.
     *
     * @return The {@link Schema}s contained (hierarchically) directly within this {@link SchemaNamespace}.
     */
    @CollectionSlot(
            linkRelationUri = "http://relation.api.wrml.org/org/wrml/relation/element",
            and = {
                    @CollectionSlotCriterion(
                            referenceSlot = "uniqueName",
                            operator = ComparisonOperator.regex,
                            valueSource = "uniqueName",

                            // TODO: Need a regex that includes the valueSource as a dynamic variable within a regex pattern
                            // that matches uniqueNames that start with this SchemaNamespace's uniqueName and add a "/Something"
                            // (only one more / followed by an alpha numeric that starts with alpha).
                            regex = "TODO"

                    )
            }
    )
    List<Schema> getSchemas();


}
