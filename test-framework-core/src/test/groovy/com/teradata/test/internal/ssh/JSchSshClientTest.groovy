/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.ssh

import com.google.common.io.Files
import com.teradata.test.process.CliProcess
import spock.lang.Ignore
import spock.lang.Specification

import java.time.Duration

import static java.nio.charset.StandardCharsets.UTF_8

public class JSchSshClientTest
        extends Specification
{
  private final def HOST = 'master'
  private final def PORT = 22
  private final def USER = 'root'
  private final def PASSWORD = 'vagrant'

  @Ignore
  def 'should connect end execute command on master'()
  {
    setup:
    JSchSshClient client = new JSchSshClient(HOST, PORT, USER, PASSWORD)

    when:
    CliProcess process = client.execute(['echo', 'hello world'])
    String line = process.nextOutputLine()
    process.waitForWithTimeoutAndKill()

    then:
    line == 'hello world'
  }

  @Ignore
  def 'should fail when invalid ssh command'()
  {
    setup:
    JSchSshClient client = new JSchSshClient(HOST, PORT, USER, PASSWORD)

    when:
    CliProcess process = client.execute(['foo'])
    process.waitForWithTimeoutAndKill()

    then:
    thrown(RuntimeException)
  }

  @Ignore
  def 'should fail when timeouted'()
  {
    setup:
    JSchSshClient client = new JSchSshClient(HOST, PORT, USER, PASSWORD)
    CliProcess process = client.execute(['sleep', '10'])

    when:
    process.waitForWithTimeoutAndKill(Duration.ofMillis(100));

    then:
    thrown(RuntimeException)
  }

  @Ignore
  def 'should upload file'()
  {
    setup:
    def client = new JSchSshClient(HOST, PORT, USER, PASSWORD)
    def file = fileWithContent('hello world')
    def suffix = UUID.randomUUID()
    client.upload(file, '/tmp/test_file.txt' + suffix)

    when:
    CliProcess process = client.execute(['cat', '/tmp/test_file.txt' + suffix])
    String line = process.nextOutputLine()
    process.waitForWithTimeoutAndKill()

    then:
    line == 'hello world'
  }

  def fileWithContent(String content)
  {
    File temp = File.createTempFile('tempfile', '.tmp');
    temp.deleteOnExit()
    Files.write(content, temp, UTF_8)
    return temp.toPath()
  }
}
