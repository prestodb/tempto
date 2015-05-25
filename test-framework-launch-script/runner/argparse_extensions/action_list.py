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


def action_list(action_classes):
    class ActionList(argparse.Action):
        def __init__(self, **kwargs):
            self.__actions = [action_class(**kwargs) for action_class in action_classes]
            super(ActionList, self).__init__(**kwargs)

        """
        This action combines multiple actions that are executed sequentially
        """
        def __call__(self, *args):
            for action in self.__actions:
                action(*args)
    return ActionList
