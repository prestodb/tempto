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

import com.google.common.base.Charsets;
import com.teradata.tpcds.Results;
import com.teradata.tpcds.Session;
import io.prestodb.tempto.fulfillment.table.hive.HiveDataSource;
import io.prestodb.tempto.fulfillment.table.hive.statistics.TableStatistics;
import io.prestodb.tempto.fulfillment.table.hive.statistics.TableStatisticsRepository;
import io.prestodb.tempto.hadoop.hdfs.HdfsClient.RepeatableContentProducer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.teradata.tpcds.Results.constructResults;
import static java.lang.String.format;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

public class TpcdsDataSource
        implements HiveDataSource
{
    private final TpcdsTable table;
    private final int scaleFactor;

    public TpcdsDataSource(TpcdsTable table, int scaleFactor)
    {
        checkArgument(scaleFactor > 0, "Scale factor should be greater than 0: %s", scaleFactor);
        this.table = table;
        this.scaleFactor = scaleFactor;
    }

    @Override
    public String getPathSuffix()
    {
        return format("tpcds/sf-%d/%s", scaleFactor, table.name()).replaceAll("\\.", "_");
    }

    @Override
    public Collection<RepeatableContentProducer> data()
    {
        return singleton(() -> new StringIteratorInputStream(generate()));
    }

    private Iterator<String> generate()
    {
        Session session = Session.getDefaultSession()
                .withScale(scaleFactor)
                .withParallelism(1)
                .withTable(table.getTable())
                .withNoSexism(false);
        Results results = constructResults(table.getTable(), session);

        return StreamSupport.stream(results.spliterator(), false)
                .flatMap(rowBatch -> rowBatch.stream())
                .map(this::formatRow)
                .flatMap(row -> Stream.of(row, "\n"))
                .iterator();
    }

    private String formatRow(List<String> row)
    {
        return row.stream()
                .map(column -> column == null ? "\\N" : column)
                .collect(Collectors.joining("|")) + "|";
    }

    @Override
    public Optional<TableStatistics> getStatistics()
    {
        TableStatisticsRepository tableStatisticsRepository = new TableStatisticsRepository();
        return Optional.of(tableStatisticsRepository.load("tpcds", scaleFactor, table.name()));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TpcdsDataSource that = (TpcdsDataSource) o;
        return scaleFactor == that.scaleFactor &&
                table == that.table;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(table, scaleFactor);
    }

    private static class StringIteratorInputStream
            extends InputStream
    {
        private final Iterator<String> data;
        public int position;
        public byte[] value;

        public StringIteratorInputStream(Iterator<String> data)
        {
            this.data = requireNonNull(data, "data is null");
        }

        @Override
        public int read()
                throws IOException
        {
            if (value == null) {
                if (data.hasNext()) {
                    position = 0;
                    value = data.next().getBytes(Charsets.UTF_8);
                }
                else {
                    return -1;
                }
            }

            if (position < value.length) {
                return value[position++];
            }
            else {
                value = null;
                return read();
            }
        }
    }
}
