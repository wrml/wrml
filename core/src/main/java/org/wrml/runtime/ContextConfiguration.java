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

import org.wrml.runtime.format.FormatLoaderConfiguration;
import org.wrml.runtime.rest.ApiLoaderConfiguration;
import org.wrml.runtime.schema.SchemaLoaderConfiguration;
import org.wrml.runtime.service.ServiceLoaderConfiguration;
import org.wrml.runtime.service.cache.ModelCacheConfiguration;
import org.wrml.runtime.syntax.SyntaxLoaderConfiguration;

/**
 * The WRML runtime context configuration.
 */
public class ContextConfiguration extends DefaultFactoryConfiguration {

    private ApiLoaderConfiguration _ApiLoaderConfiguration;

    private FormatLoaderConfiguration _FormatLoaderConfiguration;

    private ModelBuilderConfiguration _ModelBuilderConfiguration;

    private ModelCacheConfiguration _ModelCacheConfiguration;

    private SchemaLoaderConfiguration _SchemaLoaderConfiguration;

    private ServiceLoaderConfiguration _ServiceLoaderConfiguration;

    private SyntaxLoaderConfiguration _SyntaxLoaderConfiguration;


    public ContextConfiguration() {

    }

    public ApiLoaderConfiguration getApiLoader() {

        return _ApiLoaderConfiguration;
    }

    public void setApiLoader(final ApiLoaderConfiguration apiLoaderConfiguration) {

        _ApiLoaderConfiguration = apiLoaderConfiguration;
    }

    public FormatLoaderConfiguration getFormatLoader() {

        return _FormatLoaderConfiguration;
    }

    public void setFormatLoader(final FormatLoaderConfiguration formatLoaderConfiguration) {

        _FormatLoaderConfiguration = formatLoaderConfiguration;
    }

    public ModelBuilderConfiguration getModelBuilder() {

        return _ModelBuilderConfiguration;
    }

    public void setModelBuilder(final ModelBuilderConfiguration modelBuilderConfiguration) {

        _ModelBuilderConfiguration = modelBuilderConfiguration;
    }

    public ModelCacheConfiguration getModelCache() {

        return _ModelCacheConfiguration;
    }

    public void setModelCache(final ModelCacheConfiguration modelCacheConfiguration) {

        _ModelCacheConfiguration = modelCacheConfiguration;
    }

    public SchemaLoaderConfiguration getSchemaLoader() {

        return _SchemaLoaderConfiguration;
    }

    public void setSchemaLoader(final SchemaLoaderConfiguration schemaLoaderConfiguration) {

        _SchemaLoaderConfiguration = schemaLoaderConfiguration;
    }

    public ServiceLoaderConfiguration getServiceLoader() {

        return _ServiceLoaderConfiguration;
    }

    public void setServiceLoader(final ServiceLoaderConfiguration serviceLoaderConfiguration) {

        _ServiceLoaderConfiguration = serviceLoaderConfiguration;
    }

    public SyntaxLoaderConfiguration getSyntaxLoader() {

        return _SyntaxLoaderConfiguration;
    }

    public void setSyntaxLoader(final SyntaxLoaderConfiguration syntaxLoaderConfiguration) {

        _SyntaxLoaderConfiguration = syntaxLoaderConfiguration;
    }


}