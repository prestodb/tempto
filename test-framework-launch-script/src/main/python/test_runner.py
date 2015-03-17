#
# Copyright Hadapt, Inc. 2014
#

import os
import subprocess
import sys

from test_common import FAILURE, SUCCESS, USER_INTERRUPTION
from test_common import LOGGING_LISTENER, ANNOTATION_LISTENER, INITIALIZATION_LISTENER
from test_common import get_sorted_groups, get_sorted_suites, get_suite_groups
from test_common import repo_root, reporting_dir, framework_root, \
    example_tests_root
from test_runner_argument_builder import TestRunnerArgumentBuilder
from test_runner_parser import TestRunnerParser


def check(popen_args):
    process = subprocess.Popen(popen_args, stdout=subprocess.PIPE,
                               stderr=subprocess.STDOUT)
    unused_stdout, unused_stderr = process.communicate()
    retcode = process.poll()
    return retcode == 0


def password_argument():
    try:
        os.environ['JENKINS_PASSWORD']
        return None
    except KeyError:
        return ['-p']


def vcs_appropriate_submitter(user, hadoop_distro, all_args):
    COMMON_ARGS = [
        '-u', user,
        '-a', "PRODUCT_TEST_ARGS:{all_args}".format(
            all_args=' '.join(all_args)
        )
    ]
    if password_argument() is not None:
        COMMON_ARGS = COMMON_ARGS + password_argument()

    if check(["git", "rev-parse", "--show-toplevel"]):
        BASE_ARGS = [
            os.path.join(repo_root(), 'bin', 'sandbox'),
            '-j', 'sandbox-build-test-product' +
            '-' + hadoop_distro,
        ]
    else:
        BASE_ARGS = ["/bin/false"]
    return BASE_ARGS + COMMON_ARGS


def listener_arguments():
    return ' '.join([
        '-usedefaultlisteners', 'false',
        '-listener', 'org.uncommons.reportng.HTMLReporter,' +
        'org.uncommons.reportng.JUnitXMLReporter,' +
        'org.testng.reporters.XMLReporter,' +
        ','.join([LOGGING_LISTENER, ANNOTATION_LISTENER, INITIALIZATION_LISTENER])

    ])


def metadata_arguments(test_runner_argument_builder):
    return ' '.join([
        '-suitename', 'everything',
        '-testname all',
        '-verbose', test_runner_argument_builder.testng_verbosity
    ])


def show_results_location():
    sys.stdout.write(
        'See ' + os.path.join(reporting_dir(),
                              'html/index.html for detailed results.\n')
    )


def be_playful():
    sys.stdout.write('Loading TestNG run, this may take a sec.  ')
    sys.stdout.write('Please don\'t flip tables ')
    sys.stdout.write(
        u'(\u256F\u00B0\u25A1\u00B0\uFF09\u256F\uFE35 \u253B\u2501\u253B\n'.encode('UTF-8')
    )


def run_testng(test_runner_argument_builder):
    be_playful()
    classpath = ':'.join([
        os.path.join(framework_root(), 'build/libs/test-framework-core-all.jar'),
        os.path.join(example_tests_root(), 'build/libs/test-framework-examples.jar')])

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
        '-d ', reporting_dir()])
    result = subprocess.call(cmd_to_run, shell=True)

    show_results_location()
    return result


def build_fat_jar(test_runner_argument_builder):
    return subprocess.call(' '.join([
        os.path.join(repo_root(), 'gradlew'),
        ':test-framework-core:buildFatJar'
    ]), shell=True)


def is_excluded_remote_arg(arg):
    return arg == '-r' or arg == '--remote'


def remove_user_args(sys_args):
    for index in range(len(sys_args)):
        if sys_args[index] == '-u' or sys_args[index] == '--user':
            sys_args.pop(index)
            sys_args.pop(index)
            return


def run_remote_tests(args):
    if args.mapr:
        hadoop_distro = 'mapr'
    elif args.hdp:
        hadoop_distro = 'hdp'
    else:
        hadoop_distro = 'cdh'

    user = os.getenv('USER') if not args.user else args.user
    remove_user_args(sys.argv)

    all_args = [a for a in sys.argv[1:] if not is_excluded_remote_arg(a)]

    return subprocess.call(vcs_appropriate_submitter(user, hadoop_distro, all_args))


def list_suites():
    sys.stdout.write('Available suites:\n\n\t')
    sys.stdout.write('\n\t'.join(get_sorted_suites()))
    sys.stdout.write('\n')
    return SUCCESS


def list_groups():
    sys.stdout.write('Available groups:\n\n\t')
    sys.stdout.write('\n\t'.join(get_sorted_groups()))
    sys.stdout.write('\n')
    return SUCCESS


def list_suite_groups(suites):
    sys.stdout.write(
        'Available groups for suites [{suites}]:\n'.format(
            suites=' '.join(suites)
        )
    )
    for suite in suites:
        sys.stdout.write('\n\t' + suite + '\n\t' + '=' * len(suite))
        sys.stdout.write('\n\t\t')
        try:
            sys.stdout.write('\n\t\t'.join(get_suite_groups(suite)))
        except KeyError:
            sys.stdout.write('[NO GROUPS]\n')
            return FAILURE
        sys.stdout.write('\n')


def which_suite(group):
    sys.stdout.write(group + ': ')
    suites = []
    for suite in get_sorted_suites():
        if group in get_suite_groups(suite):
            suites.append(suite)
    if not suites:
        sys.stdout.write('[NO SUITES]\n')
        return FAILURE
    sys.stdout.write(', '.join(suites) + '\n')
    return SUCCESS


def determine_list_suite_groups(args):
    if args.list_suite_groups == 'all':
        return list_suite_groups(get_sorted_suites())
    else:
        return list_suite_groups(args.list_suite_groups.split(','))


def main():
    try:
        parser = TestRunnerParser()
        args = parser.parse_args()
        test_runner_argument_builder = TestRunnerArgumentBuilder(parser, args)
        if args.list_groups:
            return list_groups()
        elif args.list_suites:
            return list_suites()
        elif args.list_suite_groups != 'ALL':  # default value
            return determine_list_suite_groups(args)
        elif args.which_suite:
            return which_suite(args.which_suite)
        elif args.remote:
            return run_remote_tests(args)
        else:
            if args.build_fat_jar and build_fat_jar(test_runner_argument_builder) != 0:
                return FAILURE
            return run_testng(test_runner_argument_builder)
    except KeyboardInterrupt:
        sys.stderr.write('\nInterruption detected.  Exiting.\n')
        return USER_INTERRUPTION


if __name__ == '__main__':
    sys.exit(main())
