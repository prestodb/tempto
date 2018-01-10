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

package io.prestodb.tempto.fulfillment.table.hive.tpcds;

import io.prestodb.tempto.fulfillment.table.hive.statistics.ColumnStatistics;
import io.prestodb.tempto.fulfillment.table.hive.statistics.TableStatistics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TpcdsDataSourceTest
{
    @Test
    public void testStatistics()
    {
        TpcdsDataSource callCenterDataSource = new TpcdsDataSource(TpcdsTable.CALL_CENTER, 1);

        assertTrue(callCenterDataSource.getStatistics().isPresent());

        TableStatistics nationStatistics = callCenterDataSource.getStatistics().get();
        assertEquals(nationStatistics.getRowCount(), 6);

        ColumnStatistics nameStatistics = nationStatistics.getColumns().get("cc_name");
        assertEquals(nameStatistics.getNullsCount(), 0);
        assertEquals(nameStatistics.getDistinctValuesCount(), 3);
        assertEquals(nameStatistics.getMin().get(), "Mid Atlantic");
        assertEquals(nameStatistics.getMax().get(), "North Midwest");
    }
}
