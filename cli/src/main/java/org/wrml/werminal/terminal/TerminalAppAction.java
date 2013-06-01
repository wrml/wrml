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
package org.wrml.werminal.terminal;

import com.googlecode.lanterna.gui.Action;
import org.wrml.runtime.Context;

public abstract class TerminalAppAction implements Action
{

    private final TerminalApp _App;

    private String _Title;

    public TerminalAppAction(final TerminalApp app, final String title)
    {

        _App = app;
        _Title = title;
    }

    @SuppressWarnings("unchecked")
    public final <T extends TerminalApp> T getApp()
    {

        return (T) _App;
    }

    public final Context getContext()
    {

        return getApp().getContext();
    }

    public final String getTitle()
    {

        return _Title;
    }

    public final void setTitle(final String title)
    {

        _Title = title;
    }

}
