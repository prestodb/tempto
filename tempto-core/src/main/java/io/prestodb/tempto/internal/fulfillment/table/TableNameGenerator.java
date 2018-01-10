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

package io.prestodb.tempto.internal.fulfillment.table;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class TableNameGenerator
{
    private static final String MUTABLE_TABLE_NAME_PREFIX = "tempto_mut_";

    public String generateMutableTableNameInDatabase(String baseTableName)
    {
        String tableName = MUTABLE_TABLE_NAME_PREFIX + baseTableName + "_" + randomAlphanumeric(8);
        return tableName.toLowerCase();
    }

    public boolean isMutableTableName(String tableNameInDatabase)
    {
        return tableNameInDatabase.startsWith(MUTABLE_TABLE_NAME_PREFIX);
    }
}
