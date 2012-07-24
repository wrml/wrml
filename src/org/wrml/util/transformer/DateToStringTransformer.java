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

package org.wrml.util.transformer;

import java.util.Date;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

public class DateToStringTransformer implements ToStringTransformer<Date> {

    @Override
    public String aToB(Date date) {
        return DateUtils.formatDate(date);
    }

    @Override
    public Date bToA(String dateValue) {
        try {
            return DateUtils.parseDate(dateValue);
        }
        catch (final DateParseException e) {
            throw new TransformerException("Failed to transform String value \"" + dateValue + "\" to a Date", e, this);
        }
    }
}
