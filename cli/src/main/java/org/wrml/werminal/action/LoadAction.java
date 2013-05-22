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
package org.wrml.werminal.action;

import org.wrml.model.Model;
import org.wrml.model.rest.Api;
import org.wrml.model.rest.Document;
import org.wrml.model.rest.LinkRelation;
import org.wrml.model.schema.Schema;
import org.wrml.runtime.Context;
import org.wrml.runtime.rest.ApiLoader;
import org.wrml.runtime.rest.SystemApi;
import org.wrml.runtime.rest.SystemLinkRelation;
import org.wrml.runtime.schema.SchemaLoader;
import org.wrml.werminal.Werminal;
import org.wrml.werminal.window.ModelWindow;

import java.net.URI;

public class LoadAction extends WerminalAction
{

    /**
     * <p>
     * Returns true if the specified {@link Model} is <i>loadable</i> by the WRML runtime. <i>Loadability</i> only
     * applies to built-in, system {@link Model}s such as {@link Api}, {@link LinkRelation}s, and {@link Schema}.
     * </p>
     * <p>
     * Loading these {@link Model}s at runtime enables a WRML runtime system to change and evolve dynamically (without
     * restart).
     * </p>
     */
    public final static boolean appliesTo(final Model model)
    {

        if (!(model instanceof Document))
        {
            return false;
        }

        final URI uri = ((Document) model).getUri();
        if (uri == null)
        {
            return false;
        }

        if (model instanceof Api)
        {

            for (final SystemApi systemApi : SystemApi.values())
            {
                if (systemApi.getUri().equals(uri))
                {
                    return false;
                }
            }

            return true;

        }
        else if (model instanceof LinkRelation)
        {

            for (final SystemLinkRelation systemLinkRelation : SystemLinkRelation.values())
            {
                if (systemLinkRelation.getUri().equals(uri))
                {
                    return false;
                }
            }

            return true;
        }
        else if (model instanceof Schema)
        {
            // If it got this far, the Schema is already "loaded" but it may not be class generated, loaded, or
            // (finally) prototyped by the runtime. Check if we already have a Prototype associated with the Schema,
            // if so there is no more "loading" to be done with the Schema.
            final SchemaLoader schemaLoader = model.getContext().getSchemaLoader();
            return !schemaLoader.isPrototyped(uri);
        }

        return false;
    }

    public LoadAction(final Werminal werminal)
    {

        super(werminal, "Load");
    }

    @Override
    public void doAction()
    {

        final Werminal werminal = getWerminal();
        final ModelWindow modelWindow = (ModelWindow) werminal.getTopWindow();
        final Model model = modelWindow.syncModel();

        if (model == null)
        {
            werminal.showError("The model is null.");
        }

        final Context context = werminal.getContext();
        final ApiLoader apiLoader = context.getApiLoader();

        if (!LoadAction.appliesTo(model))
        {
            werminal.showError("The model (schema: " + model.getSchemaUri() + ") is not loadable.");
        }

        final URI uri = ((Document) model).getUri();
        if (uri == null)
        {
            return;
        }

        if (model instanceof Api)
        {
            final Api api = (Api) model;
            apiLoader.loadApi(api);
        }
        else if (model instanceof LinkRelation)
        {
            final LinkRelation linkRelation = (LinkRelation) model;
            apiLoader.loadLinkRelation(linkRelation);
        }
        else if (model instanceof Schema)
        {
            final Schema schema = (Schema) model;
            final SchemaLoader schemaLoader = context.getSchemaLoader();
            schemaLoader.load(schema);
            schemaLoader.getPrototype(uri);
        }

        modelWindow.updateLoadButton();

        werminal.showMessageBox(getTitle() + " Complete", "\n\nThe load operation completed successfully.\n");
    }

}
