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

package io.prestodb.tempto.fulfillment.table.hive.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.net.URL;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Locale.ENGLISH;

public class TableStatisticsRepository
{
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module());

    public TableStatistics load(String benchmark, double scaleFactor, String table)
    {
        String schema = schema(scaleFactor);
        String resourcePath = "/statistics/" + benchmark.toLowerCase(ENGLISH) + "/" + schema + "/" + table.toLowerCase(ENGLISH) + ".json";
        URL resource = getClass().getResource(resourcePath);
        checkState(resource != null, "Unable to find statistics data file, trying with: %s", resourcePath);
        try {
            return objectMapper.readValue(resource, TableStatistics.class);
        }
        catch (Exception e) {
            throw new RuntimeException(format("Failed to parse stats from resource [%s]", resourcePath), e);
        }
    }

    private String schema(double scaleFactor)
    {
        return ("sf" + scaleFactor).replaceAll("\\.0*$", "");
    }
}
