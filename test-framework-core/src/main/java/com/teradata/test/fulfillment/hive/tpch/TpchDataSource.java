/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive.tpch;

import com.teradata.test.fulfillment.hive.DataSource;
import com.teradata.test.tpch.IterableTpchEntityInputStream;
import com.teradata.test.tpch.TpchTable;

import java.io.InputStream;

import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;

public class TpchDataSource
        implements DataSource
{

    private static final String DATA_REVISION = "v.1.0";

    private final TpchTable table;
    private final double scaleFactor;

    public TpchDataSource(TpchTable table, double scaleFactor)
    {
        this.table = table;
        this.scaleFactor = scaleFactor;
    }

    @Override
    public String getName()
    {
        // {TESTS_PATH}/tpch/sf-{scaleFactor}/{tableName}
        String testsPath = testContext().getDependency(String.class, "tests.hdfs.path");
        return String.format("%s/tpch/sf-%.2f/%s", testsPath, scaleFactor, table.name()).replaceAll("\\.", "_");
    }

    @Override
    public InputStream data()
    {
        @SuppressWarnings("unchecked")
        Iterable<? extends io.airlift.tpch.TpchEntity> tableDataGenerator = table.getTpchTableEntity().createGenerator(scaleFactor, 1, 1);
        return new IterableTpchEntityInputStream<>(tableDataGenerator);
    }

    @Override
    public String revisionMarker()
    {
        return DATA_REVISION;
    }
}
