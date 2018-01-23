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

import io.prestodb.tempto.fulfillment.table.TableInstance;
import io.prestodb.tempto.internal.fulfillment.table.TableName;

public class KafkaTableInstance
        extends TableInstance<KafkaTableDefinition>
{
    protected KafkaTableInstance(TableName name, KafkaTableDefinition tableDefinition)
    {
        super(name, tableDefinition);
    }
}
