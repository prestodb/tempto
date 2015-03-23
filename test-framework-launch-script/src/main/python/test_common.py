#
# Copyright Hadapt, Inc. 2013
#

import json
import os
import sys


# Error codes
(
    SUCCESS,
    FAILURE,
    ENVIRONMENT_ERROR,
    USER_INTERRUPTION
) = range(0, 4)


PACKAGE_PREFIX = 'com.teradata.test'

LOGGING_LISTENER = PACKAGE_PREFIX + '.listeners.ProgressLoggingListener'
ANNOTATION_LISTENER = PACKAGE_PREFIX + '.listeners.ProductTestAnnotationTransformer'
INITIALIZATION_LISTENER = PACKAGE_PREFIX + '.initialization.TestInitializationListener'

def repo_root():
    try:
        return os.environ['SCRIPT_ROOT']
    except KeyError:
        sys.stderr.write(
            'Did not detect script root. Did you run using bin/*-test?\n'
        )
        sys.exit(ENVIRONMENT_ERROR)

SUITES_FILE = os.path.join(
    repo_root(), 'test-framework-examples/src/main/resources/suites.json'
)

def framework_root():
    return os.path.join(repo_root(), 'test-framework-core')

def reporting_dir():
    return os.path.join(framework_root(), 'build/reports/')

def raw_dict_for(filename):
    return dict(json.load(open(filename)))


def dict_without_private_fields_of(py_dict):
    return dict(
        (key, py_dict[key]) for key in py_dict if not key.startswith('__')
    )

def get_public_dict_for(filename):
    return dict_without_private_fields_of(raw_dict_for(filename))


def get_suites_dict():
    return get_public_dict_for(SUITES_FILE)


def get_suite_groups(suite):
    return get_suites_dict()[suite]


def get_suites():
    return get_suites_dict().keys()


def get_sorted_suites():
    suites = get_suites()
    suites.sort()
    return suites


def get_groups():
    groups = get_suites_dict().values()
    groups = [group for group_lst in groups for group in group_lst]
    # Ensure uniqueness, but convert back to list
    return list(set(groups))


def get_sorted_groups():
    groups = get_groups()
    groups.sort()
    return groups
