/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive.tpch;

import com.teradata.test.fulfillment.hive.HiveTableDefinition;
import com.teradata.test.fulfillment.hive.HiveType;
import com.teradata.test.tpch.TpchTable;

public class TpchTableDefinitions
{
    public static final HiveTableDefinition NATION =
            HiveTableDefinition.builder()
                    .setName("nation")
                    .addColumn("n_nationkey", HiveType.INT)
                    .addColumn("n_name", HiveType.STRING)
                    .addColumn("n_regionkey", HiveType.INT)
                    .addColumn("n_comment", HiveType.STRING)
                    .setDataSource(new TpchHiveDataSource(TpchTable.NATION, 1.0))
                    .build();
}
