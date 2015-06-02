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
import tempfile
from string import Template

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
    def report_dir(self):
        return self.__args.report_dir

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
        groups = self.__groups_string()
        if groups is not None:
            return '-groups ' + groups
        return ''

    @property
    def testng_verbosity(self):
        return self.__args.testng_verbosity

    def __groups_string(self):
        """
        Returns which groups or suites were specified.
        """
        groups = []
        if self.__args.groups is not None:
            groups += self.__args.groups
        if groups:
            return ','.join(groups)

    @property
    def suite_xml_if_no_classes_methods(self):
        if self.classes_argument is '':
            suite_xml_path = tempfile.mktemp(suffix='.xml')
            with open(os.path.join(os.path.dirname(os.path.realpath(__file__)),
                                   'all-testng-template.xml'),
                      'r') as template_xml_file:
                template_xml = Template(template_xml_file.read())
            with open(suite_xml_path, 'w') as suite_xml_file:
                suite_xml_file.write(template_xml.substitute(package=self.__args.tests_package))
            return suite_xml_path
        return ''

    @property
    def system_properties(self):
        return self.__joined_string_excluding_nulls_for([
            self.__logs_collection_string(),
            self.__test_configuration_argument(),
            self.__test_configuration_local_argument()
        ])

    def __test_configuration_local_argument(self):
        return self.__resource_file_argument(self.__args.test_configuration_local, "tempto.configuration.local")

    def __test_configuration_argument(self):
        return self.__resource_file_argument(self.__args.test_configuration, "tempto.configuration")

    def __resource_file_argument(self, resource_path, argument_key):
        if resource_path is not None:
            if resource_path.startswith('file:') or resource_path.startswith('classpath:'):
                resource_uri = resource_path
            elif os.path.isfile(resource_path):
                resource_uri = 'file:' + resource_path
            else:
                resource_uri = 'classpath:' + resource_path
            return '-D%s=%s' % (argument_key,resource_uri)
        return ''

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

    @staticmethod
    def __joined_string_excluding_nulls_for(lst):
        is_not_none = lambda x: (0 if x is None else 1)
        return ' '.join(filter(is_not_none, lst))
