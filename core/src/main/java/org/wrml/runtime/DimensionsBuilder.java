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

import com.rits.cloning.Cloner;
import org.wrml.model.Model;
import org.wrml.model.rest.Document;
import org.wrml.model.schema.Schema;
import org.wrml.util.AsciiArt;

import java.net.URI;
import java.util.*;

/**
 * A simple builder for immutable {@link Dimensions}.
 */
public final class DimensionsBuilder
{

    private final MutableDimensions _Dimensions;

    /**
     * Default constructor creates initially "empty" Dimensions.
     */
    public DimensionsBuilder()
    {

        _Dimensions = new MutableDimensions();
    }

    /**
     * Creates the {@link DimensionsBuilder} based upon the initial {@link Dimensions}.
     */
    public DimensionsBuilder(final Dimensions dimensions)
    {

        _Dimensions = ((MutableDimensions) dimensions).clone();
    }

    /**
     * Creates the {@link DimensionsBuilder} with the initial dimensioned schema.
     */
    public DimensionsBuilder(final URI schemaUri)
    {

        this();
        _Dimensions.setSchemaUri(schemaUri);

    }

    public SortedMap<String, String> getMetadata()
    {

        return _Dimensions.getMetadata();
    }

    public SortedMap<String, String> getQueryParameters()
    {

        return _Dimensions.getQueryParameters();
    }

    public List<String> getEmbeddedLinkSlotNames()
    {

        return _Dimensions.getEmbeddedLinkSlotNames();
    }

    public List<String> getExcludedSlotNames()
    {

        return _Dimensions.getExcludedSlotNames();
    }

    public List<String> getIncludedSlotNames()
    {

        return _Dimensions.getIncludedSlotNames();
    }

    public Locale getLocale()
    {

        return _Dimensions.getLocale();
    }

    /**
     * @see Dimensions#getLocale()
     */
    public DimensionsBuilder setLocale(final Locale locale)
    {

        _Dimensions.setLocale(locale);
        return this;
    }

    public URI getReferrerUri()
    {

        return _Dimensions.getReferrerUri();
    }

    /**
     * @see Dimensions#getReferrerUri()
     */
    public DimensionsBuilder setReferrerUri(final URI referrerUri)
    {

        _Dimensions.setReferrerUri(referrerUri);
        return this;
    }

    public URI getSchemaUri()
    {

        return _Dimensions.getSchemaUri();
    }

    /**
     * <p>
     * The <b>required</b> {@link URI} id ({@link Document#getUri()}) associated with the {@link Schema} that describes
     * the structure of the {@link Model} to be retrieved with these {@link Dimensions}.
     * </p>
     *
     * @see Dimensions#getSchemaUri()
     */
    public DimensionsBuilder setSchemaUri(final URI schemaUri)
    {

        _Dimensions.setSchemaUri(schemaUri);
        return this;
    }

    /**
     * Return the build {@link Dimensions}.
     */
    public Dimensions toDimensions()
    {

        return _Dimensions;
    }

    @Override
    public String toString()
    {

        return getClass().getSimpleName() + " { dimensions : " + _Dimensions + "}";
    }

