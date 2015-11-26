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
package com.teradata.tempto.internal.fulfillment.table.hive;

import com.teradata.tempto.fulfillment.table.TableInstance;
import com.teradata.tempto.fulfillment.table.hive.HiveTableDefinition;
import com.teradata.tempto.internal.fulfillment.table.TableName;

import java.util.Optional;

public class HiveTableInstance
        extends TableInstance<HiveTableDefinition>
{
    private final Optional<String> mutableDataHdfsDataPath;

    public HiveTableInstance(TableName tableName, HiveTableDefinition tableDefinition, Optional<String> mutableDataHdfsDataPath)
    {
        super(tableName, tableDefinition);
        this.mutableDataHdfsDataPath = mutableDataHdfsDataPath;
    }

    public Optional<String> getMutableDataHdfsDataPath()
    {
        return mutableDataHdfsDataPath;
    }
}
