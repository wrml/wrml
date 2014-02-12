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

import org.wrml.runtime.Context;

import java.net.URI;

public abstract class AbstractFormatter implements Formatter {


    private Context _Context;

    private URI _FormatUri;

    private FormatterConfiguration _Config;

    @Override
    public final void init(final Context context, FormatterConfiguration config) {


        if (context == null) {
            throw new NullPointerException("The context cannot be null.");
        }

        _Context = context;

        if (context == null) {
            throw new NullPointerException("The config cannot be null.");
        }

        _Config = config;
        _FormatUri = _Config.getFormatUri();
        initFromConfiguration(_Config);
    }

    @Override
    public final Context getContext() {

        return _Context;
    }

    @Override
    public final FormatterConfiguration getConfig() {

        return _Config;
    }

    @Override
    public final URI getFormatUri() {

        return _FormatUri;
    }

    @Override
    public boolean isApplicableTo(final URI schemaUri) {

        return true;
    }

    @Override
    public String toString() {

        return getClass().getSimpleName() + " [Format = " + _FormatUri + "]";
    }

    protected void initFromConfiguration(final FormatterConfiguration config) {
        // Do nothing by default
    }
}
