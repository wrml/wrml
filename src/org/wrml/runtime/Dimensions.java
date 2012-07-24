/**
 * Copyright (C) 2012 WRML.org <mark@wrml.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wrml.runtime;

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;

import org.wrml.model.Model;
import org.wrml.model.schema.Link;
import org.wrml.util.UniversallyUniqueObject;

/**
 * A {@link Model}'s dimensions form a "request closure", which is stored in the
 * lightweight {@link ModelReference} facades (Java
 * {@link java.lang.reflect.Proxy} instances) to help influence their
 * representation of the modeled data.
 * 
 * The closure aspect will be come more important once Models have the ability
 * to {@link Link} and thus chain the request closure's state via propagation of
 * the same dimensions.
 */
public class Dimensions extends UniversallyUniqueObject implements Serializable {

    private static final long serialVersionUID = -6508915228548590752L;

    private final Context _Context;

    private Method _RequestedMethod;
    private URI _RequestedSchemaId;
    private URI _RequestedFormatId;
    private Locale _RequestedLocale;
    private Locale _Locale;
    private Integer _EphemeralDimension;
    private String[] _IncludeFieldNames;
    private String[] _ExcludeFieldNames;

    /*
     * NOTE: When adding new fields to this class also revise the copy
     * constructor and toString accordingly.
     */

    public Dimensions(final Context context) {
        _Context = context;
        _RequestedMethod = Method.GetContent;
        _RequestedFormatId = _Context.getDefaultFormatId();
        _RequestedLocale = Locale.getDefault();
        _Locale = _RequestedLocale;
    }

    public Dimensions(final Dimensions toCopy) {
        this(toCopy.getContext());

        _RequestedMethod = toCopy._RequestedMethod;
        _RequestedSchemaId = toCopy._RequestedSchemaId;
        _RequestedFormatId = toCopy._RequestedFormatId;
        _RequestedLocale = toCopy._RequestedLocale;
        _Locale = toCopy._Locale;
        _EphemeralDimension = toCopy._EphemeralDimension;
        _IncludeFieldNames = toCopy._IncludeFieldNames;
        _ExcludeFieldNames = toCopy._ExcludeFieldNames;
    }

    public Context getContext() {
        return _Context;
    }

    public Integer getEphemeralDimension() {
        return _EphemeralDimension;
    }

    public String[] getExcludeFieldNames() {
        return _ExcludeFieldNames;
    }

    public String[] getIncludeFieldNames() {
        return _IncludeFieldNames;
    }

    public Locale getLocale() {
        return _Locale;
    }

    public URI getRequestedFormatId() {
        return _RequestedFormatId;
    }

    public Locale getRequestedLocale() {
        return _RequestedLocale;
    }

    public Method getRequestedMethod() {
        return _RequestedMethod;
    }

    public URI getRequestedSchemaId() {
        return _RequestedSchemaId;
    }

    public void setEphemeralDimension(Integer ephemeralDimension) {
        _EphemeralDimension = ephemeralDimension;
    }

    public void setExcludeFieldNames(String[] excludeFieldNames) {
        _ExcludeFieldNames = excludeFieldNames;
    }

    public void setIncludeFieldNames(String[] includeFieldNames) {
        _IncludeFieldNames = includeFieldNames;
    }

    public void setLocale(Locale locale) {
        _Locale = locale;
    }

    public void setRequestedFormatId(URI requestedFormatId) {
        _RequestedFormatId = requestedFormatId;
    }

    public void setRequestedLocale(Locale requestedLocale) {
        _RequestedLocale = requestedLocale;
    }

    public void setRequestedMethod(Method requestedMethod) {
        _RequestedMethod = requestedMethod;
    }

    public void setRequestedSchemaId(URI requestedSchemaId) {
        _RequestedSchemaId = requestedSchemaId;
    }

    @Override
    public String toString() {
        return "Dimensions [requestedSchemaId = " + _RequestedSchemaId + ", requestedFormatId = " + _RequestedFormatId
                + ", locale = " + _Locale + ", includeFieldNames = " + Arrays.toString(_IncludeFieldNames)
                + ", excludeFieldNames = " + Arrays.toString(_ExcludeFieldNames) + ", ephemeralDimension = "
                + _EphemeralDimension + ", requestedMethod = " + _RequestedMethod + ", requestedLocale = "
                + _RequestedLocale + "]";
    }

}
