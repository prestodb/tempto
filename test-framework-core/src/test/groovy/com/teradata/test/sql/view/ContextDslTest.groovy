/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.sql.view

import com.teradata.test.context.ContextDsl
import com.teradata.test.context.ContextRunnable
import com.teradata.test.query.QueryExecutor
import com.teradata.test.query.QueryResult
import spock.lang.Specification

import static com.teradata.test.query.QueryType.UPDATE

class ContextDslTest
        extends Specification
{

  def 'executeWithView'()
  {
    setup:
    String viewName = "test_view";
    String selectSql = "SELECT * FROM nation"
    ContextRunnable testRunnable = Mock(ContextRunnable)
    QueryExecutor queryExecutor = Mock(QueryExecutor)
    queryExecutor.executeQuery("DROP VIEW test_view", UPDATE) >> QueryResult.forSingleIntegerValue(1)
    queryExecutor.executeQuery("CREATE VIEW test_view AS SELECT * FROM nation", UPDATE) >> QueryResult.forSingleIntegerValue(1)
    queryExecutor.executeQuery("DROP VIEW test_view", UPDATE) >> QueryResult.forSingleIntegerValue(1)

    ViewContextProvider contextProvider = new ViewContextProvider(viewName, selectSql, queryExecutor)

    when:
    ContextDsl.executeWith(contextProvider, testRunnable)

    then:
    1 * testRunnable.run(_)
  }
}
