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

from . import ensure_arg_is_list, lower_case_list


def choice_action(choice_func, case_sensitive=True):
    class ChoiceAction(argparse.Action):
        """
        Type for argparse supporting comma separated values and validating all of them
        with predefined choices list
        """
        def __init__(self, **kwargs):
            self.__choice_func = choice_func
            self.__case_sensitive = case_sensitive
            super(ChoiceAction, self).__init__(**kwargs)

        def __get_choices(self, namespace):
            choices = self.__choice_func(namespace)
            if not self.__case_sensitive:
                choices = lower_case_list(choices)
            return choices

        def __call__(self, parser, namespace, values, option_string=None):
            values = ensure_arg_is_list(values)
            if not self.__case_sensitive:
                values = lower_case_list(values)
            choices = self.__get_choices(namespace)
            remainder = sorted(set(values) - set(choices))
            if len(remainder) > 0:
                raise argparse.ArgumentTypeError("invalid choices: %r (choose from %r)"
                                                 % (remainder, choices))
    return ChoiceAction
