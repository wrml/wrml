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
 * Copyright (C) 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
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
package org.wrml.model.rest;

import org.wrml.model.Abstract;
import org.wrml.model.Virtual;

/**
 * <p>
 * Used by the WRML runtime to mark a Schema as an aggregation/composition of all of its link-embedded component parts.
 * </p>
 * <p>
 * When extended by a WRML Schema (runtime's Java class representation), the Aggregate base Schema
 * allows a subschema to be marked as "an Aggregate only" or a "Virtual" model. Such Documents effectively
 * mark *all* of their (non-Link, non-inherited) slots as "keys"; making them (composite) key-complete, or fully
 * initialized from the (request) input Keys. This model-oriented approach to Document (link-based) aggregation will
 * enable virtual aggregates (URI hacking) to hit different backend systems with different values to create a uniformly
 * structured result (aggregate) response].
 * <p/>
 * <p>
 * An Aggregate model is key-complete, with all of the declared slots (together) forming its composite key (making it unique),
 * and each one of the keys may be used (automatically) in one or more links (href values) to other Documents (models).
 * WRML manages the fetching of the linked Documents (server-side) and mashes them all up into the HomeScreen to build
 * the response JSON object (with embedded objects); aka the aggregate model.
 * </p>
 */
public interface AggregateDocument extends Abstract, Virtual, Document
{
}
