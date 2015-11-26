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

package com.teradata.tempto.fulfillment.table.hive.tpch;

import com.teradata.tempto.fulfillment.table.hive.HiveTableDefinition;
import com.teradata.tempto.fulfillment.table.TableDefinitionsRepository.RepositoryTableDefinition;

public class TpchTableDefinitions
{
    @RepositoryTableDefinition
    public static final HiveTableDefinition NATION =
            HiveTableDefinition.builder("nation")
                    .setCreateTableDDLTemplate("" +
                            "CREATE EXTERNAL TABLE %NAME%(" +
                            "   n_nationkey     INT," +
                            "   n_name          STRING," +
                            "   n_regionkey     INT," +
                            "   n_comment       STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '%LOCATION%'")
                    .setDataSource(new TpchDataSource(TpchTable.NATION, 1.0))
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition REGION =
            HiveTableDefinition.builder("region")
                    .setCreateTableDDLTemplate("" +
                            "CREATE EXTERNAL TABLE %NAME%(" +
                            "   r_regionkey     INT," +
                            "   r_name          STRING," +
                            "   r_comment       STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '%LOCATION%'")
                    .setDataSource(new TpchDataSource(TpchTable.REGION, 1.0))
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition PART =
            HiveTableDefinition.builder("part")
                    .setCreateTableDDLTemplate("" +
                            "CREATE EXTERNAL TABLE %NAME%(" +
                            "   p_partkey     INT," +
                            "   p_name        STRING," +
                            "   p_mfgr        STRING," +
                            "   p_brand       STRING," +
                            "   p_type        STRING," +
                            "   p_size        INT," +
                            "   p_container   STRING," +
                            "   p_retailprice DOUBLE," +
                            "   p_comment     STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '%LOCATION%'")
                    .setDataSource(new TpchDataSource(TpchTable.PART, 1.0))
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition SUPPLIER =
            HiveTableDefinition.builder("supplier")
                    .setCreateTableDDLTemplate("" +
                            "CREATE EXTERNAL TABLE %NAME%(" +
                            "   s_suppkey     INT," +
                            "   s_name        STRING," +
                            "   s_address     STRING," +
                            "   s_nationkey   INT," +
                            "   s_phone       STRING," +
                            "   s_acctbal     DOUBLE," +
                            "   s_comment     STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '%LOCATION%'")
                    .setDataSource(new TpchDataSource(TpchTable.SUPPLIER, 1.0))
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition PART_SUPPLIER =
            HiveTableDefinition.builder("partsupp")
                    .setCreateTableDDLTemplate("" +
                            "CREATE EXTERNAL TABLE %NAME%(" +
                            "   ps_partkey     INT," +
                            "   ps_suppkey     INT," +
                            "   ps_availqty    INT," +
                            "   ps_supplycost  DOUBLE," +
                            "   ps_comment     STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '%LOCATION%'")
                    .setDataSource(new TpchDataSource(TpchTable.PART_SUPPLIER, 1.0))
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition CUSTOMER =
            HiveTableDefinition.builder("customer")
                    .setCreateTableDDLTemplate("" +
                            "CREATE EXTERNAL TABLE %NAME%(" +
                            "   c_custkey     INT," +
                            "   c_name        STRING," +
                            "   c_address     STRING," +
                            "   c_nationkey   INT," +
                            "   c_phone       STRING," +
                            "   c_acctbal     DOUBLE  ," +
                            "   c_mktsegment  STRING," +
                            "   c_comment     STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '%LOCATION%'")
                    .setDataSource(new TpchDataSource(TpchTable.CUSTOMER, 1.0))
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition ORDERS =
            HiveTableDefinition.builder("orders")
                    .setCreateTableDDLTemplate("" +
                            "CREATE EXTERNAL TABLE %NAME%(" +
                            "   o_orderkey       INT," +
                            "   o_custkey        INT," +
                            "   o_orderstatus    STRING," +
                            "   o_totalprice     DOUBLE," +
                            "   o_orderdate      DATE," +
                            "   o_orderpriority  STRING,  " +
                            "   o_clerk          STRING, " +
                            "   o_shippriority   INT," +
                            "   o_comment        STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '%LOCATION%'")
                    .setDataSource(new TpchDataSource(TpchTable.ORDERS, 1.0))
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition LINE_ITEM =
            HiveTableDefinition.builder("lineitem")
                    .setCreateTableDDLTemplate("" +
                            "CREATE EXTERNAL TABLE %NAME%(" +
                            "   l_orderkey      INT," +
                            "   l_partkey       INT," +
                            "   l_suppkey       INT," +
                            "   l_linenumber    INT," +
                            "   l_quantity      DOUBLE," +
                            "   l_extendedprice DOUBLE," +
                            "   l_discount      DOUBLE," +
                            "   l_tax           DOUBLE," +
                            "   l_returnflag    STRING," +
                            "   l_linestatus    STRING," +
                            "   l_shipdate      DATE," +
                            "   l_commitdate    DATE," +
                            "   l_receiptdate   DATE," +
                            "   l_shipinstruct  STRING," +
                            "   l_shipmode      STRING," +
                            "   l_comment       STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '%LOCATION%'")
                    .setDataSource(new TpchDataSource(TpchTable.LINE_ITEM, 1.0))
                    .build();
}
