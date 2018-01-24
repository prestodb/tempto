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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.reflections.util.ClasspathHelper.forJavaClassPath;

public final class ReflectionHelper
{
    private static final LoadingCache<Class<? extends Annotation>, Set<Field>> FIELDS_ANNOTATED_WITH = CacheBuilder.newBuilder()
            .build(new CacheLoader<Class<? extends Annotation>, Set<Field>>()
            {
                @Override
                public Set<Field> load(Class<? extends Annotation> key)
                        throws Exception
                {
                    Reflections reflections = new Reflections(forJavaClassPath(),
                            new FieldAnnotationsScanner(), ReflectionHelper.class.getClassLoader());
                    return unmodifiableSet(reflections.getFieldsAnnotatedWith(key));
                }
            });

    private static final LoadingCache<Class, Set<Class>> SUBTYPES_OF = CacheBuilder.newBuilder()
            .build(new CacheLoader<Class, Set<Class>>()
            {
                @Override
                @SuppressWarnings("unchecked")
                public Set<Class> load(Class key)
                        throws Exception
                {
                    Reflections reflections = new Reflections(forJavaClassPath());
                    return reflections.getSubTypesOf(key);
                }
            });

    public static <T> T getStaticFieldValue(Field field)
    {
        try {
            return (T) field.get(null);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<Field> getFieldsAnnotatedWith(Class<? extends Annotation> annotation)
    {
        return FIELDS_ANNOTATED_WITH.getUnchecked(annotation);
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<Class<? extends T>> getAnnotatedSubTypesOf(Class<T> clazz, Class<? extends Annotation> annotation)
    {
        Set<Class<? extends T>> subtypesOf = (Set) SUBTYPES_OF.getUnchecked(clazz);
        return subtypesOf.stream()
                .filter(c -> c.getAnnotation(annotation) != null)
                .collect(toSet());
    }

    public static <T> List<? extends T> instantiate(Collection<Class<? extends T>> classes)
    {
        return classes
                .stream()
                .map(ReflectionHelper::instantiate)
                .collect(toList());
    }

    public static <T> T instantiate(String className)
    {
        try {
            return instantiate((Class<T>) Class.forName(className));
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to find specified class: " + className, e);
        }
    }

    private static <T> T instantiate(Class<? extends T> clazz)
    {
        try {
            return clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private ReflectionHelper()
    {
    }
}
