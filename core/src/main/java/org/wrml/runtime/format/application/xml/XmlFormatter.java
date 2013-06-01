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
package org.wrml.runtime.format.application.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;
import org.wrml.runtime.format.*;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of {@link Formatter} to marshal and unmarshal XML using {@link XStream}.
 */
public class XmlFormatter extends AbstractFormatter
{

    private static final Logger LOG = LoggerFactory.getLogger(XmlFormatter.class);

    private Context _Context;

    @XStreamOmitField
    private XStream _XStream;

    public XmlFormatter()
    {

    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends Model> M readModel(final InputStream in, final Keys rootModelKeys, final Dimensions rootModelDimensions) throws ModelReadingException
    {

        // TODO: Implement this

        M result;
        try
        {
            Object obj = _XStream.fromXML(in);
            result = (M) obj;
            LOG.debug("unmarshalled input xml to Model={}", result);
        }
        catch (Exception e)
        {
            throw new ModelReadingException("Unable to marshall input xml to Model", e, this);
        }
        return result;
    }

    @Override
    public void writeModel(final OutputStream out, final Model model, final ModelWriteOptions writeOptions) throws ModelWritingException
    {

        // TODO: Implement this

        try
        {
            _XStream.toXML(model, out);
        }
        catch (Exception e)
        {
            throw new ModelWritingException(getClass().getSimpleName() + " had an Exception (" + e.getMessage() + ") processing model:\n" + model, e, this);
        }

    }

    @Override
    protected void initFromConfiguration(final FormatterConfiguration configu)
    {

        _XStream = new XStream();
        _XStream.autodetectAnnotations(true);

    }
}
