#
# Copyright Hadapt, Inc. 2013
#

import os

from test_common import framework_root, get_suite_groups,\
    reporting_dir


class TestRunnerArgumentBuilder(object):
    """
    This class is responsible for taking in
    a namespace and retrieving the correct
    arguments from it in order to form an argument
    string to pass to the test runner.
    """

    def __init__(self, parser, args):
        self.__parser = parser
        self.__args = args

    @property
    def tests_classpath_argument(self):
        if self.__args.tests_classpath is not None:
            return self.__args.tests_classpath
        return ''

    @property
    def methods_argument(self):
        if self.__args.methods is not None:
            return '-methods ' + self.__args.methods
        return ''

    @property
    def classes_argument(self):
        if self.__args.classes is not None:
            return '-testclass ' + self.__args.classes
        return ''

    @property
    def excluded_groups_argument(self):
        if self.__args.excluded_groups:
            return '-excludegroups ' + ','.join(self.__args.excluded_groups)
        return ''

    @property
    def groups_argument(self):
        groups_or_suites = self.__groups_or_suites_string()
        if groups_or_suites is not None:
            return '-groups ' + groups_or_suites
        return ''

    @property
    def testng_verbosity(self):
        return self.__args.testng_verbosity

    def __groups_or_suites_string(self):
        """
        Returns which groups or suites were specified.
        """
        groups = []
        if self.__args.groups is not None:
            groups += self.__args.groups
        if self.__args.suites:
            for suite in self.__args.suites:
                groups += get_suite_groups(suite)
        if groups:
            return ','.join(groups)

    @property
    def suite_xml_if_no_classes_methods(self):
        if self.classes_argument is '' and self.methods_argument is '':
            return os.path.join(framework_root(),
                                'src/main/resources/all-testng.xml')
        return ''

    @property
    def system_properties(self):
        return self.__joined_string_excluding_nulls_for([
            self.__logs_collection_string(),
        ])

    def __joined_string_excluding_nulls_for(self, lst):
        isNotNone = lambda x: (0 if x is None else 1)
        return ' '.join(filter(isNotNone, lst))

    def __logs_collection_string(self):
        """
        Returns a system property for whether or not to collect logs
        after a test run.
        """
        if self.__args.nologs:
            return '-Dcollect-logs=false'

    @property
    def test_java_properties(self):
        return self.__joined_string_excluding_nulls_for([
            self.__debug_string(),
        ])

    def __debug_string(self):
        """
        Returns debug flags if any were set.
        """
        debug_flags = []
        if self.__args.debug:
            debug_flags.append(
                '-XX:+UseParallelGC ' +
                '-agentlib:jdwp=transport=dt_socket,server=y,address=5005,suspend=y'
            )

        return ' '.join(debug_flags)
