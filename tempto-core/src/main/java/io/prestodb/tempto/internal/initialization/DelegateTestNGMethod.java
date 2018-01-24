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

package io.prestodb.tempto.internal.initialization;

import org.testng.IClass;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.internal.ConstructorOrMethod;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class DelegateTestNGMethod
        implements ITestNGMethod
{
    protected final ITestNGMethod delegate;

    public DelegateTestNGMethod(ITestNGMethod delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public Class getRealClass()
    {
        return delegate.getRealClass();
    }

    @Override
    public ITestClass getTestClass()
    {
        return delegate.getTestClass();
    }

    @Override
    public void setTestClass(ITestClass cls)
    {
        delegate.setTestClass(cls);
    }

    @Override
    @Deprecated
    public Method getMethod()
    {
        return delegate.getMethod();
    }

    @Override
    public String getMethodName()
    {
        return delegate.getMethodName();
    }

    @Override
    @Deprecated
    public Object[] getInstances()
    {
        return delegate.getInstances();
    }

    @Override
    public Object getInstance()
    {
        return delegate.getInstance();
    }

    @Override
    public long[] getInstanceHashCodes()
    {
        return delegate.getInstanceHashCodes();
    }

    @Override
    public String[] getGroups()
    {
        return delegate.getGroups();
    }

    @Override
    public String[] getGroupsDependedUpon()
    {
        return delegate.getGroupsDependedUpon();
    }

    @Override
    public String getMissingGroup()
    {
        return delegate.getMissingGroup();
    }

    @Override
    public void setMissingGroup(String group)
    {
        delegate.setMissingGroup(group);
    }

    @Override
    public String[] getBeforeGroups()
    {
        return delegate.getBeforeGroups();
    }

    @Override
    public String[] getAfterGroups()
    {
        return delegate.getAfterGroups();
    }

    @Override
    public String[] getMethodsDependedUpon()
    {
        return delegate.getMethodsDependedUpon();
    }

    @Override
    public void addMethodDependedUpon(String methodName)
    {
        delegate.addMethodDependedUpon(methodName);
    }

    @Override
    public boolean isTest()
    {
        return delegate.isTest();
    }

    @Override
    public boolean isBeforeMethodConfiguration()
    {
        return delegate.isBeforeMethodConfiguration();
    }

    @Override
    public boolean isAfterMethodConfiguration()
    {
        return delegate.isAfterMethodConfiguration();
    }

    @Override
    public boolean isBeforeClassConfiguration()
    {
        return delegate.isBeforeClassConfiguration();
    }

    @Override
    public boolean isAfterClassConfiguration()
    {
        return delegate.isAfterClassConfiguration();
    }

    @Override
    public boolean isBeforeSuiteConfiguration()
    {
        return delegate.isBeforeSuiteConfiguration();
    }

    @Override
    public boolean isAfterSuiteConfiguration()
    {
        return delegate.isAfterSuiteConfiguration();
    }

    @Override
    public boolean isBeforeTestConfiguration()
    {
        return delegate.isBeforeTestConfiguration();
    }

    @Override
    public boolean isAfterTestConfiguration()
    {
        return delegate.isAfterTestConfiguration();
    }

    @Override
    public boolean isBeforeGroupsConfiguration()
    {
        return delegate.isBeforeGroupsConfiguration();
    }

    @Override
    public boolean isAfterGroupsConfiguration()
    {
        return delegate.isAfterGroupsConfiguration();
    }

    @Override
    public long getTimeOut()
    {
        return delegate.getTimeOut();
    }

    @Override
    public void setTimeOut(long timeOut)
    {
        delegate.setTimeOut(timeOut);
    }

    @Override
    public int getInvocationCount()
    {
        return delegate.getInvocationCount();
    }

    @Override
    public void setInvocationCount(int count)
    {
        delegate.setInvocationCount(count);
    }

    @Override
    public int getTotalInvocationCount()
    {
        return delegate.getTotalInvocationCount();
    }

    @Override
    public int getSuccessPercentage()
    {
        return delegate.getSuccessPercentage();
    }

    @Override
    public String getId()
    {
        return delegate.getId();
    }

    @Override
    public void setId(String id)
    {
        delegate.setId(id);
    }

    @Override
    public long getDate()
    {
        return delegate.getDate();
    }

    @Override
    public void setDate(long date)
    {
        delegate.setDate(date);
    }

    @Override
    public boolean canRunFromClass(IClass testClass)
    {
        return delegate.canRunFromClass(testClass);
    }

    @Override
    public boolean isAlwaysRun()
    {
        return delegate.isAlwaysRun();
    }

    @Override
    public int getThreadPoolSize()
    {
        return delegate.getThreadPoolSize();
    }

    @Override
    public void setThreadPoolSize(int threadPoolSize)
    {
        delegate.setThreadPoolSize(threadPoolSize);
    }

    @Override
    public boolean getEnabled()
    {
        return delegate.getEnabled();
    }

    @Override
    public String getDescription()
    {
        return delegate.getDescription();
    }

    @Override
    public void setDescription(String description)
    {
        delegate.setDescription(description);
    }

    @Override
    public void incrementCurrentInvocationCount()
    {
        delegate.incrementCurrentInvocationCount();
    }

    @Override
    public int getCurrentInvocationCount()
    {
        return delegate.getCurrentInvocationCount();
    }

    @Override
    public void setParameterInvocationCount(int n)
    {
        delegate.setParameterInvocationCount(n);
    }

    @Override
    public int getParameterInvocationCount()
    {
        return delegate.getParameterInvocationCount();
    }

    @Override
    public IRetryAnalyzer getRetryAnalyzer()
    {
        return delegate.getRetryAnalyzer();
    }

    @Override
    public void setRetryAnalyzer(IRetryAnalyzer retryAnalyzer)
    {
        delegate.setRetryAnalyzer(retryAnalyzer);
    }

    @Override
    public boolean skipFailedInvocations()
    {
        return delegate.skipFailedInvocations();
    }

    @Override
    public void setSkipFailedInvocations(boolean skip)
    {
        delegate.setSkipFailedInvocations(skip);
    }

    @Override
    public long getInvocationTimeOut()
    {
        return delegate.getInvocationTimeOut();
    }

    @Override
    public boolean ignoreMissingDependencies()
    {
        return delegate.ignoreMissingDependencies();
    }

    @Override
    public void setIgnoreMissingDependencies(boolean ignore)
    {
        delegate.setIgnoreMissingDependencies(ignore);
    }

    @Override
    public List<Integer> getInvocationNumbers()
    {
        return delegate.getInvocationNumbers();
    }

    @Override
    public void setInvocationNumbers(List<Integer> numbers)
    {
        delegate.setInvocationNumbers(numbers);
    }

    @Override
    public void addFailedInvocationNumber(int number)
    {
        delegate.addFailedInvocationNumber(number);
    }

    @Override
    public List<Integer> getFailedInvocationNumbers()
    {
        return delegate.getFailedInvocationNumbers();
    }

    @Override
    public int getPriority()
    {
        return delegate.getPriority();
    }

    @Override
    public void setPriority(int priority)
    {
        delegate.setPriority(priority);
    }

    @Override
    public XmlTest getXmlTest()
    {
        return delegate.getXmlTest();
    }

    @Override
    public ConstructorOrMethod getConstructorOrMethod()
    {
        return delegate.getConstructorOrMethod();
    }

    @Override
    public Map<String, String> findMethodParameters(XmlTest test)
    {
        return delegate.findMethodParameters(test);
    }

    @Override
    public void setMoreInvocationChecker(Callable<Boolean> moreInvocationChecker)
    {
        delegate.setMoreInvocationChecker(moreInvocationChecker);
    }

    @Override
    public boolean hasMoreInvocation()
    {
        return delegate.hasMoreInvocation();
    }

    @Override
    public String getQualifiedName()
    {
        return delegate.getQualifiedName();
    }

    @Override
    public int compareTo(Object o)
    {
        return delegate.compareTo(o);
    }

    @Override
    public String toString()
    {
        return delegate.toString();
    }

    @Override
    abstract public ITestNGMethod clone();
}
