# Copyright Teradata Inc., 2015"

import argparse
from argparse_extensions import ensure_arg_is_list, lower_case_list

def choice_list(choice_func, case_sensitive=True):
    class ChoiceList(argparse.Action):
        """
        Type for argparse supporting comma separated values and validating all of them
        with predefined choices list
        """
        def __init__(self, **kwargs):
            self.__choice_func = choice_func
            self.__case_sensitive = case_sensitive
            super(ChoiceList, self).__init__(**kwargs)

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
    return ChoiceList
