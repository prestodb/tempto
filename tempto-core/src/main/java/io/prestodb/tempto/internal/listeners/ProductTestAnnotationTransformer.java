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

package io.prestodb.tempto.internal.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Responsible for transforming the {@link org.testng.annotations.Test} annotations in the following ways:
 * <p>
 * <ol>
 * <li>Ensure that the annotation is only on a test method and not a class or constructor</li>
 * </ol>
 */
public class ProductTestAnnotationTransformer
        implements IAnnotationTransformer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductTestAnnotationTransformer.class);

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod)
    {
        ensureAnnotationIsOnlyOnTestMethod(testClass, testConstructor, testMethod);
    }

    private static void ensureAnnotationIsOnlyOnTestMethod(Class testClass, Constructor testConstructor, Method testMethod)
    {
        if (testMethod == null) {
            String annotatedObjectName = (testClass != null) ? testClass.getName() : testConstructor.getName();
            LOGGER.error("Cannot annotate '{}' with @Test in the product tests, only a method can be a product test", annotatedObjectName);
            throw new IllegalStateException("Illegal @Test annotation on " + annotatedObjectName);
        }
    }
}
