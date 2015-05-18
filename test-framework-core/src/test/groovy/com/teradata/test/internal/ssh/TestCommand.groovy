/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.ssh

import org.apache.sshd.server.Command
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback

class TestCommand
        implements Command
{
  private final String response;
  private final int errorCode;

  private InputStream input;
  private OutputStream output;
  private OutputStream error;
  private ExitCallback callback;

  public TestCommand(String response, int errorCode)
  {
    this.response = response;
    this.errorCode = errorCode;
  }

  public void setInputStream(InputStream input)
  {
    this.input = input;
  }

  public void setOutputStream(OutputStream out)
  {
    this.output = out;
  }

  public void setErrorStream(OutputStream err)
  {
    this.error = err;
  }

  public void setExitCallback(ExitCallback callback)
  {
    this.callback = callback;
  }

  public void start(Environment env)
          throws IOException
  {
    output.write(response.getBytes());
    output.flush();
    callback.onExit(errorCode, response);
  }

  public void destroy()
  {
  }
}
