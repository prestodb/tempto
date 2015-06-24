#!/opt/py26/bin/python


"""
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

 This script simplifies the task of executing the Java SqlResultGenerator program.
"""

import argparse
import subprocess
import os
import glob

CLASSPATH = "..:./libs/expected-result-generator-all-@version@.jar"
JAVA_CLASS = "com.teradata.tempto.sql.SqlResultGenerator"

def setup_argument_parser():
    parser = argparse.ArgumentParser(description="SQL Query Result generator.")
    parser.add_argument("--properties", dest='db_properties', required=True,
                        metavar='"Database properties file"')
    test_source_group = parser.add_mutually_exclusive_group(required=True)
    test_source_group.add_argument('-s', metavar='"SQL file or directory"',
                                   help="Test SQL file (or directory of SQL files)" +
                                   " to generate expected results for")
    test_source_group.add_argument('--test-list-file', dest='test_list_file',
                                   help='File listing SQL files (or directories of SQL ' +
                                   'files) to generate expected results for, one per line')
    return parser


def generate_test_list(sql_file_or_dir, parser):
    test_list = set()
    if os.path.isfile(sql_file_or_dir):
        test_list.add(sql_file_or_dir)
    elif os.path.isdir(sql_file_or_dir):
        for file in os.listdir(sql_file_or_dir):
            extension = os.path.splitext(file)[1].lower()
            test_file = os.path.join(sql_file_or_dir, file)
            if extension == '.sql':
                test_list.add(test_file)
    else:
        usage(parser, 'File or directory {0} does not exist.'
              .format(sql_file_or_dir))

    return test_list


def read_test_list_file(test_list_file, parser):
    test_list = set()
    if os.path.isfile(test_list_file):
        with open(test_list_file, 'r') as tests:
            for test in tests:
                test = test.strip()
                if (not test == "") and (not test.startswith("#")):
                    test_list |= generate_test_list(test, parser)
    if len(test_list) == 0:
        usage(parser, 'No tests were specified')
    return test_list


def get_test_list(parser, args):
    if args.test_list_file:
        return read_test_list_file(args.test_list_file, parser)
    else:
        return generate_test_list(args.s, parser)


def usage(parser, error_message):
    parser.print_help()
    if (error_message):
        print('\nERROR: ' + error_message + '\n')
    exit(1)


def get_db_properties_file(parser, args):
    if args.db_properties:
        properties_file = args.db_properties
        if not os.path.isfile(properties_file):
            usage(parser, 'Database properties file {0} does not exist'
                  .format(properties_file))
    return properties_file


def main():
    parser = setup_argument_parser()
    args = parser.parse_args()

    test_list = get_test_list(parser, args)
    properties_file = get_db_properties_file(parser, args)

    for test in test_list:
        print "Generating expected results for file: " + test
        java_command = ('java -Xmx6g -cp {0} {3} ' +
                        '-p {1} -s {2}').format(CLASSPATH,properties_file, test, JAVA_CLASS)
        subprocess.call(java_command, shell=True)
    print "Finished generating expected results"

if __name__ == '__main__':
    main()
