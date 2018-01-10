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

package io.prestodb.tempto.examples;

import com.google.inject.Inject;
import io.prestodb.tempto.AfterTestWithContext;
import io.prestodb.tempto.BeforeTestWithContext;
import io.prestodb.tempto.ProductTest;
import io.prestodb.tempto.fulfillment.table.ImmutableTablesState;
import io.prestodb.tempto.fulfillment.table.MutableTablesState;
import org.testng.annotations.Test;

import javax.inject.Named;

import static org.assertj.core.api.Assertions.assertThat;

public class InjectionTest
        extends ProductTest
{
    @Inject
    MutableTablesState mutableTablesState;

    @BeforeTestWithContext
    @Inject
    public void setUp(
            ImmutableTablesState immutableTablesState,
            @Named("hdfs.username") String hdfsUsername
    )
    {
        testMethodInjection(immutableTablesState, hdfsUsername);
    }

    @Inject
    @Test(groups = "injection")
    public void testInjection()
    {
        assertThat(mutableTablesState).isNotNull();
    }

    @AfterTestWithContext
    @Inject
    public void tearDown(
            ImmutableTablesState immutableTablesState,
            @Named("hdfs.username") String hdfsUsername
    )
    {
        testMethodInjection(immutableTablesState, hdfsUsername);
    }

    private void testMethodInjection(ImmutableTablesState immutableTablesState, String hdfsUsername)
    {
        assertThat(immutableTablesState).isNotNull();
        assertThat(hdfsUsername).isEqualTo("hdfs");
    }
}
