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

package io.prestodb.tempto.fulfillment.table.hive;

import com.google.common.io.ByteSource;
import io.prestodb.tempto.hadoop.hdfs.HdfsClient.RepeatableContentProducer;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import static com.google.common.collect.Iterators.cycle;
import static com.google.common.collect.Iterators.limit;
import static com.google.common.io.ByteSource.concat;
import static com.google.common.io.ByteSource.wrap;
import static com.google.common.io.Resources.getResource;
import static java.lang.String.format;
import static java.util.Collections.singleton;

public abstract class InlineDataSource
        implements HiveDataSource
{
    private final String tableName;

    private InlineDataSource(String tableName)
    {
        this.tableName = tableName;
    }

    public static HiveDataSource createResourceDataSource(String tableName, String dataResource)
    {
        return new InlineDataSource(tableName)
        {
            @Override
            public Collection<RepeatableContentProducer> data()
            {
                return singleton(() -> getResource(dataResource).openStream());
            }
        };
    }

    public static HiveDataSource createStringDataSource(String tableName, String data)
    {
        return new InlineDataSource(tableName)
        {
            @Override
            public Collection<RepeatableContentProducer> data()
            {
                return singleton(() -> wrap(data.getBytes()).openStream());
            }
        };
    }

    public static HiveDataSource createSameRowDataSource(String tableName, int splitCount, int rowsInEachSplit, String rowData)
    {
        return new InlineDataSource(tableName)
        {
            @Override
            public Collection<RepeatableContentProducer> data()
            {
                return new AbstractCollection<RepeatableContentProducer>()
                {
                    @Override
                    public Iterator<RepeatableContentProducer> iterator()
                    {
                        ByteSource singleRowSource = concat(wrap(rowData.getBytes()), wrap("\n".getBytes()));
                        ByteSource singleSplitSource = concat(limit(cycle(singleRowSource), rowsInEachSplit));
                        return limit(cycle(singleSplitSource::openStream), splitCount);
                    }

                    @Override
                    public int size()
                    {
                        return splitCount;
                    }
                };
            }
        };
    }

    @Override
    public String getPathSuffix()
    {
        // {TESTS_PATH}/datasets/{dataSetName}
        return format("inline-tables/%s", tableName);
    }
}
