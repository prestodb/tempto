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
            self.__framework_properties(),
            self.__cluster_string(),
            self.__teradata_cluster_string(),
            self.__teardown_string(),
            self.__uninstall_string(),
            self.__logs_collection_string(),
            self.__software_installed_string(),
            self.__jenkins_job_string(),
            self.__hadoop_string(),
            self.__aws_instance_type(),
            self.__chef_log_lvl(),
            self.__sandbox_string()
        ])

    def __joined_string_excluding_nulls_for(self, lst):
        isNotNone = lambda x: (0 if x is None else 1)
        return ' '.join(filter(isNotNone, lst))

    def __framework_properties(self):
        return (
            '-DperformanceResultDir=' + os.path.join(reporting_dir(),
                                                     'performance')
        )

    def __cluster_string(self):
        """
        Returns a system property for an existing cluster if
        one specified.
        """
        if self.__args.cluster is not None:
            return '-Dcluster=' + self.__args.cluster

    def __teradata_cluster_string(self):
        """
        Returns a system property for an existing teradata cluster
        if one specified
        """
        if self.__args.teradata_cluster is not None:
            return '-Dteradata_cluster=' + self.__args.teradata_cluster

    def __logs_collection_string(self):
        """
        Returns a system property for whether or not to collect logs
        after a test run.
        """
        if self.__args.nologs:
            return '-Dcollect-logs=false'

    def __uninstall_string(self):
        """
        Returns the system property for whether or not to uninstall software at
        the end of a test run.
        """
        if self.__args.dont_uninstall:
            return '-Duninstall-software=false'

    def __teardown_string(self):
        """
        Returns the system property for EC2 teardown, if any
        """
        if self.__args.always_teardown:
            return '-Dec2-teardown-policy=always'
        if self.__args.if_tests_succeeded:
            return '-Dec2-teardown-policy=if_all_tests_succeeded'
        if self.__args.never_teardown:
            return '-Dec2-teardown-policy=never'

    def __software_installed_string(self):
        """
        Returns a system property to not install software
        onto the existing cluster if the user desires.  This
        will throw a parse error if no cluster was specified.
        """
        if self.__args.software_installed:
            if self.__cluster_string() is None:
                self.__parser.error(
                    'Cannot say that software is already installed ' +
                    'without specifying an existing cluster to run against.'
                )
            return '-Ddo-install-product=false'

    def __jenkins_job_string(self):
        """
        Returns a system property to pull artifacts from a
        Jenkins job.
        """
        if self.__args.job is None and self.__args.build is not None:
            self.__parser.error(
                'Must specify a job if trying to use a build number'
            )
        if self.__args.job is not None:
            system_prop = '-Dhadapt-admin-tool-deploy-job=' + self.__args.job
            if self.__args.build is not None:
                system_prop += ' -Dhadapt-admin-tool-deploy-build=' + \
                               self.__args.build
            return system_prop

    def __hadoop_string(self):
        """
        Returns which hadoop version was specified.
        """
        if self.__args.mapr:
            return '-Dhadoop=mapr'
        elif self.__args.hdp:
            return '-Dhadoop=hdp'
        elif self.__args.cloudera:
            return '-Dhadoop=cdh'
        else:
            self.__parser.error('Invalid hadoop distro specified.')

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

        if self.__args.debug_listeners is not None:
            if self.__args.debug_listeners:
                debug_flags.append('-Ddebug-listeners={0}'.format(self.__args.debug_listeners))
            else:
                debug_flags.append('-Ddebug-listeners')

        return ' '.join(debug_flags)

    def __aws_instance_type(self):
        """
        Returns a system property for the instance type,
        else returns NULL, and the test framework default
        will be used.
        """
        if self.__args.instance_type is not None:
            return '-DinstanceType=' + self.__args.instance_type

    def __chef_log_lvl(self):
        """
        Returns a system property for the logging level to be
        used when running Chef.
        """
        return '-Dchef-log-lvl=' + self.__args.chef_log_lvl

    def __sandbox_string(self):
        """
        Returns a system property for sandbox if specified
        """
        if self.__args.sandbox:
            return '-Dsandbox=true'
