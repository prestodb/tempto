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

package com.teradata.tempto.fulfillment.table.hive;

import com.teradata.tempto.fulfillment.table.hive.statistics.TableStatistics;
import com.teradata.tempto.hadoop.FileSystemClient.RepeatableContentProducer;

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
     * For each {@link RepeatableContentProducer} separate file will be created on file system (HDFS, S3, etc)
     */
    Collection<RepeatableContentProducer> data();

    /**
     * Revision marker is used to determine if data should be regenerated. This
     * method should be fast.
     *
     * @return revision marker
     */
    String revisionMarker();

    default Optional<TableStatistics> getStatistics()
    {
        return Optional.empty();
    }
}
