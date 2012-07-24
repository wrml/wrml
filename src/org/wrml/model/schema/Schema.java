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

import java.net.URI;

import org.wrml.model.Descriptive;
import org.wrml.model.Document;
import org.wrml.model.Named;
import org.wrml.model.Versioned;
import org.wrml.util.observable.ObservableList;

/**
 * Schema models describe the structure for a "class" of models. In this way,
 * Schema models are like Java's "Class" object instances. Schema models are
 * also a bit like: SQL tables, XML DTDS, JSON or XML schemas, AVRO schemas, XLS
 * templates, CMS content types, etc.
 * 
 * In summary, WRML's schemas describe a data structure which other model's may
 * represent.
 */
public interface Schema extends Named, Versioned, Descriptive, Constrainable, Document {

    /**
     * Gets a list ({@link Type#List}) of this schema's base schema ids (List of
     * URIs).
     * 
     * WRML supports interface style inheritance via URI reference, thus this
     * schema indicates the schemas that it "inherits" from or "mixes in" to
     * define its own models.
     * 
     * WRML's schematic inheritance is recursive, meaning that base schema's are
     * linked so that a schema virally inherits the base schemas of its base
     * schemas, and their base schemas, and so on...
     * 
     * @return An {@link ObservableList} containing the base schema documents'
     *         URIs.
     */
    public ObservableList<URI> getBaseSchemaIds();

    /**
     * Gets a list ({@link Type#List}) of Field models. Each Field model
     * describes a field that may be accessed in models representing this
     * schema.
     * 
     * The names of the fields must be unique within this Schema, meaning that
     * the list may not contain more than one Field with the same exact value in
     * the Field model's "name" field.
     * 
     * If the name of a field in this list "overrides" a field of the same name
     * defined in one (or more) of this schema's base schema(s), then models of
     * this schema should consider this shema's field's constraints to
     * "take precedence" over any similar constraints defined by the ancestral
     * schema(s).
     * 
     * @return A List containing this Schema model's Field models.
     */
    public ObservableList<Field> getFields();

    public Key getKey();

    public void setKey(Key key);
}
