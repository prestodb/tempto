/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.prestodb.tempto.examples;

import com.google.inject.Inject;
import io.prestodb.tempto.ProductTest;
import io.prestodb.tempto.process.CliProcess;
import io.prestodb.tempto.ssh.SshClient;
import io.prestodb.tempto.ssh.SshClientFactory;
import org.testng.annotations.Test;

import javax.inject.Named;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import static java.nio.file.Files.readAllLines;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ExampleSshClientUsage
        extends ProductTest
{
    @Inject
    @Named("host_by_password")
    private SshClient sshClientByPassword;

    @Inject
    @Named("host_by_identity")
    private SshClient sshClientByIdentity;

    @Inject
    private SshClientFactory sshClientFactory;

    @Inject
    @Named("ssh.roles.host_by_password.host")
    private String hostByPassword;

    @Inject
    @Named("ssh.roles.host_by_password.port")
    private Integer hostByPasswordPort;

    @Inject
    @Named("ssh.roles.host_by_identity.host")
    private String hostByIdentity;

    @Inject
    @Named("ssh.roles.host_by_identity.port")
    private Integer hostByIdentityPort;

    @Test(groups = "ssh")
    public void sshClientUsage()
            throws Exception
    {
        Path etcHosts = Paths.get("/etc/hosts");
        sshClientByPassword.upload(etcHosts, "/tmp");
        try (CliProcess catProcess = sshClientByIdentity.execute("cat /tmp/hosts")) {
            assertThat(catProcess.readRemainingOutputLines()).isEqualTo(readAllLines(etcHosts));
            catProcess.waitForWithTimeoutAndKill();
        }
    }

    @Test(groups = "ssh")
    public void dynamicSshClient()
            throws Exception
    {
        SshClient sshClient = sshClientFactory.create(hostByPassword, hostByIdentityPort);
        try (CliProcess remoteProcess = sshClient.execute("echo success")) {
            assertThat(remoteProcess.nextOutputLine()).contains("success");
            remoteProcess.waitForWithTimeoutAndKill();
        }

        sshClient = sshClientFactory.create(hostByIdentity, hostByIdentityPort);
        try (CliProcess remoteProcess = sshClient.execute("echo success")) {
            assertThat(remoteProcess.nextOutputLine()).contains("success");
            remoteProcess.waitForWithTimeoutAndKill();
        }
    }

    @Test(groups = "ssh")
    public void longRunningCommandTimeout()
            throws Exception
    {
        try (CliProcess cliProcess = sshClientByPassword.execute("sleep 30")) {
            // Within this method std::out,std::err will both be printed to the log
            cliProcess.waitForWithTimeoutAndKill(Duration.ofSeconds(10));
            fail("This command should timeout!");
        }
        catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("did not finish within given timeout");
        }
    }
}
