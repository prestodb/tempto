/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.fulfillment.hive;

import com.teradata.test.fulfillment.hive.DataSource;

import java.util.Optional;

public interface HiveDataSourceWriter
{
    void ensureDataOnHdfs(DataSource dataSource, Optional<String> customDataSourcePath);
}
