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

package io.prestodb.tempto.internal

import org.testng.ITestNGMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.testng.internal.ConstructorOrMethod
import spock.lang.Specification

class DataProvidersTest
        extends Specification
{
    public static final Object[] EXTERNAL_DATA_PROVIDER_PARAMS = [["ex1"].toArray(), ["ex2"].toArray()].toArray()
    public static final Object[] INTERNAL_DATA_PROVIDER_PARAMS = [["int1"].toArray(), ["int2"].toArray()].toArray()

    private static class ExternalDataProviderClass
    {
        @DataProvider(name = "external_data_provider")
        static Object[][] externalDataProvider()
        {
            return EXTERNAL_DATA_PROVIDER_PARAMS
        }
    }

    private static class TestClass
    {
        @DataProvider(name = "internal_data_provider")
        static Object[][] internalDataProvider()
        {
            return INTERNAL_DATA_PROVIDER_PARAMS
        }

        @Test
        void testMethodWithoutDataProvider()
        {}

        @Test(dataProvider = "internal_data_provider")
        void testMethodWithInternalDataProvider()
        {}

        @Test(dataProvider = "external_data_provider", dataProviderClass = ExternalDataProviderClass.class)
        void testMethodWithExternalDataProvider()
        {}
    }

    def "should return absent for method without data provider"()
    {
        when:
        ITestNGMethod mockTestNGMethod = mockTestNGMethod("testMethodWithoutDataProvider")
        def parameters = DataProviders.getParametersForMethod(mockTestNGMethod)

        then:
        !parameters.isPresent()
    }

    def "should return parameters for method with internal data provider"()
    {
        when:
        ITestNGMethod mockTestNGMethod = mockTestNGMethod("testMethodWithInternalDataProvider")
        def parameters = DataProviders.getParametersForMethod(mockTestNGMethod)

        then:
        parameters.isPresent()
        parameters.get() == INTERNAL_DATA_PROVIDER_PARAMS
    }

    def "should return parameters for method with external data provider"()
    {
        when:
        ITestNGMethod mockTestNGMethod = mockTestNGMethod("testMethodWithExternalDataProvider")
        def parameters = DataProviders.getParametersForMethod(mockTestNGMethod)

        then:
        parameters.isPresent()
        parameters.get() == EXTERNAL_DATA_PROVIDER_PARAMS
    }

    private ITestNGMethod mockTestNGMethod(String testMethodName)
    {
        ITestNGMethod mockedTestNGMethod = Mock()
        mockedTestNGMethod.getInstance() >> new TestClass()
        mockedTestNGMethod.getRealClass() >> TestClass.class
        ConstructorOrMethod mockedConstructorOrMethod = new ConstructorOrMethod(TestClass.getMethod(testMethodName))
        mockedTestNGMethod.getConstructorOrMethod() >> mockedConstructorOrMethod
        return mockedTestNGMethod
    }
}


