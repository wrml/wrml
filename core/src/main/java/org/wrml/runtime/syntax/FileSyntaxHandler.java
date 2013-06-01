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
package org.wrml.runtime.syntax;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSyntaxHandler extends SyntaxHandler<File>
{

    @Override
    public String formatSyntaxValue(final File file)
    {
        if (file == null)
        {
            return null;
        }

        return String.valueOf(file);
    }

    @Override
    public File parseSyntacticText(final String filePath)
    {
        if (filePath == null || filePath.isEmpty())
        {
            return null;
        }

        try
        {
            final Path path = Paths.get(filePath);
            final File file = path.toFile();
            if (file.exists())
            {
                return path.toRealPath().toFile();
            }
            else
            {
                return file;
            }
        }
        catch (final IOException e)
        {
            throw new SyntaxHandlerException(e.getLocalizedMessage(), e, this);
        }
    }

}
