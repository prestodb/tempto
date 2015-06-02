# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os
import subprocess
import sys

from test_common import USER_INTERRUPTION
from test_common import ANNOTATION_LISTENER
from test_runner_argument_builder import TestRunnerArgumentBuilder
from test_runner_parser import TestRunnerParser


def check(popen_args):
    process = subprocess.Popen(popen_args, stdout=subprocess.PIPE,
                               stderr=subprocess.STDOUT)
    unused_stdout, unused_stderr = process.communicate()
    retcode = process.poll()
    return retcode == 0


def listener_arguments():
    return ' '.join([
        '-usedefaultlisteners', 'false',
        '-listener',
        'org.testng.reporters.XMLReporter,' +
        'org.testng.reporters.jq.Main,' +
        ','.join([ANNOTATION_LISTENER])
    ])


def metadata_arguments(test_runner_argument_builder):
    return ' '.join([
        '-suitename', 'everything',
        '-testname all',
        '-verbose', test_runner_argument_builder.testng_verbosity
    ])


def show_results_location(test_runner_argument_builder):
    sys.stdout.write(
        'See ' + os.path.join(test_runner_argument_builder.report_dir,
                              'index.html for detailed results.\n')
    )


def be_playful():
    sys.stdout.write('Loading TestNG run, this may take a sec.  ')
    sys.stdout.write('Please don\'t flip tables ')
    sys.stdout.write(
        u'(\u256F\u00B0\u25A1\u00B0\uFF09\u256F\uFE35 \u253B\u2501\u253B\n'.encode('UTF-8')
    )


def run_testng(test_runner_argument_builder):
    be_playful()
    classpath = ':'.join(
        test_runner_argument_builder.tests_classpath_argument
    )

    # TODO shouldn't we take into account $JAVA_HOME here?
    cmd_to_run = ' '.join([
        'java', '-classpath', classpath,
        test_runner_argument_builder.system_properties,
        test_runner_argument_builder.test_java_properties,
        'org.testng.TestNG', test_runner_argument_builder.suite_xml_if_no_classes_methods,
        test_runner_argument_builder.methods_argument,
        test_runner_argument_builder.classes_argument,
        test_runner_argument_builder.groups_argument,
        test_runner_argument_builder.excluded_groups_argument,
        metadata_arguments(test_runner_argument_builder), listener_arguments(),
        '-d ', test_runner_argument_builder.report_dir])
    result = subprocess.call(cmd_to_run, shell=True)

    show_results_location(test_runner_argument_builder)
    return result


def is_excluded_remote_arg(arg):
    return arg == '-r' or arg == '--remote'


def remove_user_args(sys_args):
    for index in range(len(sys_args)):
        if sys_args[index] == '-u' or sys_args[index] == '--user':
            sys_args.pop(index)
            sys_args.pop(index)
            return

def main():
    try:
        parser = TestRunnerParser()
        args = parser.parse_args()
        test_runner_argument_builder = TestRunnerArgumentBuilder(parser, args)
        return run_testng(test_runner_argument_builder)
    except KeyboardInterrupt:
        sys.stderr.write('\nInterruption detected.  Exiting.\n')
        return USER_INTERRUPTION


if __name__ == '__main__':
    sys.exit(main())
