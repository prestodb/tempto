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

try:
    from setuptools import setup
except ImportError:
    from distutils.core import setup

# =====================================================
# Welcome to HackLand! We monkey patch the _get_rc_file
# method of PyPIRCCommand so that we can read a .pypirc
# that is located in the current directory. This enables
# us to check it in with the code and not require
# developers to create files in their home directory.
from distutils.config import PyPIRCCommand


def get_custom_rc_file(self):
    home_pypi = os.path.join(os.path.expanduser('~'),
                             '.pypirc')
    local_pypi = os.path.join(
        os.path.dirname(os.path.realpath(__file__)),
        '.pypirc')
    return local_pypi if os.path.exists(local_pypi) \
        else home_pypi

PyPIRCCommand._get_rc_file = get_custom_rc_file
# Thank you for visiting HackLand!
# =====================================================

setup(
    name='product-test',
    version='1.0',
    description='product-test will run your product tests using a custom SQL on Hadoop test harness.',
    author='Teradata Corporation',
    author_email='anton.petrov@teradata.com',
    url='https://github.com/teradatalabs/test-framework',
    packages=['runner', 'runner.argparse_extensions'],
    include_package_data=True,
    package_data={'runner': ['*.xml']},
    keywords=['sql', 'hadoop', 'test-framework'],
    entry_points={'console_scripts': ['product-test = runner.test_runner:main']},
    install_requires=['argparse>=1.3.0']
)