    @Override
    public boolean equals(final Object o)
    {

        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final DimensionsBuilder that = (DimensionsBuilder) o;

        if (!_Dimensions.equals(that._Dimensions))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {

        return _Dimensions.hashCode();
    }

    /**
     * Internal, mutable implementation of {@link Dimensions}.
     */
    private static final class MutableDimensions implements Cloneable, Dimensions
    {

        private static final Cloner CLONER = new Cloner();

        private static final long serialVersionUID = 1L;

        private final SortedMap<String, String> _Metadata;

        private final List<String> _EmbeddedLinkSlotNames;

        private final List<String> _ExcludedSlotNames;

        private final List<String> _IncludedSlotNames;

        private final SortedMap<String, String> _QueryParameters;

        private URI _SchemaUri;

        private URI _ReferrerUri;

        private Locale _Locale;

        MutableDimensions()
        {

            _Metadata = new TreeMap<>();
            _QueryParameters = new TreeMap<>();
            _EmbeddedLinkSlotNames = new LinkedList<>();
            _ExcludedSlotNames = new LinkedList<>();
            _IncludedSlotNames = new LinkedList<>();

        }

        @Override
        public MutableDimensions clone()
        {

            return CLONER.deepClone(this);
        }

        @Override
        public SortedMap<String, String> getMetadata()
        {

            return _Metadata;
        }

        @Override
        public List<String> getEmbeddedLinkSlotNames()
        {

            return _EmbeddedLinkSlotNames;

        }

        @Override
        public List<String> getExcludedSlotNames()
        {

            return _ExcludedSlotNames;
        }

        @Override
        public List<String> getIncludedSlotNames()
        {

            return _IncludedSlotNames;
        }

        @Override
        public Locale getLocale()
        {

            return _Locale;
        }

        /**
         * @see #getLocale()
         */
        void setLocale(final Locale locale)
        {

            _Locale = locale;
        }

        @Override
        public SortedMap<String, String> getQueryParameters()
        {

            return _QueryParameters;
        }

        @Override
        public URI getReferrerUri()
        {

            return _ReferrerUri;
        }

        /**
         * @see #getReferrerUri()
         */
        void setReferrerUri(final URI referrerDocumentUri)
        {

            _ReferrerUri = referrerDocumentUri;
        }

        @Override
        public URI getSchemaUri()
        {

            return _SchemaUri;
        }

        /**
         * <p>
         * The <b>required</b> {@link URI} id ({@link Document#getUri()}) associated with the {@link Schema} that
         * describes the structure of the {@link Model} to be retrieved with these {@link Dimensions}.
         * </p>
         *
         * @see #getSchemaUri()
         */
        void setSchemaUri(final URI schemaUri)
        {

            _SchemaUri = schemaUri;
        }

        @Override
        public boolean equals(final Object o)
        {

            if (this == o)
            {
                return true;
            }
            if (!(o instanceof MutableDimensions))
            {
                return false;
            }

            final MutableDimensions that = (MutableDimensions) o;

            if (_Metadata != null ? !_Metadata.equals(that._Metadata) : that._Metadata != null)
            {
                return false;
            }
            if (_EmbeddedLinkSlotNames != null ? !_EmbeddedLinkSlotNames.equals(that._EmbeddedLinkSlotNames) : that._EmbeddedLinkSlotNames != null)
            {
                return false;
            }
            if (_ExcludedSlotNames != null ? !_ExcludedSlotNames.equals(that._ExcludedSlotNames) : that._ExcludedSlotNames != null)
            {
                return false;
            }
            if (_IncludedSlotNames != null ? !_IncludedSlotNames.equals(that._IncludedSlotNames) : that._IncludedSlotNames != null)
            {
                return false;
            }
            if (_Locale != null ? !_Locale.equals(that._Locale) : that._Locale != null)
            {
                return false;
            }
            if (_QueryParameters != null ? !_QueryParameters.equals(that._QueryParameters) : that._QueryParameters != null)
            {
                return false;
            }
            if (_ReferrerUri != null ? !_ReferrerUri.equals(that._ReferrerUri) : that._ReferrerUri != null)
            {
                return false;
            }
            if (_SchemaUri != null ? !_SchemaUri.equals(that._SchemaUri) : that._SchemaUri != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {

            int result = _SchemaUri != null ? _SchemaUri.hashCode() : 0;
            result = 31 * result + (_ReferrerUri != null ? _ReferrerUri.hashCode() : 0);
            result = 31 * result + (_Locale != null ? _Locale.hashCode() : 0);
            result = 31 * result + (_Metadata != null ? _Metadata.hashCode() : 0);
            result = 31 * result + (_EmbeddedLinkSlotNames != null ? _EmbeddedLinkSlotNames.hashCode() : 0);
            result = 31 * result + (_ExcludedSlotNames != null ? _ExcludedSlotNames.hashCode() : 0);
            result = 31 * result + (_IncludedSlotNames != null ? _IncludedSlotNames.hashCode() : 0);
            result = 31 * result + (_QueryParameters != null ? _QueryParameters.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {

            return AsciiArt.express(this);
        }

    }

}
