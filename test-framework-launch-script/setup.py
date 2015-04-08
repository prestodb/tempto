#
# Copyright 2015, Teradata, Inc. All rights reserved.
#

from setuptools import setup

setup(
    name='product-test',
    version='1.0',
    description='product-test will run your product tests using a custom SQL on Hadoop test harness.',
    author='Teradata Corporation',
    author_email='anton.petrov@teradata.com',
    url='https://github.com/teradatalabs/test-framework',
    packages=['argparse_extensions'],
    py_modules=['test_common', 'test_runner', 'test_runner_argument_builder', 'test_runner_parser'],
    keywords=['sql', 'hadoop', 'test-framework'],
    entry_points={'console_scripts': ['product-test = test_runner:main']}
)