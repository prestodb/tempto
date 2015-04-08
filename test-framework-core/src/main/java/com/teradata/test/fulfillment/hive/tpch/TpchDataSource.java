/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive.tpch;

import com.google.common.io.ByteSource;
import com.teradata.test.fulfillment.hive.DataSource;
import com.teradata.test.internal.fulfillment.hive.tpch.TpchEntityByteSource;

import java.util.Collection;

import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static java.lang.String.format;
import static java.util.Collections.singleton;

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
    public String getPath()
    {
        // {TESTS_PATH}/tpch/sf-{scaleFactor}/{tableName}
        String testsPath = testContext().getDependency(String.class, "tests.hdfs.path");
        return format("%s/tpch/sf-%.2f/%s", testsPath, scaleFactor, table.name()).replaceAll("\\.", "_");
    }

    @Override
    public Collection<ByteSource> data()
    {
        @SuppressWarnings("unchecked")
        Iterable<? extends io.airlift.tpch.TpchEntity> tableDataGenerator = table.getTpchTableEntity().createGenerator(scaleFactor, 1, 1);
        return singleton(new TpchEntityByteSource<>(tableDataGenerator));
    }

    @Override
    public String revisionMarker()
    {
        return DATA_REVISION;
    }
}
