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

import java.io.IOException;
import java.net.URI;

public class HelpGuideAction extends WerminalAction
{

    private final URI GUIDE_URI = URI.create("http://www.wrml.org/werminal/WerminalMastersHandbook.pdf");

    public HelpGuideAction(final Werminal werminal)
    {

        super(werminal, "Help Guide");
    }

    @Override
    public void doAction()
    {

        final Werminal werminal = getWerminal();

        if (!java.awt.Desktop.isDesktopSupported())
        {
            werminal.showSplashWindow();
            return;
        }

        final java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE))
        {
            werminal.showSplashWindow();
            return;
        }

        try
        {
            desktop.browse(GUIDE_URI);
        }
        catch (final IOException e)
        {
            System.err.println(e.getMessage());
            werminal.showSplashWindow();
            return;
        }
    }

}
