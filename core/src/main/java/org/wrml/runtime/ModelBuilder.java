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

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

public interface ModelBuilder extends ContextSensitive {
    <M extends Model> M copyModel(Model model);

    Dimensions newDimensions(final Class<?> schemaInterface) throws ModelBuilderException;

    Dimensions newDimensions(final String schemaInterfaceName) throws ModelBuilderException;

    Dimensions newDimensions(final URI schemaUri) throws ModelBuilderException;

    Model newModel() throws ModelBuilderException;

    <M extends Model> M newModel(final Class<?> schemaInterface) throws ModelBuilderException;

    <M extends Model> M newModel(final Dimensions dimensions) throws ModelBuilderException;

    <M extends Model> M newModel(final Dimensions dimensions, final ConcurrentHashMap<String, Object> slots)
            throws ModelBuilderException;

    <M extends Model> M newModel(final String schemaInterfaceName) throws ModelBuilderException;

    <M extends Model> M newModel(final URI schemaUri) throws ModelBuilderException;
}
