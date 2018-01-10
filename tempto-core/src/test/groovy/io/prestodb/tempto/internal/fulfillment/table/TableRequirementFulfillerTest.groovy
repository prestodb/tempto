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

package io.prestodb.tempto.internal.fulfillment.table

import io.prestodb.tempto.fulfillment.TestStatus
import io.prestodb.tempto.fulfillment.table.ImmutableTableRequirement
import io.prestodb.tempto.fulfillment.table.ImmutableTablesState
import io.prestodb.tempto.fulfillment.table.MutableTableRequirement
import io.prestodb.tempto.fulfillment.table.MutableTablesState
import io.prestodb.tempto.fulfillment.table.TableDefinition
import io.prestodb.tempto.fulfillment.table.TableHandle
import io.prestodb.tempto.fulfillment.table.TableInstance
import io.prestodb.tempto.fulfillment.table.TableManager
import io.prestodb.tempto.fulfillment.table.TableManagerDispatcher
import spock.lang.Specification

import static com.google.common.collect.Iterables.getOnlyElement
import static io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State.CREATED
import static io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State.LOADED
import static io.prestodb.tempto.fulfillment.table.TableHandle.tableHandle
import static junit.framework.TestCase.fail

class TableRequirementFulfillerTest
        extends Specification
{
    private static final String DATABASE_NAME = "database_name"
    private static final String OTHER_DATABASE_NAME = "other_database_name"
    private static final String OTHER_DATABASE_NAME_2 = "other_database_name_2"

    TableManager tableManager = Mock(TableManager)
    TableManager otherTableManager = Mock(TableManager)
    TableManager otherTableManager2 = Mock(TableManager)
    TableManagerDispatcher tableManagerDispatcher

    void setup()
    {
        tableManager.databaseName >> DATABASE_NAME
        tableManager.tableDefinitionClass >> TestTableDefinition
        otherTableManager.databaseName >> OTHER_DATABASE_NAME
        otherTableManager.tableDefinitionClass >> OtherTestTableDefinition
        otherTableManager2.databaseName >> OTHER_DATABASE_NAME_2
        otherTableManager2.tableDefinitionClass >> OtherTestTableDefinition
        tableManagerDispatcher = new DefaultTableManagerDispatcher([
                (DATABASE_NAME)        : tableManager,
                (OTHER_DATABASE_NAME)  : otherTableManager,
                (OTHER_DATABASE_NAME_2): otherTableManager2])
    }

    def "test mutable table fulfill/cleanup"()
    {
        setup:
        def tableDefinition = getTableDefinition("nation")

        def mutableTableInstanceLoaded = new TableInstance(new TableName(DATABASE_NAME, Optional.empty(), "nation", "nation_mutable"), tableDefinition)
        def mutableTableRequirementLoaded = MutableTableRequirement.builder(tableDefinition).build()

        tableManager.createMutable(tableDefinition, LOADED, _) >> mutableTableInstanceLoaded

        MutableTablesFulfiller fulfiller = new MutableTablesFulfiller(tableManagerDispatcher)

        when:
        def states = fulfiller.fulfill([mutableTableRequirementLoaded] as Set)

        assert states.size() == 1
        def state = (MutableTablesState) getOnlyElement(states)
        assert state.get('nation') != null
        assert state.get('nation') == mutableTableInstanceLoaded
        assert state.get(tableHandle('nation').inDatabase(DATABASE_NAME)) == mutableTableInstanceLoaded

        then:
        1 * tableManager.createMutable(tableDefinition, LOADED, _) >> mutableTableInstanceLoaded

        when:
        fulfiller.cleanup(TestStatus.SUCCESS)

        then:
        1 * tableManager.dropTable(_)
    }

    def "test mutable named and created table fulfill/cleanup"()
    {
        setup:
        def tableDefinition = getTableDefinition("nation")

        def tableInstanceName = "table_instance_name"
        def mutableTableInstanceNamedCreated = new TableInstance(new TableName(DATABASE_NAME, Optional.empty(), tableInstanceName, tableInstanceName), tableDefinition)
        def mutableTableRequirementNamedCreated = MutableTableRequirement.builder(tableDefinition)
                .withName(tableInstanceName)
                .withState(CREATED)
                .build()

        tableManager.createMutable(tableDefinition, CREATED, _) >> mutableTableInstanceNamedCreated

        MutableTablesFulfiller fulfiller = new MutableTablesFulfiller(tableManagerDispatcher)

        when:
        def states = fulfiller.fulfill([mutableTableRequirementNamedCreated] as Set)

        assert states.size() == 1
        def state = (MutableTablesState) getOnlyElement(states)
        assert state.get(tableInstanceName) != null
        assert state.get(tableInstanceName) == mutableTableInstanceNamedCreated
        assert state.get(tableHandle(tableInstanceName).inDatabase(DATABASE_NAME)) == mutableTableInstanceNamedCreated

        then:
        1 * tableManager.createMutable(tableDefinition, CREATED, _) >> mutableTableInstanceNamedCreated

        when:
        fulfiller.cleanup(TestStatus.FAILURE)

        then:
        0 * _
    }

    def "test immutable table fulfill/cleanup"()
    {
        setup:
        def tableDefinition = getTableDefinition("nation")
        def tableInstance = new TableInstance(new TableName(DATABASE_NAME, Optional.empty(), "nation", "nation"), tableDefinition)
        def requirement = new ImmutableTableRequirement(tableDefinition)

        tableManager.createImmutable(tableDefinition) >> tableInstance

        ImmutableTablesFulfiller fulfiller = new ImmutableTablesFulfiller(tableManagerDispatcher)

        when:
        def states = fulfiller.fulfill([requirement] as Set)

        assert states.size() == 1
        def state = (ImmutableTablesState) getOnlyElement(states)
        assert state.get('nation') == tableInstance
        assert state.get(tableHandle('nation').inDatabase(DATABASE_NAME)) == tableInstance

        then:
        1 * tableManager.createImmutable(tableDefinition) >> tableInstance

        when:
        fulfiller.cleanup()

        then:
        0 * _
    }

    def "test same immutable tables on different databases"()
    {
        setup:
        def tableDefinition = getOtherTableDefinition("nation")
        def tableInstance = new TableInstance(new TableName(OTHER_DATABASE_NAME, Optional.empty(), "nation", "nation"), tableDefinition)
        def tableInstance2 = new TableInstance(new TableName(OTHER_DATABASE_NAME_2, Optional.empty(), "nation", "nation"), tableDefinition)

        def tableHandle = TableHandle.tableHandle("nation").inDatabase(OTHER_DATABASE_NAME)
        def requirement = new ImmutableTableRequirement(tableDefinition, tableHandle)

        def tableHandle2 = TableHandle.tableHandle("nation").inDatabase(OTHER_DATABASE_NAME_2)
        def requirement2 = new ImmutableTableRequirement(tableDefinition, tableHandle2)

        otherTableManager.createImmutable(tableDefinition) >> tableInstance
        otherTableManager2.createImmutable(tableDefinition) >> tableInstance2

        ImmutableTablesFulfiller fulfiller = new ImmutableTablesFulfiller(tableManagerDispatcher)

        when:
        def states = fulfiller.fulfill([requirement, requirement2] as Set)

        assert states.size() == 1
        def state = (ImmutableTablesState) getOnlyElement(states)
        assert state.get(tableHandle) == tableInstance
        assert state.get(tableHandle2) == tableInstance2
        try {
            state.get('nation')
            fail('Expected exception')
        }
        catch (RuntimeException ex) {
            assert ex.message.contains('please use more detailed table handle')
        }

        then:
        1 * otherTableManager.createImmutable(tableDefinition) >> tableInstance
        1 * otherTableManager2.createImmutable(tableDefinition) >> tableInstance2

        when:
        fulfiller.cleanup()

        then:
        0 * _
    }

    def "test same immutable tables on same databases with different database aliases are filtered"()
    {
        setup:
        def tableDefinition = getTableDefinition("nation")
        def tableInstance = new TableInstance(new TableName(DATABASE_NAME, Optional.empty(), "nation", "nation"), tableDefinition)
        def requirement = new ImmutableTableRequirement(tableDefinition, tableHandle("nation").inDatabase(DATABASE_NAME))
        def requirementOnDefault = new ImmutableTableRequirement(tableDefinition)

        tableManager.createImmutable(tableDefinition) >> tableInstance

        ImmutableTablesFulfiller fulfiller = new ImmutableTablesFulfiller(tableManagerDispatcher)

        when:
        def states = fulfiller.fulfill([requirement, requirementOnDefault] as Set)

        assert states.size() == 1
        def state = (ImmutableTablesState) getOnlyElement(states)
        assert state.get(tableHandle('nation').inDatabase(DATABASE_NAME)) == tableInstance
        assert state.get('nation') == tableInstance

        then:
        1 * tableManager.createImmutable(tableDefinition) >> tableInstance

        when:
        fulfiller.cleanup()

        then:
        0 * _
    }

    def getTableDefinition(String tableName)
    {
        return new TestTableDefinition(tableHandle(tableName))
    }

    def getOtherTableDefinition(String tableName)
    {
        return new OtherTestTableDefinition(tableHandle(tableName))
    }

    static class TestTableDefinition
            extends TableDefinition
    {
        TestTableDefinition(TableHandle handle)
        {
            super(handle)
        }
    }

    static class OtherTestTableDefinition
            extends TableDefinition
    {
        OtherTestTableDefinition(TableHandle handle)
        {
            super(handle)
        }
    }
}
