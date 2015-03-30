/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive.tpch;

import com.google.common.collect.ImmutableList;
import com.teradata.test.fulfillment.hive.HiveTableDefinition;

import java.util.List;

public class TpchTableDefinitions
{
    public static final HiveTableDefinition NATION =
            HiveTableDefinition.builder()
                    .setName("nation")
                    .setCreateTableDDLTemplate("" +
                            "CREATE TABLE nation(" +
                            "   n_nationkey     INT," +
                            "   n_name          STRING," +
                            "   n_regionkey     INT," +
                            "   n_comment       STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '{0}'")
                    .setDataSource(new TpchDataSource(TpchTable.NATION, 1.0))
                    .build();

    public static final HiveTableDefinition REGION =
            HiveTableDefinition.builder()
                    .setName("region")
                    .setCreateTableDDLTemplate("" +
                            "CREATE TABLE region(" +
                            "   r_regionkey     INT," +
                            "   r_name          STRING," +
                            "   r_comment       STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '{0}'")
                    .setDataSource(new TpchDataSource(TpchTable.REGION, 1.0))
                    .build();

    public static final HiveTableDefinition PART =
            HiveTableDefinition.builder()
                    .setName("part")
                    .setCreateTableDDLTemplate("" +
                            "CREATE TABLE part(" +
                            "   p_partkey     INT," +
                            "   p_name        STRING," +
                            "   p_mfgr        CHAR(25)," +
                            "   p_brand       CHAR(10)," +
                            "   p_type        STRING," +
                            "   p_size        INT," +
                            "   p_container   CHAR(10)," +
                            "   p_retailprice DECIMAL(15,2)," +
                            "   p_comment     STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '{0}'")
                    .setDataSource(new TpchDataSource(TpchTable.PART, 1.0))
                    .build();

    public static final HiveTableDefinition SUPPLIER =
            HiveTableDefinition.builder()
                    .setName("supplier")
                    .setCreateTableDDLTemplate("" +
                            "CREATE TABLE supplier(" +
                            "   s_suppkey     INT," +
                            "   s_name        CHAR(25)," +
                            "   s_address     STRING," +
                            "   s_nationkey   INT," +
                            "   s_phone       CHAR(15)," +
                            "   s_acctbal     DECIMAL(15,2)," +
                            "   s_comment     STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '{0}'")
                    .setDataSource(new TpchDataSource(TpchTable.SUPPLIER, 1.0))
                    .build();

    public static final HiveTableDefinition PART_SUPPLIER =
            HiveTableDefinition.builder()
                    .setName("partsupp")
                    .setCreateTableDDLTemplate("" +
                            "CREATE TABLE partsupp(" +
                            "   ps_partkey     INT," +
                            "   ps_suppkey     INT," +
                            "   ps_availqty    INT," +
                            "   ps_supplycost  DECIMAL(15,2)," +
                            "   ps_comment     STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '{0}'")
                    .setDataSource(new TpchDataSource(TpchTable.PART_SUPPLIER, 1.0))
                    .build();

    public static final HiveTableDefinition CUSTOMER =
            HiveTableDefinition.builder()
                    .setName("customer")
                    .setCreateTableDDLTemplate("" +
                            "CREATE TABLE customer(" +
                            "   c_custkey     INT," +
                            "   c_name        STRING," +
                            "   c_address     STRING," +
                            "   c_nationkey   INT," +
                            "   c_phone       CHAR(15)," +
                            "   c_acctbal     DECIMAL(15,2)  ," +
                            "   c_mktsegment  CHAR(10)," +
                            "   c_comment     STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '{0}'")
                    .setDataSource(new TpchDataSource(TpchTable.CUSTOMER, 1.0))
                    .build();

    public static final HiveTableDefinition ORDERS =
            HiveTableDefinition.builder()
                    .setName("orders")
                    .setCreateTableDDLTemplate("" +
                            "CREATE TABLE orders(" +
                            "   o_orderkey       INT," +
                            "   o_custkey        INT," +
                            "   o_orderstatus    CHAR(1)," +
                            "   o_totalprice     DECIMAL(15,2)," +
                            "   o_orderdate      DATE," +
                            "   o_orderpriority  CHAR(15),  " +
                            "   o_clerk          CHAR(15), " +
                            "   o_shippriority   INT," +
                            "   o_comment        STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '{0}'")
                    .setDataSource(new TpchDataSource(TpchTable.ORDERS, 1.0))
                    .build();

    public static final HiveTableDefinition LINE_ITEM =
            HiveTableDefinition.builder()
                    .setName("lineitem")
                    .setCreateTableDDLTemplate("" +
                            "CREATE TABLE lineitem(" +
                            "   l_orderkey      INT," +
                            "   l_partkey       INT," +
                            "   l_suppkey       INT," +
                            "   l_linenumber    INT," +
                            "   l_quantity      DECIMAL(15,2)," +
                            "   l_extendedprice DECIMAL(15,2)," +
                            "   l_discount      DECIMAL(15,2)," +
                            "   l_tax           DECIMAL(15,2)," +
                            "   l_returnflag    CHAR(1)," +
                            "   l_linestatus    CHAR(1)," +
                            "   l_shipdate      DATE," +
                            "   l_commitdate    DATE," +
                            "   l_receiptdate   DATE," +
                            "   l_shipinstruct  CHAR(25)," +
                            "   l_shipmode      CHAR(10)," +
                            "   l_comment       STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '{0}'")
                    .setDataSource(new TpchDataSource(TpchTable.LINE_ITEM, 1.0))
                    .build();

    public static final List<HiveTableDefinition> TABLES = ImmutableList.of(NATION, REGION, PART, SUPPLIER, PART_SUPPLIER, CUSTOMER, ORDERS, LINE_ITEM);
}
