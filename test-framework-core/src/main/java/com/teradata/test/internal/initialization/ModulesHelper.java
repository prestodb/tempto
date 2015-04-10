package com.teradata.test.internal.initialization;

import com.google.inject.Module;
import com.teradata.test.configuration.Configuration;
import org.reflections.Reflections;
import org.testng.ITestResult;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public final class ModulesHelper
{
    public static List<? extends SuiteModuleProvider> scanForSuiteModuleProviders()
    {
        return instantiate(getClasses(SuiteModuleProvider.class));
    }

    public static List<? extends TestMethodModuleProvider> scanForTestMethodModuleProviders()
    {
        return instantiate(getClasses(TestMethodModuleProvider.class));
    }

    public static List<Module> getSuiteModules(List<? extends SuiteModuleProvider> suiteModuleProviders, Configuration configuration)
    {
        return suiteModuleProviders
                .stream()
                .map(provider -> provider.getModule(configuration))
                .collect(toList());
    }

    public static List<Module> getTestModules(List<? extends TestMethodModuleProvider> testMethodModuleProviders, Configuration configuration, ITestResult testResult)
    {
        return testMethodModuleProviders
                .stream()
                .map(provider -> provider.getModule(configuration, testResult))
                .collect(toList());
    }

    private static <T> Set<Class<? extends T>> getClasses(Class<T> clazz)
    {
        Reflections reflections = new Reflections("com.teradata.test.internal");
        return reflections.getSubTypesOf(clazz);
    }

    private static <T> List<? extends T> instantiate(Collection<Class<? extends T>> classes)
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
