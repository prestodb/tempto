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

package io.prestodb.tempto.fulfillment.table.hive.tpch;

/**
 * Enum containing names of all TPCH tables. Moreover it holds reference to
 * {@link io.airlift.tpch.TpchTable} entity which is used for generating data.
 */
public enum TpchTable
{
    NATION(io.airlift.tpch.TpchTable.NATION),
    REGION(io.airlift.tpch.TpchTable.REGION),
    PART(io.airlift.tpch.TpchTable.PART),
    ORDERS(io.airlift.tpch.TpchTable.ORDERS),
    CUSTOMER(io.airlift.tpch.TpchTable.CUSTOMER),
    SUPPLIER(io.airlift.tpch.TpchTable.SUPPLIER),
    LINE_ITEM(io.airlift.tpch.TpchTable.LINE_ITEM),
    PART_SUPPLIER(io.airlift.tpch.TpchTable.PART_SUPPLIER);

    private final io.airlift.tpch.TpchTable airliftTpchTableEntity;

    TpchTable(io.airlift.tpch.TpchTable airliftTpchTableEntity)
    {
        this.airliftTpchTableEntity = airliftTpchTableEntity;
    }

    public io.airlift.tpch.TpchTable getTpchTableEntity()
    {
        return airliftTpchTableEntity;
    }
}
