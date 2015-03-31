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
                            "   p_mfgr        STRING," +
                            "   p_brand       STRING," +
                            "   p_type        STRING," +
                            "   p_size        INT," +
                            "   p_container   STRING," +
                            "   p_retailprice DOUBLE," +
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
                            "   s_name        STRING," +
                            "   s_address     STRING," +
                            "   s_nationkey   INT," +
                            "   s_phone       STRING," +
                            "   s_acctbal     DOUBLE," +
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
                            "   ps_supplycost  DOUBLE," +
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
                            "   c_phone       STRING," +
                            "   c_acctbal     DOUBLE  ," +
                            "   c_mktsegment  STRING," +
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
                            "   o_orderstatus    STRING," +
                            "   o_totalprice     DOUBLE," +
                            "   o_orderdate      DATE," +
                            "   o_orderpriority  STRING,  " +
                            "   o_clerk          STRING, " +
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
                            "LOCATION '{0}'")
                    .setDataSource(new TpchDataSource(TpchTable.LINE_ITEM, 1.0))
                    .build();

    public static final List<HiveTableDefinition> TABLES = ImmutableList.of(NATION, REGION, PART, SUPPLIER, PART_SUPPLIER, CUSTOMER, ORDERS, LINE_ITEM);
}
