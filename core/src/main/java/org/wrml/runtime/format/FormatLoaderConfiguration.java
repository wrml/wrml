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
package org.wrml.runtime.format;


import org.wrml.runtime.DefaultFactoryConfiguration;

import java.net.URI;

/**
 * Configuration for the {@link FormatLoader} component.
 */
public final class FormatLoaderConfiguration extends DefaultFactoryConfiguration {

    private FormatterConfiguration[] _FormatterConfigurations;

    private URI _DefaultFormatUri;


    public FormatLoaderConfiguration() {

    }

    public URI getDefaultFormatUri() {

        return _DefaultFormatUri;
    }

    public void setDefaultFormatUri(final URI defaultFormatUri) {

        _DefaultFormatUri = defaultFormatUri;
    }


    public FormatterConfiguration[] getFormatters() {

        return _FormatterConfigurations;
    }

    public void setFormatters(final FormatterConfiguration[] formatterConfigurations) {

        _FormatterConfigurations = formatterConfigurations;
    }

}
