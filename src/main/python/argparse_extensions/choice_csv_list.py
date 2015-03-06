# Copyright Teradata Inc., 2015"

import argparse

def lower_case_list(l):
    return [value.lower() for value in l]

class ChoiceCSVList(object):
    """
    Type for argparse supporting comma separated values and validating all of them
    with predefined choices list
    """
    def __init__(self, choices, case_sensitive=True):
        self.__choices = choices
        self.__case_sensitive = case_sensitive

        if not self.__case_sensitive:
            self.__choices = lower_case_list(self.__choices)

    def __repr__(self):
        return '%s(%r)' % (type(self).__name__, self.__choices)

    def __call__(self, csv):
        args = csv.split(',')
        if not self.__case_sensitive:
            args = lower_case_list(args)
        remainder = sorted(set(args) - set(self.__choices))
        if len(remainder) > 0:
            raise argparse.ArgumentTypeError("invalid choices: %r (choose from %r)"
                                             % (remainder, self.__choices))
        return args