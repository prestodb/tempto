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

package io.prestodb.tempto.fulfillment.table.jdbc.tpch;

import com.google.common.collect.ImmutableList;
import io.airlift.tpch.TpchTable;
import io.prestodb.tempto.fulfillment.table.TableDefinitionsRepository.RepositoryTableDefinition;
import io.prestodb.tempto.fulfillment.table.jdbc.RelationalTableDefinition;

import java.sql.JDBCType;

import static io.prestodb.tempto.fulfillment.table.jdbc.RelationalTableDefinition.relationalTableDefinition;
import static java.sql.JDBCType.BIGINT;
import static java.sql.JDBCType.VARCHAR;

public class JdbcTpchTableDefinitions
{
    public static final double DEFAULT_SCALE_FACTOR = 0.01;
    public static final ImmutableList<JDBCType> NATION_TYPES = ImmutableList.of(BIGINT, VARCHAR, BIGINT, VARCHAR);

    @RepositoryTableDefinition
    public static final RelationalTableDefinition NATION =
            relationalTableDefinition("nation_jdbc",
                    "CREATE TABLE %NAME%(" +
                            "   n_nationkey     BIGINT," +
                            "   n_name          VARCHAR(25)," +
                            "   n_regionkey     BIGINT," +
                            "   n_comment       VARCHAR(152)) ", new JdbcTpchDataSource(TpchTable.NATION, NATION_TYPES, DEFAULT_SCALE_FACTOR));

    private JdbcTpchTableDefinitions() {}
}

