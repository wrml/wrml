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

import java.net.URI;
import java.util.Set;

public class ModelWriteOptions
{

    private boolean _PrettyPrint;

    private Set<URI> _ExcludedSchemaUris;

    private boolean _DocumentKeyExcludedIfSecondary;

    private boolean _EmbeddedDocumentUriExcluded;

    private boolean _LinksExcluded;

    private boolean _CollectionsExcluded;


    public ModelWriteOptions()
    {
        _PrettyPrint = false;
    }

    public Set<URI> getExcludedSchemaUris()
    {
        return _ExcludedSchemaUris;
    }

    public boolean isPrettyPrint()
    {
        return _PrettyPrint;
    }

    public void setExcludedSchemaUris(final Set<URI> excludedSchemaUris)
    {
        _ExcludedSchemaUris = excludedSchemaUris;
    }

    public void setPrettyPrint(final boolean prettyPrint)
    {
        _PrettyPrint = prettyPrint;
    }

    public boolean isCollectionsExcluded()
    {

        return _CollectionsExcluded;
    }

    public void setCollectionsExcluded(final boolean collectionsExcluded)
    {

        _CollectionsExcluded = collectionsExcluded;
    }

    public boolean isDocumentKeyExcludedIfSecondary()
    {

        return _DocumentKeyExcludedIfSecondary;
    }

    public void setDocumentKeyExcludedIfSecondary(final boolean documentKeyExcludedIfSecondary)
    {

        _DocumentKeyExcludedIfSecondary = documentKeyExcludedIfSecondary;
    }

    public boolean isEmbeddedDocumentUriExcluded()
    {

        return _EmbeddedDocumentUriExcluded;
    }

    public void setEmbeddedDocumentUriExcluded(final boolean embeddedDocumentUriExcluded)
    {

        _EmbeddedDocumentUriExcluded = embeddedDocumentUriExcluded;
    }

    public boolean isLinksExcluded()
    {

        return _LinksExcluded;
    }

    public void setLinksExcluded(final boolean linksExcluded)
    {

        _LinksExcluded = linksExcluded;
    }
}
