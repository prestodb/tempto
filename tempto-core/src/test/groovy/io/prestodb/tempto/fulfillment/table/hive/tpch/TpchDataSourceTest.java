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
package io.prestodb.tempto.fulfillment.table.hive.tpch;

import io.prestodb.tempto.fulfillment.table.hive.statistics.ColumnStatistics;
import io.prestodb.tempto.fulfillment.table.hive.statistics.TableStatistics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TpchDataSourceTest
{
    @Test
    public void testStatistics()
    {
        TpchDataSource nationDataSource = new TpchDataSource(TpchTable.NATION, 1);

        assertTrue(nationDataSource.getStatistics().isPresent());

        TableStatistics nationStatistics = nationDataSource.getStatistics().get();
        assertEquals(nationStatistics.getRowCount(), 25);

        ColumnStatistics nationkeyStatistics = nationStatistics.getColumns().get("n_nationkey");
        assertEquals(nationkeyStatistics.getNullsCount(), 0);
        assertEquals(nationkeyStatistics.getDistinctValuesCount(), 25);
        assertEquals(nationkeyStatistics.getMin().get(), 0);
        assertEquals(nationkeyStatistics.getMax().get(), 24);
    }
}
