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

import argparse

from argparse_extensions import comma_separated_list, lower_case_list
from argparse_extensions.choice_action import choice_action
from argparse_extensions.combine_action import CombineAction
from argparse_extensions.action_list import action_list


class TestRunnerParser(object):
    """
    This class is responsible for building
    an object capable of parsing command
    line options for the test runner.
    """

    @staticmethod
    def __create_argparser():
        parser = argparse.ArgumentParser(
            prog='product-test',
            description='Run a set of product tests',
            formatter_class=argparse.ArgumentDefaultsHelpFormatter
        )
        TestRunnerParser.__add_test_organization_arguments(parser)
        TestRunnerParser.__add_test_selection_arguments(parser)
        TestRunnerParser.__add_test_environment_arguments(parser)
        TestRunnerParser.__add_miscellaneous_arguments(parser)
        return parser

    @staticmethod
    def __add_test_organization_arguments(parser):
        test_organization_arguments = parser.add_argument_group(
            title='Test Organization Arguments',
            description='Arguments which expose how test cases are organized in the ' +
                        'framework, such as the tests JAR classpath or the available test ' +
                        'suites and test groups.'
        )

        test_organization_arguments.add_argument(
            '--tests-classpath',
            metavar='JAR_PATH[:JAR_PATH...]',
            dest='tests_classpath',
            default=[],
            type=TestRunnerParser.__classpath(),
            action=CombineAction,
            help='Classpath containing test cases'
        )
        test_organization_arguments.add_argument(
            '--tests-package',
            dest='tests_package',
            help='Package containing tests (e.g.: com.tests.*)'
        )
        test_organization_arguments.add_argument(
            '--test-configuration',
            dest='test_configuration',
            metavar='TEST_CONFIGURATION_URI',
            default='classpath:/test-configuration.yaml',
            help='URI to Test configuration YAML file. If lacks uri schema defaults to file:. If file is not found defaults to classpath:.'
        )
        test_organization_arguments.add_argument(
            '--test-configuration-local',
            dest='test_configuration_local',
            metavar='TEST_CONFIGURATION_LOCAL_URI',
            default='classpath:/test-configuration-local.yaml',
            help='URI to Test configuration local YAML file. If lacks uri schema defaults to file:. If file is not found defaults to classpath:.'
        )
        test_organization_arguments.add_argument(
            '--report-dir',
            metavar='DIR',
            dest='report_dir',
            required=True,
            help='Directory to store report files'
        )

    @staticmethod
    def __add_test_selection_arguments(parser):
        test_selection_arguments = parser.add_argument_group(
            title='Test Selection Arguments',
            description='Arguments which determine the set of test cases that are run.  ' +
                        'Any combination of these arguments can be specified to fine-tune ' +
                        'the selection.'
        )

        # XXX Make groups and suites not mutually exclusive
        groups_suites = test_selection_arguments.add_mutually_exclusive_group()
        groups_suites.add_argument(
            '--groups', '-g',
            metavar='GROUP[,GROUP...]',
            dest='groups',
            default=[],
            type=TestRunnerParser.__lower_case_comma_separated_list(),
            help='List of test groups to be included in the selection, case-insensitive. Possible values {%(choices)s}'
        )

        test_selection_arguments.add_argument(
            '--classes', '-C',
            metavar='CLASSES',
            dest='classes',
            help='A comma-separated list of test classes to be executed, case-sensitive, fully-qualified.'
        )

        test_selection_arguments.add_argument(
            '--methods', '-M',
            metavar='METHODS',
            dest='methods',
            type=lambda value: value.replace("#", "."),
            help='A comma-separated list of test methods to be executed, case-sensitive, fully-qualified. ' +
                 '\'#\' chars will be replaced by \'.\' chars, so all following arguments will be interpreted ' +
                 'exactly the same way: "module.class.method" "module#class#method" "module.class#method".' +
                 'For convention based sql tests test name is <convention.test.package>.<test_dir_name>' +
                 '.<sql_file_without_extension>'
        )

        test_selection_arguments.add_argument(
            '--exclude-groups', '--exclude', '--all-but-groups', '-x',
            dest='excluded_groups',
            metavar='GROUP[,GROUP...]',
            default=[],
            type=TestRunnerParser.__lower_case_comma_separated_list(),
            help='List of test groups to be *excluded* from the selection, case-insensitive. Possible values {%(choices)s}'
        )

    @staticmethod
    def __add_test_environment_arguments(parser):
        test_environment_arguments = parser.add_argument_group(
            title='Test Environment Arguments',
            description='Arguments which determine the environment the selected test cases ' +
                        'are run on, such as the logging properties.'
        )

        test_environment_arguments.add_argument(
            '--no-logs', '--nologs',
            dest='nologs',
            action='store_true',
            help='Do not collect cluster logs at the end of a test run.'
        )

    @staticmethod
    def __add_miscellaneous_arguments(parser):
        miscellaneous_arguments = parser.add_argument_group(
            title='Miscellaneous Arguments'
        )

        miscellaneous_arguments.add_argument(
            '--debug',
            dest='debug',
            action='store_true',
            help='Whether or not to debug this run of the framework. ' +
                 'Currently the debug port is 5005.'
        )

        miscellaneous_arguments.add_argument(
            '--testng-verbosity',
            dest='testng_verbosity',
            default='0',
            help='The verbosity level at which TestNG will log'
        )

    @staticmethod
    def __classpath():
        return lambda value: value.split(':')

    @staticmethod
    def __lower_case_string():
        return lambda value: value.lower()

    @staticmethod
    def __lower_case_comma_separated_list():
        return lambda value: lower_case_list(comma_separated_list(value))

    def __init__(self):
        self.__argparser = TestRunnerParser.__create_argparser()

    def error(self, message):
        self.__argparser.error(message)

    def parse_args(self):
        return self.__argparser.parse_args()

    def print_help(self):
        return self.__argparser.print_help()

    @property
    def targets(self):
        try:
            return self.parse_args().targets
        except AttributeError:
            return None