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
package io.prestodb.tempto.fulfillment.table.kafka;

import com.google.common.base.Preconditions;
import io.prestodb.tempto.fulfillment.table.TableDefinition;
import io.prestodb.tempto.fulfillment.table.TableHandle;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class KafkaTableDefinition
        extends TableDefinition
{
    private final String topic;
    private final KafkaDataSource dataSource;
    private final int partitionsCount;
    private final int replicationLevel;

    public KafkaTableDefinition(String name, String topic, KafkaDataSource dataSource, int partitionsCount, int replicationLevel)
    {
        super(TableHandle.parse(requireNonNull(name, "name is null")));
        this.topic = requireNonNull(topic, "topic is null");
        this.dataSource = requireNonNull(dataSource, "dataSource is null");
        checkArgument(partitionsCount >= 1, "partitionsCount must be grater than or equal 1");
        this.partitionsCount = partitionsCount;
        checkArgument(replicationLevel >= 1, "replicationLevel must be grater than or equal 1");
        this.replicationLevel = replicationLevel;
    }

    public String getTopic()
    {
        return topic;
    }

    public KafkaDataSource getDataSource()
    {
        return dataSource;
    }

    public int getPartitionsCount()
    {
        return partitionsCount;
    }

    public int getReplicationLevel()
    {
        return replicationLevel;
    }
}


