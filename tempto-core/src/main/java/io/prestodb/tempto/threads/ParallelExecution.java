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
package io.prestodb.tempto.threads;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.synchronizedList;
import static java.util.stream.Collectors.toList;

/**
 * A class implementing parallel execution of code blocks.
 */
public class ParallelExecution
{
    private final List<Thread> threads;
    private final List<Throwable> throwables = synchronizedList(newArrayList());

    private ParallelExecution(List<IndexedRunnable> runnables)
    {
        threads = asThreads(runnables);
    }

    public ParallelExecution start()
    {
        threads.stream().forEach(Thread::start);
        return this;
    }

    /**
     * Joins all child threads and throws {@link ParallelExecutionException} if some
     * child throws a {@link Throwable}.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    public void joinAndRethrow()
            throws InterruptedException
    {
        joinAndRethrow(0L);
    }

    /**
     * @param timeout Milliseconds
     * @return true if child threads were successfully joined within given timeout.
     * @throws InterruptedException if the thread is interrupted
     */
    public boolean joinAndRethrow(long timeout)
            throws InterruptedException
    {
        boolean joinedWithinTimeout = join(timeout);

        if (!throwables.isEmpty()) {
            throw new ParallelExecutionException(throwables);
        }

        return joinedWithinTimeout;
    }

    public void join()
            throws InterruptedException
    {
        join(0L);
    }

    public boolean join(long timeout)
            throws InterruptedException
    {
        if (timeout == 0) {
            timeout = Long.MAX_VALUE;
        }

        long startTime = currentTimeMillis();

        for (Thread thread : threads) {
            long remaining = calculateRemainingTime(startTime, timeout);
            if (remaining > 0) {
                thread.join(remaining);
            }
        }

        return calculateRemainingTime(startTime, timeout) > 0;
    }

    private long calculateRemainingTime(long startTime, long timeout)
    {
        long elapsed = currentTimeMillis() - startTime;
        return timeout - elapsed;
    }

    /**
     * @return {@link Throwable}s that were caught in child threads during execution.
     */
    public List<Throwable> getThrowables()
    {
        return throwables;
    }

    private List<Thread> asThreads(List<IndexedRunnable> runnables)
    {
        List<Thread> threads = newArrayList();
        for (int i = 0; i < runnables.size(); ++i) {
            final int threadIndex = i;
            threads.add(new Thread(() -> {
                try {
                    runnables.get(threadIndex).run(threadIndex);
                }
                catch (Throwable throwable) {
                    throwables.add(throwable);
                }
            }));
        }
        return threads;
    }

    public static ParallelExecution parallelExecution(int nTimes, IndexedRunnable indexedRunnable)
    {
        return builder().addRunnable(nTimes, indexedRunnable).build();
    }

    public static ParallelExecutionBuilder builder()
    {
        return new ParallelExecutionBuilder();
    }

    public static class ParallelExecutionBuilder
    {
        private final List<IndexedRunnable> indexedRunnables = newArrayList();
        private final List<Runnable> runnables = newArrayList();

        public ParallelExecutionBuilder addRunnable(IndexedRunnable indexedRunnable)
        {
            return addRunnable(1, indexedRunnable);
        }

        public ParallelExecutionBuilder addRunnable(int nTimes, IndexedRunnable indexedRunnable)
        {
            for (int i = 0; i < nTimes; ++i) {
                indexedRunnables.add(indexedRunnable);
            }
            return this;
        }

        public ParallelExecutionBuilder addRunnable(Runnable runnable)
        {
            runnables.add(runnable);
            return this;
        }

        public ParallelExecution build()
        {
            List<IndexedRunnable> allIndexedRunnables =
                    ImmutableList.<IndexedRunnable>builder()
                            .addAll(indexedRunnables)
                            .addAll(asParallelRunnables(runnables))
                            .build();
            return new ParallelExecution(allIndexedRunnables);
        }
    }

    private static List<IndexedRunnable> asParallelRunnables(List<Runnable> runnables)
    {
        return runnables
                .stream()
                .map((Runnable runnable) -> (IndexedRunnable) (int threadIndex) -> runnable.run())
                .collect(toList());
    }
}
