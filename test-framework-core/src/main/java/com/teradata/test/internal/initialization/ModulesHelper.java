package com.teradata.test.internal.initialization;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.reflections.util.ClasspathHelper.forPackage;

public final class ModulesHelper
{
    private static final String PACKAGES_PREFIX = "com.teradata";

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
        Reflections reflections = new Reflections(forPackage(PACKAGES_PREFIX),
                new FieldAnnotationsScanner(), ModulesHelper.class.getClassLoader());
        return reflections.getFieldsAnnotatedWith(annotation);
    }

    public static <T> Set<Class<? extends T>> getClasses(Class<T> clazz)
    {
        Reflections reflections = new Reflections(PACKAGES_PREFIX);
        return reflections.getSubTypesOf(clazz);
    }

    public static <T> List<? extends T> instantiate(Collection<Class<? extends T>> classes)
    {
        return classes
                .stream()
                .map(clazz -> {
                    try {
                        return clazz.newInstance();
                    }
                    catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(toList());
    }

    private ModulesHelper()
    {
    }
}
