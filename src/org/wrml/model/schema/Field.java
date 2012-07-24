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

package org.wrml.model.schema;

import org.wrml.model.Descriptive;
import org.wrml.model.Embedded;
import org.wrml.model.Model;
import org.wrml.model.Named;
import org.wrml.model.Versioned;

/**
 * Field models are embedded within {@link Schema} documents.
 * 
 * Fields may (optionally) have a list of associated {@link Constraint} models.
 * 
 * Fields are named and described by their Schema's designers (please take this
 * responsibility seriously).
 * 
 * Fields have an associated {@link Type} and are {@link Versioned} using
 * {@link Long}-based
 * incremental numbering to denote revisions.
 */
public interface Field extends Named, Descriptive, Typed, Constrainable, Versioned, Embedded, Model {

    public Object getDefaultValue();

    public boolean isReadOnly();

    public boolean isValueRequired();

    public void setDefaultValue(Object defaultValue);

    public void setReadOnly(boolean readOnly);

    public void setValueRequired(boolean valueRequired);
}
