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

package io.prestodb.tempto.fulfillment.table.hive.tpch;

import io.prestodb.tempto.fulfillment.table.TableDefinitionsRepository.RepositoryTableDefinition;
import io.prestodb.tempto.fulfillment.table.hive.HiveTableDefinition;

// Table definitions according to: http://www.tpc.org/tpc_documents_current_versions/pdf/tpc-h_v2.17.1.pdf
// TODO: support for CHAR
// TODO: move to separate module
public class TpchTableDefinitions
{
    public static final double DEFAULT_SCALE_FACTOR = 1;

    @RepositoryTableDefinition
    public static final HiveTableDefinition NATION =
            HiveTableDefinition.builder("nation")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "   n_nationkey     BIGINT," +
                            "   n_name          VARCHAR(25)," +
                            "   n_regionkey     BIGINT," +
                            "   n_comment       VARCHAR(152)) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpchDataSource(TpchTable.NATION, DEFAULT_SCALE_FACTOR))
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition REGION =
            HiveTableDefinition.builder("region")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "   r_regionkey     BIGINT," +
                            "   r_name          VARCHAR(25)," +
                            "   r_comment       VARCHAR(152)) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpchDataSource(TpchTable.REGION, DEFAULT_SCALE_FACTOR))
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition PART =
            HiveTableDefinition.builder("part")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "   p_partkey     BIGINT," +
                            "   p_name        VARCHAR(55)," +
                            "   p_mfgr        VARCHAR(25)," +
                            "   p_brand       VARCHAR(10)," +
                            "   p_type        VARCHAR(25)," +
                            "   p_size        INT," +
                            "   p_container   VARCHAR(10)," +
                            "   p_retailprice DECIMAL(12,2)," +
                            "   p_comment     VARCHAR(23)) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpchDataSource(TpchTable.PART, DEFAULT_SCALE_FACTOR))
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition SUPPLIER =
            HiveTableDefinition.builder("supplier")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "   s_suppkey     BIGINT," +
                            "   s_name        VARCHAR(25)," +
                            "   s_address     VARCHAR(40)," +
                            "   s_nationkey   BIGINT," +
                            "   s_phone       VARCHAR(15)," +
                            "   s_acctbal     DECIMAL(12,2)," +
                            "   s_comment     VARCHAR(101)) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpchDataSource(TpchTable.SUPPLIER, DEFAULT_SCALE_FACTOR))
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition PART_SUPPLIER =
            HiveTableDefinition.builder("partsupp")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "   ps_partkey     BIGINT," +
                            "   ps_suppkey     BIGINT," +
                            "   ps_availqty    INT," +
                            "   ps_supplycost  DECIMAL(12,2)," +
                            "   ps_comment     VARCHAR(199)) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpchDataSource(TpchTable.PART_SUPPLIER, DEFAULT_SCALE_FACTOR))
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition CUSTOMER =
            HiveTableDefinition.builder("customer")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "   c_custkey     BIGINT," +
                            "   c_name        VARCHAR(25)," +
                            "   c_address     VARCHAR(40)," +
                            "   c_nationkey   BIGINT," +
                            "   c_phone       VARCHAR(15)," +
                            "   c_acctbal     DECIMAL(12,2)," +
                            "   c_mktsegment  VARCHAR(10)," +
                            "   c_comment     VARCHAR(117)) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpchDataSource(TpchTable.CUSTOMER, DEFAULT_SCALE_FACTOR))
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition ORDERS =
            HiveTableDefinition.builder("orders")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "   o_orderkey       BIGINT," +
                            "   o_custkey        BIGINT," +
                            "   o_orderstatus    VARCHAR(1)," +
                            "   o_totalprice     DECIMAL(12,2)," +
                            "   o_orderdate      DATE," +
                            "   o_orderpriority  VARCHAR(15),  " +
                            "   o_clerk          VARCHAR(15), " +
                            "   o_shippriority   INT," +
                            "   o_comment        VARCHAR(79)) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpchDataSource(TpchTable.ORDERS, DEFAULT_SCALE_FACTOR))
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition LINE_ITEM =
            HiveTableDefinition.builder("lineitem")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "   l_orderkey      BIGINT," +
                            "   l_partkey       BIGINT," +
                            "   l_suppkey       BIGINT," +
                            "   l_linenumber    INT," +
                            "   l_quantity      DECIMAL(12,2)," +
                            "   l_extendedprice DECIMAL(12,2)," +
                            "   l_discount      DECIMAL(12,2)," +
                            "   l_tax           DECIMAL(12,2)," +
                            "   l_returnflag    VARCHAR(1)," +
                            "   l_linestatus    VARCHAR(1)," +
                            "   l_shipdate      DATE," +
                            "   l_commitdate    DATE," +
                            "   l_receiptdate   DATE," +
                            "   l_shipinstruct  VARCHAR(25)," +
                            "   l_shipmode      VARCHAR(10)," +
                            "   l_comment       VARCHAR(44)) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpchDataSource(TpchTable.LINE_ITEM, DEFAULT_SCALE_FACTOR))
                    .build();

    private TpchTableDefinitions() {}
}
