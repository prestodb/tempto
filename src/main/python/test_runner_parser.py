#
# Copyright Hadapt, Inc. 2013
#

import argparse

from test_common import test_runner_type, get_groups, get_suites
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
            prog=test_runner_type() + '-test',
            description='Run a set of ' + test_runner_type() + 'tests',
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
            '--job', '-j',
            dest='job',
            help='Name of Jenkins job from which to pull artifacts.'
        )
        test_environment_arguments.add_argument(
            '--build', '-b',
            dest='build',
            help='Build to pull artifacts from (must be used with -j). ' +
                 'If none provided and -j is used, will be lastSuccessfulBuild.'
        )
        test_environment_arguments.add_argument(
            '--software-already-installed', '-s',
            dest='software_installed',
            action='store_true',
            help='Indicates that software (e.g., Hadoop, Hadapt) ' +
                 'has already been installed onto the cluster.  Must be ' +
                 'used in combination with a specified cluster.'
        )
        test_environment_arguments.add_argument(
            '--do-not-uninstall', '--dont-uninstall',
            dest='dont_uninstall',
            action='store_true',
            help='Indicates that at the end of a test run, the ' +
                 'software (e.g., Hadoop, Hadapt) should not be uninstalled ' +
                 'whether or not the framework installed them.'
        )

        hadoop_arguments = test_environment_arguments.add_mutually_exclusive_group()
        hadoop_arguments.add_argument(
            '--cloudera', '--cdh', '--cdh4',
            dest='cloudera',
            action='store_true',
            default=True,
            help='Run the tests for Cloudera hadoop.'
        )
        hadoop_arguments.add_argument(
            '--mapr',
            dest='mapr',
            action='store_true',
            help='Run the tests for MapR hadoop.'
        )
        hadoop_arguments.add_argument(
            '--hdp', '--hortonworks',
            dest='hdp',
            action='store_true',
            help='Run the tests for Hortonworks hadoop.'
        )

        test_environment_arguments.add_argument(
            '--cluster', '-c',
            dest='cluster',
            help='Name of cluster to run test against (as specified by hfab).'
        )
        test_environment_arguments.add_argument(
            '--instance-type', '-i',
            dest='instance_type',
            help='Type of AWS instance to run the test on. If not specified, ' +
            'uses the test framework default (only applies to EC2).'
        )
        test_environment_arguments.add_argument(
            '--no-logs', '--nologs',
            dest='nologs',
            action='store_true',
            help='Do not collect cluster logs at the end of a test run.'
        )
        test_environment_arguments.add_argument(
            '--teradata-cluster', '-t',
            dest='teradata_cluster',
            help='Name of teradata cluster to run agains (as specified by hfab).'
        )

        test_environment_arguments.add_argument(
            '--sandbox',
            dest='sandbox',
            action='store_true',
            default=False,
            help='To run the tests with your local changes. Otherwise some artifacts from automation could be taken.'
        )

        cluster_teardown_arguments = test_environment_arguments.add_mutually_exclusive_group()
        cluster_teardown_arguments.add_argument(
            '--always-teardown-cluster', '--always-teardown',
            dest='always_teardown',
            action='store_true',
            default=False,
            help='Always tear down the cluster that this test ' +
                 'is run against (only applies to EC2).'
        )
        cluster_teardown_arguments.add_argument(
            '--teardown-cluster-if-tests-succeeded', '--teardown-if-success',
            '--teardown-on-success', '--if-tests-succeeded',
            dest='if_tests_succeeded',
            action='store_true',
            default=False,
            help='Tear down the cluster that this test is run ' +
                 'against only if all tests succeeded (only applies to EC2).'
        )
        cluster_teardown_arguments.add_argument(
            '--never-teardown-cluster', '--never-teardown',
            dest='never_teardown',
            action='store_true',
            default=False,
            help='Never tear down the cluster that this test ' +
                 'is run against (only applies to EC2).'
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
            '--debug-listeners',
            metavar='DEBUG_ARGUMENTS',
            dest='debug_listeners',
            const='',
            nargs='?',
            help='Whether or not to debug listeners started on the remote cluster.' +
                 'The port used for each listener is selected dynamically and will ' +
                 'be printed to the console when a listener starts.  The optional ' +
                 'parameter is passed to the hadapt script via its debug flag; ' +
                 'to see the various options, run \'bin/hadapt --help --debug\'.'
        )

        miscellaneous_arguments.add_argument(
            '--build-fat-jar', '--build-jar', '--build-framework',
            '--build-test-framework', '--compile-test',
            dest='build_fat_jar',
            action='store_true',
            help='Rebuild the runnable test JAR before launching the test suite.'
        )

        miscellaneous_arguments.add_argument(
            '--remote', '-r',
            dest='remote',
            action='store_true',
            help='Runs the tests remotely using a jenkins slave.'
        )

        miscellaneous_arguments.add_argument(
            '--user', '-u',
            dest='user',
            help='Which user to run the tests as on Jenkins.'
        )

        miscellaneous_arguments.add_argument(
            '--chef-log-lvl',
            dest='chef_log_lvl',
            default='info',
            choices=['info', 'debug'],
            help='Logging level used for Chef run. Possible choice are '
                 'info or debug.'
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
