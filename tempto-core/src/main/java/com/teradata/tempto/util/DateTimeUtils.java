/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teradata.tempto.util;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.joda.time.format.DateTimePrinter;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateTimeUtils
{

    private static DateTimeParser[] timestampWithoutTimeZoneParser = {
            DateTimeFormat.forPattern("yyyy-M-d").getParser(),
            DateTimeFormat.forPattern("yyyy-M-d H:m").getParser(),
            DateTimeFormat.forPattern("yyyy-M-d H:m:s").getParser(),
            DateTimeFormat.forPattern("yyyy-M-d H:m:s.SSS").getParser()};
    private static DateTimePrinter timestampWithoutTimeZonePrinter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").getPrinter();
    private static DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder().append(timestampWithoutTimeZonePrinter, timestampWithoutTimeZoneParser).toFormatter().withZoneUTC();

    private DateTimeUtils() {}

    /**
     * Parse the input date time string (pattern: yyyy-M-d H:m:s.SSS) and interpret it in UTC.
     *
     * @param value textual date time input
     * @return a <code>java.sql.Timestamp</code> object representing the input date time.
     */
    public static Timestamp parseTimestampInUTC(String value)
    {
        return new Timestamp(DATE_TIME_FORMATTER.parseMillis(value));
    }
}

