/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive.tpch;

import com.teradata.test.fulfillment.hive.HiveTableDefinition;
import com.teradata.test.tpch.TpchTable;

public class TpchTableDefinitions
{
    public static final HiveTableDefinition NATION =
            HiveTableDefinition.builder()
                    .setName("nation")
                    .setCreateTableDDLTemplate("" +
                            "CREATE TABLE nation(" +
                            "   nationid INT," +
                            "   n_name STRING, " +
                            "   n_regionkey INT, " +
                            "   n_comment STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '{0}'")
                    .setDataSource(new TpchDataSource(TpchTable.NATION, 1.0))
                    .build();
}
