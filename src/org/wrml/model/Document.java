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

package org.wrml.model;

import java.net.URI;

/**
 * "Well, the most important thing that was new was the idea of URI Ð
 * or URL [it was UDI back then, universal document identifier].
 * The idea that any piece of information anywhere should have an identifier,
 * which will not only identify it, but allow you to get hold of it.
 * That idea was the basic clue to the universality of the Web.
 * That was the only thing I insisted upon.
 * 
 * - Tim Berners-Lee,
 * http://www.wired.com/science/discoveries/news/1999/10/31830?currentPage=all
 * 
 * In WRML's model of REST, a "Document" is a resource archetype used to model a
 * singular concept.
 */
public interface Document extends Cacheable, Model {

    public URI getId();

    // TODO: Would it be better to use an Annotation to keep the Keys in Java?
    /*
     * public enum Key1 {
     * 
     * id;
     * }
     */

}
