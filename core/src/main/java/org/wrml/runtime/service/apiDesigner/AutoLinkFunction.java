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
package org.wrml.runtime.service.apiDesigner;

import org.wrml.model.Virtual;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.Method;
import org.wrml.runtime.schema.LinkSlot;
import org.wrml.runtime.schema.WRML;

/**
 * // TODO: Move this schema to a resource file.
 * <p>
 * A function (aka "Controller") dedicated to auto-linking a REST API ({@link org.wrml.model.rest.Api}) by creating the (default)
 * {@link org.wrml.model.rest.LinkTemplate}s based on the resources and schemas present in the parameter API's design.
 * </p>
 */
@WRML(uniqueName = "org/wrml/runtime/service/apiDesigner/AutoLinkFunction")
public interface AutoLinkFunction extends Virtual, Document {

    @LinkSlot(linkRelationUri = "http://relation.api.wrml.org/org/wrml/relation/invoke", method = Method.Invoke)
    Api autoLink(Api parameter);

    @LinkSlot(linkRelationUri = "http://relation.api.wrml.org/org/wrml/relation/home", method = Method.Get)
    ApiDesignerHome getHome();


}
