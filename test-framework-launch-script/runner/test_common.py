#
# Copyright Hadapt, Inc. 2013
#

import json
import os
import sys
import zipfile


# Error codes
(
    SUCCESS,
    FAILURE,
    ENVIRONMENT_ERROR,
    USER_INTERRUPTION
) = range(0, 4)


PACKAGE_PREFIX = 'com.teradata.test.internal'

ANNOTATION_LISTENER = PACKAGE_PREFIX + '.listeners.ProductTestAnnotationTransformer'
TEST_METHOD_SELECTOR = PACKAGE_PREFIX + '.listeners.TestNameGroupNameMethodSelector:100'  # 100 is selector priority, anything bigger than default 10 is good
SUITES_FILE = 'suites.json'
#
# extra listeners added through ProductTest super class are:
#  - TestInitializationListener


def raw_dict_for(jsonfile):
    return dict(json.load(jsonfile))


def dict_without_private_fields_of(py_dict):
    return dict(
        (key, py_dict[key]) for key in py_dict if not key.startswith('__')
    )


def get_public_dict_for(tests_classpath):
    for path in tests_classpath:
        if zipfile.is_zipfile(path):
            archive = zipfile.ZipFile(path, 'r')
            if not SUITES_FILE in archive.namelist():
                continue
            return dict_without_private_fields_of(raw_dict_for(archive.open(SUITES_FILE)))
        elif os.path.isdir(path):
            if not SUITES_FILE in os.listdir(path):
                continue
            return dict_without_private_fields_of(raw_dict_for(open(os.path.join(path, SUITES_FILE))))

    sys.stderr.write(
        "No %s provided on tests classpath\n" % SUITES_FILE
    )
    sys.exit(ENVIRONMENT_ERROR)


def get_suites_dict(tests_classpath):
    return get_public_dict_for(tests_classpath)


def get_suite_groups(suite, tests_classpath):
    return get_suites_dict(tests_classpath)[suite]


def get_suites(tests_classpath):
    return get_suites_dict(tests_classpath).keys()


def get_sorted_suites(tests_classpath):
    suites = get_suites(tests_classpath)
    suites.sort()
    return suites


def get_groups(tests_classpath):
    groups = get_suites_dict(tests_classpath).values()
    groups = [group for group_lst in groups for group in group_lst]
    # Ensure uniqueness, but convert back to list
    return list(set(groups))


def get_sorted_groups(tests_classpath):
    groups = get_groups(tests_classpath)
    groups.sort()
    return groups
