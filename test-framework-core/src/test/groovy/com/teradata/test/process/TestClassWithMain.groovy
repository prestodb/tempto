/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.process

import static org.assertj.core.api.Assertions.assertThat

class TestClassWithMain
{
  public static final String EXPECTED_ARGUMENT = "foo";
  public static final String EXPECTED_LINE = "hello";
  public static final String PRODUCED_LINE = "world";

  public static void main(String[] args)
  {
    assertThat(args.length).isEqualTo(1)
    assertThat(args[0]).isEqualTo(EXPECTED_ARGUMENT)

    Scanner scanner = new Scanner(System.in)
    assertThat(scanner.nextLine()).isEqualTo(EXPECTED_LINE)
    System.out.println(PRODUCED_LINE)
  }
}
