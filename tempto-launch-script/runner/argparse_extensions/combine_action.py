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

import argparse


class CombineAction(argparse.Action):

    def __init__(self, **kwargs):
        self.__default = kwargs['default'] if 'default' in kwargs else None
        super(CombineAction, self).__init__(**kwargs)

    """
    This action combines multiple LIST arguments into one, by joining all of them
    """
    def __call__(self, parser, namespace, values, option_string=None):
        if hasattr(namespace, self.dest):
            old_values = getattr(namespace, self.dest)
            if old_values is not self.__default:
                values = values + old_values
        setattr(namespace, self.dest, values)
