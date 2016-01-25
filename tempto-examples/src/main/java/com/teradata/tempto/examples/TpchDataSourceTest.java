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

package com.teradata.tempto.examples;

import com.teradata.tempto.ProductTest;
import com.teradata.tempto.fulfillment.table.hive.tpch.TpchDataSource;
import com.teradata.tempto.hadoop.hdfs.HdfsClient;
import com.teradata.tempto.internal.fulfillment.table.hive.tpch.TpchEntityByteSource;
import io.airlift.tpch.TpchEntity;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.teradata.tempto.context.ThreadLocalTestContextHolder.testContext;
import static com.teradata.tempto.fulfillment.table.hive.tpch.TpchTable.REGION;
import static org.assertj.core.api.Assertions.assertThat;

public class TpchDataSourceTest
        extends ProductTest
{

    @Test(groups = "example_smoketest")
    public void testDataSource()
            throws IOException
    {
        TpchDataSource dataSource = new TpchDataSource(REGION, 1.0);

        String path = "/tempto/" + dataSource.getPathSuffix();

        assertThat(path).isEqualTo("/tempto/tpch/sf-1_00/REGION");

        HdfsClient hdfsClient = testContext().getDependency(HdfsClient.class);

        Iterable<TpchEntity> generator = REGION.getTpchTableEntity().createGenerator(1.0, 1, 1);
        String expectedData = IOUtils.toString(new TpchEntityByteSource<>(generator).openStream());
        hdfsClient.saveFile(path + "/data_0", new TpchEntityByteSource<>(generator).openStream());
        String storedData = hdfsClient.loadFile(path + "/data_0");

        assertThat(expectedData).isEqualTo(storedData);
    }
}
