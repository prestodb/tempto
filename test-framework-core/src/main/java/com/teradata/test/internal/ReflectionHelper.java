package com.teradata.test.internal;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.reflections.util.ClasspathHelper.forPackage;

public final class ReflectionHelper
{
    private static final String PACKAGES_PREFIX = "com";

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
                new FieldAnnotationsScanner(), ReflectionHelper.class.getClassLoader());
        return reflections.getFieldsAnnotatedWith(annotation);
    }

    public static <T> Set<Class<? extends T>> getAnnotatedSubTypesOf(Class<T> clazz, Class<? extends Annotation> annotation)
    {
        Reflections reflections = new Reflections(PACKAGES_PREFIX);
        return reflections.getSubTypesOf(clazz)
                .stream()
                .filter(c -> c.getAnnotation(annotation) != null)
                .collect(Collectors.toSet());
    }

    public static <T> List<? extends T> instantiate(Collection<Class<? extends T>> classes)
    {
        return classes
                .stream()
                .map(ReflectionHelper::instantiate)
                .collect(toList());
    }

    public static <T> T instantiate(String className) {
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
