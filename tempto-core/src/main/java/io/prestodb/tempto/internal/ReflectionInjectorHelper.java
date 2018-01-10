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

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static java.lang.ClassLoader.getSystemClassLoader;
import static net.bytebuddy.description.modifier.Visibility.PUBLIC;

public class ReflectionInjectorHelper
{
    private static InjectAnnotation INJECT_ANNOTATION = new InjectAnnotation();

    public Object[] getMethodArguments(Injector injector, Method method)
    {
        if (isAnnotatedWithInject(method)) {
            Parameter[] parameters = method.getParameters();
            Object instance = createInstanceWithFields(parameters);
            injector.injectMembers(instance);
            return readFieldValues(instance);
        }
        else {
            return new Object[] {};
        }
    }

    private boolean isAnnotatedWithInject(Method method)
    {
        return method.getAnnotation(Inject.class) != null ||
                method.getAnnotation(javax.inject.Inject.class) != null;
    }

    private Object createInstanceWithFields(Parameter[] parameters)
    {
        DynamicType.Builder<Object> objectBuilder = new ByteBuddy().subclass(Object.class)
                .modifiers(PUBLIC);
        for (Parameter parameter : parameters) {
            objectBuilder = objectBuilder.defineField(parameter.getName(), parameter.getType(), PUBLIC)
                    .annotateField(ArrayUtils.add(parameter.getAnnotations(), INJECT_ANNOTATION));
        }
        try {
            Class<?> createdClass = objectBuilder.make()
                    .load(getSystemClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
            return createdClass
                    .getConstructor()
                    .newInstance();
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] readFieldValues(Object instance)
    {
        Class<?> instanceClass = instance.getClass();
        Field[] fields = instanceClass.getFields();
        Object[] values = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            try {
                values[i] = fields[i].get(instance);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return values;
    }

    private static class InjectAnnotation
            implements Annotation
    {
        @Override
        public Class<? extends Annotation> annotationType()
        {
            return javax.inject.Inject.class;
        }
    }
}
