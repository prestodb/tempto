/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.examples;

import com.teradata.test.ProductTest;
import com.teradata.test.Requirement;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.Requires;
import com.teradata.test.fulfillment.hive.tpch.TpchHiveDataSource;
import com.teradata.test.hadoop.hdfs.HdfsClient;
import com.teradata.test.requirements.TableRequirements;
import com.teradata.test.tpch.IterableTpchEntityInputStream;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static com.teradata.test.requirements.TableRequirements.table;
import static com.teradata.test.tpch.TpchTable.NATION;
import static org.assertj.core.api.Assertions.assertThat;

public class TpchHiveDataSourceTest
        extends ProductTest
{

    @Requires(BaseProductTestRequirements.class)
    @Test(groups = "example_smoketest")
    public void testDataSource()
            throws IOException
    {
        TpchHiveDataSource dataSource = new TpchHiveDataSource(NATION, 1.0);

        String path = dataSource.ensureDataOnHdfs();

        assertThat(path).isEqualTo("/product-test/tpch/sf-1_00/NATION");

        HdfsClient hdfsClient = testContext().getDependency(HdfsClient.class);
        String hdfsUsername = testContext().getDependency(String.class, "hdfs.username");

        Iterable generator = NATION.getTpchTableEntity().createGenerator(1.0, 1, 1);
        String expectedData = IOUtils.toString(new IterableTpchEntityInputStream<>(generator));
        String storedData = hdfsClient.loadFile(path, hdfsUsername);

        assertThat(expectedData).isEqualTo(storedData);
    }

    public static class BaseProductTestRequirements
            implements RequirementsProvider
    {

        @Override
        public Requirement getRequirements()
        {
            return table("dummy table");
        }
    }
}
