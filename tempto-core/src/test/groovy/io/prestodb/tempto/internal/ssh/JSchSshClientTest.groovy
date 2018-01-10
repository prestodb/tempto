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
package io.prestodb.tempto.internal.ssh

import com.google.common.io.Files
import io.prestodb.tempto.process.CliProcess
import org.apache.sshd.SshServer
import org.apache.sshd.server.Command
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.PublickeyAuthenticator
import org.apache.sshd.server.auth.UserAuthPassword
import org.apache.sshd.server.auth.UserAuthPublicKey
import org.apache.sshd.server.command.ScpCommandFactory
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.session.ServerSession
import spock.lang.Shared
import spock.lang.Specification

import java.security.PublicKey
import java.time.Duration

import static Thread.sleep
import static java.nio.charset.StandardCharsets.UTF_8

class JSchSshClientTest
        extends Specification
{
    private static final def HOST = 'localhost'
    private static final def PORT = 1234
    private static final def USER = 'test'
    private static final def PASSWORD = 'password'

    @Shared
    private SshServer sshd

    @Shared
    private JschSshClientFactory factory = new JschSshClientFactory()

    def 'should connect end execute command on master'()
    {
        setup:
        JSchSshClient client = factory.create(HOST, PORT, USER, Optional.of(PASSWORD))

        when:
        CliProcess process = client.execute(['echo', 'hello world'])
        String line = process.nextOutputLine()
        process.waitForWithTimeoutAndKill()

        then:
        line == 'hello world'
    }

    def 'should fail when invalid ssh command'()
    {
        setup:
        JSchSshClient client = factory.create(HOST, PORT, USER, Optional.of(PASSWORD))

        when:
        CliProcess process = client.execute(['foo'])
        process.waitForWithTimeoutAndKill()

        then:
        thrown(RuntimeException)
    }

    def 'should fail when timeouted'()
    {
        setup:
        JSchSshClient client = factory.create(HOST, PORT, USER, Optional.of(PASSWORD))
        CliProcess process = client.execute(['sleep', '5'])

        when:
        process.waitForWithTimeoutAndKill(Duration.ofMillis(100));

        then:
        thrown(RuntimeException)
    }

    def 'should upload file'()
    {
        setup:
        JSchSshClient client = factory.create(HOST, PORT, USER, Optional.of(PASSWORD))
        def file = fileWithContent('hello world')
        def suffix = UUID.randomUUID()
        def fileName = "/tmp/test_file.txt_${suffix}"
        client.upload(file, fileName)

        when:
        def content = Files.toString(new File(fileName), UTF_8)

        then:
        content == 'hello world'
    }

    def 'should connect with just a private key'()
    {
        setup:
        factory.addIdentity('src/test/resources/ssh/ssh.pem')
        JSchSshClient client = factory.create(HOST, PORT, USER)

        when:
        CliProcess process = client.execute(['echo', 'hello world'])
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

    def setupSpec()
    {
        sshd = SshServer.setUpDefaultServer()
        sshd.setPort(PORT)
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());

        setupAuthentication()
        setupCommandFactories()

        sshd.start()
    }

    private void setupAuthentication()
    {
        sshd.setUserAuthFactories([new UserAuthPassword.Factory(), new UserAuthPublicKey.Factory()])

        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            boolean authenticate(String username, String password, ServerSession session)
            {
                return USER.equals(username) && PASSWORD.equals(password);
            }
        });

        sshd.setPublickeyAuthenticator(new PublickeyAuthenticator() {
            @Override
            boolean authenticate(String username, PublicKey key, ServerSession session)
            {
                return true
            }
        })
    }

    private void setupCommandFactories()
    {
        CommandFactory delegateFactory = new CommandFactory() {
            @Override
            Command createCommand(String command)
            {
                if (command == '"echo" "hello world"') {
                    return new TestCommand('hello world\n', 0)
                }
                else if (command == '"sleep" "5"') {
                    sleep(5000);
                    return new TestCommand('', 0);
                }
                else {
                    return new TestCommand("Unknown command: ${command}\n", 1)
                }
            }
        }
        sshd.setCommandFactory(new ScpCommandFactory(delegateFactory))
    }

    def cleanupSpec()
    {
        sshd.stop()
    }
}
