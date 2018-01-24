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

package io.prestodb.tempto.internal;

import org.testng.ITestNGMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static java.util.Arrays.asList;

/**
 * Helper routines for working with TestNG data providers.
 */
public class DataProviders
{
    private DataProviders() {}

    /**
     * Obtains list of parameter sets for test method based on defined data provider.
     * If no data provider is defined then {@link Optional#empty()} is returned.
     * This method takes into consideration only data providers defined through annotation.
     *
     * @param method to be examined
     * @return list of parameter sets
     */
    public static Optional<Object[][]> getParametersForMethod(ITestNGMethod method)
    {
        Test testAnnotation = method.getConstructorOrMethod().getMethod().getAnnotation(Test.class);
        Class dataProviderClass = testAnnotation.dataProviderClass();
        if (dataProviderClass == null || dataProviderClass == Object.class) {
            dataProviderClass = method.getRealClass();
        }
        String dataProviderName = testAnnotation.dataProvider();
        if (dataProviderName.isEmpty()) {
            return Optional.empty();
        }

        Optional<Method> dataProviderMethodOptional = asList(dataProviderClass.getMethods()).stream().filter(
                m -> {
                    DataProvider annotation = m.getAnnotation(DataProvider.class);
                    return annotation != null && annotation.name().equals(dataProviderName);
                }
        ).findFirst();
        if (dataProviderMethodOptional.isPresent()) {
            try {
                return Optional.of((Object[][]) dataProviderMethodOptional.get().invoke(null));
            }
            catch (Exception e) {
                throw new RuntimeException("Exception while calling data provider for " + method, e);
            }
        }
        else {
            return Optional.empty();
        }
    }
}
