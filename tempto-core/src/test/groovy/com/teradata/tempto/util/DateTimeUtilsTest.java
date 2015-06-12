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

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.sql.Timestamp;

import static com.teradata.tempto.util.DateTimeUtils.parseTimestampInUTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

public class DateTimeUtilsTest
{

    @Test
    public void shouldParseMultipleFormatsInUTC()
            throws Exception
    {
        assertThat(parseTimestampInUTC("2015-05-10 12:15:35.123").getTime()).isEqualTo(1431260135123l);
        assertThat(parseTimestampInUTC("2015-05-10 12:15:35").getTime()).isEqualTo(1431260135000l);
        assertThat(parseTimestampInUTC("2015-05-10 12:15").getTime()).isEqualTo(1431260100000l);
        assertThat(parseTimestampInUTC("2015-05-10").getTime()).isEqualTo(1431216000000l);
    }
}