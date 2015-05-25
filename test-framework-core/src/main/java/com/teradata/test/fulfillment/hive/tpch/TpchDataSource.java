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

package com.teradata.test.fulfillment.hive.tpch;

import com.teradata.test.fulfillment.hive.DataSource;
import com.teradata.test.hadoop.hdfs.HdfsClient.RepeatableContentProducer;
import com.teradata.test.internal.fulfillment.hive.tpch.TpchEntityByteSource;

import java.util.Collection;

import static java.lang.String.format;
import static java.util.Collections.singleton;

public class TpchDataSource
        implements DataSource
{

    private static final String DATA_REVISION = "v.1.0";

    private final TpchTable table;
    private final double scaleFactor;

    public TpchDataSource(TpchTable table, double scaleFactor)
    {
        this.table = table;
        this.scaleFactor = scaleFactor;
    }

    @Override
    public String getPathSuffix()
    {
        // {TESTS_PATH}/tpch/sf-{scaleFactor}/{tableName}
        return format("tpch/sf-%.2f/%s", scaleFactor, table.name()).replaceAll("\\.", "_");
    }

    @Override
    public Collection<RepeatableContentProducer> data()
    {
        @SuppressWarnings("unchecked")
        Iterable<? extends io.airlift.tpch.TpchEntity> tableDataGenerator = table.getTpchTableEntity().createGenerator(scaleFactor, 1, 1);
        return singleton(() -> new TpchEntityByteSource<>(tableDataGenerator).openStream());
    }

    @Override
    public String revisionMarker()
    {
        return DATA_REVISION;
    }
}
