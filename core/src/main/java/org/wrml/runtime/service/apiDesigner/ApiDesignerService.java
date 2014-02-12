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

import org.wrml.model.Model;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.LinkRelation;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.rest.ApiBuilder;
import org.wrml.runtime.rest.SystemLinkRelation;
import org.wrml.runtime.service.AbstractService;
import org.wrml.runtime.service.ServiceConfiguration;
import org.wrml.runtime.service.ServiceException;

import java.net.URI;
import java.util.Map;

/**
 * A service supporting the REST API designer tool/app.
 */
public class ApiDesignerService extends AbstractService {
    @Override
    protected void initFromConfiguration(final ServiceConfiguration config) {

        // TODO: Replace this with an API loaded via config
        final Map<String, String> settings = config.getSettings();
        final String apiUriString = settings.get("apiUri");
        final URI apiUri = URI.create(apiUriString);

        final Context context = getContext();
        ApiBuilder apiBuilder = new ApiBuilder(context);
        apiBuilder.uri(apiUri);
        apiBuilder.title("WRML Design REST API");
        apiBuilder.description("REST API supporting WRML design applications.");


        apiBuilder.resource("/");
        apiBuilder.resource("/functions/autoLink");
        apiBuilder.resource("/relations/{uniqueName}");

        apiBuilder.link("/", SystemLinkRelation.self.getUri(), "/", ApiDesignerHome.class);
        apiBuilder.link("/functions/autoLink", SystemLinkRelation.self.getUri(), "/functions/autoLink", AutoLinkFunction.class);
        apiBuilder.link("/relations/{uniqueName}", SystemLinkRelation.self.getUri(), "/relations/{uniqueName}", LinkRelation.class);

        // TODO: Once this API is file/resource based, change this link relation URI to be based on the apiUri + /relations/{uniqueName} URI
        // TODO: Remove "autoLink" from the SystemLinkRelation enum
        apiBuilder.link("/", SystemLinkRelation.autoLink.getUri(), "/functions/autoLink", AutoLinkFunction.class);
        apiBuilder.link("/functions/autoLink", SystemLinkRelation.invoke.getUri(), "/functions/autoLink", Api.class, Api.class);
        apiBuilder.link("/functions/autoLink", SystemLinkRelation.home.getUri(), "/", ApiDesignerHome.class);
        apiBuilder.load();
    }

    @Override
    public Model get(final Keys keys, final Dimensions dimensions) throws UnsupportedOperationException {

        // TODO: Should this service extend the mongo or file service?
        // TODO: Instead of having this Service dedicated to invoke, should it be replaced by a "controller" that can be plugged into some other service(s) that are dedicated to CRUD?

        throwUnsupportedOperationException("get");

        // Never reached
        return null;
    }

    @Override
    public Model invoke(final Model function, final Dimensions responseDimensions, final Model parameter) {

        // TODO: Should this "invocation handler" be implemented via composition instead of having this Service dedicated to invoke and other service(s) dedicated to CRUD?

        if (function instanceof AutoLinkFunction) {
            if (!(parameter instanceof Api)) {
                throw new ServiceException("The function: " + function.getSchemaUri() + " expects a parameter of type: " + Api.class + ", instead of: " + parameter, null, this);
            }

            final Api apiParameter = (Api) parameter;
            final ApiBuilder apiBuilder = new ApiBuilder(apiParameter);
            apiBuilder.autoLink();
            final Api apiResponse = apiBuilder.toApi();
            return apiResponse;

        }

        return null;
    }
}
