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
#
# extra listeners added through ProductTest super class are:
#  - TestInitializationListener


def raw_dict_for(jsonfile):
    return dict(json.load(jsonfile))


def dict_without_private_fields_of(py_dict):
    return dict(
        (key, py_dict[key]) for key in py_dict if not key.startswith('__')
    )
