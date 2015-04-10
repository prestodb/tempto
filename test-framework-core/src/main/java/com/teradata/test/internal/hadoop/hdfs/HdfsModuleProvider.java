/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.hadoop.hdfs;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.hadoop.hdfs.HdfsClient;
import com.teradata.test.internal.initialization.SuiteModuleProvider;

public class HdfsModuleProvider
        implements SuiteModuleProvider
{
    @Override
    public Module getModule(Configuration configuration)
    {
        return new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind(HdfsClient.class).to(WebHDFSClient.class);
                bind(HdfsDataSourceWriter.class).to(DefaultHdfsDataSourceWriter.class);
            }
        };
    }
}
