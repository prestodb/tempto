/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

public interface HiveDataSourceWriter
{
    void ensureDataOnHdfs(DataSource dataSource);
}
