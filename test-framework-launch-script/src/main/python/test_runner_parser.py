#
# Copyright Hadapt, Inc. 2013
#

import argparse

from test_common import get_groups, get_suites
from argparse_extensions.choice_csv_list import ChoiceCSVList
from argparse_extensions.combine_action import CombineAction

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
                        'framework, such as the available test suites and test groups.'
        )

        test_organization_arguments.add_argument(
            '--list-suites',
            dest='list_suites',
            action='store_true',
            help='Prints a list of all suites available in the framework.'
        )
        test_organization_arguments.add_argument(
            '--list-groups', '--list',
            dest='list_groups',
            action='store_true',
            help='Prints a list of all groups available in the framework.'
        )
        test_organization_arguments.add_argument(
            '--groups-in-suite', '--list-suite-groups',
            metavar='SUITE',
            dest='list_suite_groups',
            nargs='?',
            const='all',
            default='ALL',
            choices=get_suites() + ['all', 'ALL'],
            help='Prints a list of the group(s) in a given suite. Possible values {%(choices)s}'
        )
        test_organization_arguments.add_argument(
            '--suites-for-group', '--which-suite',
            metavar='GROUP',
            dest='which_suite',
            choices=get_groups(),
            help='Prints a list of the suite(s) a given group belongs to. Possible values {%(choices)s}'
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
            action=CombineAction,
            type=ChoiceCSVList(get_groups(), case_sensitive=False),
            help='List of test groups to be included in the selection, case-insensitive. Possible values {%(choices)s}'
        )
        groups_suites.add_argument(
            '--suites',
            metavar='SUITE[,SUITE...]',
            dest='suites',
            action=CombineAction,
            type=ChoiceCSVList(get_suites(), case_sensitive=False),
            help='List of test suites to be included in the selection, case-insensitive. Possible values {%(choices)s}'
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
            type=lambda value : value.replace("#", "."),
            help='A comma-separated list of test methods to be executed, case-sensitive, fully-qualified. ' +
                 '\'#\' chars will be replaced by \'.\' chars, so all following arguments will be interpreted ' +
                 'exactly the same way: "module.class.method" "module#class#method" "module.class#method"'
        )

        test_selection_arguments.add_argument(
            '--exclude-groups', '--exclude', '--all-but-groups', '-x',
            dest='excluded_groups',
            metavar='GROUP[,GROUP...]',
            default=[],
            action=CombineAction,
            type=ChoiceCSVList(get_groups(), case_sensitive=False),
            help='List of test groups to be *excluded* from the selection, case-insensitive. Possible values {%(choices)s}'
        )

    @staticmethod
    def __add_test_environment_arguments(parser):
        test_environment_arguments = parser.add_argument_group(
            title='Test Environment Arguments',
            description='Arguments which determine the environment the selected test cases ' +
                        'are run on, such as the cluster to use or the software to install.'
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
