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


PACKAGE_PREFIX = 'com.teradata.test'

LOGGING_LISTENER = PACKAGE_PREFIX + '.listeners.ProgressLoggingListener'
ANNOTATION_LISTENER = PACKAGE_PREFIX + '.listeners.ProductTestAnnotationTransformer'
INITIALIZATION_LISTENER = PACKAGE_PREFIX + '.initialization.TestInitializationListener'
SUITES_FILE = 'suites.json'

def repo_root():
    try:
        return os.environ['SCRIPT_ROOT']
    except KeyError:
        sys.stderr.write(
            'Did not detect script root. Did you run using bin/*-test?\n'
        )
        sys.exit(ENVIRONMENT_ERROR)


def framework_root():
    return os.path.join(repo_root(), 'test-framework-core')


def reporting_dir():
    return os.path.join(framework_root(), 'build/reports/')


def raw_dict_for(jsonfile):
    return dict(json.load(jsonfile))


def dict_without_private_fields_of(py_dict):
    return dict(
        (key, py_dict[key]) for key in py_dict if not key.startswith('__')
    )


def get_public_dict_for(classpath):
    for path in classpath:
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
        "No %s provided on classpath\n" % SUITES_FILE
    )
    sys.exit(ENVIRONMENT_ERROR)


def get_suites_dict(classpath):
    return get_public_dict_for(classpath)


def get_suite_groups(suite, classpath):
    return get_suites_dict(classpath)[suite]


def get_suites(classpath):
    return get_suites_dict(classpath).keys()


def get_sorted_suites(classpath):
    suites = get_suites(classpath)
    suites.sort()
    return suites


def get_groups(classpath):
    groups = get_suites_dict(classpath).values()
    groups = [group for group_lst in groups for group in group_lst]
    # Ensure uniqueness, but convert back to list
    return list(set(groups))


def get_sorted_groups(classpath):
    groups = get_groups(classpath)
    groups.sort()
    return groups
