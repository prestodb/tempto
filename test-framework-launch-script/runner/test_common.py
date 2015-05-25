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
