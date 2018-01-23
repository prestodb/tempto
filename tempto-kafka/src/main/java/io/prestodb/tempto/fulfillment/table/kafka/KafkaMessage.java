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

import java.util.Optional;
import java.util.OptionalInt;

import static java.util.Objects.requireNonNull;

public class KafkaMessage
{
    private final OptionalInt partition;
    private final Optional<byte[]> key;
    private final byte[] value;

    public KafkaMessage(byte[] value)
    {
        this(Optional.empty(), value, OptionalInt.empty());
    }

    public KafkaMessage(byte[] value, OptionalInt partition)
    {
        this(Optional.empty(), value, partition);
    }

    public KafkaMessage(byte[] key, byte[] value)
    {
        this(Optional.of(key), value, OptionalInt.empty());
    }

    public KafkaMessage(Optional<byte[]> key, byte[] value, OptionalInt partition)
    {
        this.partition = requireNonNull(partition, "partition is null");
        this.key = requireNonNull(key, "key is null");
        this.value = requireNonNull(value, "value is null");
    }

    public OptionalInt getPartition()
    {
        return partition;
    }

    public Optional<byte[]> getKey()
    {
        return key;
    }

    public byte[] getValue()
    {
        return value;
    }
}
