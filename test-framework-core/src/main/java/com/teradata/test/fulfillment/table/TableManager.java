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
package com.teradata.test.fulfillment.table;

import com.teradata.test.context.TestContext;
import com.teradata.test.fulfillment.table.MutableTableRequirement.State;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.teradata.test.context.TestContextDsl.runWithTestContext;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static com.teradata.test.fulfillment.table.MutableTableRequirement.State.LOADED;
import static com.teradata.test.fulfillment.table.TableManagerDispatcher.getTableManagerDispatcher;

/**
 * Provides functionality of creating/dropping tables based on {@link TableDefinition}.
 */
public interface TableManager
{
    /**
     * {@link TableManager} classes annotated with this annotation
     * will be automatically scanned for.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface AutoTableManager
    {
        Class<? extends TableDefinition> tableDefinitionClass();

        String name();
    }

    TableInstance createImmutable(TableDefinition tableDefinition);

    TableInstance createMutable(TableDefinition tableDefinition, State state);

    default TableInstance createMutable(TableDefinition tableDefinition)
    {
        return createMutable(tableDefinition, LOADED);
    }

    void drop(TableInstance tableInstance);

    /**
     * Makes a {@link TableInstance} dropped when a given {@link TestContext} is closed.
     */
    default void dropOnTestContextClose(TestContext testContext, TableInstance tableInstance)
    {
        testContext.registerCloseCallback(context -> runWithTestContext(context, () -> drop(tableInstance)));
    }

    default void dropOnTestContextClose(TableInstance tableInstance)
    {
        dropOnTestContextClose(testContext(), tableInstance);
    }

    public static TableInstance createImmutableTable(TableDefinition tableDefinition)
    {
        return getTableManagerDispatcher().getTableManagerFor(tableDefinition).createImmutable(tableDefinition);
    }

    public static TableInstance createMutableTable(TableDefinition tableDefinition, State state)
    {
        return getTableManagerDispatcher().getTableManagerFor(tableDefinition).createMutable(tableDefinition, state);
    }

    public static TableInstance createMutableTable(TableDefinition tableDefinition)
    {
        return getTableManagerDispatcher().getTableManagerFor(tableDefinition).createMutable(tableDefinition);
    }

    public static void dropTable(TableInstance tableInstance)
    {
        getTableManagerDispatcher().getTableManagerFor(tableInstance).drop(tableInstance);
    }

    public static void dropTableOnTestContextClose(TableInstance tableInstance)
    {
        getTableManagerDispatcher().getTableManagerFor(tableInstance).dropOnTestContextClose(tableInstance);
    }
}
