/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.examples;

import com.teradata.test.ProductTest;
import com.teradata.test.fulfillment.hive.tpch.TpchDataSource;
import com.teradata.test.hadoop.hdfs.HdfsClient;
import com.teradata.test.internal.fulfillment.hive.tpch.TpchEntityByteSource;
import io.airlift.tpch.TpchEntity;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static com.teradata.test.fulfillment.hive.tpch.TpchTable.NATION;
import static com.teradata.test.fulfillment.hive.tpch.TpchTable.REGION;
import static org.assertj.core.api.Assertions.assertThat;

public class TpchDataSourceTest
        extends ProductTest
{

    @Test(groups = "example_smoketest")
    public void testDataSource()
            throws IOException
    {
        TpchDataSource dataSource = new TpchDataSource(REGION, 1.0);

        String path = "/product-test/" + dataSource.getPathSuffix();

        assertThat(path).isEqualTo("/product-test/tpch/sf-1_00/REGION");

        HdfsClient hdfsClient = testContext().getDependency(HdfsClient.class);
        String hdfsUsername = testContext().getDependency(String.class, "hdfs.username");

        Iterable<TpchEntity> generator = REGION.getTpchTableEntity().createGenerator(1.0, 1, 1);
        String expectedData = IOUtils.toString(new TpchEntityByteSource<>(generator).openStream());
        hdfsClient.saveFile(path + "/data_0", hdfsUsername, new TpchEntityByteSource<>(generator).openStream());
        String storedData = hdfsClient.loadFile(path + "/data_0", hdfsUsername);

        assertThat(expectedData).isEqualTo(storedData);
    }
}
