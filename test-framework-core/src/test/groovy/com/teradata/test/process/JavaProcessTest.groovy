/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.process

import spock.lang.Specification

import static JavaProcessLauncher.defaultJavaProcessLauncher
import static com.teradata.test.process.TestClassWithMain.*

class JavaProcessTest
        extends Specification
{
  def 'test execute Java process'()
          throws IOException, InterruptedException
  {
    setup:
    CliProcess child = new CliProcess(defaultJavaProcessLauncher().launch(TestClassWithMain.class, [EXPECTED_ARGUMENT]))
    child.getInput().println(EXPECTED_LINE)

    expect:
    child.readRemainingLines() == [PRODUCED_LINE]
    child.waitWithTimeoutAndKill()
  }
}
