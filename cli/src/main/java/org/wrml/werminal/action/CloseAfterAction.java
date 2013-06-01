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
package org.wrml.werminal.action;

import org.wrml.werminal.Werminal;

public class CloseAfterAction extends DelegatingPhasedWerminalAction
{

    public CloseAfterAction(final Werminal werminal)
    {

        super(werminal, "Close");
    }

    public CloseAfterAction(final Werminal werminal, final String title)
    {

        super(werminal, title);
    }

    public CloseAfterAction(final Werminal werminal, final String title, final WerminalAction delegate)
    {

        super(werminal, title, delegate);
    }

    public CloseAfterAction(final Werminal werminal, final WerminalAction delegate)
    {

        super(werminal, delegate);
    }

    @Override
    protected void postDoIt()
    {

        getWerminal().closeTopWindow();
    }

}
