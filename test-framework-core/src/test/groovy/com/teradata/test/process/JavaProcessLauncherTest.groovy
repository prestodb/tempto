/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.process

import spock.lang.Specification

import static JavaProcessLauncher.defaultJavaProcessLauncher
import static com.teradata.test.process.TestClassWithMain.*

class JavaProcessLauncherTest
        extends Specification
{
  def 'test execute CLI Java process'()
          throws IOException, InterruptedException
  {
    setup:
    LocalCliProcess child = new LocalCliProcess(defaultJavaProcessLauncher().launch(TestClassWithMain.class, [EXPECTED_ARGUMENT]))
    child.getProcessInput().println(EXPECTED_LINE)

    expect:
    child.readRemainingOutputLines() == [PRODUCED_LINE]
    child.waitForWithTimeoutAndKill()
  }
}
