/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.tpch;

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
