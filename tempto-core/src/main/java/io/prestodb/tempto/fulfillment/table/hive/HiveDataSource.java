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

import io.prestodb.tempto.fulfillment.table.hive.statistics.TableStatistics;
import io.prestodb.tempto.hadoop.hdfs.HdfsClient.RepeatableContentProducer;

import java.util.Collection;
import java.util.Optional;

/**
 * Responsible for providing data.
 */
public interface HiveDataSource
{
    /**
     * @return path suffix where data source data should be stored
     */
    String getPathSuffix();

    /**
     * @return collection with table files {@link RepeatableContentProducer}.
     * For each {@link RepeatableContentProducer} separate file will be created on HDFS
     */
    Collection<RepeatableContentProducer> data();

    default Optional<TableStatistics> getStatistics()
    {
        return Optional.empty();
    }
}
