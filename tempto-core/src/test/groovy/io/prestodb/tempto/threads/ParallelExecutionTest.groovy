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
package io.prestodb.tempto.threads

import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger

import static ParallelExecution.parallelExecution

class ParallelExecutionTest
        extends Specification
{
    def 'should execute runnables in parallel'()
    {
        setup:
        def parallelExecutionBuilder = ParallelExecution.builder()
        def executionCount = new AtomicInteger()

        parallelExecutionBuilder.addRunnable(new Runnable() {
            @Override
            void run()
            {
                executionCount.incrementAndGet()
            }
        })

        for (int i = 0; i < 10; i++) {
            final int expectedThreadIndex = i
            parallelExecutionBuilder.addRunnable(new IndexedRunnable() {
                void run(int threadIndex)
                {
                    assert expectedThreadIndex == threadIndex
                    executionCount.incrementAndGet()
                }
            })
        }

        def parallelExecution = parallelExecutionBuilder.build()

        when:
        parallelExecution.start()
        parallelExecution.join()

        then:
        executionCount.get() == 11
    }

    def 'should propagate assertions from runnable'()
    {
        setup:
        def parallelExecutionBuilder = ParallelExecution.builder()

        for (int i = 0; i < 2; i++) {
            parallelExecutionBuilder.addRunnable(new Runnable() {
                @Override
                void run()
                {
                    assert false
                }
            })
        }

        def parallelExecution = parallelExecutionBuilder.build()

        when:
        parallelExecution.start()
        parallelExecution.joinAndRethrow()

        then:
        thrown(ParallelExecutionException)
    }

    def 'should fail timeout'()
    {
        setup:
        def parallelExecution = parallelExecution(1, new IndexedRunnable() {
            @Override
            void run(int threadIndex)
                    throws Exception
            {
                Thread.sleep(500000)
            }
        })

        when:
        parallelExecution.start()

        then:
        !parallelExecution.join(100)
    }
}
