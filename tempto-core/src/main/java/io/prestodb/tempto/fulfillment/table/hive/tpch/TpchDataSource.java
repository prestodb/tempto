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

import io.prestodb.tempto.fulfillment.table.hive.HiveDataSource;
import io.prestodb.tempto.fulfillment.table.hive.statistics.TableStatistics;
import io.prestodb.tempto.fulfillment.table.hive.statistics.TableStatisticsRepository;
import io.prestodb.tempto.hadoop.hdfs.HdfsClient.RepeatableContentProducer;
import io.prestodb.tempto.internal.fulfillment.table.hive.tpch.TpchEntityByteSource;

import java.util.Collection;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.singleton;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class TpchDataSource
        implements HiveDataSource
{
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
    public Optional<TableStatistics> getStatistics()
    {
        TableStatisticsRepository tableStatisticsRepository = new TableStatisticsRepository();
        return Optional.of(tableStatisticsRepository.load("tpch", scaleFactor, table.name()));
    }

    @Override
    public boolean equals(Object o)
    {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return reflectionHashCode(this);
    }
}
